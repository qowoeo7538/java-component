package org.lucas.component.common.logger.jdk;

import org.lucas.component.common.logger.Level;
import org.lucas.component.common.logger.Logger;
import org.lucas.component.common.logger.LoggerAdapter;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;

public class JdkLoggerAdapter implements LoggerAdapter {

    private static final String GLOBAL_LOGGER_NAME = "global";

    private File file;

    public JdkLoggerAdapter() {
        try {
            // 资源目录下的 logging.properties 文件
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties");
            if (in != null) {
                LogManager.getLogManager().readConfiguration(in);
            } else {
                System.err.println("No such logging.properties in classpath for jdk logging config!");
            }
        } catch (Throwable t) {
            System.err.println("Failed to load logging.properties in classpath for jdk logging config, cause: " + t.getMessage());
        }
        try {
            // 获取日志处理对象
            Handler[] handlers = java.util.logging.Logger.getLogger(GLOBAL_LOGGER_NAME).getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof FileHandler) {
                    FileHandler fileHandler = (FileHandler) handler;
                    Field field = fileHandler.getClass().getField("files");
                    File[] files = (File[]) field.get(fileHandler);
                    if (files != null && files.length > 0) {
                        file = files[0];
                    }
                }
            }
        } catch (Throwable t) {
        }
    }

    /**
     * 获取对应日志的日志等级
     *
     * @param level 日志等级
     * @return
     */
    private static java.util.logging.Level toJdkLevel(Level level) {
        switch (level) {
            case ALL:
                return java.util.logging.Level.ALL;
            case TRACE:
                return java.util.logging.Level.FINER;
            case DEBUG:
                return java.util.logging.Level.FINE;
            case INFO:
                return java.util.logging.Level.INFO;
            case WARN:
                return java.util.logging.Level.WARNING;
            case ERROR:
                return java.util.logging.Level.SEVERE;
            default:
                return java.util.logging.Level.OFF;
        }
    }

    @Override
    public Logger getLogger(Class<?> key) {
        return new JdkLogger(java.util.logging.Logger.getLogger(key == null ? "" : key.getName()));
    }

    @Override
    public Logger getLogger(String key) {
        return new JdkLogger(java.util.logging.Logger.getLogger(key));
    }

    @Override
    public Level getLevel() {
        return null;
    }

    @Override
    public void setLevel(Level level) {
        java.util.logging.Logger.getLogger(GLOBAL_LOGGER_NAME).setLevel(toJdkLevel(level));
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public void setFile(File file) {

    }

}
