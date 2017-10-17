package com.alltheducks.configutils;

import com.alltheducks.configutils.service.CachingConfigurationService;
import com.alltheducks.configutils.service.JsonConfigurationService;
import com.alltheducks.configutils.service.ReloadableConfigurationService;
import com.alltheducks.configutils.service.XmlConfigurationService;

import java.io.File;

public class ConfigurationServiceBuilder<T> {

    private EncodingType encodingType;
    private File configFile;
    private Class<T> configClass;
    private String defaultConfigFileClasspathLocation;

    public ConfigurationServiceBuilder<T> withEncodingType(final EncodingType encodingType) {
        this.encodingType = encodingType;
        return this;
    }

    public ConfigurationServiceBuilder<T> withConfigFile(final File configFile) {
        this.configFile = configFile;
        return this;
    }

    public ConfigurationServiceBuilder<T> withConfigClass(final Class<T> configClass) {
        this.configClass = configClass;
        return this;
    }

    public ConfigurationServiceBuilder<T> withDefaultConfig(final String defaultConfigFileClasspathLocation) {
        this.defaultConfigFileClasspathLocation = defaultConfigFileClasspathLocation;
        return this;
    }

    public ReloadableConfigurationService<T> build() {
        if (configFile == null) {
            throw new RuntimeException("Configuration file not specified");
        }
        if (configClass == null) {
            throw new RuntimeException("Configuration class not specified");
        }
        if (encodingType == EncodingType.XML) {
            final XmlConfigurationService<T> xmlConfigurationService = new XmlConfigurationService<>(configClass, configFile, defaultConfigFileClasspathLocation);
            return new CachingConfigurationService<>(xmlConfigurationService);
        }
        if (encodingType == EncodingType.JSON) {
            final JsonConfigurationService<T> jsonConfigurationService = new JsonConfigurationService<T>(configClass, configFile, defaultConfigFileClasspathLocation);
            return new CachingConfigurationService<>(jsonConfigurationService);
        } else {
            throw new RuntimeException("Not implemented");
        }
    }

    public enum EncodingType {
        JSON, XML
    }


}
