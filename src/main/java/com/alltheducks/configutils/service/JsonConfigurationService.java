package com.alltheducks.configutils.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonConfigurationService<C> extends FileConfigurationService<C> {

    private final ObjectReader objectReader;
    private final ObjectWriter objectWriter;

    public JsonConfigurationService(Class<C> configClass, File configurationFile) {
        this(configClass, configurationFile, null, null);
    }

    public JsonConfigurationService(Class<C> configClass, File configurationFile, ObjectReader objectReader, ObjectWriter objectWriter) {
        this(configClass, configurationFile, null, objectReader, objectWriter);
    }

    public JsonConfigurationService(Class<C> configClass, File configurationFile, String defaultConfigFileClasspathLocation) {
        this(configClass, configurationFile, defaultConfigFileClasspathLocation, null, null);
    }

    public JsonConfigurationService(Class<C> configClass, File configurationFile, String defaultConfigFileClasspathLocation, ObjectReader objectReader, ObjectWriter objectWriter) {
        super(configClass, configurationFile, defaultConfigFileClasspathLocation);

        ObjectMapper objectMapper = null;
        if (objectReader == null) {
            objectMapper = newObjectMapper();
            this.objectReader = objectMapper.reader();
        } else {
            this.objectReader = objectReader;
        }

        if (objectWriter == null) {
            if (objectMapper == null) {
                objectMapper = newObjectMapper();
            }
            this.objectWriter = objectMapper.writer();
        } else {
            this.objectWriter = objectWriter;
        }
    }

    private ObjectMapper newObjectMapper() {
        return new ObjectMapper();
    }

    @Override
    @SuppressWarnings("unchecked")
    C decode(InputStream inputStream) {
        try {
            return (C) objectReader.forType(configClass).readValue(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    C decode(InputStream inputStream, C defaultConfig) {
        try {
            return objectReader.withValueToUpdate(defaultConfig).readValue(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void encode(C configuration, OutputStream outputStream) {
        try {
            objectWriter.writeValue(outputStream, configuration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
