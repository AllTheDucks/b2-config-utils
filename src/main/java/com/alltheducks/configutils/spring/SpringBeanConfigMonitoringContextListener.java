package com.alltheducks.configutils.spring;

import com.alltheducks.configutils.monitor.ConfigMonitoringContextListener;
import com.alltheducks.configutils.exception.ConfigurationMonitorInitialisationException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

/**
 * Created by Shane Argo on 24/11/14.
 */
public class SpringBeanConfigMonitoringContextListener extends ConfigMonitoringContextListener {

    public static final String BEAN_PARAM_NAME = "ConfigurationMonitor.Bean";
    public static final String BEAN_NAME_DEFAULT = "configurationMonitor";

    @Override
    public Runnable getConfigurationMonitor(ServletContext servletContext) throws ConfigurationMonitorInitialisationException {
        String beanName = servletContext.getInitParameter(BEAN_PARAM_NAME);

        if(beanName == null) {
            beanName = BEAN_NAME_DEFAULT;
        }

        final WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        return (Runnable)springContext.getBean(beanName);
    }

}
