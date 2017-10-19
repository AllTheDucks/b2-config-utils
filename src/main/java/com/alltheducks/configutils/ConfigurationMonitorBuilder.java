package com.alltheducks.configutils;

import com.alltheducks.configutils.monitor.ConfigurationChangeListener;
import com.alltheducks.configutils.monitor.PollingConfigurationMonitor;
import com.alltheducks.configutils.service.ReloadableConfigurationService;

import java.io.File;
import java.util.List;

public class ConfigurationMonitorBuilder<T> {

    private static final int DEFUALT_POLLING_FREQ_SECONDS = 10;

    private File configFile;
    private int pollingFreqSeconds;
    private ReloadableConfigurationService<T> configService;
    private List<? extends ConfigurationChangeListener<T>> configChangeListeners;

    public ConfigurationMonitorBuilder<T> withConfigService(final ReloadableConfigurationService<T> configService) {
        this.configService = configService;
        return this;
    }

    public ConfigurationMonitorBuilder<T> withConfigFile(final File configFile) {
        this.configFile = configFile;
        return this;
    }

    public ConfigurationMonitorBuilder<T> withPollingFreqSeconds(final int pollingFreqSeconds) {
        this.pollingFreqSeconds = pollingFreqSeconds;
        return this;
    }

    public ConfigurationMonitorBuilder<T> withConfigChangeListeners(final List<? extends ConfigurationChangeListener<T>> configChangeListeners) {
        this.configChangeListeners = configChangeListeners;
        return this;
    }

    public Runnable build() {
        if (configService == null) {
            throw new RuntimeException("Configuration service not specified");
        }
        if (configFile == null) {
            throw new RuntimeException("Configuration file not specified");
        }
        if (pollingFreqSeconds <= 0) {
            pollingFreqSeconds = DEFUALT_POLLING_FREQ_SECONDS;
        }

        return new PollingConfigurationMonitor<T>(pollingFreqSeconds, configFile, configService, configChangeListeners);
    }

}
