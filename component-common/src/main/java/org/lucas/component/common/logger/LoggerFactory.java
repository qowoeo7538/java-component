package org.lucas.component.common.logger;

import org.lucas.component.common.logger.jdk.JdkLoggerAdapter;
import org.lucas.component.common.logger.support.UnifiedLogger;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LoggerFactory {

    private static final String JdkLogger = "jdk";

    private static volatile LoggerAdapter LOGGER_ADAPTER;

    private static final ConcurrentMap<String, UnifiedLogger> LOGGERS = new ConcurrentHashMap<>();

    static {
        String logger = System.getProperty("application.logger");
        if (JdkLogger.equals(logger)) {
            setLoggerAdapter(new JdkLoggerAdapter());
        }
    }

    private LoggerFactory() {
    }

    public static void setLoggerAdapter(LoggerAdapter loggerAdapter) {
        if (loggerAdapter != null) {
            Logger logger = loggerAdapter.getLogger(LoggerFactory.class.getName());
            logger.info("using logger: " + loggerAdapter.getClass().getName());
            LOGGER_ADAPTER = loggerAdapter;
            for (Iterator<Map.Entry<String, UnifiedLogger>> iterator = LOGGERS.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, UnifiedLogger> entry = iterator.next();
                entry.getValue().setLogger(LOGGER_ADAPTER.getLogger(entry.getKey()));
            }
        }
    }
}
