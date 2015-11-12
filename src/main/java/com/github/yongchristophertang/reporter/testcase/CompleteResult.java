/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yongchristophertang.reporter.testcase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Model for a complete structure representative of all test results.
 *
 * @author Yong Tang
 * @since 0.2
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = CompleteResult.CompleteResultBuilder.class)
public class CompleteResult {
    private final long queueTaskId;
    private final String targetVersion;
    private final String targetApplication;
    private final String testProjectName;
    private final String testDateTime;
    private final List<SuiteResult> suite;

    private CompleteResult(long queueTaskId, String targetVersion, String targetApplication, String testProjectName,
        String testDateTime, List<SuiteResult> suite) {
        this.queueTaskId = queueTaskId;
        this.targetVersion = targetVersion;
        this.targetApplication = targetApplication;
        this.testProjectName = testProjectName;
        this.testDateTime = testDateTime;
        this.suite = suite;
    }

    public long getQueueTaskId() {
        return queueTaskId;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public String getTargetApplication() {
        return targetApplication;
    }

    public String getTestProjectName() {
        return testProjectName;
    }

    public String getTestDateTime() {
        return testDateTime;
    }

    public List<SuiteResult> getSuite() {
        return suite;
    }

    @Override
    public String toString() {
        return "CompleteResult{" +
            "queueTaskId='" + queueTaskId + '\'' +
            ", targetVersion='" + targetVersion + '\'' +
            ", targetApplication='" + targetApplication + '\'' +
            ", testProjectName='" + testProjectName + '\'' +
            ", testDateTime='" + testDateTime + '\'' +
            ", suite=" + suite +
            '}';
    }

    @JsonPOJOBuilder(buildMethodName = "build")
    public static final class CompleteResultBuilder {
        private final long queueTaskId;

        private String targetVersion = System.getProperty("targetVersion");
        private String targetApplication = System.getProperty("targetApplication");
        private String testProjectName = System.getProperty("testProjectName");
        private String testDateTime = System.getProperty("testDateTime");
        private List<SuiteResult> suite;

        @JsonCreator
        public CompleteResultBuilder(@JsonProperty("queue_task_id") long queueTaskId) {
            this.queueTaskId = queueTaskId;
        }

        public CompleteResultBuilder withTargetVersion(String targetVersion) {
            this.targetVersion = targetVersion;
            return this;
        }

        public CompleteResultBuilder withTargetApplication(String targetApplication) {
            this.targetApplication = targetApplication;
            return this;
        }

        public CompleteResultBuilder withTestProjectName(String testProjectName) {
            this.testProjectName = testProjectName;
            return this;
        }

        public CompleteResultBuilder withTestDateTime(long date) {
            this.testDateTime = LocalDateTime.ofEpochSecond(date / 1000, 0, ZoneOffset.ofHours(8))
                .format(DateTimeFormatter.ISO_DATE_TIME);
            return this;
        }

        public CompleteResultBuilder withSuite(SuiteResult suite) {
            this.suite = Collections.unmodifiableList(Arrays.asList(suite));
            return this;
        }

        public CompleteResult build() {
            return new CompleteResult(queueTaskId, targetVersion, targetApplication, testProjectName, testDateTime,
                suite);
        }
    }
}
