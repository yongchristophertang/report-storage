package com.github.yongchristophertang.reporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Created by YongTang on 2015/8/10.
 *
 * @author Yong Tang
 * @since 1.0
 */
@Listeners(ReporterService.class)
public class ReporterTest {
    private static final Logger logger = LogManager.getLogger();

    @BeforeClass
    public void setUp() {
//        throw new IllegalAccessError();
        logger.info("before");
    }

    @Test
    public void testDemo() {
        logger.info("test");
    }
}
