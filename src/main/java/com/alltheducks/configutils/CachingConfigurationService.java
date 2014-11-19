package com.alltheducks.configutils;


/**
 * Adds a caching layer on top of an existing ConfigurationService.
 *
 * When loading configuration it defers the call to the ConfigurationService passed into the constructor then internally
 * caches the result. Next time the an attempt to load the configuration is made, it is read directly from memory,
 * instead of deferring to the other ConfigurationService.
 *
 * When persisting configuration, it is deferred to the ConfigurationService passed into the constructor and then the
 * cache is updated.
 *
 * Warning: caching is local to the object; another instance of this class will not have its cache updated when
 * persisting.
 *
 * Created by Shane Argo on 23/05/14.
 */
public class CachingConfigurationService<C> implements ReloadableConfigurationService<C> {

    public C configurationCache = null;
    public ConfigurationService<C> internalConfigService = null;

    /**
     * @param internalConfigurationService The ConfigurationService used to do the actual loading and persisting of
     *                                     configuration.
     */
    public CachingConfigurationService(ConfigurationService internalConfigurationService) {
        internalConfigService = internalConfigurationService;
    }

    /**
     * Defers to the ConfigurationService passed into the constructor to load the configuration and then caches the
     * result prior to returning it.
     * @return The configuration that has been cached.
     */
    @Override
    public C loadConfiguration() {
        if (configurationCache == null) {
            synchronized (this) {
                if (configurationCache == null) {
                    configurationCache = internalConfigService.loadConfiguration();
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
        internalConfigService.persistConfiguration(configuration);
        configurationCache = configuration;
    }


    @Override
    public void reload() {
        C config = internalConfigService.loadConfiguration();
        configurationCache = config;
    }

}