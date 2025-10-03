package com.aldrineeinsteen.fun.options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base template for all plugins with lifecycle management support.
 * Provides singleton pattern implementation and plugin lifecycle hooks.
 */
public abstract class PluginTemplate {
    // Thread-safe singleton instance registry
    private static final Map<Class<? extends PluginTemplate>, PluginTemplate> instances = new ConcurrentHashMap<>();
    
    private final static Logger logger = LoggerFactory.getLogger(PluginTemplate.class);
    
    // Plugin lifecycle state
    protected final AtomicBoolean initialized = new AtomicBoolean(false);
    protected final AtomicBoolean started = new AtomicBoolean(false);
    protected final AtomicBoolean stopped = new AtomicBoolean(false);
    
    // Plugin metadata
    private String pluginName;
    private String pluginVersion;

    public static synchronized <T extends PluginTemplate> T getInstance(Class<T> clazz) {
        if (!instances.containsKey(clazz)) {
            try {
                T instance = clazz.getDeclaredConstructor().newInstance();
                instance.setPluginName(clazz.getSimpleName());
                
                // Initialize the plugin
                if (!instance.initialized.get()) {
                    instance.initialize();
                    instance.initialized.set(true);
                }
                
                instances.put(clazz, instance);
                logger.info("Plugin '{}' instantiated and initialized successfully", clazz.getSimpleName());
            } catch (Exception e) {
                logger.error("Error creating instance for plugin: {}", clazz.getName(), e);
                throw new RuntimeException("Error creating instance for " + clazz.getName(), e);
            }
        }
        return clazz.cast(instances.get(clazz));
    }

    /**
     * Execute a specific action for this plugin.
     * @param actionName the name of the action to execute
     */
    public abstract void executeAction(String actionName);

    /**
     * Initialize the plugin. Called once during plugin instantiation.
     * Override this method to perform plugin-specific initialization.
     */
    protected void initialize() {
        logger.debug("Initializing plugin: {}", getPluginName());
        // Default implementation - subclasses can override
    }

    /**
     * Start the plugin. Can be called multiple times.
     * Override this method to perform plugin-specific startup tasks.
     */
    public void start() {
        if (started.compareAndSet(false, true)) {
            logger.info("Starting plugin: {}", getPluginName());
            onStart();
        } else {
            logger.debug("Plugin '{}' is already started", getPluginName());
        }
    }

    /**
     * Stop the plugin. Can be called multiple times.
     * Override this method to perform plugin-specific cleanup tasks.
     */
    public void stop() {
        if (started.get() && stopped.compareAndSet(false, true)) {
            logger.info("Stopping plugin: {}", getPluginName());
            onStop();
            started.set(false);
        } else {
            logger.debug("Plugin '{}' is already stopped or not started", getPluginName());
        }
    }

    /**
     * Get the current lifecycle state of the plugin.
     */
    public PluginState getState() {
        if (!initialized.get()) {
            return PluginState.UNINITIALIZED;
        } else if (stopped.get()) {
            return PluginState.STOPPED;
        } else if (started.get()) {
            return PluginState.STARTED;
        } else {
            return PluginState.INITIALIZED;
        }
    }

    /**
     * Check if the plugin is in a valid state to execute actions.
     */
    public boolean isReady() {
        return initialized.get() && !stopped.get();
    }

    /**
     * Validate plugin configuration and state.
     * Override this method to add plugin-specific validation.
     * @return true if plugin is valid, false otherwise
     */
    public boolean validate() {
        if (!initialized.get()) {
            logger.error("Plugin '{}' is not initialized", getPluginName());
            return false;
        }
        return true;
    }

    // Lifecycle hooks for subclasses to override
    protected void onStart() {
        // Default implementation - subclasses can override
    }

    protected void onStop() {
        // Default implementation - subclasses can override
    }

    // Getters and setters
    public String getPluginName() {
        return pluginName != null ? pluginName : this.getClass().getSimpleName();
    }

    public String getPluginVersion() {
        return pluginVersion != null ? pluginVersion : "1.0.0";
    }

    protected void setPluginVersion(String version) {
        this.pluginVersion = version;
    }

    protected void setPluginName(String name) {
        this.pluginName = name;
    }

    protected PluginTemplate() {
        // Protected constructor to enforce singleton pattern
    }

    /**
     * Plugin lifecycle states
     */
    public enum PluginState {
        UNINITIALIZED,
        INITIALIZED,
        STARTED,
        STOPPED
    }
}
