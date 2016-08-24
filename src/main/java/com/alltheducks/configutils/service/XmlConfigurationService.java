package com.alltheducks.configutils.service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Loads and persists the configuration from and into an XML file in Blackboard's shared content.
 * Created by Shane Argo on 23/05/14.
 * <p/>
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public class XmlConfigurationService<C> implements ConfigurationService<C> {
    final Logger logger = LoggerFactory.getLogger(XmlConfigurationService.class);

    private File configurationXmlFile;
    private String defaultConfigFileClasspathLocation;
    private XStream xStream;
    private Class<C> configClass;

    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

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
        logger.debug("Initialising XmlConfigurationService.");
        this.configurationXmlFile = configurationXmlFile;
        this.defaultConfigFileClasspathLocation = defaultConfigFileClasspathLocation;
        this.configClass = configClass;

        if (xStream == null) {
            this.xStream = new XStream(new DomDriver("UTF-8"));
            this.xStream.ignoreUnknownElements();
        } else {
            this.xStream = xStream;
        }
    }

    /**
     * Loads the configuration from the central configuration file on Blackboard's shared content.
     *
     * @return The loaded configuration
     */
    @Override
    public C loadConfiguration() {
        C configuration = null;

        InputStream defaultConfigIS = null;
        if (defaultConfigFileClasspathLocation != null){
            defaultConfigIS = XmlConfigurationService.class.getResourceAsStream(defaultConfigFileClasspathLocation);
            if(defaultConfigIS == null) {
                logger.warn("Could not locate default configuration file on the classpath: {}", defaultConfigFileClasspathLocation);
            }
        }
        if(defaultConfigIS != null) {
            configuration = decodeXmlIS(defaultConfigIS, xStream);
        } else if(configClass != null) {
            try {
                configuration = configClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Could not instantiate an instance of the configuration bean.", e);
            }
        }

        if (!configurationXmlFile.exists())
        {
            return configuration;
        }

        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            try (InputStream inputStream = new FileInputStream(configurationXmlFile)) {
                logger.debug("Loading configuration from XML file");
                return decodeXmlIS(inputStream, xStream, configuration);
            } catch (IOException ex) {
                logger.error("Unexpected IOException while loading XML", ex);
                throw new RuntimeException(ex);
            }
        } finally {
            readLock.unlock();
        }
    }

    private C decodeXmlIS(InputStream inputStream, XStream xstream) {
        return decodeXmlIS(inputStream, xstream, null);
    }

    @SuppressWarnings("unchecked")
    private C decodeXmlIS(InputStream inputStream, XStream xstream, C root) {
        C configuration = (C) xstream.fromXML(inputStream, root);
        checkType(configuration);
        return configuration;
    }


    /**
     * Persists the configuration into the central configuration file on Blackboard's shared content.
     *
     * @param configuration The configuration to be persisted.
     */
    @Override
    public void persistConfiguration(C configuration) {
        checkType(configuration);

        Lock writeLock = rwLock.writeLock();

        writeLock.lock();
        try (FileChannel fileChannel = new RandomAccessFile(configurationXmlFile, "rw").getChannel()) {
            FileLock fileLock = fileChannel.lock();
            fileChannel.truncate(0);
            try (OutputStream outputStream = Channels.newOutputStream(fileChannel)) {
                logger.debug("Persisting configuration to XML file");
                xStream.toXML(configuration, outputStream);
                fileLock.release();
            }

        } catch (IOException ex) {
            logger.error("Unexpected IOException while persisting XML", ex);
            throw new RuntimeException(String.format("Failed to open configuration file for writing: %s", configurationXmlFile.getAbsolutePath()), ex);
        } finally {
            writeLock.unlock();
        }

    }

    private void checkType(C configuration) {
        if(configClass != null && !configuration.getClass().isInstance(configClass)) {
            logger.error("Configuration class is not the expected type.");
            throw new RuntimeException("Configuration class is not the expected type.");
        }
    }



}
