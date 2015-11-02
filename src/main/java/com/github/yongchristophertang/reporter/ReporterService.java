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

import com.github.yongchristophertang.reporter.annotation.Bug;
import com.github.yongchristophertang.reporter.annotation.TestCase;
import com.github.yongchristophertang.reporter.testcase.TestCaseResult;
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
import java.util.stream.Collectors;

/**
 * Submit generated test results and associated contexts to remote storage.
 *
 * @author Yong Tang
 * @since 0.1
 */
public class ReporterService extends AbstractReporter implements IReporter {
    private static final Logger logger = LogManager.getLogger();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService service = Executors.newCachedThreadPool();

    /**
     * Submit all results asynchronously to remote storage with location at {@link #getUrl()}.
     * If a set of result fails to transmit, another attempt will be activated. However only one more chance will be
     * tried.
     */
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<Tuple2<TestCaseResult, Future<ResponseEntity<String>>>> futureTuples = new ArrayList<>();

        for (ISuite suite : suites) {
            for (IInvokedMethod testCase : suite.getAllInvokedMethods()) {
                ITestNGMethod method = testCase.getTestMethod();
                CasePostProcessor processor = new CasePostProcessor(method);
                TestCaseResult result = new TestCaseResult.TestCaseResultBuilder(method.getMethodName(),
                        testCase.getTestResult().getEndMillis() - testCase.getTestResult().getStartMillis(),
                        testCase.getTestResult().getStatus(), Reporter.getOutput(testCase.getTestResult()))
                        .className(method.getTestClass().getName()).testName(testCase.getTestResult().getTestName())
                        .suiteName(suite.getName()).configuration(testCase.isConfigurationMethod())
                        .caseDescription(processor.getCaseDescription()).expectedResult(processor.getExpectedResult())
                        .bug(processor.getBugInfo()).date(testCase.getDate()).createTestCaseResult();
                futureTuples.add(new Tuple2<>(result, service.submit(new UploadResults(result))));
            }
        }

        // First round attempt to upload results, and for failed cases attempt once more
        List<Future<ResponseEntity<String>>> futures = futureTuples.parallelStream()
                .filter(f -> Try.of(() -> !f._2.get(5, TimeUnit.SECONDS).getStatusCode().is2xxSuccessful())
                        .onFailure(t -> logger.error("Failed to upload results to remote storage.", t))
                        .orElse(true)).map(f -> service.submit(new UploadResults(f._1))).collect(Collectors.toList());

        // Check if all attempts succeed, if not, prompt notice of errors
        long count = futures.parallelStream()
                .filter(f -> Try.of(() -> !f.get(5, TimeUnit.SECONDS).getStatusCode().is2xxSuccessful()).orElse(true))
                .count();
        logger.error("There are " + count + " cases failed to upload to remote storage.");

        try {
            service.shutdown();
            if (!service.awaitTermination(2, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Reporter service has been interrupted while shutting down", e);
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
            return Try.of(() -> method.getAnnotation(TestCase.class).value()).orElse("");
        }

        String getExpectedResult() {
            return Try.of(() -> method.getAnnotation(TestCase.class).expected()).orElse("");
        }

        String getBugInfo() {
            return Try.of(() -> method.getAnnotation(Bug.class).value()).orElse(null);
        }
    }

    /**
     * Async callable service for uploading test results.
     */
    private class UploadResults implements Callable<ResponseEntity<String>> {
        private final TestCaseResult result;

        UploadResults(TestCaseResult result) {
            this.result = result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ResponseEntity<String> call() throws Exception {
            ResponseEntity<String> response = restTemplate.postForEntity(getUrl(), this.result, String.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Test case result about to submit: ", result);
                logger.debug("Remote storage responds: ", response);
            }
            return response;
        }
    }
}
