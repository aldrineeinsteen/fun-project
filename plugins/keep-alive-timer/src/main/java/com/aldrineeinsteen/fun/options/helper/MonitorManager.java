package com.aldrineeinsteen.fun.options.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages multi-monitor detection and configuration.
 */
public class MonitorManager {
    private static final Logger logger = LoggerFactory.getLogger(MonitorManager.class);
    private static final long MONITOR_REFRESH_INTERVAL = 10000; // 10 seconds
    
    private final GraphicsEnvironment graphicsEnvironment;
    private GraphicsDevice[] allGraphicsDevices;
    private List<DisplayModeWrapper> allDisplayModes;
    private DisplayModeWrapper primaryDisplayMode;
    private DisplayModeWrapper currentDisplayMode;
    private long lastMonitorRefreshTime = 0;

    public MonitorManager() {
        this.graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        refreshMonitorList();
        logAvailableMonitors();
    }

    /**
     * Refresh the list of available monitors
     */
    public void refreshMonitorList() {
        logger.debug("Refreshing monitor list");
        this.allGraphicsDevices = graphicsEnvironment.getScreenDevices();
        this.allDisplayModes = Arrays.stream(allGraphicsDevices)
                .map(device -> new DisplayModeWrapper(device.getDisplayMode(), device))
                .collect(Collectors.toList());
        
        // Set primary display mode (default screen device)
        GraphicsDevice primaryDevice = graphicsEnvironment.getDefaultScreenDevice();
        this.primaryDisplayMode = new DisplayModeWrapper(primaryDevice.getDisplayMode(), primaryDevice);
        
        // If currentDisplayMode is null, initialize it to primary
        if (currentDisplayMode == null) {
            this.currentDisplayMode = primaryDisplayMode;
        } else {
            maintainCurrentDisplay();
        }
        
        lastMonitorRefreshTime = System.currentTimeMillis();
    }

    /**
     * Try to maintain the current display after refresh
     */
    private void maintainCurrentDisplay() {
        boolean foundCurrentDisplay = false;
        String currentDeviceId = currentDisplayMode.getDevice().getIDstring();
        
        for (DisplayModeWrapper display : allDisplayModes) {
            if (display.getDevice().getIDstring().equals(currentDeviceId)) {
                this.currentDisplayMode = display;
                foundCurrentDisplay = true;
                logger.debug("Maintained current display after refresh: {}", currentDeviceId);
                break;
            }
        }
        
        if (!foundCurrentDisplay) {
            logger.info("Current display {} no longer available, switching to primary", currentDeviceId);
            this.currentDisplayMode = primaryDisplayMode;
        }
    }

    /**
     * Log all available monitors with their details
     */
    private void logAvailableMonitors() {
        logger.info("=== Multi-Monitor Detection Results ===");
        logger.info("Total monitors detected: {}", allDisplayModes.size());

        for (int i = 0; i < allDisplayModes.size(); i++) {
            DisplayModeWrapper display = allDisplayModes.get(i);
            GraphicsDevice device = display.getDevice();
            boolean isPrimary = device.equals(graphicsEnvironment.getDefaultScreenDevice());

            logger.info("Monitor {}: {}x{} at ({},{}) {} - Device ID: {} {}",
                    i + 1,
                    display.getWidth(),
                    display.getHeight(),
                    display.getX(),
                    display.getY(),
                    isPrimary ? "(PRIMARY)" : "",
                    device.getIDstring(),
                    isPrimary ? "- ACTIVE" : ""
            );
        }
        logger.info("========================================");
    }

    /**
     * Detect which monitor contains the given coordinates
     */
    public DisplayModeWrapper detectCurrentMonitor(int x, int y) {
        // Check if we need to refresh the monitor list
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMonitorRefreshTime > MONITOR_REFRESH_INTERVAL) {
            logger.debug("Monitor refresh interval reached, checking for new monitors");
            int previousCount = allDisplayModes.size();
            refreshMonitorList();
            int newCount = allDisplayModes.size();
            
            if (newCount != previousCount) {
                logger.info("Monitor configuration changed: previous={}, current={}", previousCount, newCount);
                logAvailableMonitors();
            }
        }
        
        logger.debug("Detecting monitor for position ({},{})", x, y);
        for (DisplayModeWrapper display : allDisplayModes) {
            Rectangle bounds = display.getBounds();
            if (bounds.contains(x, y)) {
                logger.debug("Found monitor: {} for position ({},{})",
                        display.getDevice().getIDstring(), x, y);
                return display;
            }
        }
        
        // If no monitor contains the point, find the closest one
        DisplayModeWrapper closestMonitor = findClosestMonitor(x, y);
        if (closestMonitor != null) {
            logger.debug("No monitor contains position ({},{}), using closest monitor: {}",
                    x, y, closestMonitor.getDevice().getIDstring());
            return closestMonitor;
        }
        
        logger.debug("No monitor found for position ({},{}), falling back to primary", x, y);
        return primaryDisplayMode;
    }

    /**
     * Find the closest monitor to the given coordinates
     */
    private DisplayModeWrapper findClosestMonitor(int x, int y) {
        DisplayModeWrapper closestMonitor = null;
        double minDistance = Double.MAX_VALUE;
        
        for (DisplayModeWrapper display : allDisplayModes) {
            Rectangle bounds = display.getBounds();
            double distance = distanceToRectangle(x, y, bounds);
            if (distance < minDistance) {
                minDistance = distance;
                closestMonitor = display;
            }
        }
        
        return closestMonitor;
    }

    /**
     * Calculate the distance from a point to a rectangle
     */
    private double distanceToRectangle(int x, int y, Rectangle rect) {
        if (rect.contains(x, y)) {
            return 0;
        }
        
        int closestX = Math.max(rect.x, Math.min(x, rect.x + rect.width));
        int closestY = Math.max(rect.y, Math.min(y, rect.y + rect.height));
        
        return Math.sqrt(Math.pow(x - closestX, 2) + Math.pow(y - closestY, 2));
    }

    // Getters
    public DisplayModeWrapper getCurrentDisplayMode() {
        return currentDisplayMode;
    }

    public void setCurrentDisplayMode(DisplayModeWrapper displayMode) {
        this.currentDisplayMode = displayMode;
    }

    public DisplayModeWrapper getPrimaryDisplayMode() {
        return primaryDisplayMode;
    }

    public List<DisplayModeWrapper> getAllDisplayModes() {
        return allDisplayModes;
    }
}

// Made with Bob
