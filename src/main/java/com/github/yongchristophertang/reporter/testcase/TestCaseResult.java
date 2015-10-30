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

package com.github.yongchristophertang.reporter.testcase;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Object representative of one test case result.
 *
 * @author Yong Tang
 * @since 0.1
 */
public class TestCaseResult {
    private final String suiteName;
    private final String testName;
    private final String className;
    private final String caseName;
    private final long duration;
    private final List<String> outputs;
    private final boolean configuration;
    private final int status;
    private final String bug;
    private final String caseDescription;
    private final String expectedResult;
    private final String dateTime;
    private final String version = System.getProperty("test_version", "0.0");

    private TestCaseResult(int status, String suiteName, String testName, String className, String caseName,
                           long duration, List<String> outputs, boolean configuration, String bug, String caseDescription,
                           String expectedResult, String dateTime) {
        this.status = status;

        this.suiteName = suiteName;
        this.testName = testName;
        this.className = className;
        this.caseName = caseName;
        this.duration = duration;
        this.dateTime = dateTime;
        this.outputs = Collections.unmodifiableList(outputs);
        this.configuration = configuration;
        this.bug = bug;
        this.caseDescription = caseDescription;
        this.expectedResult = expectedResult;
    }

    public boolean isConfiguration() {
        return configuration;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getVersion() {
        return version;
    }

    public int getStatus() {
        return status;
    }

    public long getDuration() {
        return duration;
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
                ", dateTime=" + dateTime +
                ", version='" + version + '\'' +
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
        private boolean configurationBuilder;
        private String bugBuilder;
        private String caseDescriptionBuilder;
        private String expectedResultBuilder;
        private String dateTimeBuilder;

        /**
         * A builder that is to create a {@link com.github.yongchristophertang.reporter.testcase.TestCaseResult}.
         *
         * @param caseName the name of the test case
         * @param duration the duration for execution of this test case
         * @param status   the status of the result for this test case
         * @param outputs  output logs of this test case
         */
        public TestCaseResultBuilder(String caseName, long duration, int status, List<String> outputs) {
            this.caseNameBuilder = caseName;
            this.durationBuilder = duration;
            this.statusBuilder = status;
            this.outputsBuilder = outputs;
        }

        /**
         * the name of the suite
         */
        public TestCaseResultBuilder suiteName(String suiteName) {
            this.suiteNameBuilder = suiteName;
            return this;
        }

        /**
         * the name of the test
         */
        public TestCaseResultBuilder testName(String testName) {
            this.testNameBuilder = testName;
            return this;
        }

        /**
         * the name of the test class
         */
        public TestCaseResultBuilder className(String className) {
            this.classNameBuilder = className;
            return this;
        }

        /**
         * is this test method a configuration
         */
        public TestCaseResultBuilder configuration(boolean configuration) {
            this.configurationBuilder = configuration;
            return this;
        }

        /**
         * the bug associated with this test case if applicable
         */
        public TestCaseResultBuilder bug(String bug) {
            this.bugBuilder = bug;
            return this;
        }

        /**
         * the case description of this test case
         */
        public TestCaseResultBuilder caseDescription(String caseDescription) {
            this.caseDescriptionBuilder = caseDescription;
            return this;
        }

        /**
         * the expected result of this test case
         */
        public TestCaseResultBuilder expectedResult(String expectedResult) {
            this.expectedResultBuilder = expectedResult;
            return this;
        }

        /**
         * the execution date of this test case
         */
        public TestCaseResultBuilder date(long date) {
            this.dateTimeBuilder = LocalDateTime.ofEpochSecond(date / 1000, 0, ZoneOffset.ofHours(8))
                    .format(DateTimeFormatter.ISO_DATE_TIME);
            return this;
        }

        public TestCaseResult createTestCaseResult() {
            return new TestCaseResult(statusBuilder, suiteNameBuilder, testNameBuilder, classNameBuilder,
                    caseNameBuilder, durationBuilder, outputsBuilder, configurationBuilder, bugBuilder,
                    caseDescriptionBuilder, expectedResultBuilder, dateTimeBuilder);
        }
    }

}
