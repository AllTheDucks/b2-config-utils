# Blacboard Building Block Config Utilities Library #
This is a collection of classes designed to make loading and saving configuration settings easier in Building Blocks.

The classes in this library load and persist the configuration to and from an XML file. For effeciency the configuration 
is cached in memory. The servlet context listener starts a thread which monitors the configuration file for changes 
and recaches it when necessary and is therefore safe to use in a multi-server setup.



## Building ##
The build tool used is called [Gradle](http://www.gradle.org). It will do all the work to build the building block, 
including downloading dependencies, compiling java classes and zipping up the JAR. This is all done with a single 
command - no installation necessary. From the root of the project execute the following command:

On Windows:
gradlew build

On Linux/Mac:
./gradlew build

This built package will be output to:
build/libs/b2-config-utils-[version].jar



## Cleaning ##
If you want to clean the build artifacts execute this command:

On Windows:
gradlew clean

On Linux/Mac:
./gradlew clean



## Using this library ##
1. Add the JAR as a dependency in your project. The steps to do this will vary depending upon your build tool.
2. Configure servlet context listener in web.xml.
3. Create a POJO for loading and persisting your configuration.
4. Persist and load with ConfigurationService.



## Spring Example ##
If you are using [Spring Beans](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html) for 
Dependency Injection, the library provides an implementation of the servlet context listener. 

**web.xml**
````xml
<listener>
    <listener-class>com.alltheducks.configutils.servlet.SpringBeanConfigMonitoringContextListener</listener-class>
</listener>
````

````xml
<bean id="configurationDirectory" class="blackboard.platform.plugin.PlugInUtil" factory-method="getConfigDirectory">
    <constructor-arg name="vid" ref="me" />
    <constructor-arg name="handle" ref="myb2" />
</bean>
<bean id="configurationXMLFile" class="java.io.File">
    <constructor-arg index="0" value="config.xml" />
    <constructor-arg index="1" ref="configurationDirectory" />
</bean>
<bean id="configurationService"
      class="com.alltheducks.configutils.service.CachingConfigurationService">
    <constructor-arg name="internalConfigurationService">
        <bean class="com.alltheducks.configutils.service.XmlConfigurationService">
            <constructor-arg name="configurationXmlFile" ref="configurationXMLFile" />
        </bean>
    </constructor-arg>
</bean>
<bean id="configurationMonitor"
      class="com.alltheducks.configutils.monitor.PollingConfigurationMonitor">
    <constructor-arg name="configurationFile" ref="configurationXMLFile" />
    <constructor-arg name="configurationService" ref="configurationService" />
    <constructor-arg name="pollFreqSeconds" value="10" />
</bean>
````

**Configuration POJO**
````java
public class Configuration {
    
    private String myString;
    private int myInt;
    private SomeOtherPojo myClass;

    public String getMyString() {
        return myString;
    }

    public void setMyString(String myString) {
        this.myString = myString;
    }

    public int getMyInt() {
        return myInt;
    }

    public void setMyInt(int myInt) {
        this.myInt = myInt;
    }

    public SomeOtherPojo getMyClass() {
        return myClass;
    }

    public void setMyClass(SomeOtherPojo myClass) {
        this.myClass = myClass;
    }
}
````

**Loading Configuration**
````java
private ConfigurationService<Configuration> configService; //injected with Spring

public doStuff() {
	Configuration config = configService.loadConfiguration();
	//do what ever you want with config
}
````

**Persisting Configuration**
````java
private ConfigurationService<Configuration> configService; //injected with Spring

public doStuff() {
	//load and modify or create a new Configuration object
	configService.loadConfiguration(config);
}
````


## Not using Spring Beans? ##
These utilities can be used with other Dependency Injection frameworks, or none at all, but you must implement the
servlet context listener for yourself.

This is an example that doesn't use Dependency Injection at all. Instead it uses a static factory class. This is only
and example. It will work, but there is more effecient methods, with less reliance on "synchronized".

**Factory**
````java
public class MyFactory {
	public static ConfigurationService service;
	public static File configFile;

	public static synchronized File getConfigFile() {
		if(configFile == null) {
			File b2ConfigDir = blackboard.platform.plugin.PlugInUtil.getConfigDirectory("me", "myb2");
        	configFile = new File("myConfig.xml", b2ConfigDir);
    	}
    	return configFile;
	}

	public static synchronized File getConfigService() {
    	if(service == null) {
        	ConfigurationService xmlService = new XmlConfigurationService(getConfigFile());
        	service = new CachingConfigurationService(xmlService)
    	}   
    	return service;
    }
}
````

**Servlet Context Listener**
````java
public class MyConfigListener extends ConfigMonitoringContextListener {

    @Override
    public Runnable getConfigurationMonitor(ServletContext servletContext) throws ConfigurationMonitorInitialisationException {
        return new PollingConfigurationMonitor(10, MyFactory.getConfigFile(), MyFactory.getConfigService());
    }

}
````

**web.xml**
````xml
<listener>
    <listener-class>my.package.name.MyConfigListener</listener-class>
</listener>
````

**Loading Configuration**
````java
public doStuff() {
	Configuration config = MyFactory.getConfigService().loadConfiguration();
	//do what ever you want with config
}
````

**Persisting Configuration**
````java
public doStuff() {
	//load and modify or create a new Configuration object
	MyFactory.getConfigService().loadConfiguration(config);
}
````



## Configuration Change Listener ##
There are some cases when you'll want to be notified of a configuration reload. There is an optional fourth parameter on
the PollingConfigurationMonitor class. This parameter is a list of ConfigurationChangeListener objects.

**Example Change Listener**
````java
public class MyChangeListener implements ConfigurationChangeListener<Configuration> {
    @Override
    public void configurationChanged(Configuration configuration) {
 		//do something       
    }
}
````

**Spring**
````xml
<bean id="myChangeListener" class="my.package.name.MyChangeListener" />
<bean id="configurationMonitor"
      class="com.alltheducks.configutils.monitor.PollingConfigurationMonitor">
    <constructor-arg name="configurationFile" ref="configurationXMLFile" />
    <constructor-arg name="configurationService" ref="configurationService" />
    <constructor-arg name="pollFreqSeconds" value="10" />
    <constructor-arg name="listeners">
        <list>
            <ref bean="myChangeListener"/>
        </list>
    </constructor-arg>
</bean>
````

**Static Factory**
````java
public class MyConfigListener extends ConfigMonitoringContextListener {
    @Override
    public Runnable getConfigurationMonitor(ServletContext servletContext) throws ConfigurationMonitorInitialisationException {
    	List<ConfigurationChangeListener> listeners = new ArrayList<ConfigurationChangeListeners>();
    	listeners.Add(new MyChangeListener());
        return new PollingConfigurationMonitor(10, MyFactory.getConfigFile(), MyFactory.getConfigService(), listeners);
    }
}
````
