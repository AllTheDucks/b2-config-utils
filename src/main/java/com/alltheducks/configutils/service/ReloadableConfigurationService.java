package com.alltheducks.configutils.service;

import com.alltheducks.configutils.service.ConfigurationService;

/**
 * Created by wiley on 19/11/14.
 */
public interface ReloadableConfigurationService<C> extends ConfigurationService<C> {
    public void reload();
}
