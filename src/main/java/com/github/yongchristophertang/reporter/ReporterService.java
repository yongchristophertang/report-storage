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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yongchristophertang.reporter.annotation.Bug;
import com.github.yongchristophertang.reporter.annotation.TestCase;
import com.github.yongchristophertang.reporter.testcase.TestCaseResult;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import javaslang.Tuple2;
import javaslang.control.Try;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private final HttpClient client = HttpClientBuilder.create().build();

    /**
     * Submit all results asynchronously to remote storage with location at {@link #getUrl()}.
     * If a set of result fails to transmit, another attempt will be activated. However only one more chance will be
     * tried.
     */
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<Tuple2<TestCaseResult, Future<Boolean>>> futureTuples = new ArrayList<>();

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
                if (logger.isDebugEnabled()) {
                    logger.debug("Test case details about to send: {}", result);
                }
                futureTuples.add(new Tuple2<>(result, service.submit(new UploadResults(result))));
            }
        }

        // First round attempt to upload results, and for failed cases attempt once more
        List<Future<Boolean>> futures = futureTuples.parallelStream()
                .filter(f -> Try.of(() -> !f._2.get(5, TimeUnit.SECONDS))
                        .onFailure(t -> logger.error("Failed to upload results to remote storage.", t)).orElse(true))
                .map(f -> service.submit(new UploadResults(f._1))).collect(Collectors.toList());

        // Check if all attempts succeed, if not, prompt notice of errors
        if (futures.size() > 0) {
            long count = futures.parallelStream().filter(f -> Try.of(() -> !f.get(5, TimeUnit.SECONDS)).orElse(true))
                    .count();
            if (count > 0) {
                logger.error("There are {} cases failed to upload to remote storage.", count);
            } else {
                logger.info("All test results have been successfully transmitted to remote storage.");
            }
        }

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
     * Async callable service for uploading test results using {@link HttpClient}
     */
    private class UploadResults implements Callable<Boolean> {
        private final TestCaseResult result;

        UploadResults(TestCaseResult result) {
            this.result = result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean call() throws Exception {
            HttpPost post = new HttpPost(getUrl());
            post.setEntity(new UrlEncodedFormEntity(Lists.newArrayList(new BasicNameValuePair("info_type", "json"),
                    new BasicNameValuePair("other_info",
                            new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                                    .writeValueAsString(result)), new BasicNameValuePair("queue_task_id", "1"),
                    new BasicNameValuePair("task_id", "2"), new BasicNameValuePair("user_id", "3"),
                    new BasicNameValuePair("user_name", "tester"),
                    new BasicNameValuePair("script_name", result.getCaseName()),
                    new BasicNameValuePair("testcase_no", result.getCaseName()),
                    new BasicNameValuePair("testsuite_no", result.getSuiteName()),
                    new BasicNameValuePair("testdescription", result.getCaseDescription()),
                    new BasicNameValuePair("expect_result", result.getExpectedResult()),
                    new BasicNameValuePair("actual_result", "n/a"),
                    new BasicNameValuePair("is_success", String.valueOf(result.getStatus())),
                    new BasicNameValuePair("run_time", String.valueOf(result.getDuration())))));
            HttpResponse response = client.execute(post);
            if (logger.isDebugEnabled()) {
                logger.debug("Request sent: {}", post);
                logger.debug("Response received: {}", response);
            }
            return response.getStatusLine().getStatusCode() == 200;
        }
    }
}
