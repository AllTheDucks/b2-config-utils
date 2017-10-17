package com.alltheducks.configutils.monitor;

import com.alltheducks.configutils.exception.ConfigurationMonitorInitialisationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConfigMonitorRunner {
    final Logger logger = LoggerFactory.getLogger(ConfigMonitorRunner.class);

    static final int TERMINATION_TIMEOUT_SECONDS = 5;

    private ExecutorService executorService;
    private Runnable configMonitor;

    public ConfigMonitorRunner(Runnable configMonitor) {
        this.configMonitor = configMonitor;
    }

    public void start() {
        logger.info("Initialising configuration monitor.");

        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(configMonitor);
    }

    public void stop() {
        logger.info("Destroying configuration monitor.");

        if (executorService != null) {
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

}
