package com.aldrineeinsteen.fun;

import com.aldrineeinsteen.fun.options.helper.PluginConfig;
import com.aldrineeinsteen.fun.options.helper.PluginRepository;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class Main {

    private static final PluginRepository pluginRepository = new PluginRepository();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        pluginRepository.init();  // Initialize plugins from plugin.yaml

        CommandLineParser parser = new DefaultParser();
        try {
            logger.debug("Plugin Repo: {}", pluginRepository.toString());
            CommandLine cmd = parser.parse(pluginRepository.getOptions(), args);
            Map<String, PluginConfig> activePlugins = pluginRepository.getActivePlugins(cmd);
            logger.debug(activePlugins.keySet().toString());
            for (String pluginName : activePlugins.keySet()) {
                Object pluginInstance = pluginRepository.createPluginInstance(pluginName);
                if (pluginInstance instanceof Runnable) {
                    ((Runnable) pluginInstance).run();
                } else {
                    // Handle non-Runnable plugins as needed, or log error
                    logger.warn("Plugin {} does not implement Runnable.", pluginName);
                }
            }

        } catch (ParseException e) {
            logger.error("Failed to parse command line options", e);
        } catch (Exception e) {
            logger.error("Failed to instantiate plugin", e);
        }
    }
}