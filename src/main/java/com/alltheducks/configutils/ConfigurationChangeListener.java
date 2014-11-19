package com.alltheducks.configutils;

/**
 * Created by wiley on 19/11/14.
 */
public interface ConfigurationChangeListener<C> {

    public void configurationChanged(C configuration);

}
