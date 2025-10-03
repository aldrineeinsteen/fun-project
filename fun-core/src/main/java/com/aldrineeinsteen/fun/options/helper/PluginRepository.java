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

    // A class to hold plugin metadata for better help documentation
    public static class PluginInfo {
        private final String name;
        private final String className;
        private final String description;
        private final List<Option> options;
        private final List<Option> params;
        private final List<ShortcutAction> shortcuts;

        public PluginInfo(String name, String className, String description) {
            this.name = name;
            this.className = className;
            this.description = description;
            this.options = new ArrayList<>();
            this.params = new ArrayList<>();
            this.shortcuts = new ArrayList<>();
        }

        // Getters
        public String getName() { return name; }
        public String getClassName() { return className; }
        public String getDescription() { return description; }
        public List<Option> getOptions() { return options; }
        public List<Option> getParams() { return params; }
        public List<ShortcutAction> getShortcuts() { return shortcuts; }

        // Methods to add options, params, and shortcuts
        public void addOption(Option option) { this.options.add(option); }
        public void addParam(Option param) { this.params.add(param); }
        public void addShortcut(ShortcutAction shortcut) { this.shortcuts.add(shortcut); }
    }

    // A class to hold shortcut and plugin information
    public static class ShortcutAction {
        private final String action;
        private final String plugin;
        private final String keyCombination;

        @Override
        public String toString() {
            return "ShortcutAction{" +
                    "action='" + action + '\'' +
                    ", plugin='" + plugin + '\'' +
                    ", keyCombination='" + keyCombination + '\'' +
                    '}';
        }

        public ShortcutAction(String action, String plugin, String keyCombination) {
            this.action = action;
            this.plugin = plugin;
            this.keyCombination = keyCombination;
        }

        // Getters
        public String getAction() {
            return action;
        }

        public String getPlugin() {
            return plugin;
        }

        public String getKeyCombination() {
            return keyCombination;
        }
    }

    // Updated Map to hold both action and plugin info
    private static final Map<String, ShortcutAction> shortcutActions = new HashMap<>();
    
    // Store plugin metadata for enhanced help documentation
    private static final Map<String, PluginInfo> pluginInfos = new HashMap<>();

    public static Options getOptions() {
        return options;
    }
    
    public static Map<String, PluginInfo> getPluginInfos() {
        return pluginInfos;
    }
    
    /**
     * Generate a structured help message showing plugins and their individual documentation
     */
    public static String generateStructuredHelp() {
        StringBuilder help = new StringBuilder();
        help.append("Fun Project - Java Console Application with Multi-Monitor Support\n");
        help.append("========================================================================\n\n");
        
        // Global options first
        help.append("GLOBAL OPTIONS:\n");
        help.append("  -h, --help                Show this help message\n\n");
        
        // Plugin-specific sections
        if (pluginInfos.isEmpty()) {
            help.append("No plugins loaded.\n");
        } else {
            help.append("AVAILABLE PLUGINS:\n");
            help.append("------------------\n\n");
            
            for (PluginInfo pluginInfo : pluginInfos.values()) {
                help.append(String.format("Plugin: %s\n", pluginInfo.getName()));
                help.append(String.format("Description: %s\n", pluginInfo.getDescription()));
                help.append(String.format("Class: %s\n", pluginInfo.getClassName()));
                
                // Main options
                if (!pluginInfo.getOptions().isEmpty()) {
                    help.append("  Main Options:\n");
                    for (Option option : pluginInfo.getOptions()) {
                        help.append(String.format("    -%s, --%s%s%s\n", 
                            option.getOpt(), 
                            option.getLongOpt(),
                            option.hasArg() ? " <arg>" : "",
                            option.getDescription() != null && !option.getDescription().isEmpty() 
                                ? "  " + option.getDescription() : ""));
                    }
                }
                
                // Parameters
                if (!pluginInfo.getParams().isEmpty()) {
                    help.append("  Parameters:\n");
                    for (Option param : pluginInfo.getParams()) {
                        help.append(String.format("    -%s, --%s%s%s\n", 
                            param.getOpt(), 
                            param.getLongOpt(),
                            param.hasArg() ? " <arg>" : "",
                            param.getDescription() != null && !param.getDescription().isEmpty() 
                                ? "  " + param.getDescription() : ""));
                    }
                }
                
                // Shortcuts
                if (!pluginInfo.getShortcuts().isEmpty()) {
                    help.append("  Global Shortcuts:\n");
                    for (ShortcutAction shortcut : pluginInfo.getShortcuts()) {
                        help.append(String.format("    %s  Trigger: %s\n", 
                            shortcut.getKeyCombination(), 
                            shortcut.getAction()));
                    }
                }
                
                help.append("\n");
            }
        }
        
        help.append("EXAMPLES:\n");
        help.append("---------\n");
        help.append("  java -cp \"target/lib/*:target/plugins/*:target/fun-project.jar\" com.aldrineeinsteen.fun.Main -k\n");
        help.append("    Start keep-alive timer with multi-monitor support\n\n");
        help.append("  java -cp \"target/lib/*:target/plugins/*:target/fun-project.jar\" com.aldrineeinsteen.fun.Main -k -e 17:30\n");
        help.append("    Start keep-alive timer until 5:30 PM\n\n");
        help.append("  java -cp \"target/lib/*:target/plugins/*:target/fun-project.jar\" com.aldrineeinsteen.fun.Main -k --multi-monitor --cross-monitors\n");
        help.append("    Start with multi-monitor mode allowing cross-monitor movement\n\n");
        
        return help.toString();
    }

    // Getter for shortcutActions
    public static Map<String, ShortcutAction> getShortcutActions() {
        return shortcutActions;
    }

    public void init() {
        // Add standard help option
        options.addOption("h", "help", false, "Show this help message");
        
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

            // Extract plugin metadata
            String pluginName = (String) yamlData.get("name");
            String pluginClassName = (String) yamlData.get("pluginClass");
            String description = (String) yamlData.get("description");
            
            if (pluginName == null) pluginName = pluginClassName;
            if (description == null) description = "No description available";

            // Create PluginInfo to collect metadata
            PluginInfo pluginInfo = new PluginInfo(pluginName, pluginClassName, description);

            // Parse and instantiate the plugin class
            if (pluginClassName != null) {
                instantiateAndRegisterPlugin(pluginClassName);
                addLoadedPlugin(pluginClassName);
            }

            parseOptions(yamlData, pluginInfo);
            parseParams(yamlData, pluginInfo);
            parseShortcuts(pluginClassName, yamlData, pluginInfo);
            
            // Store the plugin information
            if (pluginName != null) {
                pluginInfos.put(pluginName, pluginInfo);
            }
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
    private void parseOptions(Map<String, Object> yamlData, PluginInfo pluginInfo) {
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
                pluginInfo.addOption(option);  // Add to plugin info for structured help
                logger.debug("Added command option: -{}/{} (hasArgs: {})", shortOpt, longOpt, hasArguments);
            } catch (ClassCastException e) {
                logger.error("Invalid option configuration format: {}", command, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseParams(Map<String, Object> yamlData, PluginInfo pluginInfo) {
        Object paramsObj = yamlData.get("params");
        if (paramsObj == null) {
            logger.debug("No params defined in plugin configuration");
            return;
        }

        if (!(paramsObj instanceof List)) {
            logger.error("Plugin params must be a list, found: {}", paramsObj.getClass().getSimpleName());
            return;
        }

        List<Map<String, Object>> paramOptions = (List<Map<String, Object>>) paramsObj;
        logger.debug("Processing {} param options", paramOptions.size());
        
        for (Map<String, Object> param : paramOptions) {
            try {
                String shortOpt = (String) param.get("shortOpt");
                Boolean hasArguments = (Boolean) param.get("hasArguments");
                String longOpt = (String) param.get("longOpt");
                String description = (String) param.get("description");

                if (shortOpt == null || longOpt == null) {
                    logger.error("Plugin param missing required shortOpt or longOpt: {}", param);
                    continue;
                }

                Option option = Option.builder(shortOpt)
                    .hasArg(hasArguments != null ? hasArguments : false)
                    .longOpt(longOpt)
                    .desc(description != null ? description : "")
                    .build();
                
                options.addOption(option);
                pluginInfo.addParam(option);  // Add to plugin info for structured help
                logger.debug("Added param option: -{}/{} (hasArgs: {})", shortOpt, longOpt, hasArguments);
            } catch (ClassCastException e) {
                logger.error("Invalid param configuration format: {}", param, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseShortcuts(String pluginClassName, Map<String, Object> yamlData, PluginInfo pluginInfo) {
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
                
                ShortcutAction shortcutAction = new ShortcutAction(action, pluginClassName, keyCombination);
                shortcutActions.put(keyCombination, shortcutAction);
                pluginInfo.addShortcut(shortcutAction);  // Add to plugin info for structured help
                logger.debug("Registered shortcut: {} -> {}:{}", keyCombination, pluginClassName, action);
            } catch (ClassCastException e) {
                logger.error("Invalid shortcut configuration format for plugin: {}, shortcut: {}", pluginClassName, shortcut, e);
            }
        }
    }
}
