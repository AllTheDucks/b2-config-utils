package com.alltheducks.configutils.jersey;

import com.alltheducks.configutils.monitor.ConfigMonitorRunner;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigMonitoringContainerLifecycleListener implements ContainerLifecycleListener {
    final Logger logger = LoggerFactory.getLogger(ConfigMonitoringContainerLifecycleListener.class);

    private final ConfigMonitorRunner configMonitorRunner;

    public ConfigMonitoringContainerLifecycleListener(final ConfigMonitorRunner configMonitorRunner) {
        this.configMonitorRunner = configMonitorRunner;
    }

    public ConfigMonitoringContainerLifecycleListener(final Runnable configMonitor) {
        this.configMonitorRunner = new ConfigMonitorRunner(configMonitor);
    }

    @Override
    public void onStartup(final Container container) {
        configMonitorRunner.start();
    }

    @Override
    public void onReload(final Container container) {

    }

    @Override
    public void onShutdown(final Container container) {
        configMonitorRunner.stop();
    }
}
