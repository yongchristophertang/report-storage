package com.github.yongchristophertang.reporter.testcase;

import java.util.Collections;
import java.util.List;

/**
 * Object representative of one test case result.
 *
 * @author Yong Tang
 * @since 1.0
 */
public class TestCaseResult {
    private final String suiteName;
    private final String testName;
    private final String className;
    private final String caseName;
    private final long duration;
    private final List<String> outputs;
    private final ConfigurationEnum configuration;
    private final int status;
    private final String bug;
    private final String caseDescription;
    private final String expectedResult;

    private TestCaseResult(int status, String suiteName, String testName, String className, String caseName,
            long duration, List<String> outputs, ConfigurationEnum configuration, String bug, String caseDescription,
            String expectedResult) {
        this.status = status;
        this.suiteName = suiteName;
        this.testName = testName;
        this.className = className;
        this.caseName = caseName;
        this.duration = duration;
        this.outputs = Collections.unmodifiableList(outputs);
        this.configuration = configuration;
        this.bug = bug;
        this.caseDescription = caseDescription;
        this.expectedResult = expectedResult;
    }

    public int getStatus() {
        return status;
    }

    public long getDuration() {
        return duration;
    }

    public ConfigurationEnum getConfiguration() {
        return configuration;
    }

    @Override
    public String toString() {
        return "TestCaseResult{" +
                "suiteName='" + suiteName + '\'' +
                ", testName='" + testName + '\'' +
                ", className='" + className + '\'' +
                ", caseName='" + caseName + '\'' +
                ", duration=" + duration +
                ", outputs=" + outputs +
                ", configuration=" + configuration +
                ", status=" + status +
                ", bug='" + bug + '\'' +
                ", caseDescription='" + caseDescription + '\'' +
                ", expectedResult='" + expectedResult + '\'' +
                '}';
    }

    public String getBug() {
        return bug;
    }

    public String getCaseDescription() {
        return caseDescription;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public String getTestName() {
        return testName;
    }

    public String getClassName() {
        return className;
    }

    public String getCaseName() {
        return caseName;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public static final class TestCaseResultBuilder {
        private final String caseNameBuilder;
        private final long durationBuilder;
        private final List<String> outputsBuilder;
        private final int statusBuilder;
        private String suiteNameBuilder;
        private String testNameBuilder;
        private String classNameBuilder;
        private ConfigurationEnum configurationBuilder;
        private String bugBuilder;
        private String caseDescriptionBuilder;
        private String expectedResultBuilder;

        public TestCaseResultBuilder(String caseName, long duration, int status, List<String> outputs) {
            this.caseNameBuilder = caseName;
            this.durationBuilder = duration;
            this.statusBuilder = status;
            this.outputsBuilder = outputs;
        }

        public TestCaseResultBuilder suiteName(String suiteName) {
            this.suiteNameBuilder = suiteName;
            return this;
        }

        public TestCaseResultBuilder testName(String testName) {
            this.testNameBuilder = testName;
            return this;
        }

        public TestCaseResultBuilder className(String className) {
            this.classNameBuilder = className;
            return this;
        }

        public TestCaseResultBuilder configuration(ConfigurationEnum configuration) {
            this.configurationBuilder = configuration;
            return this;
        }

        public TestCaseResultBuilder bug(String bug) {
            this.bugBuilder = bug;
            return this;
        }

        public TestCaseResultBuilder caseDescription(String caseDescription) {
            this.caseDescriptionBuilder = caseDescription;
            return this;
        }

        public TestCaseResultBuilder expectedResult(String expectedResult) {
            this.expectedResultBuilder = expectedResult;
            return this;
        }

        public TestCaseResult createTestCaseResult() {
            return new TestCaseResult(statusBuilder, suiteNameBuilder, testNameBuilder, classNameBuilder,
                    caseNameBuilder, durationBuilder, outputsBuilder, configurationBuilder, bugBuilder,
                    caseDescriptionBuilder, expectedResultBuilder);
        }
    }

}
