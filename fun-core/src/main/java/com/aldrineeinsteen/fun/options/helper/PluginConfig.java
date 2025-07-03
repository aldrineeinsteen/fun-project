package com.aldrineeinsteen.fun.options.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PluginConfig {
    private final String name;
    private final String className;
    private final List<PluginOption> enablerOptions;      // Enabler options
    private final List<PluginOption> additionalOptions;   // Additional options

    public PluginConfig(Map<String, Object> config) {
        this.name = (String) config.get("name");
        this.className = (String) config.get("class");

        // Parse enablerOptions and additionalOptions separately
        this.enablerOptions = ((List<Map<String, Object>>) config.getOrDefault("enablerOptions", List.of())).stream()
                .map(PluginOption::new)
                .toList();

        this.additionalOptions = ((List<Map<String, Object>>) config.getOrDefault("additionalOptions", List.of())).stream()
                .map(PluginOption::new)
                .toList();
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public List<PluginOption> getEnablerOptions() {
        return enablerOptions;
    }

    public List<PluginOption> getAdditionalOptions() {
        return additionalOptions;
    }

    public List<PluginOption> getAllOptions() {
        // Combines enabler and additional options if needed
        List<PluginOption> allOptions = new ArrayList<>(enablerOptions);
        allOptions.addAll(additionalOptions);
        return allOptions;
    }
}