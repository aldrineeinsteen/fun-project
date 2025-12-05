package com.aldrineeinsteen.fun.options.helper;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Handles parsing of plugin YAML configuration files.
 */
public class PluginYamlParser {
    private static final Logger logger = LoggerFactory.getLogger(PluginYamlParser.class);
    
    private final PluginLoader pluginLoader;
    private final Options options;
    private final Map<String, PluginMetadata.ShortcutAction> shortcutActions;
    private final Map<String, PluginMetadata> pluginInfos;

    public PluginYamlParser(PluginLoader pluginLoader, Options options, 
                           Map<String, PluginMetadata.ShortcutAction> shortcutActions,
                           Map<String, PluginMetadata> pluginInfos) {
        this.pluginLoader = pluginLoader;
        this.options = options;
        this.shortcutActions = shortcutActions;
        this.pluginInfos = pluginInfos;
    }

    public void parsePluginYaml(URL url) {
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
            PluginMetadata pluginInfo = new PluginMetadata(pluginName, pluginClassName, description);

            // Parse and instantiate the plugin class
            if (pluginClassName != null) {
                pluginLoader.instantiateAndRegisterPlugin(pluginClassName);
                
                // Add to loaded plugins set
                PluginRepository.addLoadedPlugin(pluginClassName);
                
                // Parse dashboard configuration
                parseDashboardConfig(pluginClassName, yamlData);
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

    @SuppressWarnings("unchecked")
    private void parseOptions(Map<String, Object> yamlData, PluginMetadata pluginInfo) {
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
                pluginInfo.addOption(option);
                logger.debug("Added command option: -{}/{} (hasArgs: {})", shortOpt, longOpt, hasArguments);
            } catch (ClassCastException e) {
                logger.error("Invalid option configuration format: {}", command, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseParams(Map<String, Object> yamlData, PluginMetadata pluginInfo) {
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
                pluginInfo.addParam(option);
                logger.debug("Added param option: -{}/{} (hasArgs: {})", shortOpt, longOpt, hasArguments);
            } catch (ClassCastException e) {
                logger.error("Invalid param configuration format: {}", param, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseShortcuts(String pluginClassName, Map<String, Object> yamlData, PluginMetadata pluginInfo) {
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
                    PluginMetadata.ShortcutAction existing = shortcutActions.get(keyCombination);
                    logger.warn("Key combination '{}' already registered by plugin '{}', overriding with plugin '{}'", 
                        keyCombination, existing.getPlugin(), pluginClassName);
                }
                
                PluginMetadata.ShortcutAction shortcutAction = new PluginMetadata.ShortcutAction(action, pluginClassName, keyCombination);
                shortcutActions.put(keyCombination, shortcutAction);
                pluginInfo.addShortcut(shortcutAction);
                logger.debug("Registered shortcut: {} -> {}:{}", keyCombination, pluginClassName, action);
            } catch (ClassCastException e) {
                logger.error("Invalid shortcut configuration format for plugin: {}, shortcut: {}", pluginClassName, shortcut, e);
            }
        }
    }
    
    /**
     * Parse dashboard configuration from plugin.yaml
     */
    @SuppressWarnings("unchecked")
    private void parseDashboardConfig(String pluginClassName, Map<String, Object> yamlData) {
        Object dashboardObj = yamlData.get("dashboard");
        if (dashboardObj == null) {
            logger.debug("No dashboard configuration for plugin: {}", pluginClassName);
            return;
        }
        
        if (!(dashboardObj instanceof Map)) {
            logger.error("Dashboard configuration must be a map for plugin: {}", pluginClassName);
            return;
        }
        
        Map<String, Object> dashboardConfig = (Map<String, Object>) dashboardObj;
        
        // Try to get utility instance first
        Runnable utility = pluginLoader.getUtility(pluginClassName);
        com.aldrineeinsteen.fun.options.PluginTemplate plugin = pluginLoader.getPlugin(pluginClassName);
        
        com.aldrineeinsteen.fun.options.DashboardRenderer renderer = null;
        
        // Check utility first
        if (utility instanceof com.aldrineeinsteen.fun.options.UtilityTemplate) {
            renderer = (com.aldrineeinsteen.fun.options.UtilityTemplate) utility;
        }
        // Then check plugin
        else if (plugin instanceof com.aldrineeinsteen.fun.options.PluginTemplate) {
            renderer = (com.aldrineeinsteen.fun.options.PluginTemplate) plugin;
        }
        
        if (renderer == null) {
            logger.debug("No dashboard-capable instance found for plugin: {}", pluginClassName);
            return;
        }
        
        applyDashboardConfiguration(renderer, dashboardConfig, pluginClassName);
    }

    private void applyDashboardConfiguration(com.aldrineeinsteen.fun.options.DashboardRenderer renderer, 
                                           Map<String, Object> dashboardConfig, String pluginClassName) {
        // Apply dashboard configuration
        Boolean enabled = (Boolean) dashboardConfig.get("enabled");
        if (enabled != null) {
            if (renderer instanceof com.aldrineeinsteen.fun.options.UtilityTemplate) {
                ((com.aldrineeinsteen.fun.options.UtilityTemplate) renderer).setDashboardEnabled(enabled);
            } else if (renderer instanceof com.aldrineeinsteen.fun.options.PluginTemplate) {
                ((com.aldrineeinsteen.fun.options.PluginTemplate) renderer).setDashboardEnabled(enabled);
            }
            logger.debug("Set dashboard enabled={} for plugin: {}", enabled, pluginClassName);
        }
        
        Object positionObj = dashboardConfig.get("position");
        if (positionObj != null) {
            int position = positionObj instanceof Integer ? (Integer) positionObj : 100;
            if (renderer instanceof com.aldrineeinsteen.fun.options.UtilityTemplate) {
                ((com.aldrineeinsteen.fun.options.UtilityTemplate) renderer).setDashboardPosition(position);
            } else if (renderer instanceof com.aldrineeinsteen.fun.options.PluginTemplate) {
                ((com.aldrineeinsteen.fun.options.PluginTemplate) renderer).setDashboardPosition(position);
            }
            logger.debug("Set dashboard position={} for plugin: {}", position, pluginClassName);
        }
        
        // Parse column configuration
        Object columnObj = dashboardConfig.get("column");
        if (columnObj != null) {
            int column = columnObj instanceof Integer ? (Integer) columnObj : 1;
            if (renderer instanceof com.aldrineeinsteen.fun.options.UtilityTemplate) {
                ((com.aldrineeinsteen.fun.options.UtilityTemplate) renderer).setDashboardColumn(column);
            } else if (renderer instanceof com.aldrineeinsteen.fun.options.PluginTemplate) {
                ((com.aldrineeinsteen.fun.options.PluginTemplate) renderer).setDashboardColumn(column);
            }
            logger.debug("Set dashboard column={} for plugin: {}", column, pluginClassName);
        }
        
        // Parse row configuration
        Object rowObj = dashboardConfig.get("row");
        if (rowObj != null) {
            int row = rowObj instanceof Integer ? (Integer) rowObj : 1;
            if (renderer instanceof com.aldrineeinsteen.fun.options.UtilityTemplate) {
                ((com.aldrineeinsteen.fun.options.UtilityTemplate) renderer).setDashboardRow(row);
            } else if (renderer instanceof com.aldrineeinsteen.fun.options.PluginTemplate) {
                ((com.aldrineeinsteen.fun.options.PluginTemplate) renderer).setDashboardRow(row);
            }
            logger.debug("Set dashboard row={} for plugin: {}", row, pluginClassName);
        }
    }
}

// Made with Bob
