package com.alltheducks.configutils.servlet;

import com.alltheducks.configutils.exception.ConfigurationMonitorInitialisationException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

/**
 *<p>
 * This Class is responsible for Executing the Configuration Monitor process.
 * It gets the instance of the configurationMonitor from the spring context,
 * and expects it to have the ID <em>configurationMonitor</em> or an alternative
 * name defined in an {@code <context-param>} with the name <em>configurationMonitor</em>.<br>
 * You configure the Listener in your web.xml as follows.</p>
 * <pre>
 * {@code
 * <listener>
 *  <listener-class>
 *     com.alltheducks.configutils.servlet.SpringBeanConfigMonitoringContextListener
 *  </listener-class>
 *</listener>}
 *</pre>
 *
 * <p>If you want to use a custom name for your spring bean, e.g. <em>myBeanId</em>,
 * your web.xml would look like this:</p>
 * <pre>
 * {@code
 * <context-param>
 *   <param-name>ConfigurationMonitor.Bean</param-name>
 *   <param-value>myBeanId</param-value>
 * </context-param>
 * <listener>
 *  <listener-class>
 *     com.alltheducks.configutils.servlet.SpringBeanConfigMonitoringContextListener
 *  </listener-class>
 *  <init-param></init-param>
 *</listener>}
 *</pre>
 * <p>If you are using the config manager "out of the box", your spring xml
 * configuration will look like the following.  This framework does not currently
 * support Spring AutoWiring.</p>
 * <pre>
 * {@code <bean id="configDir" class="blackboard.platform.plugin.PlugInUtil"
 *         factory-method="getConfigDirectory">
 *   <constructor-arg name="vid" value="atd"/>
 *   <constructor-arg name="handle" value="myCoolB2"/>
 * </bean>
 *
 * <bean id="configFile" class="java.io.File">
 *   <constructor-arg index="0" ref="configDir"/>
 *   <constructor-arg index="1" value="config.xml"/>
 * </bean>
 *
 * <bean id="configurationService"
 *       class="com.alltheducks.configutils.CachingConfigurationService">
 *   <constructor-arg name="internalConfigurationService">
 *     <bean class="com.alltheducks.configutils.XmlConfigurationService">
 *       <property name="configurationXMLFile" ref="configFile"/>
 *     </bean>
 *   </constructor-arg>
 * </bean>
 *
 * <bean id="configurationMonitor"
 *       class="com.alltheducks.configutils.PollingConfigurationMonitor">
 *   <constructor-arg name="configurationFile" ref="configFile"/>
 *   <constructor-arg name="configurationService"
 *                    ref="configurationService"/>
 *   <constructor-arg name="pollFreq" value="10"/>
 * </bean>}
 * </pre>
 *
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 *
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
