package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.PluginTemplate;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
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
    private static final Map<String, Object> plugins = new HashMap<>();

    // New addition: Methods for plugin registry
    public static void registerPlugin(String name, Object pluginInstance) {
        plugins.put(name, pluginInstance);
    }

    public static Object getPlugin(String name) {
        return plugins.get(name);
    }

    // A class to hold shortcut and plugin information
    public static class ShortcutAction {
        private String action;
        private String plugin;

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
        try {
            List<URL> urls = new ArrayList<>();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("plugin.yaml");

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                urls.add(url);
            }

            for (URL url : urls) {
                logger.debug("Found plugin.yaml in: {}", url.getPath());
                parsePluginYaml(url);
            }
        } catch (IOException e) {
            logger.error("Exception when loading the yaml: ", e);
        }

        logger.debug("Shortcuts: {}", getShortcutActions());
    }

    private void parsePluginYaml(URL url) {
        try (InputStream is = url.openConnection().getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(is);

            // Parse and instantiate the plugin class
            String pluginClassName = (String) yamlData.get("pluginClass");
            if (pluginClassName != null) {
                instantiateAndRegisterPlugin(pluginClassName);
            }

            parseOptions(yamlData);
            parseShortcuts(yamlData);
        } catch (IOException e) {
            logger.error("Error parsing plugin.yaml: ", e);
        }
    }

    private void instantiateAndRegisterPlugin(String className) {
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            Constructor<?> constructor = clazz.getConstructor();
            Object pluginInstance = constructor.newInstance();

            if (pluginInstance instanceof PluginTemplate) {
                registerPlugin(className, (PluginTemplate) pluginInstance);
                logger.debug("Registered plugin: {}", className);
            } else {
                logger.error("Class {} does not implement the Plugin interface", className);
            }
        } catch (Exception e) {
            logger.error("Error instantiating plugin: {}", className, e);
        }
    }

    private void parseOptions(Map<String, Object> yamlData) {
        ArrayList<Map<String, Object>> commandOptions = (ArrayList<Map<String, Object>>) yamlData.get("options");
        if (commandOptions != null) {
            for (Map<String, Object> command : commandOptions) {
                Option option = Option.builder((String) command.get("shortOpt")).hasArg((boolean) command.get("hasArguments")).longOpt((String) command.get("longOpt")).desc((String) command.get("description")).build();
                options.addOption(option);
            }
        }
    }

    private void parseShortcuts(Map<String, Object> yamlData) {
        List<Map<String, String>> shortcuts = (List<Map<String, String>>) yamlData.get("shortcuts");
        if (shortcuts != null) {
            for (Map<String, String> shortcut : shortcuts) {
                String keyCombination = shortcut.get("key");
                String action = shortcut.get("action");
                String plugin = shortcut.get("plugin"); // Assuming 'plugin' is part of your yaml
                shortcutActions.put(keyCombination, new ShortcutAction(action, plugin));
            }
        }
    }

    private static int convertStringToKeyCode(String keyStrokeString) {
        int modifiers = 0;
        int keyCode = 0;
        for (String keyPart : keyStrokeString.split("\\s+\\+\\s+")) {
            switch (keyPart.toUpperCase()) {
                case "CTRL":
                    modifiers |= NativeInputEvent.CTRL_MASK;
                    break;
                case "SHIFT":
                    modifiers |= NativeInputEvent.SHIFT_MASK;
                    break;
                case "ALT":
                    modifiers |= NativeInputEvent.ALT_MASK;
                    break;
                default:
                    keyCode = javaKeyToNativeKey(keyPart);
                    break;
            }
        }
        return keyCode | modifiers;
    }

    private static int javaKeyToNativeKey(String keyPart) {
        // Example mapping
        switch (keyPart.toUpperCase()) {
            case "A":
                return NativeKeyEvent.VC_A;
            case "B":
                return NativeKeyEvent.VC_B;
            case "S":
                return NativeKeyEvent.VC_S;
            // ... other key mappings
            default:
                return NativeKeyEvent.VC_UNDEFINED;
        }
    }
}
