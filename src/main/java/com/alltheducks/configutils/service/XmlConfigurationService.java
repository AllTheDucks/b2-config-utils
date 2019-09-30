package com.alltheducks.configutils.service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Loads and persists the configuration from and into an XML file in Blackboard's shared content.
 * Created by Shane Argo on 23/05/14.
 * <p/>
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public class XmlConfigurationService<C> extends FileConfigurationService<C> {

    private final Logger logger = LoggerFactory.getLogger(XmlConfigurationService.class);
    private final XStream xStream;

    public XmlConfigurationService(final File configurationXmlFile) {
        this(null, configurationXmlFile, null, null);
    }

    public XmlConfigurationService(final File configurationXmlFile,
                                   final String defaultConfigFileClasspathLocation) {
        this(null, configurationXmlFile, defaultConfigFileClasspathLocation, null);
    }

    public XmlConfigurationService(final File configurationXmlFile,
                                   final XStream xStream) {
        this(null, configurationXmlFile, null, xStream);
    }

    public XmlConfigurationService(final File configurationXmlFile,
                                   final String defaultConfigFileClasspathLocation,
                                   final XStream xStream) {
        this(null, configurationXmlFile, defaultConfigFileClasspathLocation, xStream);
    }

    public XmlConfigurationService(final Class<C> configClass,
                                   final File configurationXmlFile) {
        this(configClass, configurationXmlFile, null, null);
    }

    public XmlConfigurationService(final Class<C> configClass,
                                   final File configurationXmlFile,
                                   final String defaultConfigFileClasspathLocation) {
        this(configClass, configurationXmlFile, defaultConfigFileClasspathLocation, null);
    }

    public XmlConfigurationService(final Class<C> configClass,
                                   final File configurationXmlFile,
                                   final XStream xStream) {
        this(configClass, configurationXmlFile, null, xStream);
    }

    public XmlConfigurationService(final Class<C> configClass,
                                   final File configurationXmlFile,
                                   final String defaultConfigFileClasspathLocation,
                                   final XStream xStream) {
        super(configClass, configurationXmlFile, defaultConfigFileClasspathLocation);
        logger.debug("Initialising XmlConfigurationService.");

        if (xStream == null) {
            this.xStream = buildDefaultXStream(configClass);
        } else {
            this.xStream = xStream;
        }
    }

    @Override
    C decode(final InputStream inputStream) {
        return decode(inputStream, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    C decode(final InputStream inputStream, final C defaultConfig) {
        final Object configuration = xStream.fromXML(inputStream, defaultConfig);
        this.checkType(configuration);
        return (C) configuration;
    }

    @Override
    void encode(final C configuration, final OutputStream outputStream) {
        this.xStream.toXML(configuration, outputStream);
    }

    private static <C> XStream buildDefaultXStream(final Class<C> configClass) {
        final XStream newXStream = new XStream(new DomDriver("UTF-8"));
        newXStream.ignoreUnknownElements();

        if(configClass != null) {
            XStream.setupDefaultSecurity(newXStream);
            newXStream.allowTypes(new Class[]{configClass});
        }
        
        return newXStream;
    }

}
