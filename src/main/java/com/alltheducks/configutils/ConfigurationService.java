package com.alltheducks.configutils;


/**
 * Used for loading and persisting configuration.
 * Created by Wiley Fuller on 19/11/2014.
 */
public interface ConfigurationService<C> {

    public C loadConfiguration();

    public void persistConfiguration(C configuration);

}