package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.PluginTemplate;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;

public class PluginRepository {
    private final static Logger logger = LoggerFactory.getLogger(PluginRepository.class);
    private final static Options options = new Options();

    // New addition: Map to hold plugin instances
    private static final Map<String, PluginTemplate> plugins = new HashMap<>();
    private static final Map<String, Runnable> utilities = new HashMap<>();
    private static final Set<String> loadedPlugins = new HashSet<>();

    public static void addLoadedPlugin(String pluginName) {
        loadedPlugins.add(pluginName);
    }

    public static Set<String> getLoadedPlugins() {
        return loadedPlugins;
    }

    // New addition: Methods for plugin registry
    public static void registerPlugin(String name, PluginTemplate pluginInstance) {
        plugins.put(name, pluginInstance);
    }

    public static void registerUtility(String name, Runnable instance) {
        utilities.put(name, instance);
    }

    public static PluginTemplate getPlugin(String name) {
        return plugins.get(name);
    }

    public static Runnable getUtility(String name) {
        return utilities.get(name);
    }

    // A class to hold shortcut and plugin information
    public static class ShortcutAction {
        private final String action;
        private final String plugin;

        @Override
        public String toString() {
            return "ShortcutAction{" +
                    "action='" + action + '\'' +
                    ", plugin='" + plugin + '\'' +
                    '}';
        }

        public ShortcutAction(String action, String plugin) {
            this.action = action;
            this.plugin = plugin;


        }

        // Getters
        public String getAction() {
            return action;
        }

        public String getPlugin() {
            return plugin;
        }
    }

    // Updated Map to hold both action and plugin info
    private static final Map<String, ShortcutAction> shortcutActions = new HashMap<>();

    public static Options getOptions() {
        return options;
    }

    // Getter for shortcutActions
    public static Map<String, ShortcutAction> getShortcutActions() {
        return shortcutActions;
    }

    public void init() {
        logger.info("Initializing dynamic plugin discovery system...");
        try {
            List<URL> urls = new ArrayList<>();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("plugin.yaml");

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                urls.add(url);
            }

            logger.info("Discovered {} plugin configuration files", urls.size());
            
            for (URL url : urls) {
                logger.info("Processing plugin configuration: {}", url.getPath());
                parsePluginYaml(url);
            }
            
            // Summary logging
            logger.info("Plugin discovery complete:");
            logger.info("  - Total plugins loaded: {}", getLoadedPlugins().size());
            logger.info("  - Plugin instances registered: {}", plugins.size());
            logger.info("  - Utility instances registered: {}", utilities.size());
            logger.info("  - CLI options registered: {}", options.getOptions().size());
            logger.info("  - Global shortcuts registered: {}", shortcutActions.size());
            
            if (!getLoadedPlugins().isEmpty()) {
                logger.info("Loaded plugins: {}", String.join(", ", getLoadedPlugins()));
            }
            
            if (!shortcutActions.isEmpty()) {
                logger.info("Available shortcuts:");
                shortcutActions.forEach((key, action) -> 
                    logger.info("  {} -> {}:{}", key, action.getPlugin(), action.getAction()));
            }
            
        } catch (IOException e) {
            logger.error("Exception when loading plugin configurations: ", e);
        }

        logger.debug("Complete shortcut mappings: {}", getShortcutActions());
    }

    private void parsePluginYaml(URL url) {
        try (InputStream is = url.openConnection().getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(is);

            // Parse and instantiate the plugin class
            String pluginClassName = (String) yamlData.get("pluginClass");
            if (pluginClassName != null) {
                instantiateAndRegisterPlugin(pluginClassName);
                addLoadedPlugin(pluginClassName);
            }

            parseOptions(yamlData);
            parseShortcuts(pluginClassName, yamlData);
        } catch (IOException e) {
            logger.error("Error parsing plugin.yaml: ", e);
        }
    }

    private void instantiateAndRegisterPlugin(String className) {
        if (className == null || className.trim().isEmpty()) {
            logger.error("Plugin class name is null or empty");
            return;
        }

        logger.debug("Attempting to instantiate plugin: {}", className);
        
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            logger.debug("Successfully loaded class: {}", className);

            if (PluginTemplate.class.isAssignableFrom(clazz)) {
                // Cast to PluginTemplate class and get the instance
                logger.debug("Instantiating PluginTemplate: {}", className);
                PluginTemplate pluginInstance = PluginTemplate.getInstance(clazz.asSubclass(PluginTemplate.class));
                registerPlugin(className, pluginInstance);
                logger.info("Successfully registered plugin: {}", className);
            } else if (Runnable.class.isAssignableFrom(clazz)) {
                // Handle Runnable utilities that are not PluginTemplates
                logger.debug("Instantiating Runnable utility: {}", className);
                Runnable utilityInstance = (Runnable) clazz.getDeclaredConstructor().newInstance();
                registerUtility(className, utilityInstance);
                logger.info("Successfully registered utility: {}", className);
            } else {
                logger.error("Class {} does not implement PluginTemplate or Runnable interface. " +
                    "Available interfaces: {}", className, java.util.Arrays.toString(clazz.getInterfaces()));
            }
        } catch (ClassNotFoundException e) {
            logger.error("Plugin class not found in classpath: {}. Check if the plugin JAR is properly deployed.", className, e);
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
            logger.error("Unexpected error instantiating plugin {}: {} - {}", className, e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void parseOptions(Map<String, Object> yamlData) {
        Object optionsObj = yamlData.get("option");
        if (optionsObj == null) {
            logger.debug("No options defined in plugin configuration");
            return;
        }

        if (!(optionsObj instanceof List)) {
            logger.error("Plugin options must be a list, found: {}", optionsObj.getClass().getSimpleName());
            return;
        }

        List<Map<String, Object>> commandOptions = (List<Map<String, Object>>) optionsObj;
        logger.debug("Processing {} command options", commandOptions.size());
        
        for (Map<String, Object> command : commandOptions) {
            try {
                String shortOpt = (String) command.get("shortOpt");
                Boolean hasArguments = (Boolean) command.get("hasArguments");
                String longOpt = (String) command.get("longOpt");
                String description = (String) command.get("description");

                if (shortOpt == null || longOpt == null) {
                    logger.error("Plugin option missing required shortOpt or longOpt: {}", command);
                    continue;
                }

                Option option = Option.builder(shortOpt)
                    .hasArg(hasArguments != null ? hasArguments : false)
                    .longOpt(longOpt)
                    .desc(description != null ? description : "")
                    .build();
                
                options.addOption(option);
                logger.debug("Added command option: -{}/{} (hasArgs: {})", shortOpt, longOpt, hasArguments);
            } catch (ClassCastException e) {
                logger.error("Invalid option configuration format: {}", command, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseShortcuts(String pluginClassName, Map<String, Object> yamlData) {
        Object shortcutsObj = yamlData.get("shortcuts");
        if (shortcutsObj == null) {
            logger.debug("No shortcuts defined for plugin: {}", pluginClassName);
            return;
        }

        if (!(shortcutsObj instanceof List)) {
            logger.error("Plugin shortcuts must be a list for plugin: {}, found: {}", 
                pluginClassName, shortcutsObj.getClass().getSimpleName());
            return;
        }

        List<Map<String, String>> shortcuts = (List<Map<String, String>>) shortcutsObj;
        logger.debug("Processing {} shortcuts for plugin: {}", shortcuts.size(), pluginClassName);
        
        for (Map<String, String> shortcut : shortcuts) {
            try {
                String keyCombination = shortcut.get("key");
                String action = shortcut.get("action");
                
                if (keyCombination == null || keyCombination.trim().isEmpty()) {
                    logger.error("Shortcut missing key combination for plugin: {}, shortcut: {}", pluginClassName, shortcut);
                    continue;
                }
                
                if (action == null || action.trim().isEmpty()) {
                    logger.error("Shortcut missing action for plugin: {}, key: {}", pluginClassName, keyCombination);
                    continue;
                }
                
                if (shortcutActions.containsKey(keyCombination)) {
                    ShortcutAction existing = shortcutActions.get(keyCombination);
                    logger.warn("Key combination '{}' already registered by plugin '{}', overriding with plugin '{}'", 
                        keyCombination, existing.getPlugin(), pluginClassName);
                }
                
                shortcutActions.put(keyCombination, new ShortcutAction(action, pluginClassName));
                logger.debug("Registered shortcut: {} -> {}:{}", keyCombination, pluginClassName, action);
            } catch (ClassCastException e) {
                logger.error("Invalid shortcut configuration format for plugin: {}, shortcut: {}", pluginClassName, shortcut, e);
            }
        }
    }
}
