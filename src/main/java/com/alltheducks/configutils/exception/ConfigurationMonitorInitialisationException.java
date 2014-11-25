package com.alltheducks.configutils.exception;

/**
 * <p>Exception thrown when initialising a Configuration Monitor
 * within a {@link com.alltheducks.configutils.servlet.ConfigMonitoringContextListener}</p>
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public class ConfigurationMonitorInitialisationException extends RuntimeException {

    public ConfigurationMonitorInitialisationException() {
    }

    public ConfigurationMonitorInitialisationException(String message) {
        super(message);
    }

    public ConfigurationMonitorInitialisationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationMonitorInitialisationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationMonitorInitialisationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
