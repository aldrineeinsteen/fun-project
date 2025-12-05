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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the TUI dashboard display for all plugins.
 * Coordinates rendering and updates from multiple plugin sources.
 */
public class DashboardManager implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(DashboardManager.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private final Map<String, DashboardRenderer> renderers = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final int refreshIntervalMs;
    private String projectVersion = "1.0.0";
    private Map<String, String> systemInfo = new LinkedHashMap<>();
    
    // ANSI escape codes for terminal control
    private static final String CLEAR_SCREEN = "\033[H\033[2J";
    private static final String HIDE_CURSOR = "\033[?25l";
    private static final String SHOW_CURSOR = "\033[?25h";
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String CYAN = "\033[36m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    
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
     * Render the complete dashboard with grid layout support
     */
    private void renderDashboard() {
        StringBuilder dashboard = new StringBuilder();
        
        // Clear screen and move cursor to top
        dashboard.append(CLEAR_SCREEN);
        
        // Header
        dashboard.append(BOLD).append(CYAN);
        dashboard.append("FunProject v").append(projectVersion);
        dashboard.append(RESET).append("\n");
        dashboard.append("─".repeat(80)).append("\n");
        
        // Current time
        dashboard.append(YELLOW).append("Current Time: ").append(RESET);
        dashboard.append(LocalDateTime.now().format(TIME_FORMATTER)).append("\n\n");
        
        // Organize renderers into a grid structure
        Map<Integer, Map<Integer, DashboardRenderer>> grid = organizeRenderersIntoGrid();
        
        logger.debug("Rendering dashboard with {} registered renderers in grid layout", renderers.size());
        
        // Render the grid
        if (!grid.isEmpty()) {
            renderGrid(dashboard, grid);
        }
        
        // Footer
        dashboard.append("\n");
        dashboard.append("─".repeat(80)).append("\n");
        dashboard.append(YELLOW).append("Press Ctrl+C to exit").append(RESET).append("\n");
        
        // Output the dashboard
        System.out.print(dashboard.toString());
        System.out.flush();
    }
    
    /**
     * Organize renderers into a grid structure based on their column and row positions
     */
    private Map<Integer, Map<Integer, DashboardRenderer>> organizeRenderersIntoGrid() {
        Map<Integer, Map<Integer, DashboardRenderer>> grid = new TreeMap<>();
        
        for (Map.Entry<String, DashboardRenderer> entry : renderers.entrySet()) {
            DashboardRenderer renderer = entry.getValue();
            int row = renderer.getDashboardRow();
            int column = renderer.getDashboardColumn();
            
            grid.computeIfAbsent(row, k -> new TreeMap<>()).put(column, renderer);
        }
        
        return grid;
    }
    
    /**
     * Render the grid with box-drawing characters and dividers
     */
    private void renderGrid(StringBuilder dashboard, Map<Integer, Map<Integer, DashboardRenderer>> grid) {
        int columnWidth = 38; // Width for each column
        
        for (Map.Entry<Integer, Map<Integer, DashboardRenderer>> rowEntry : grid.entrySet()) {
            Map<Integer, DashboardRenderer> columns = rowEntry.getValue();
            int numColumns = columns.size();
            
            // Top border
            dashboard.append("┌");
            for (int i = 1; i <= numColumns; i++) {
                dashboard.append("─".repeat(columnWidth));
                if (i < numColumns) {
                    dashboard.append("┬");
                }
            }
            dashboard.append("┐\n");
            
            // Plugin names header
            dashboard.append("│");
            for (Map.Entry<Integer, DashboardRenderer> colEntry : columns.entrySet()) {
                DashboardRenderer renderer = colEntry.getValue();
                String pluginName = renderer.getDashboardPluginName();
                dashboard.append(" ").append(BOLD).append(CYAN).append(pluginName).append(RESET);
                int padding = columnWidth - pluginName.length() - 1;
                dashboard.append(" ".repeat(Math.max(0, padding))).append("│");
            }
            dashboard.append("\n");
            
            // Separator after header
            dashboard.append("├");
            for (int i = 1; i <= numColumns; i++) {
                dashboard.append("─".repeat(columnWidth));
                if (i < numColumns) {
                    dashboard.append("┼");
                }
            }
            dashboard.append("┤\n");
            
            // Collect data from all columns
            List<List<String>> columnData = new ArrayList<>();
            int maxRows = 0;
            
            for (Map.Entry<Integer, DashboardRenderer> colEntry : columns.entrySet()) {
                DashboardRenderer renderer = colEntry.getValue();
                List<String> lines = new ArrayList<>();
                
                try {
                    Map<String, String> data = renderer.getDashboardData();
                    if (data != null && !data.isEmpty()) {
                        for (Map.Entry<String, String> dataEntry : data.entrySet()) {
                            String line = GREEN + dataEntry.getKey() + ": " + RESET + dataEntry.getValue();
                            lines.add(line);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error rendering dashboard for plugin", e);
                    lines.add(GREEN + "Error: " + RESET + "Failed to render");
                }
                
                columnData.add(lines);
                maxRows = Math.max(maxRows, lines.size());
            }
            
            // Render data rows
            for (int lineIdx = 0; lineIdx < maxRows; lineIdx++) {
                dashboard.append("│");
                for (int colIdx = 0; colIdx < numColumns; colIdx++) {
                    List<String> lines = columnData.get(colIdx);
                    String line = lineIdx < lines.size() ? lines.get(lineIdx) : "";
                    
                    // Calculate visible length (excluding ANSI codes)
                    int visibleLength = line.replaceAll("\033\\[[0-9;]+m", "").length();
                    int padding = columnWidth - visibleLength - 1;
                    
                    dashboard.append(" ").append(line);
                    dashboard.append(" ".repeat(Math.max(0, padding))).append("│");
                }
                dashboard.append("\n");
            }
            
            // Bottom border
            dashboard.append("└");
            for (int i = 1; i <= numColumns; i++) {
                dashboard.append("─".repeat(columnWidth));
                if (i < numColumns) {
                    dashboard.append("┴");
                }
            }
            dashboard.append("┘\n");
        }
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
