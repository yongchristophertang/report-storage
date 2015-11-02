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

package com.github.yongchristophertang.reporter;

import com.github.yongchristophertang.reporter.annotation.Bug;
import com.github.yongchristophertang.reporter.annotation.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.ITestNGMethod;
import org.testng.internal.ConstructorOrMethod;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ReporterService}
 *
 * @author Yong Tang
 * @since 0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class ReporterServiceTest {
    @Bug("Test_Bug_Id")
    @TestCase(value = "case description", expected = "expected results")
    public void testMethod() {
        System.out.println("Just a test");
    }

    public void testMethod2() {
        System.out.println("Just another test");
    }

    @Test
    public void testCasePostProcessorWithAllValueDefined() throws NoSuchMethodException {
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ConstructorOrMethod com = mock(ConstructorOrMethod.class);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(com);
        when(com.getMethod()).thenReturn(this.getClass().getMethod("testMethod"));

        ReporterService.CasePostProcessor processor = new ReporterService().getCasePostProcessor(testNGMethod);
        Assert.assertThat("Bug info", processor.getBugInfo(), is("Test_Bug_Id"));
        Assert.assertThat("Case description", processor.getCaseDescription(), is("case description"));
        Assert.assertThat("Expected results", processor.getExpectedResult(), is("expected results"));
    }

    @Test
    public void testCasePostProcessorWithAllValueNotDefined() throws NoSuchMethodException {
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ConstructorOrMethod com = mock(ConstructorOrMethod.class);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(com);
        when(com.getMethod()).thenReturn(this.getClass().getMethod("testMethod2"));

        ReporterService.CasePostProcessor processor = new ReporterService().getCasePostProcessor(testNGMethod);
        Assert.assertThat("Bug info", processor.getBugInfo(), nullValue());
        Assert.assertThat("Case description", processor.getCaseDescription(), is(""));
        Assert.assertThat("Expected results", processor.getExpectedResult(), is(""));
    }
}
