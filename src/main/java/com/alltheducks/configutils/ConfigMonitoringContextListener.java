package com.alltheducks.configutils;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This Class is responsible for Executing the Configuration Monitor process.
 * It gets the instance of the configurationMonitor from the spring context,
 * and expects it to have the ID "configurationMonitor".
 *
 * @author Shane Argo
 */
public class ConfigMonitoringContextListener implements ServletContextListener {

    private Runnable configurationMonitor;

    ExecutorService executorService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initialising configuration monitor.");

        final WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
        configurationMonitor = (Runnable)springContext.getBean("configurationMonitor");

        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(configurationMonitor);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Destroying configuration monitor.");

        if(executorService != null) {
            executorService.shutdownNow();

            boolean terminated;
            try {
                terminated = executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interruption whilst terminating configuration monitor");
            }

            if (!terminated) {
                throw new RuntimeException("Configuration monitor did not terminate within the timeout.");
            }
        }
    }
}