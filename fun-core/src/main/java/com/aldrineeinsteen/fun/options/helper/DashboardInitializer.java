/*
 * Copyright 2017-2025 Aldrine Einsteen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.DashboardRenderer;
import com.aldrineeinsteen.fun.options.PluginTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Initializes and configures the dashboard system.
 * Handles dashboard setup, plugin registration, and system information display.
 */
public class DashboardInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DashboardInitializer.class);
    private final DashboardManager dashboardManager;
    private final String projectVersion;
    
    public DashboardInitializer() {
        this.dashboardManager = new DashboardManager(1000); // 1 second refresh
        this.projectVersion = loadProjectVersion();
        this.dashboardManager.setProjectVersion(projectVersion);
    }
    
    /**
     * Load project version from application.properties
     */
    private String loadProjectVersion() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.warn("Unable to find application.properties, using default version");
                return "unknown";
            }
            
            Properties prop = new Properties();
            prop.load(input);
            String version = prop.getProperty("application.version", "unknown");
            logger.debug("Loaded project version: {}", version);
            return version;
        } catch (IOException e) {
            logger.error("Error loading project version", e);
            return "unknown";
        }
    }
    
    /**
     * Initialize dashboard with system information
     */
    public void initialize() {
        Map<String, String> systemInfo = new LinkedHashMap<>();
        systemInfo.put("Loaded Plugins", String.valueOf(PluginRepository.getLoadedPlugins().size()));
        
        // List enabled plugins
        List<String> enabledPlugins = new ArrayList<>();
        for (String pluginName : PluginRepository.getLoadedPlugins()) {
            String simpleName = pluginName.substring(pluginName.lastIndexOf('.') + 1);
            enabledPlugins.add(simpleName);
        }
        systemInfo.put("Active Plugins", String.join(", ", enabledPlugins));
        
        // List shortcuts
        Map<String, PluginMetadata.ShortcutAction> shortcuts = PluginRepository.getShortcutActions();
        if (!shortcuts.isEmpty()) {
            systemInfo.put("Global Shortcuts", String.valueOf(shortcuts.size()));
            for (Map.Entry<String, PluginMetadata.ShortcutAction> entry : shortcuts.entrySet()) {
                String pluginSimpleName = entry.getValue().getPlugin().substring(entry.getValue().getPlugin().lastIndexOf('.') + 1);
                systemInfo.put("  " + entry.getKey(), pluginSimpleName + ":" + entry.getValue().getAction());
            }
        }
        
        dashboardManager.setSystemInfo(systemInfo);
        logger.info("Dashboard initialized with version: {}", projectVersion);
    }
    
    /**
     * Register all plugins with dashboard renderers
     */
    public void registerPlugins() {
        PluginRepository.getLoadedPlugins().forEach(pluginName -> {
            // Check utilities first
            Runnable utility = PluginRepository.getUtility(pluginName);
            if (utility instanceof DashboardRenderer) {
                DashboardRenderer renderer = (DashboardRenderer) utility;
                dashboardManager.registerRenderer(pluginName, renderer);
                logger.debug("Registered dashboard renderer for utility: {}", pluginName);
            }
            
            // Also check regular plugins for dashboard support
            PluginTemplate plugin = PluginRepository.getPlugin(pluginName);
            if (plugin instanceof DashboardRenderer) {
                DashboardRenderer renderer = (DashboardRenderer) plugin;
                dashboardManager.registerRenderer(pluginName, renderer);
                logger.debug("Registered dashboard renderer for plugin: {}", pluginName);
            }
        });
        logger.info("Dashboard registration complete. Registered {} renderers", dashboardManager.getRendererCount());
    }
    
    /**
     * Start the dashboard rendering
     */
    public void start() {
        dashboardManager.start();
        
        // Add shutdown hook to clean up dashboard
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dashboardManager != null) {
                dashboardManager.stop();
            }
        }));
    }
    
    /**
     * Get the dashboard manager instance
     */
    public DashboardManager getDashboardManager() {
        return dashboardManager;
    }
    
    /**
     * Get the project version
     */
    public String getProjectVersion() {
        return projectVersion;
    }
}

// Made with Bob
