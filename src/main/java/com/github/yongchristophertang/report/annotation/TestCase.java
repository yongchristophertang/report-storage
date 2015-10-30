package com.github.yongchristophertang.report.annotation;

import java.lang.annotation.*;

/**
 * Indication used to submit description and expected result of a test case.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface TestCase {

    /**
     * Case description.
     */
    String value() default "";

    /**
     * Expected result of this case.
     */
    String expected() default "";
}
