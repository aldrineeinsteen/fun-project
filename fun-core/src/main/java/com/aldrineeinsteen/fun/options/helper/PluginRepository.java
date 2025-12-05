package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.PluginTemplate;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Central repository for plugin management and discovery.
 * Coordinates plugin loading, registration, and access.
 */
public class PluginRepository {
    private static final Logger logger = LoggerFactory.getLogger(PluginRepository.class);
    private static final Options options = new Options();
    private static final Set<String> loadedPlugins = new HashSet<>();
    private static final Map<String, PluginMetadata.ShortcutAction> shortcutActions = new HashMap<>();
    private static final Map<String, PluginMetadata> pluginInfos = new HashMap<>();
    
    private final PluginLoader pluginLoader = new PluginLoader();
    private final PluginYamlParser yamlParser;

    public PluginRepository() {
        this.yamlParser = new PluginYamlParser(pluginLoader, options, shortcutActions, pluginInfos);
    }

    public static void addLoadedPlugin(String pluginName) {
        loadedPlugins.add(pluginName);
    }

    public static Set<String> getLoadedPlugins() {
        return loadedPlugins;
    }

    public static void registerPlugin(String name, PluginTemplate pluginInstance) {
        getInstance().pluginLoader.registerPlugin(name, pluginInstance);
    }

    public static void registerUtility(String name, Runnable instance) {
        getInstance().pluginLoader.registerUtility(name, instance);
    }

    public static PluginTemplate getPlugin(String name) {
        // This needs to remain static for backward compatibility
        // Access through a singleton or static instance
        return getInstance().pluginLoader.getPlugin(name);
    }

    public static Runnable getUtility(String name) {
        return getInstance().pluginLoader.getUtility(name);
    }

    public static Options getOptions() {
        return options;
    }
    
    public static Map<String, PluginMetadata> getPluginInfos() {
        return pluginInfos;
    }
    
    public static String generateStructuredHelp() {
        return HelpTextGenerator.generateStructuredHelp(pluginInfos);
    }

    public static Map<String, PluginMetadata.ShortcutAction> getShortcutActions() {
        return shortcutActions;
    }

    // Singleton instance for backward compatibility
    private static PluginRepository instance;
    
    private static synchronized PluginRepository getInstance() {
        if (instance == null) {
            instance = new PluginRepository();
        }
        return instance;
    }

    /**
     * Initialize the plugin discovery system
     */
    public void init() {
        // Set this instance as the singleton
        instance = this;
        
        // Add standard help option
        options.addOption("h", "help", false, "Show this help message");
        
        // Add dashboard option
        options.addOption("dash", "dashboard", false, "Enable TUI dashboard mode");
        
        logger.info("Initializing dynamic plugin discovery system...");
        try {
            List<URL> urls = discoverPluginConfigurations();
            logger.info("Discovered {} plugin configuration files", urls.size());
            
            for (URL url : urls) {
                logger.info("Processing plugin configuration: {}", url.getPath());
                yamlParser.parsePluginYaml(url);
            }
            
            logDiscoverySummary();
            
        } catch (IOException e) {
            logger.error("Exception when loading plugin configurations: ", e);
        }

        logger.debug("Complete shortcut mappings: {}", getShortcutActions());
    }

    /**
     * Discover all plugin.yaml files in the classpath
     */
    private List<URL> discoverPluginConfigurations() throws IOException {
        List<URL> urls = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("plugin.yaml");

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            urls.add(url);
        }
        
        return urls;
    }

    /**
     * Log summary of plugin discovery results
     */
    private void logDiscoverySummary() {
        logger.info("Plugin discovery complete:");
        logger.info("  - Total plugins loaded: {}", getLoadedPlugins().size());
        logger.info("  - Plugin instances registered: {}", pluginLoader.getPlugins().size());
        logger.info("  - Utility instances registered: {}", pluginLoader.getUtilities().size());
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
    }

    // Backward compatibility aliases
    @Deprecated
    public static class PluginInfo extends PluginMetadata {
        public PluginInfo(String name, String className, String description) {
            super(name, className, description);
        }
    }

    @Deprecated
    public static class ShortcutAction extends PluginMetadata.ShortcutAction {
        public ShortcutAction(String action, String plugin, String keyCombination) {
            super(action, plugin, keyCombination);
        }
    }
}

// Made with Bob
