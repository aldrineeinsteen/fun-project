package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KeepAliveTimer extends UtilityTemplate {
    private static KeepAliveTimer instance; //used for singleton design pattern
    private final static Logger logger = LoggerFactory.getLogger(KeepAliveTimer.class);
    private static final int DEFAULT_DELAY_MILLISECONDS = 30 * 1000;

    private int delayMilliseconds = DEFAULT_DELAY_MILLISECONDS;
    private LocalTime endTime;
    private final Robot robot = new Robot();
    
    // Fields to track mouse position changes
    private Point lastAutoPosition = null;
    private Point lastKnownPosition = null;
    private boolean userMovedMouse = false;
    
    // Multi-monitor support
    private final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private GraphicsDevice[] allGraphicsDevices;
    private List<DisplayModeWrapper> allDisplayModes;
    private DisplayModeWrapper primaryDisplayMode;
    
    // Current active display mode (can be switched between monitors)
    private DisplayModeWrapper currentDisplayMode;
    
    // Track when monitors were last refreshed
    private long lastMonitorRefreshTime = 0;
    private static final long MONITOR_REFRESH_INTERVAL = 10000; // 10 seconds

    public KeepAliveTimer() throws AWTException {
        this(DEFAULT_DELAY_MILLISECONDS, LocalTime.parse("18:30"));
    }

    public KeepAliveTimer(LocalTime endTime) throws AWTException {
        this(DEFAULT_DELAY_MILLISECONDS, endTime);
    }

    public KeepAliveTimer(int delayMilliseconds, LocalTime endTime) throws AWTException {
        this.delayMilliseconds = delayMilliseconds;
        this.endTime = endTime;
        
        // Initialize multi-monitor support
        refreshMonitorList();
        
        // Log all available monitors
        logAvailableMonitors();
    }
    
    /**
     * Refresh the list of available monitors
     * This should be called periodically to detect newly connected displays
     */
    private void refreshMonitorList() {
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
            // Try to find the current display in the refreshed list to maintain continuity
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
        
        lastMonitorRefreshTime = System.currentTimeMillis();
    }

    // Public method to get the instance
    public static synchronized KeepAliveTimer getInstance() {
        if (instance == null) {
            try {
                instance = new KeepAliveTimer();
            } catch (AWTException e) {
                // Handle the exception
                e.printStackTrace();
            }
        }
        return instance;
    }

    // Setter methods
    /*public void setDelayMilliseconds(int delayMilliseconds) {
        this.delayMilliseconds = delayMilliseconds;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    public void setDisplayMode(DisplayModeWrapper displayMode) {
        this.displayMode = displayMode;
    }*/

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

    public void runUtility() {
        if (robot == null || endTime == null || currentDisplayMode == null) {
            logger.error("KeepAliveTimer is not properly initialized");
            return;
        }

        // Force a refresh of monitor list at startup
        refreshMonitorList();
        
        int screenHeight = currentDisplayMode.getHeight() - 1;
        int screenWidth = currentDisplayMode.getWidth() - 1;

        logger.info("Starting keep-alive on monitor: {}x{} (Device: {})",
                currentDisplayMode.getWidth(),
                currentDisplayMode.getHeight(),
                currentDisplayMode.getDevice().getIDstring());

        while (LocalTime.now().isBefore(endTime)) {
            robot.delay(delayMilliseconds);

            // Get current mouse position
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            Point currentPosition = pointerInfo.getLocation();
            int xPosition = currentPosition.x;
            int yPosition = currentPosition.y;

            // Check if user has moved the mouse since our last automatic movement
            if (lastAutoPosition != null && lastKnownPosition != null) {
                // If current position is different from last auto position but also different from last known position,
                // then user has moved the mouse
                if (!currentPosition.equals(lastAutoPosition) && !currentPosition.equals(lastKnownPosition)) {
                    userMovedMouse = true;
                    logger.debug("User moved mouse to {},{}", xPosition, yPosition);
                }
            }
            
            // Update last known position
            lastKnownPosition = new Point(xPosition, yPosition);

            // Detect which monitor the mouse is currently on
            DisplayModeWrapper currentMonitor = detectCurrentMonitor(xPosition, yPosition);
            if (currentMonitor != null && !currentMonitor.equals(currentDisplayMode)) {
                logger.info("Mouse moved to different monitor: {} - Updating active display",
                        currentMonitor.getDevice().getIDstring());
                currentDisplayMode = currentMonitor;
                screenHeight = currentDisplayMode.getHeight() - 1;
                screenWidth = currentDisplayMode.getWidth() - 1;
            }

            // If user moved the mouse, don't move it automatically this time
            if (userMovedMouse) {
                logger.info("Skipping automatic movement as user moved the mouse");
                userMovedMouse = false;
                continue;
            }

            // Calculate monitor-relative coordinates
            int monitorRelativeX = xPosition - currentDisplayMode.getX();
            int monitorRelativeY = yPosition - currentDisplayMode.getY();

            // Calculate the center of the current monitor
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            
            // Calculate distance from center
            int distanceFromCenterX = Math.abs(monitorRelativeX - centerX);
            int distanceFromCenterY = Math.abs(monitorRelativeY - centerY);
            
            if (monitorRelativeX < screenWidth && monitorRelativeY < screenHeight) {
                // Small movement within the current monitor
                int increment = monitorRelativeX % 2 == 0 ? 1 : -1;
                monitorRelativeX += increment;
                monitorRelativeY += increment;
            } else {
                // If we're outside the monitor bounds, move towards the center
                // instead of resetting to a fixed position
                if (monitorRelativeX >= screenWidth) {
                    monitorRelativeX = screenWidth - 50;
                } else if (monitorRelativeX < 0) {
                    monitorRelativeX = 50;
                }
                
                if (monitorRelativeY >= screenHeight) {
                    monitorRelativeY = screenHeight - 50;
                } else if (monitorRelativeY < 0) {
                    monitorRelativeY = 50;
                }
            }

            // Convert back to absolute coordinates for robot.mouseMove
            int absoluteX = monitorRelativeX + currentDisplayMode.getX();
            int absoluteY = monitorRelativeY + currentDisplayMode.getY();

            // Move the mouse
            robot.mouseMove(absoluteX, absoluteY);
            
            // Store the position we moved to
            lastAutoPosition = new Point(absoluteX, absoluteY);
            
            logger.info("Updated position - {}, {} on monitor: {} (monitor-relative: {},{}, bounds: {})",
                    absoluteX, absoluteY,
                    currentDisplayMode.getDevice().getIDstring(),
                    monitorRelativeX, monitorRelativeY,
                    currentDisplayMode.getBounds());
        }
    }
    
    /**
     * Detect which monitor contains the given coordinates
     * Periodically refreshes the monitor list to detect newly connected displays
     */
    private DisplayModeWrapper detectCurrentMonitor(int x, int y) {
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
            boolean contains = bounds.contains(x, y);
            logger.debug("Checking monitor: {} with bounds {} - contains: {}",
                    display.getDevice().getIDstring(), bounds, contains);
            if (contains) {
                logger.debug("Found monitor: {} for position ({},{})",
                        display.getDevice().getIDstring(), x, y);
                return display;
            }
        }
        
        // If no monitor contains the point, find the closest one
        DisplayModeWrapper closestMonitor = null;
        double minDistance = Double.MAX_VALUE;
        
        for (DisplayModeWrapper display : allDisplayModes) {
            Rectangle bounds = display.getBounds();
            // Calculate distance to this monitor's bounds
            double distance = distanceToRectangle(x, y, bounds);
            if (distance < minDistance) {
                minDistance = distance;
                closestMonitor = display;
            }
        }
        
        if (closestMonitor != null) {
            logger.debug("No monitor contains position ({},{}), using closest monitor: {}",
                    x, y, closestMonitor.getDevice().getIDstring());
            return closestMonitor;
        }
        
        logger.debug("No monitor found for position ({},{}), falling back to primary", x, y);
        return primaryDisplayMode; // fallback to primary
    }
    
    /**
     * Calculate the distance from a point to a rectangle
     * Returns 0 if the point is inside the rectangle
     */
    private double distanceToRectangle(int x, int y, Rectangle rect) {
        if (rect.contains(x, y)) {
            return 0;
        }
        
        // Find the closest point on the rectangle to the given point
        int closestX = Math.max(rect.x, Math.min(x, rect.x + rect.width));
        int closestY = Math.max(rect.y, Math.min(y, rect.y + rect.height));
        
        // Calculate Euclidean distance
        return Math.sqrt(Math.pow(x - closestX, 2) + Math.pow(y - closestY, 2));
    }

    @Override
    protected void logStart() {
        logger.info("Utility: '{}' started successfully", KeepAliveTimer.class.getSimpleName());
    }
}
