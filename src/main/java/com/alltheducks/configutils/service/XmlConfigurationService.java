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
    private XStream xStream;

    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public XmlConfigurationService(File configurationXmlFile) {
        this(configurationXmlFile, null);
    }

    public XmlConfigurationService(File configurationXmlFile, XStream xStream) {
        logger.debug("Initialising XmlConfigurationService.");
        this.configurationXmlFile = configurationXmlFile;

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
    @SuppressWarnings("unchecked")
    public C loadConfiguration() {
        Lock readLock = rwLock.readLock();
        C configuration = null;

        readLock.lock();
        try {
            if (!configurationXmlFile.exists()) {
                logger.warn("XML configuration file doesn't exist: %s", configurationXmlFile.getAbsolutePath());
                return null;
            }

            try (InputStream inputStream = new FileInputStream(configurationXmlFile)) {
                logger.debug("Loading configuration from XML file");
                configuration = decodeXmlIS(inputStream, xStream);
            } catch (IOException ex) {
                logger.error("Unexpected IOException while loading XML", ex);
                throw new RuntimeException(ex);
            }
        } finally {
            readLock.unlock();
        }

        return configuration;
    }

    @SuppressWarnings("unchecked")
    private C decodeXmlIS(InputStream inputStream, XStream xstream) {
        return (C) xstream.fromXML(inputStream);
    }


    /**
     * Persists the configuration into the central configuration file on Blackboard's shared content.
     *
     * @param configuration The configuration to be persisted.
     */
    @Override
    public void persistConfiguration(C configuration) {
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
            throw new RuntimeException(String.format("Failed to open configuration file for writing: {}", configurationXmlFile.getAbsolutePath()), ex);
        } finally {
            writeLock.unlock();
        }

    }


}
