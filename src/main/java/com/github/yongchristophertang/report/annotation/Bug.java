package com.github.yongchristophertang.report.annotation;

import java.lang.annotation.*;

/**
 * Indication used to mark a test case as bug infected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Bug {
	
	/**
	 * Bug ID or description.
	 */
	String value() default "";
}
