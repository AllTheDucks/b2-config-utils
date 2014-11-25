package com.alltheducks.configutils.service;


/**
 * Used for loading and persisting configuration.
 * Created by Wiley Fuller on 19/11/2014.
 */
public interface ConfigurationService<C> {

    public C loadConfiguration();

    public void persistConfiguration(C configuration);

}