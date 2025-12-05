package com.aldrineeinsteen.fun.options.helper;

import org.apache.commons.cli.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds plugin metadata for documentation and configuration.
 */
public class PluginMetadata {
    private final String name;
    private final String className;
    private final String description;
    private final List<Option> options;
    private final List<Option> params;
    private final List<ShortcutAction> shortcuts;

    public PluginMetadata(String name, String className, String description) {
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

    /**
     * Represents a keyboard shortcut action for a plugin.
     */
    public static class ShortcutAction {
        private final String action;
        private final String plugin;
        private final String keyCombination;

        public ShortcutAction(String action, String plugin, String keyCombination) {
            this.action = action;
            this.plugin = plugin;
            this.keyCombination = keyCombination;
        }

        public String getAction() {
            return action;
        }

        public String getPlugin() {
            return plugin;
        }

        public String getKeyCombination() {
            return keyCombination;
        }

        @Override
        public String toString() {
            return "ShortcutAction{" +
                    "action='" + action + '\'' +
                    ", plugin='" + plugin + '\'' +
                    ", keyCombination='" + keyCombination + '\'' +
                    '}';
        }
    }
}

// Made with Bob
