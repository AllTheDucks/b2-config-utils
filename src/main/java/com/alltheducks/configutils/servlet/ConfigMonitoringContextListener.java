package com.alltheducks.configutils.servlet;

import com.alltheducks.configutils.exception.ConfigurationMonitorInitialisationException;
import com.alltheducks.configutils.monitor.ConfigMonitorRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * <p>
 * This is the base class that all ConfigMonitoringContextListener implementations
 * extend. Responsible for Executing the Configuration Monitor process.<br>
 * Override the {@link #getConfigurationMonitor} method to return the
 * Configuration Monitor that should be used.</p>
 * <p>You configure the Listener in your web.xml as follows.</p>
 * <pre>
 * {@code
 * <listener>
 *  <listener-class>
 *     my.package.MyConfigMonitoringContextListener
 *  </listener-class>
 * </listener>}
 * </pre>
 * <p>
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public abstract class ConfigMonitoringContextListener implements ServletContextListener {
    final Logger logger = LoggerFactory.getLogger(ConfigMonitoringContextListener.class);

    private ConfigMonitorRunner configMonitorRunner;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        configMonitorRunner = new ConfigMonitorRunner(getConfigurationMonitor(sce.getServletContext()));
        configMonitorRunner.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (configMonitorRunner != null) {
            configMonitorRunner.stop();
        }
    }

    public abstract Runnable getConfigurationMonitor(ServletContext servletContext) throws ConfigurationMonitorInitialisationException;

}