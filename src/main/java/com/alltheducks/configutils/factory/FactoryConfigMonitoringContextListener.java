package com.alltheducks.configutils.factory;

import com.alltheducks.configutils.monitor.ConfigMonitoringContextListener;
import com.alltheducks.configutils.exception.ConfigurationMonitorInitialisationException;

import javax.servlet.ServletContext;

/**
 * Created by Shane Argo on 24/11/14.
 */
public class FactoryConfigMonitoringContextListener extends ConfigMonitoringContextListener {

    public static final String FACTORY_CLASS_PARAM_NAME = "ConfigurationMonitorFactory.Class";

    @Override
    public Runnable getConfigurationMonitor(ServletContext servletContext) throws ConfigurationMonitorInitialisationException {
        String factoryClassName = servletContext.getInitParameter(FACTORY_CLASS_PARAM_NAME);

        if(factoryClassName == null) {
            throw new ConfigurationMonitorInitialisationException(String.format("%s not defined in the web.xml.", FACTORY_CLASS_PARAM_NAME));
        }

        Class factoryClass;
        try {
            factoryClass = Class.forName(factoryClassName);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationMonitorInitialisationException(String.format("Class defined in %s in the web.xml could not be found.", FACTORY_CLASS_PARAM_NAME), e);
        }

        ConfigurationMonitorFactory factory;
        try {
            factory = (ConfigurationMonitorFactory) factoryClass.newInstance();
        } catch (ClassCastException e) {
            throw new ConfigurationMonitorInitialisationException(String.format("Class defined in %s in the web.xml does not implement ConfigurationMonitorFactory.", FACTORY_CLASS_PARAM_NAME), e);
        } catch (InstantiationException e) {
            throw new ConfigurationMonitorInitialisationException(String.format("Could not initialise class defined in %s in the web.xml.", FACTORY_CLASS_PARAM_NAME), e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationMonitorInitialisationException(String.format("Illegal access to class defined in %s in the web.xml.", FACTORY_CLASS_PARAM_NAME), e);
        }

        return factory.getConfigurationMonitor();
    }

}
