package com.alltheducks.configutils;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Shane Argo on 18/09/2014.
 */
public class PollingConfigurationMonitor implements Runnable {

    private final ReloadableConfigurationService configurationService;
    private final List<ConfigurationChangeListener> listeners;
    private File configurationFile;
    private int pollFreq;

    private long lastReload = 0;

    public PollingConfigurationMonitor(int pollFreq, File configurationFile,
                                       ReloadableConfigurationService configurationService) {
        this(pollFreq, configurationFile, configurationService, null);
    }

    public PollingConfigurationMonitor(int pollFreq, File configurationFile,
                                       ReloadableConfigurationService configurationService,
                                       List<ConfigurationChangeListener> listeners) {
        this.configurationFile = configurationFile;
        this.pollFreq = pollFreq;
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
                Thread.sleep(pollFreq);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


}