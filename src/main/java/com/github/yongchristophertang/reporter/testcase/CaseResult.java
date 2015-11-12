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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Model representative of one test case result.
 *
 * @author Yong Tang
 * @since 0.1
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseResult {
    private final String suiteName;
    private final String testName;
    private final String className;
    private final String name;
    private final long duration;
    @JsonProperty("testLog")
    private final List<String> outputs;
    private final boolean isConfiguration;
    private final int status;
    private final String bug;
    @JsonProperty("description")
    private final String caseDescription;
    private final String expectedResult;
    private final String dateTime;

    private CaseResult(int status, String suiteName, String testName, String className, String name,
        long duration, List<String> outputs, boolean isConfiguration, String bug, String caseDescription,
        String expectedResult, String dateTime) {
        this.status = status;

        this.suiteName = suiteName;
        this.testName = testName;
        this.className = className;
        this.name = name;
        this.duration = duration;
        this.dateTime = dateTime;
        this.outputs = Collections.unmodifiableList(outputs);
        this.isConfiguration = isConfiguration;
        this.bug = bug;
        this.caseDescription = caseDescription;
        this.expectedResult = expectedResult;
    }

    @JsonProperty("isConfiguration")
    public boolean isConfiguration() {
        return isConfiguration;
    }

    public String getDateTime() {
        return dateTime;
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
            ", name='" + name + '\'' +
            ", duration=" + duration +
            ", outputs=" + outputs +
            ", isConfiguration=" + isConfiguration +
            ", status=" + status +
            ", bug='" + bug + '\'' +
            ", caseDescription='" + caseDescription + '\'' +
            ", expectedResult='" + expectedResult + '\'' +
            ", dateTime=" + dateTime +
            '}';
    }

    public List<String> getBug() {
        return bug == null ? null : Arrays.asList(bug);
    }

    public List<String> getCaseDescription() {
        return caseDescription == null ? null : Arrays.asList(caseDescription);
    }

    public List<String> getExpectedResult() {
        return expectedResult == null ? null : Arrays.asList(expectedResult);
    }

    public List<String> getSuiteName() {
        return suiteName == null ? null : Arrays.asList(suiteName);
    }

    public List<String> getTestName() {
        return testName == null ? null : Arrays.asList(testName);
    }

    public List<String> getClassName() {
        return className == null ? null : Arrays.asList(className);
    }

    public String getName() {
        return name;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public static final class CaseResultBuilder {
        private final String nameBuilder;
        private final long durationBuilder;
        private final List<String> outputsBuilder;
        private final int statusBuilder;
        private final boolean isConfigurationBuilder;
        private String suiteNameBuilder;
        private String testNameBuilder;
        private String classNameBuilder;
        private String bugBuilder;
        private String caseDescriptionBuilder;
        private String expectedResultBuilder;
        private String dateTimeBuilder;

        /**
         * A builder that is to create a {@link CaseResult}.
         *
         * @param name            the name of the test case
         * @param duration        the duration for execution of this test case
         * @param status          the status of the result for this test case
         * @param outputs         output logs of this test case
         * @param isConfiguration is this case a configuration or not
         */
        public CaseResultBuilder(String name, long duration, int status, List<String> outputs,
            boolean isConfiguration) {
            this.nameBuilder = name;
            this.durationBuilder = duration;
            this.statusBuilder = status;
            this.outputsBuilder = outputs;
            this.isConfigurationBuilder = isConfiguration;
        }

        /**
         * the name of the withSuite
         */
        public CaseResultBuilder suiteName(String suiteName) {
            this.suiteNameBuilder = suiteName;
            return this;
        }

        /**
         * the name of the test
         */
        public CaseResultBuilder testName(String testName) {
            this.testNameBuilder = testName;
            return this;
        }

        /**
         * the name of the test class
         */
        public CaseResultBuilder className(String className) {
            this.classNameBuilder = className;
            return this;
        }

        /**
         * the bug associated with this test case if applicable
         */
        public CaseResultBuilder bug(String bug) {
            this.bugBuilder = bug;
            return this;
        }

        /**
         * the case description of this test case
         */
        public CaseResultBuilder caseDescription(String caseDescription) {
            this.caseDescriptionBuilder = caseDescription;
            return this;
        }

        /**
         * the expected result of this test case
         */
        public CaseResultBuilder expectedResult(String expectedResult) {
            this.expectedResultBuilder = expectedResult;
            return this;
        }

        /**
         * the execution date of this test case
         */
        public CaseResultBuilder date(long date) {
            this.dateTimeBuilder = LocalDateTime.ofEpochSecond(date / 1000, 0, ZoneOffset.ofHours(8))
                .format(DateTimeFormatter.ISO_DATE_TIME);
            return this;
        }

        public CaseResult createTestCaseResult() {
            return new CaseResult(statusBuilder, suiteNameBuilder, testNameBuilder, classNameBuilder,
                nameBuilder, durationBuilder, outputsBuilder, isConfigurationBuilder, bugBuilder,
                caseDescriptionBuilder, expectedResultBuilder, dateTimeBuilder);
        }
    }

}
