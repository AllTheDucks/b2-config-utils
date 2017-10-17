package com.alltheducks.configutils.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public abstract class FileConfigurationService<C> implements ConfigurationService<C> {
    final Logger logger = LoggerFactory.getLogger(FileConfigurationService.class);

    private File configurationFile;
    private String defaultConfigFileClasspathLocation;
    protected Class<C> configClass;

    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public FileConfigurationService(Class<C> configClass, File configurationFile, String defaultConfigFileClasspathLocation) {
        logger.debug("Initialising XmlConfigurationService.");
        this.configurationFile = configurationFile;
        this.defaultConfigFileClasspathLocation = defaultConfigFileClasspathLocation;
        this.configClass = configClass;
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
        if (defaultConfigFileClasspathLocation != null) {
            defaultConfigIS = FileConfigurationService.class.getResourceAsStream(defaultConfigFileClasspathLocation);
            if (defaultConfigIS == null) {
                logger.warn("Could not locate default configuration file on the classpath: {}", defaultConfigFileClasspathLocation);
            }
        }
        if (defaultConfigIS != null) {
            configuration = decode(defaultConfigIS);
        } else if (configClass != null) {
            try {
                configuration = configClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Could not instantiate an instance of the configuration bean.", e);
            }
        }

        if (!configurationFile.exists()) {
            return configuration;
        }

        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            try (InputStream inputStream = new FileInputStream(configurationFile)) {
                logger.debug("Loading configuration from XML file");
                return decode(inputStream, configuration);
            } catch (IOException ex) {
                logger.error("Unexpected IOException while loading XML", ex);
                throw new RuntimeException(ex);
            }
        } finally {
            readLock.unlock();
        }
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
        try (FileChannel fileChannel = new RandomAccessFile(configurationFile, "rw").getChannel()) {
            FileLock fileLock = fileChannel.lock();
            try (OutputStream outputStream = Channels.newOutputStream(fileChannel)) {
                fileChannel.truncate(0);
                logger.debug("Persisting configuration to XML file");
                encode(configuration, outputStream);

            } finally {
                if (fileLock.isValid()) {
                    fileLock.release();
                }
            }

        } catch (IOException ex) {
            logger.error("Unexpected IOException while persisting XML", ex);
            throw new RuntimeException(String.format("Failed to open configuration file for writing: %s", configurationFile.getAbsolutePath()), ex);
        } finally {
            writeLock.unlock();
        }

    }

    protected void checkType(Object configuration) {
        if (configClass != null && !configClass.isInstance(configuration)) {
            logger.error("Configuration class is not the expected type.");
            throw new RuntimeException("Configuration class is not the expected type.");
        }
    }

    abstract C decode(InputStream inputStream);

    abstract C decode(InputStream inputStream, C defaultConfig);

    abstract void encode(C configuration, OutputStream outputStream);

}
