package com.alltheducks.configutils.monitor;

import com.alltheducks.configutils.service.ReloadableConfigurationService;

import java.io.File;
import java.util.List;


/**
 * <p>Monitors a configuration file for changes.</p>
 * <p>The PollingConfigurationMonitor runs on a background thread and monitors
 * a configuration file for changes.  When changes are detected, it reloads
 * the configuration locally, and calls {@link com.alltheducks.configutils.monitor.ConfigurationChangeListener#configurationChanged(Object)}
 * on each registered listener.</p>
 * @see com.alltheducks.configutils.monitor.ConfigurationChangeListener
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public class PollingConfigurationMonitor implements Runnable {

    private final ReloadableConfigurationService configurationService;
    private final List<ConfigurationChangeListener> listeners;
    private File configurationFile;
    private int pollFreqSeconds;

    private long lastReload = 0;

    public PollingConfigurationMonitor(int pollFreqSeconds, File configurationFile,
                                       ReloadableConfigurationService configurationService) {
        this(pollFreqSeconds, configurationFile, configurationService, null);
    }

    public PollingConfigurationMonitor(int pollFreqSeconds, File configurationFile,
                                       ReloadableConfigurationService configurationService,
                                       List<ConfigurationChangeListener> listeners) {
        this.configurationFile = configurationFile;
        this.pollFreqSeconds = pollFreqSeconds;
        this.configurationService = configurationService;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (configurationFile.lastModified() > lastReload) {
                configurationService.reload();
                lastReload = configurationFile.lastModified();
            }
            Object config = configurationService.loadConfiguration();
            if (listeners != null) {
                for (ConfigurationChangeListener listener : listeners) {
                    listener.configurationChanged(config);
                }
            }
            try {
                Thread.sleep(pollFreqSeconds * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


}