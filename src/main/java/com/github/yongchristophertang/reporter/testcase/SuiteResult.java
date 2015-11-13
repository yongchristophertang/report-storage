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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Model for a structure representative of a test withSuite.
 *
 * @author Yong Tang
 * @since 0.2
 */
public class SuiteResult {
    private final String name;
    @JsonProperty("suite")
    private final List<TestResult> tests;
    private final String alias = "Suite";

    public SuiteResult(String name, TestResult tests) {
        this.name = name;
        this.tests = Collections.unmodifiableList(Arrays.asList(tests));
    }

    public String getName() {
        return name;
    }

    public List<TestResult> getTests() {
        return tests;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "SuiteResult{" +
            "name='" + name + '\'' +
            ", tests=" + tests +
            '}';
    }
}