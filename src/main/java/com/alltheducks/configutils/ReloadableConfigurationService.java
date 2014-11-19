package com.alltheducks.configutils;

/**
 * Created by wiley on 19/11/14.
 */
public interface ReloadableConfigurationService<C> extends ConfigurationService<C> {
    public void reload();
}
