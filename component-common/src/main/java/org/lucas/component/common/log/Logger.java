package org.lucas.component.common.log;

public interface Logger {
    /**
     * Logs a message with trace logger level.
     *
     * @param msg logger this message
     */
    void trace(String msg);

    /**
     * Logs an error with trace logger level.
     *
     * @param e logger this cause
     */
    void trace(Throwable e);

    /**
     * Logs an error with trace logger level.
     *
     * @param msg logger this message
     * @param e   logger this cause
     */
    void trace(String msg, Throwable e);

    /**
     * Logs a message with debug logger level.
     *
     * @param msg logger this message
     */
    void debug(String msg);

    /**
     * Logs an error with debug logger level.
     *
     * @param e logger this cause
     */
    void debug(Throwable e);

    /**
     * Logs an error with debug logger level.
     *
     * @param msg logger this message
     * @param e   logger this cause
     */
    void debug(String msg, Throwable e);

    /**
     * Logs a message with info logger level.
     *
     * @param msg logger this message
     */
    void info(String msg);

    /**
     * Logs an error with info logger level.
     *
     * @param e logger this cause
     */
    void info(Throwable e);

    /**
     * Logs an error with info logger level.
     *
     * @param msg logger this message
     * @param e   logger this cause
     */
    void info(String msg, Throwable e);

    /**
     * Logs a message with warn logger level.
     *
     * @param msg logger this message
     */
    void warn(String msg);

    /**
     * Logs a message with warn logger level.
     *
     * @param e logger this message
     */
    void warn(Throwable e);

    /**
     * Logs a message with warn logger level.
     *
     * @param msg logger this message
     * @param e   logger this cause
     */
    void warn(String msg, Throwable e);

    /**
     * Logs a message with error logger level.
     *
     * @param msg logger this message
     */
    void error(String msg);

    /**
     * Logs an error with error logger level.
     *
     * @param e logger this cause
     */
    void error(Throwable e);

    /**
     * Logs an error with error logger level.
     *
     * @param msg logger this message
     * @param e   logger this cause
     */
    void error(String msg, Throwable e);

    /**
     * Is trace logging currently enabled?
     *
     * @return true if trace is enabled
     */
    boolean isTraceEnabled();

    /**
     * Is debug logging currently enabled?
     *
     * @return true if debug is enabled
     */
    boolean isDebugEnabled();

    /**
     * Is info logging currently enabled?
     *
     * @return true if info is enabled
     */
    boolean isInfoEnabled();

    /**
     * Is warn logging currently enabled?
     *
     * @return true if warn is enabled
     */
    boolean isWarnEnabled();

    /**
     * Is error logging currently enabled?
     *
     * @return true if error is enabled
     */
    boolean isErrorEnabled();
}
