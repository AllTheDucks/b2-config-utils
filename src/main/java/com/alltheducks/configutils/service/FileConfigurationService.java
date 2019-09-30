package com.alltheducks.configutils.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public abstract class FileConfigurationService<C> implements ConfigurationService<C> {
    private final Logger logger = LoggerFactory.getLogger(FileConfigurationService.class);

    private final File configurationFile;
    private final String defaultConfigFileClasspathLocation;
    protected final Class<C> configClass;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public FileConfigurationService(final Class<C> configClass, final File configurationFile, final String defaultConfigFileClasspathLocation) {
        this.logger.debug("Initialising XmlConfigurationService.");
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
        final InputStream defaultConfigIS;
        if (defaultConfigFileClasspathLocation != null) {
            defaultConfigIS = FileConfigurationService.class.getResourceAsStream(defaultConfigFileClasspathLocation);
            if (defaultConfigIS == null) {
                this.logger.warn("Could not locate default configuration file on the classpath: {}", defaultConfigFileClasspathLocation);
            }
        } else {
            defaultConfigIS = null;
        }

        C configuration = null;
        if (defaultConfigIS != null) {
            configuration = decode(defaultConfigIS);
        } else if (configClass != null) {
            try {
                configuration = configClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                this.logger.warn("Could not instantiate an instance of the configuration bean.", e);
            }
        }

        if (!configurationFile.exists()) {
            return configuration;
        }

        final Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            try (final InputStream inputStream = new FileInputStream(configurationFile)) {
                this.logger.debug("Loading configuration from XML file");
                return decode(inputStream, configuration);
            } catch (IOException ex) {
                this.logger.error("Unexpected IOException while loading XML", ex);
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
    public void persistConfiguration(final C configuration) {
        this.checkType(configuration);

        final Lock writeLock = rwLock.writeLock();

        writeLock.lock();
        try (final FileChannel fileChannel = new RandomAccessFile(configurationFile, "rw").getChannel()) {
            final FileLock fileLock = fileChannel.lock();
            try (final OutputStream outputStream = Channels.newOutputStream(fileChannel)) {
                fileChannel.truncate(0);
                this.logger.debug("Persisting configuration to XML file");
                this.encode(configuration, outputStream);
            } finally {
                if (fileLock.isValid()) {
                    fileLock.release();
                }
            }

        } catch (IOException ex) {
            this.logger.error("Unexpected IOException while persisting XML", ex);
            throw new RuntimeException(String.format("Failed to open configuration file for writing: %s", configurationFile.getAbsolutePath()), ex);
        } finally {
            writeLock.unlock();
        }

    }

    protected void checkType(final Object configuration) {
        if (configClass != null && !configClass.isInstance(configuration)) {
            this.logger.error("Configuration class is not the expected type.");
            throw new RuntimeException("Configuration class is not the expected type.");
        }
    }

    abstract C decode(InputStream inputStream);

    abstract C decode(InputStream inputStream, C defaultConfig);

    abstract void encode(C configuration, OutputStream outputStream);

}
