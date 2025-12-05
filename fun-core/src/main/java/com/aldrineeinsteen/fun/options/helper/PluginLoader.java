package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.PluginTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles plugin instantiation and registration.
 */
public class PluginLoader {
    private static final Logger logger = LoggerFactory.getLogger(PluginLoader.class);
    
    private final Map<String, PluginTemplate> plugins = new HashMap<>();
    private final Map<String, Runnable> utilities = new HashMap<>();

    /**
     * Instantiate and register a plugin by class name
     */
    public void instantiateAndRegisterPlugin(String className) {
        if (className == null || className.trim().isEmpty()) {
            logger.error("Plugin class name is null or empty");
            return;
        }

        logger.debug("Attempting to instantiate plugin: {}", className);
        
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            logger.debug("Successfully loaded class: {}", className);

            if (PluginTemplate.class.isAssignableFrom(clazz)) {
                instantiatePluginTemplate(className, clazz);
            } else if (Runnable.class.isAssignableFrom(clazz)) {
                instantiateRunnableUtility(className, clazz);
            } else {
                logger.error("Class {} does not implement PluginTemplate or Runnable interface. " +
                    "Available interfaces: {}", className, java.util.Arrays.toString(clazz.getInterfaces()));
            }
        } catch (ClassNotFoundException e) {
            logger.error("Plugin class not found in classpath: {}. Check if the plugin JAR is properly deployed.", className, e);
        } catch (Exception e) {
            logger.error("Unexpected error instantiating plugin {}: {} - {}", className, e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void instantiatePluginTemplate(String className, Class<?> clazz) {
        try {
            logger.debug("Instantiating PluginTemplate: {}", className);
            PluginTemplate pluginInstance = PluginTemplate.getInstance(clazz.asSubclass(PluginTemplate.class));
            registerPlugin(className, pluginInstance);
            logger.info("Successfully registered plugin: {}", className);
        } catch (Exception e) {
            logger.error("Error instantiating PluginTemplate {}: {}", className, e.getMessage(), e);
        }
    }

    private void instantiateRunnableUtility(String className, Class<?> clazz) {
        try {
            logger.debug("Instantiating Runnable utility: {}", className);
            Runnable utilityInstance = (Runnable) clazz.getDeclaredConstructor().newInstance();
            registerUtility(className, utilityInstance);
            logger.info("Successfully registered utility: {}", className);
        } catch (NoSuchMethodException e) {
            logger.error("Plugin class {} does not have a required no-argument constructor", className, e);
        } catch (IllegalAccessException e) {
            logger.error("Cannot access constructor for plugin class {}. Check if class and constructor are public", className, e);
        } catch (InstantiationException e) {
            logger.error("Cannot instantiate plugin class {}. Check if class is concrete (not abstract or interface)", className, e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            logger.error("Error in plugin constructor for class {}: {}", className, e.getTargetException().getMessage(), e);
        } catch (SecurityException e) {
            logger.error("Security manager prevents plugin instantiation for class {}", className, e);
        } catch (Exception e) {
            logger.error("Unexpected error instantiating utility {}: {} - {}", className, e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    public void registerPlugin(String name, PluginTemplate pluginInstance) {
        plugins.put(name, pluginInstance);
    }

    public void registerUtility(String name, Runnable instance) {
        utilities.put(name, instance);
    }

    public PluginTemplate getPlugin(String name) {
        return plugins.get(name);
    }

    public Runnable getUtility(String name) {
        return utilities.get(name);
    }

    public Map<String, PluginTemplate> getPlugins() {
        return new HashMap<>(plugins);
    }

    public Map<String, Runnable> getUtilities() {
        return new HashMap<>(utilities);
    }
}

// Made with Bob
