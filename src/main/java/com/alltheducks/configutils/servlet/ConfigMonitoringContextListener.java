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

    ExecutorService executorService;
    final Logger logger = LoggerFactory.getLogger(ConfigMonitoringContextListener.class);

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
                terminated = executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new ConfigurationMonitorInitialisationException("Interruption whilst terminating configuration monitor");
            }

            if (!terminated) {
                throw new ConfigurationMonitorInitialisationException("Configuration monitor did not terminate within the timeout.");
            }
        }
    }

    public abstract Runnable getConfigurationMonitor(ServletContext servletContext) throws ConfigurationMonitorInitialisationException;

}