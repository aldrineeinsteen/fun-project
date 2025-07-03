package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.Main;
import org.apache.commons.cli.*;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginRepository {

    private final Map<String, PluginConfig> plugins = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(PluginRepository.class);

    public void init() {
        Yaml yaml = new Yaml();
        List<Map<String, Object>> allPlugins = new ArrayList<>(); // Combined plugins list

        try {
            // Use the class loader to find all resources named "plugin.yaml"
            Enumeration<URL> resources = getClass().getClassLoader().getResources("plugin.yaml");

            // Iterate over each found resource
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (InputStream in = resource.openStream()) {
                    // Load the YAML data from the current resource
                    Map<String, List<Map<String, Object>>> yamlData = yaml.load(in);
                    List<Map<String, Object>> pluginList = yamlData.get("plugins");
                    if (pluginList != null) {
                        allPlugins.addAll(pluginList); // Add plugins to combined list
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Process combined plugins
            for (Map<String, Object> pluginData : allPlugins) {
                PluginConfig pluginConfig = new PluginConfig(pluginData);
                plugins.put(pluginConfig.getName(), pluginConfig);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Options getOptions() {
        Options options = new Options();
        for (PluginConfig pluginConfig : plugins.values()) {
            for (PluginOption option : pluginConfig.getAllOptions()) {
                options.addOption(option.toOption());
            }
        }
        return options;
    }

    public Map<String, PluginConfig> getActivePlugins(CommandLine cmd) {
        Map<String, PluginConfig> activePlugins = new HashMap<>();
        for (PluginConfig pluginConfig : plugins.values()) {
            // Check if any enabler option is present to activate the plugin
            boolean isActive = pluginConfig.getEnablerOptions().stream()
                    .anyMatch(option -> cmd.hasOption(option.getShortOpt()) || cmd.hasOption(option.getLongOpt()));

            if (isActive) {
                activePlugins.put(pluginConfig.getName(), pluginConfig);
            }
        }
        return activePlugins;
    }

    public Object createPluginInstance(String pluginName) throws Exception {
        logger.info("Loading plugin: {}", pluginName);
        PluginConfig pluginConfig = plugins.get(pluginName);
        if (pluginConfig != null) {
            String pluginConfigClassName = pluginConfig.getClassName();
            logger.info("Trying to load {} based on plugin: {}", pluginConfigClassName, pluginName);
            Class<?> clazz = Class.forName(pluginConfigClassName);
            logger.info("Loaded {} successfully.", pluginConfigClassName);
            return clazz.getDeclaredConstructor().newInstance();
        }
        return null;
    }

    @Override
    public String toString() {
        return plugins.toString();
    }
}