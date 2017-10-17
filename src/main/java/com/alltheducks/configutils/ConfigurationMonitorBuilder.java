package com.alltheducks.configutils;

import com.alltheducks.configutils.monitor.ConfigurationChangeListener;
import com.alltheducks.configutils.monitor.PollingConfigurationMonitor;
import com.alltheducks.configutils.service.ReloadableConfigurationService;

import java.io.File;
import java.util.List;

public class ConfigurationMonitorBuilder {

    private static final int DEFUALT_POLLING_FREQ_SECONDS = 10;

    private File configFile;
    private int pollingFreqSeconds;
    private ReloadableConfigurationService configService;
    private List<ConfigurationChangeListener> configChangeListeners;

    public ConfigurationMonitorBuilder withConfigService(final ReloadableConfigurationService configService) {
        this.configService = configService;
        return this;
    }

    public ConfigurationMonitorBuilder withConfigFile(final File configFile) {
        this.configFile = configFile;
        return this;
    }

    public ConfigurationMonitorBuilder withPollingFreqSeconds(final int pollingFreqSeconds) {
        this.pollingFreqSeconds = pollingFreqSeconds;
        return this;
    }

    public ConfigurationMonitorBuilder withConfigChangeListeners(final List<ConfigurationChangeListener> configChangeListeners) {
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

        return new PollingConfigurationMonitor(pollingFreqSeconds, configFile, configService, configChangeListeners);
    }

}
