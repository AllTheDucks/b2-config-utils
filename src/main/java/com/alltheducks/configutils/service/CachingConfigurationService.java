package com.alltheducks.configutils.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Adds a caching layer on top of an existing ConfigurationService.</p>
 *
 * <p>When loading configuration it defers the call to the ConfigurationService passed into the constructor then internally
 * caches the result. Next time the an attempt to load the configuration is made, it is read directly from memory,
 * instead of deferring to the other ConfigurationService.</p>
 *
 * <p>When persisting configuration, it is deferred to the ConfigurationService passed into the constructor and then the
 * cache is updated.</p>
 *
 * <p><strong>Warning:</strong> caching is local to the object; another instance of this class will not have its cache updated when
 * persisting.</p>
 * @see com.alltheducks.configutils.monitor.PollingConfigurationMonitor
 * <p>Copyright All the Ducks Pty Ltd. 2014.</p>
 */
public class CachingConfigurationService<C> implements ReloadableConfigurationService<C> {
    final Logger logger = LoggerFactory.getLogger(CachingConfigurationService.class);

    public C configurationCache = null;
    public ConfigurationService<C> internalConfigurationService = null;

    /**
     * @param internalConfigurationService The ConfigurationService used to do the actual loading and persisting of
     *                                     configuration.
     */
    public CachingConfigurationService(ConfigurationService<C> internalConfigurationService) {
        logger.debug("Initialising CachingConfigurationService with internal configuration service of type {}.", internalConfigurationService.getClass().getName());
        this.internalConfigurationService = internalConfigurationService;
    }

    /**
     * Defers to the ConfigurationService passed into the constructor to load the configuration and then caches the
     * result prior to returning it.
     * @return The configuration that has been cached.
     */
    @Override
    public C loadConfiguration() {
        logger.trace("Entering loadConfiguration on CachingConfigurationService");
        if (configurationCache == null) {
            synchronized (this) {
                if (configurationCache == null) {
                    configurationCache = internalConfigurationService.loadConfiguration();
                    logger.debug("Configuration loaded from internal ConfigurationService ({}).", internalConfigurationService.getClass().getName());
                }
            }
        }
        return configurationCache;
    }

    /**
     * Defers persisting to the ConfigurationService passed into the constructor and then updates the cache.
     * @param configuration The configuration to be persisted and cached.
     */
    @Override
    public void persistConfiguration(C configuration) {
        logger.trace("Entering persistConfiguration on CachingConfigurationService");
        internalConfigurationService.persistConfiguration(configuration);
        configurationCache = configuration;
    }


    @Override
    public void reload() {
        logger.trace("Entering reload on CachingConfigurationService");
        C config = internalConfigurationService.loadConfiguration();
        configurationCache = config;
    }

}