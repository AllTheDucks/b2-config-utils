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
    final Logger logger = LoggerFactory.getLogger(XmlConfigurationService.class);

    private XStream xStream;

    public XmlConfigurationService(File configurationXmlFile) {
        this(null, configurationXmlFile, null, null);
    }

    public XmlConfigurationService(File configurationXmlFile, String defaultConfigFileClasspathLocation) {
        this(null, configurationXmlFile, defaultConfigFileClasspathLocation, null);
    }

    public XmlConfigurationService(File configurationXmlFile, XStream xStream) {
        this(null, configurationXmlFile, null, xStream);
    }

    public XmlConfigurationService(File configurationXmlFile, String defaultConfigFileClasspathLocation, XStream xStream) {
        this(null, configurationXmlFile, defaultConfigFileClasspathLocation, xStream);
    }

    public XmlConfigurationService(Class<C> configClass, File configurationXmlFile) {
        this(configClass, configurationXmlFile, null, null);
    }

    public XmlConfigurationService(Class<C> configClass, File configurationXmlFile, String defaultConfigFileClasspathLocation) {
        this(configClass, configurationXmlFile, defaultConfigFileClasspathLocation, null);
    }

    public XmlConfigurationService(Class<C> configClass, File configurationXmlFile, XStream xStream) {
        this(configClass, configurationXmlFile, null, xStream);
    }

    public XmlConfigurationService(Class<C> configClass, File configurationXmlFile, String defaultConfigFileClasspathLocation, XStream xStream) {
        super(configClass, configurationXmlFile, defaultConfigFileClasspathLocation);
        logger.debug("Initialising XmlConfigurationService.");

        if (xStream == null) {
            this.xStream = new XStream(new DomDriver("UTF-8"));
            this.xStream.ignoreUnknownElements();
        } else {
            this.xStream = xStream;
        }
    }

    @Override
    C decode(InputStream inputStream) {
        return decode(inputStream, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    C decode(InputStream inputStream, C defaultConfig) {
        Object configuration = xStream.fromXML(inputStream, defaultConfig);
        checkType(configuration);
        return (C) configuration;
    }

    @Override
    void encode(C configuration, OutputStream outputStream) {
        xStream.toXML(configuration, outputStream);
    }

}
