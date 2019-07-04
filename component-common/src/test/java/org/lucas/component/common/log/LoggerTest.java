package org.lucas.component.common.log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.lucas.component.common.log.jdk.JdkLoggerAdapter;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

// 测试类指定特殊的运行器
@RunWith(Parameterized.class)
public class LoggerTest {

    /**
     * 用于存放测试数据和期望值.
     */
    private Logger logger;

    public LoggerTest(final Class<? extends LoggerAdapter> loggerAdapter) throws Exception {
        final LoggerAdapter adapter = loggerAdapter.getConstructor().newInstance();
        adapter.setLevel(Level.ALL);
        this.logger = adapter.getLogger(this.getClass());
    }

    /**
     * 初始化参数对
     *
     * @return 参数对
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JdkLoggerAdapter.class}
        });
    }

    @Test
    public void testAllLogMethod() {
        logger.error("error");
        logger.warn("warn");
        logger.info("info");
        logger.debug("debug");
        logger.trace("info");

        logger.error(new Exception("error"));
        logger.warn(new Exception("warn"));
        logger.info(new Exception("info"));
        logger.debug(new Exception("debug"));
        logger.trace(new Exception("trace"));

        logger.error("error", new Exception("error"));
        logger.warn("warn", new Exception("warn"));
        logger.info("info", new Exception("info"));
        logger.debug("debug", new Exception("debug"));
        logger.trace("trace", new Exception("trace"));
    }

    @Test
    public void testLevelEnable() {
        assertThat(logger.isWarnEnabled(), not(nullValue()));
        assertThat(logger.isTraceEnabled(), not(nullValue()));
        assertThat(logger.isErrorEnabled(), not(nullValue()));
        assertThat(logger.isInfoEnabled(), not(nullValue()));
        assertThat(logger.isDebugEnabled(), not(nullValue()));
    }
}
