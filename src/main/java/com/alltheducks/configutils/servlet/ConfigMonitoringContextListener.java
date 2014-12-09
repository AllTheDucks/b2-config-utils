package com.alltheducks.configutils.servlet;

import com.alltheducks.configutils.exception.ConfigurationMonitorInitialisationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *<p>
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
 *</listener>}
 *</pre>
 *
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public abstract class ConfigMonitoringContextListener implements ServletContextListener {
    final Logger logger = LoggerFactory.getLogger(ConfigMonitoringContextListener.class);

    ExecutorService executorService;

    public static final int TERMINATION_TIMEOUT_SECONDS = 5;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initialising configuration monitor.");

        Runnable configurationMonitor = getConfigurationMonitor(sce.getServletContext());

        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(configurationMonitor);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Destroying configuration monitor.");

        if(executorService != null) {
            executorService.shutdownNow();

            boolean terminated;
            try {
                terminated = executorService.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new ConfigurationMonitorInitialisationException("Interruption whilst terminating configuration monitor");
            }

            if (!terminated) {
                throw new ConfigurationMonitorInitialisationException(String.format("Configuration monitor did not terminate within the timeout (%s seconds).", TERMINATION_TIMEOUT_SECONDS));
            }
        }
    }

    public abstract Runnable getConfigurationMonitor(ServletContext servletContext) throws ConfigurationMonitorInitialisationException;

}