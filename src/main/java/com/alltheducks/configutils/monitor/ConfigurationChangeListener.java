package com.alltheducks.configutils.monitor;

/**
 * <p>Listener interface for Configuration Change events.</p>
 * <p>This interface should be implemented by any class that needs to receive
 * notifications of configuration changes.  The Listeners should be registered
 * with the ConfigurationMonitor, and the configurationChanged method will be
 * called whenever the configuration changes.</p>
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public interface ConfigurationChangeListener<C> {

    public void configurationChanged(C configuration);

}
