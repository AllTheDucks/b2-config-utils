package com.alltheducks.configutils.service;

/**
 * <p>This interface is implemented by any ConfigurationService implementations
 * that cache the configuration. The reload method should be called by any
 * classes that need to ensure they have a fresh copy of the configuration.</p>
 * <p>
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public interface ReloadableConfigurationService<C> extends ConfigurationService<C> {
    public void reload();
}
