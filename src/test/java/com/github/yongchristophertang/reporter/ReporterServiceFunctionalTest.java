package com.github.yongchristophertang.reporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link ReporterService}'s main functions.
 *
 * @author Yong Tang
 * @since 0.1
 */
@Listeners(ReporterService.class)
public class ReporterServiceFunctionalTest {
    private static final Logger logger = LogManager.getLogger();

    @BeforeClass
    public void setUp() {
        logger.info("before");
//        new Thread(new EmbeddedServer()).start();
    }

    @Test
    public void testDemo() {
        logger.info("test");
    }

//    private class EmbeddedServer extends AbstractHandler implements Runnable {
//
//        /**
//         * {@inheritDoc}
//         */
//        @Override
//        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse
// response)
//                throws IOException, ServletException {
//            String content = request.getReader().lines().reduce("", (s1, s2) -> s1 + s2);
//            logger.info(content);
//            response.setStatus(200);
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        @Override
//        public void run() {
//            try {
//                Server server = new Server(8080);
//                ContextHandler contextHandler = new ContextHandler();
//                contextHandler.setContextPath("/api");
//                contextHandler.setHandler(this);
//
//                server.setHandler(contextHandler);
//
//                server.start();
//                logger.info("Server has been started.");
//                Thread.sleep(10000);
////                int count = 0;
////                while (!requestHandled) {
////                    Thread.sleep(100);
////                    if (++count > maxCount) {
////                        break;
////                    }
////                }
//                server.stop();
//                logger.info("Server has been stopped.");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
