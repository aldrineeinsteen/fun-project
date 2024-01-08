package com.aldrineeinsteen.fun.options.helper;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class PluginRepository {
    private final static Logger logger = LoggerFactory.getLogger(PluginRepository.class);
    private final static Options options = new Options();

    private static final Map<String, String> shortcutActions = new HashMap<>();

    public static Options getOptions() {
        return options;
    }

    // Getter for shortcutActions
    public static Map<String, String> getShortcutActions() {
        return shortcutActions;
    }

    public void init() {
        try {

            List<URL> urls = new ArrayList<>();

            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Enumeration<URL> resources = classLoader.getResources("plugin.yaml");

                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    urls.add(url);
                }
            } catch (IOException e) {
                logger.error("Exception when loading the yaml: ", e);
            }

            for (URL url : urls) {
                logger.debug("Found plugin.yaml in: {}", url.getPath());

                try (InputStream is = url.openConnection().getInputStream()) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> yamlData = yaml.load(is);

                    ArrayList<Map<String, Object>> commandOptions = (ArrayList<Map<String, Object>>) yamlData.get("options");

                    // Parse the options
                    for (Map<String, Object> command : commandOptions) {
                        Option option = Option.builder((String) command.get("shortOpt"))
                                .hasArg((boolean) command.get("hasArguments"))
                                .longOpt((String) command.get("longOpt"))
                                .desc((String) command.get("description"))
                                .build();
                        options.addOption(option);
                    }

                    // Parse the shortcuts
                    List<Map<String, String>> shortcuts = (List<Map<String, String>>) yamlData.get("shortcuts");
                    if (shortcuts != null) {
                        for (Map<String, String> shortcut : shortcuts) {
                            String keyCombination = shortcut.get("key");
                            String action = shortcut.get("action");
                            shortcutActions.put(keyCombination, action);
                        }
                    }
                }

            }
        } catch (Exception e) {
            logger.error("Plugin initialisation has failed.", e);
        }
        logger.debug("Shortcuts: {}", getShortcutActions());
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
            case "A": return NativeKeyEvent.VC_A;
            case "B": return NativeKeyEvent.VC_B;
            case "S": return NativeKeyEvent.VC_S;
            // ... other key mappings
            default: return NativeKeyEvent.VC_UNDEFINED;
        }
    }
}
