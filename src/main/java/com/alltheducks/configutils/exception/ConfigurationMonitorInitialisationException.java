package com.alltheducks.configutils.exception;

/**
 * Created by Shane Argo on 24/11/14.
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
