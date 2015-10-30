package com.github.yongchristophertang.reporter;

import com.github.yongchristophertang.report.annotation.Bug;
import com.github.yongchristophertang.report.annotation.TestCase;
import com.github.yongchristophertang.reporter.testcase.ConfigurationEnum;
import com.github.yongchristophertang.reporter.testcase.TestCaseResult;
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

/**
 * Submit generated test results and associated contexts to remote storage.
 *
 * @author Yong Tang
 * @since 0.1
 */
public class ReporterService extends AbstractReporter implements IReporter {
    static final int THREAD_POOL_SIZE = 10;
    private static final Logger logger = LogManager.getLogger();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService service = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    /**
     * Submit all results asynchronously to remote storage with location at {@link #getUrl()}.
     * If a set of result fails to transmit, another attempt will be activated. However only one more chance will be
     * tried.
     */
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<Tuple2<TestCaseResult, Future<ResponseEntity<String>>>> futures = new ArrayList<>();

        for (ISuite suite : suites) {
            for (IInvokedMethod testCase : suite.getAllInvokedMethods()) {
                ITestNGMethod method = testCase.getTestMethod();
                CasePostProcessor processor = new CasePostProcessor(method);
                TestCaseResult result = new TestCaseResult.TestCaseResultBuilder(method.getMethodName(),
                        testCase.getTestResult().getEndMillis() - testCase.getTestResult().getStartMillis(),
                        testCase.getTestResult().getStatus(), Reporter.getOutput(testCase.getTestResult()))
                        .className(method.getTestClass().getName()).testName(testCase.getTestResult().getTestName())
                        .suiteName(suite.getName()).configuration(ConfigurationEnum.AFTER_CLASS)
                        .caseDescription(processor.getCaseDescription()).expectedResult(processor.getExpectedResult())
                        .bug(processor.getBugInfo()).createTestCaseResult();
                futures.add(new Tuple2<>(result, service.submit(new UploadResults(result))));
            }
        }

        futures.parallelStream()
                .filter(f -> Try.of(() -> !f._2.get(5, TimeUnit.SECONDS).getStatusCode().is2xxSuccessful())
                        .orElse(true))
                .forEach(f -> service.submit(new UploadResults(f._1)));

        try {
            service.shutdown();
            if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Reporter service has been interrupted while shutting down", e);
        }
    }

    /**
     * Internal test case processor for handling with {@link Bug} and {@link TestCase} annotations.
     */
    private class CasePostProcessor {
        private final Method method;

        CasePostProcessor(ITestNGMethod method) {
            this.method = method.getConstructorOrMethod().getMethod();
        }

        String getCaseDescription() {
            TestCase testCase;
            if ((testCase = method.getAnnotation(TestCase.class)) == null) {
                return "";
            } else {
                return testCase.value();
            }
        }

        String getExpectedResult() {
            TestCase testCase;
            if ((testCase = method.getAnnotation(TestCase.class)) == null) {
                return "";
            } else {
                return testCase.expected();
            }
        }

        String getBugInfo() {
            Bug bug;
            if ((bug = method.getAnnotation(Bug.class)) == null) {
                return null;
            } else {
                return bug.value();
            }
        }
    }

    /**
     * Async callable service for uploading test results.
     */
    class UploadResults implements Callable<ResponseEntity<String>> {
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
