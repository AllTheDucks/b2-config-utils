package com.alltheducks.configutils.monitor;

import com.alltheducks.configutils.service.ReloadableConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;


/**
 * <p>Monitors a configuration file for changes.</p>
 * <p>The PollingConfigurationMonitor runs on a background thread and monitors
 * a configuration file for changes.  When changes are detected, it reloads
 * the configuration locally, and calls {@link com.alltheducks.configutils.monitor.ConfigurationChangeListener#configurationChanged(Object)}
 * on each registered listener.</p>
 *
 * @see com.alltheducks.configutils.monitor.ConfigurationChangeListener
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public class PollingConfigurationMonitor<T> implements Runnable {

    final Logger logger = LoggerFactory.getLogger(PollingConfigurationMonitor.class);

    private final ReloadableConfigurationService<T> configurationService;
    private final List<? extends ConfigurationChangeListener<T>> listeners;
    private File configurationFile;
    private int pollFreqSeconds;

    private long lastReload = -1;

    public PollingConfigurationMonitor(int pollFreqSeconds, File configurationFile,
                                       ReloadableConfigurationService<T> configurationService) {
        this(pollFreqSeconds, configurationFile, configurationService, null);
    }

    public PollingConfigurationMonitor(int pollFreqSeconds, File configurationFile,
                                       ReloadableConfigurationService<T> configurationService,
                                       List<? extends ConfigurationChangeListener<T>> listeners) {
        logger.debug("Initialising PollingConfigurationMonitor (Polling freq: {}, Config file: {}, Config service: {}, Listeners: {})",
                pollFreqSeconds,
                (configurationFile == null ? "null" : configurationFile.getName()),
                (configurationService == null ? "null" : configurationService.getClass().getName()),
                (listeners == null ? "null" : listeners.size()));

        this.configurationFile = configurationFile;
        this.pollFreqSeconds = pollFreqSeconds;
        this.configurationService = configurationService;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        logger.debug("Started polling for configuration file changes...");
        while (!Thread.currentThread().isInterrupted()) {
            logger.trace("Polling for changes to the config file.");
            if (configurationFile.lastModified() != lastReload) {
                logger.debug("Configuration file modified.  Reloading.");
                configurationService.reload();
                lastReload = configurationFile.lastModified();

                T config = configurationService.loadConfiguration();
                if (listeners != null) {
                    logger.debug("PollingConfigurationMonitor has {} listeners. Notifying the listeners now.", listeners.size());
                    for (ConfigurationChangeListener<T> listener : listeners) {
                        callListener(config, listener);
                    }
                }
            }

            try {
                Thread.sleep(pollFreqSeconds * 1000);
            } catch (InterruptedException e) {
                logger.debug("PollingConfigurationMonitor thread has been interrupted. Shutting down.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void callListener(T config, ConfigurationChangeListener<T> listener) {
        logger.debug("Calling configurationChanged on listener: {}", listener.getClass().getName());
        listener.configurationChanged(config);
    }

    public ReloadableConfigurationService getConfigurationService() {
        return configurationService;
    }
}
