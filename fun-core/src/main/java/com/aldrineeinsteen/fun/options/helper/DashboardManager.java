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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the TUI dashboard display for all plugins.
 * Coordinates rendering and updates from multiple plugin sources.
 */
public class DashboardManager implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(DashboardManager.class);
    
    private final Map<String, DashboardRenderer> renderers = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final int refreshIntervalMs;
    private String projectVersion = "1.0.0";
    private Map<String, String> systemInfo = new LinkedHashMap<>();
    
    // ANSI escape codes for cursor control
    private static final String HIDE_CURSOR = "\033[?25l";
    private static final String SHOW_CURSOR = "\033[?25h";
    
    private final DashboardDisplayRenderer displayRenderer = new DashboardDisplayRenderer();
    
    public DashboardManager() {
        this(1000); // Default 1 second refresh
    }
    
    public DashboardManager(int refreshIntervalMs) {
        this.refreshIntervalMs = refreshIntervalMs;
    }
    
    /**
     * Register a plugin renderer for dashboard display
     */
    public void registerRenderer(String pluginName, DashboardRenderer renderer) {
        if (renderer != null) {
            logger.debug("Attempting to register renderer for plugin: {}, isDashboardEnabled: {}",
                pluginName, renderer.isDashboardEnabled());
            if (renderer.isDashboardEnabled()) {
                renderers.put(pluginName, renderer);
                logger.info("Successfully registered dashboard renderer for plugin: {}", pluginName);
            } else {
                logger.warn("Dashboard renderer for plugin {} is disabled", pluginName);
            }
        } else {
            logger.warn("Attempted to register null renderer for plugin: {}", pluginName);
        }
    }
    
    /**
     * Unregister a plugin renderer
     */
    public void unregisterRenderer(String pluginName) {
        renderers.remove(pluginName);
        logger.debug("Unregistered dashboard renderer for plugin: {}", pluginName);
    }
    
    /**
     * Set the project version to display
     */
    public void setProjectVersion(String version) {
        this.projectVersion = version;
    }
    
    /**
     * Set system information to display
     */
    public void setSystemInfo(Map<String, String> info) {
        this.systemInfo = info;
    }
    
    /**
     * Start the dashboard rendering loop
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting dashboard manager");
            new Thread(this, "DashboardManager").start();
        }
    }
    
    /**
     * Stop the dashboard rendering
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping dashboard manager");
            // Show cursor again
            System.out.print(SHOW_CURSOR);
            System.out.flush();
        }
    }
    
    @Override
    public void run() {
        // Hide cursor for cleaner display
        System.out.print(HIDE_CURSOR);
        System.out.flush();
        
        try {
            while (running.get()) {
                renderDashboard();
                Thread.sleep(refreshIntervalMs);
            }
        } catch (InterruptedException e) {
            logger.debug("Dashboard manager interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            // Show cursor when exiting
            System.out.print(SHOW_CURSOR);
            System.out.flush();
        }
    }
    
    /**
     * Render the complete dashboard
     */
    private void renderDashboard() {
        String dashboardContent = displayRenderer.renderDashboard(projectVersion, renderers);
        System.out.print(dashboardContent);
        System.out.flush();
    }
    
    /**
     * Check if dashboard is currently running
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Get the number of registered renderers
     */
    public int getRendererCount() {
        return renderers.size();
    }
}

// Made with Bob
