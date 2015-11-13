/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yongchristophertang.reporter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.yongchristophertang.reporter.annotation.Bug;
import com.github.yongchristophertang.reporter.annotation.TestCase;
import com.github.yongchristophertang.reporter.testcase.*;
import com.github.yongchristophertang.reporter.testcase.SuiteResult;
import com.google.common.annotations.VisibleForTesting;
import javaslang.Tuple2;
import javaslang.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Submit generated test results and associated contexts to remote storage.
 *
 * @author Yong Tang
 * @since 0.1
 */
public class ReporterService extends AbstractReporter implements IReporter {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService service = Executors.newCachedThreadPool();

    /**
     * Submit all results asynchronously to remote storage with location at {@link #getStorageConfig()}.
     * If a set of result fails to transmit, another attempt will be activated. However only one more chance will be
     * tried.
     */
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<Tuple2<CompleteResult, Future<ResponseEntity<String>>>> futureTuples = new ArrayList<>();

        for (ISuite suite : suites) {
            for (IInvokedMethod testCase : suite.getAllInvokedMethods()) {
                ITestNGMethod method = testCase.getTestMethod();
                CasePostProcessor processor = new CasePostProcessor(method);
                String className, testName, suiteName;
                CompleteResult result =
                    new CompleteResult.CompleteResultBuilder(getStorageConfig().getQueueTaskId())
                        .withSuite(new SuiteResult(suiteName = suite.getName(),
                            new TestResult(testName = testCase.getTestResult().getTestName(),
                                new ClassResult(className = method.getTestClass().getName(),
                                    new CaseResult.CaseResultBuilder(method.getMethodName(),
                                        testCase.getTestResult().getEndMillis() -
                                            testCase.getTestResult().getStartMillis(),
                                        testCase.getTestResult().getStatus(),
                                        Reporter.getOutput(testCase.getTestResult()), testCase.isConfigurationMethod())
                                        .className(className).testName(testName).suiteName(suiteName)
                                        .caseDescription(processor.getCaseDescription())
                                        .expectedResult(processor.getExpectedResult())
                                        .bug(processor.getBugInfo()).date(testCase.getDate())
                                        .createTestCaseResult())))).build();
                // Send out test results asynchronously
                futureTuples.add(
                    new Tuple2<>(result, service.submit(new UploadResults<>(getStorageConfig().getUrl(), result))));
            }
        }

        Predicate<Tuple2<CompleteResult, Future<ResponseEntity<String>>>> predicate =
            f -> Try.of(() -> !f._2.get(5, TimeUnit.SECONDS).getStatusCode().is2xxSuccessful())
                .onFailure(t -> LOGGER.error("Failed to upload results to remote storage.", t))
                .orElse(true);

        // Collect futures of remote responses, if failed send again.
        futureTuples = futureTuples.parallelStream().filter(predicate)
            .map(f -> new Tuple2<>(f._1, service.submit(new UploadResults<>(getStorageConfig().getUrl(), f._1))))
            .collect(Collectors.toList());

        // Check if all attempts succeed, if not, prompt notice of errors
        long count = futureTuples.parallelStream().filter(predicate).count();
        if (count > 0) {
            LOGGER.error("There are {} cases failed to upload to remote storage.", count);
        } else {
            LOGGER.info("All test case results have been successfully transmitted to remote storage");
        }

        // Publish end of transmission signal to remote storage
        @JsonInclude(JsonInclude.Include.NON_NULL)
        class ResultEntity {
            long queueTaskId = getStorageConfig().getQueueTaskId();
            String status = count > 0 ? "failure" : "success";
            String error = count > 0 ? count + " case results failed to transmit to remote storage." : null;

            public long getQueueTaskId() {
                return queueTaskId;
            }

            public String getStatus() {
                return status;
            }

            public String getError() {
                return error;
            }

            @Override
            public String toString() {
                return "ResultEntity{" +
                    "queueTaskId=" + queueTaskId +
                    ", status='" + status + '\'' +
                    ", error='" + error + '\'' +
                    '}';
            }
        }

        try {
            Future<ResponseEntity<String>> future =
                service.submit(new UploadResults<>(getStorageConfig().getEndUrl(), new ResultEntity()));
            if (!future.get(5, TimeUnit.SECONDS).getStatusCode().is2xxSuccessful()) {
                service.submit(new UploadResults<>(getStorageConfig().getEndUrl(), new ResultEntity()));
            }
        } catch (InterruptedException e) {
            LOGGER.error("Async task has been interrupted", e);
        } catch (ExecutionException e) {
            LOGGER.error("Async task has been executed with errors, transmission is failed", e);
        } catch (TimeoutException e) {
            LOGGER.error("Remote server has not responded for 5 seconds, prepare to retransmit", e);
        }

        try {
            service.shutdown();
            if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.error("Reporter service has been interrupted while shutting down", e);
        }
    }

    /**
     * Only available for testing purpose, DO NOT USE IN DEV CODES!
     */
    @VisibleForTesting
    CasePostProcessor getCasePostProcessor(ITestNGMethod method) {
        return new CasePostProcessor(method);
    }

    /**
     * Internal test case processor for handling with {@link Bug} and {@link TestCase} annotations.
     */
    class CasePostProcessor {
        private final Method method;

        CasePostProcessor(ITestNGMethod method) {
            this.method = method.getConstructorOrMethod().getMethod();
        }

        String getCaseDescription() {
            return Try.of(() -> method.getAnnotation(TestCase.class).value()).orElse(null);
        }

        String getExpectedResult() {
            return Try.of(() -> method.getAnnotation(TestCase.class).expected()).orElse(null);
        }

        String getBugInfo() {
            return Try.of(() -> method.getAnnotation(Bug.class).value()).orElse(null);
        }
    }

    /**
     * Async callable service for uploading test results.
     */
    private class UploadResults<T> implements Callable<ResponseEntity<String>> {
        private final T result;
        private final String url;

        UploadResults(String url, T result) {
            this.result = result;
            this.url = url;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ResponseEntity<String> call() throws Exception {
            ResponseEntity<String> response = restTemplate.postForEntity(url, this.result, String.class);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Test case result about to submit to: {} \n With an enclosing entity: {}", url, result);
                LOGGER.debug("Remote storage responds: {}", response);
            }
            return response;
        }
    }
}