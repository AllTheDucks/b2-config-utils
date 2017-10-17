package com.alltheducks.configutils.service;


/**
 * <p>This is the base interface for classes that load and persist configuration.</p>
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public interface ConfigurationService<C> {

    public C loadConfiguration();

    public void persistConfiguration(C configuration);

}