package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import com.aldrineeinsteen.fun.options.helper.MonitorManager;
import com.aldrineeinsteen.fun.options.helper.MousePositionTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keep-alive timer utility with multi-monitor support.
 * Moves the mouse periodically to prevent system sleep/lock.
 */
public class KeepAliveTimer extends UtilityTemplate {
    private static KeepAliveTimer instance;
    private static final Logger logger = LoggerFactory.getLogger(KeepAliveTimer.class);
    private static final int DEFAULT_DELAY_MILLISECONDS = 30 * 1000;

    private int delayMilliseconds = DEFAULT_DELAY_MILLISECONDS;
    private LocalTime endTime;
    private final Robot robot;
    
    private final MonitorManager monitorManager;
    private final MousePositionTracker positionTracker;
    
    // Track running state
    private volatile boolean isRunning = false;
    private volatile boolean shouldRestart = true;

    public KeepAliveTimer() throws AWTException {
        this(DEFAULT_DELAY_MILLISECONDS, LocalTime.parse("18:30"));
    }

    public KeepAliveTimer(LocalTime endTime) throws AWTException {
        this(DEFAULT_DELAY_MILLISECONDS, endTime);
    }

    public KeepAliveTimer(int delayMilliseconds, LocalTime endTime) throws AWTException {
        this.delayMilliseconds = delayMilliseconds;
        this.endTime = endTime;
        this.robot = new Robot();
        this.monitorManager = new MonitorManager();
        this.positionTracker = new MousePositionTracker();
    }

    public static synchronized KeepAliveTimer getInstance() {
        if (instance == null) {
            try {
                instance = new KeepAliveTimer();
            } catch (AWTException e) {
                logger.error("Failed to create KeepAliveTimer instance", e);
            }
        }
        return instance;
    }

    @Override
    public void runUtility() {
        if (robot == null || endTime == null || monitorManager.getCurrentDisplayMode() == null) {
            logger.error("KeepAliveTimer is not properly initialized");
            return;
        }

        // Main loop with restart capability
        while (shouldRestart) {
            startTimerCycle();
            
            // If we should restart, wait a bit before restarting
            if (shouldRestart) {
                logger.info("Timer completed. Restarting in 5 seconds...");
                isRunning = false;
                robot.delay(5000);
            }
        }
        
        isRunning = false;
    }
    
    /**
     * Run a single timer cycle from now until end time
     */
    private void startTimerCycle() {
        isRunning = true;
        
        // Force a refresh of monitor list at startup
        monitorManager.refreshMonitorList();
        
        DisplayModeWrapper currentDisplay = monitorManager.getCurrentDisplayMode();
        logger.info("Starting keep-alive on monitor: {}x{} (Device: {})",
                currentDisplay.getWidth(),
                currentDisplay.getHeight(),
                currentDisplay.getDevice().getIDstring());

        while (LocalTime.now().isBefore(endTime) && shouldRestart) {
            robot.delay(delayMilliseconds);
            processMouseMovement();
        }
        
        if (LocalTime.now().isAfter(endTime) || LocalTime.now().equals(endTime)) {
            logger.info("Keep-alive timer reached end time: {}", endTime);
        }
    }

    /**
     * Process a single mouse movement cycle
     */
    private void processMouseMovement() {
        // Get current mouse position
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Point currentPosition = pointerInfo.getLocation();
        
        // Update position tracking
        positionTracker.updatePosition(currentPosition);
        
        // Detect which monitor the mouse is currently on
        DisplayModeWrapper detectedMonitor = monitorManager.detectCurrentMonitor(
            currentPosition.x, currentPosition.y);
        
        if (detectedMonitor != null && !detectedMonitor.equals(monitorManager.getCurrentDisplayMode())) {
            switchToMonitor(detectedMonitor);
        }
        
        // If user moved the mouse, don't move it automatically this time
        if (positionTracker.checkAndResetUserMovement()) {
            logger.info("Skipping automatic movement as user moved the mouse");
            return;
        }
        
        moveMouseAutomatically(currentPosition);
    }

    /**
     * Switch to a different monitor
     */
    private void switchToMonitor(DisplayModeWrapper newMonitor) {
        logger.info("Mouse moved to different monitor: {} - Updating active display",
                newMonitor.getDevice().getIDstring());
        monitorManager.setCurrentDisplayMode(newMonitor);
    }

    /**
     * Perform automatic mouse movement
     */
    private void moveMouseAutomatically(Point currentPosition) {
        DisplayModeWrapper currentDisplay = monitorManager.getCurrentDisplayMode();
        int screenHeight = currentDisplay.getHeight() - 1;
        int screenWidth = currentDisplay.getWidth() - 1;
        
        // Calculate monitor-relative coordinates
        int monitorRelativeX = currentPosition.x - currentDisplay.getX();
        int monitorRelativeY = currentPosition.y - currentDisplay.getY();
        
        // Calculate new position
        Point newRelativePosition = positionTracker.calculateNewPosition(
            monitorRelativeX, monitorRelativeY, screenWidth, screenHeight);
        
        // Convert back to absolute coordinates
        int absoluteX = newRelativePosition.x + currentDisplay.getX();
        int absoluteY = newRelativePosition.y + currentDisplay.getY();
        
        // Move the mouse
        robot.mouseMove(absoluteX, absoluteY);
        
        // Store the position we moved to
        positionTracker.recordAutoPosition(new Point(absoluteX, absoluteY));
        
        logger.info("Updated position - {}, {} on monitor: {} (monitor-relative: {},{}, bounds: {})",
                absoluteX, absoluteY,
                currentDisplay.getDevice().getIDstring(),
                newRelativePosition.x, newRelativePosition.y,
                currentDisplay.getBounds());
    }

    @Override
    protected void logStart() {
        logger.info("Utility: '{}' started successfully", KeepAliveTimer.class.getSimpleName());
    }
    
    /**
     * Get dashboard data for display
     */
    @Override
    public Map<String, String> getDashboardData() {
        Map<String, String> data = new LinkedHashMap<>();
        
        DisplayModeWrapper currentDisplay = monitorManager.getCurrentDisplayMode();
        if (currentDisplay != null) {
            data.put("Monitor", String.format("%dx%d",
                currentDisplay.getWidth(), currentDisplay.getHeight()));
            data.put("Device", currentDisplay.getDevice().getIDstring());
        }
        
        data.put("End Time", endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        data.put("Delay", String.format("%ds", delayMilliseconds / 1000));
        
        // Show actual running status
        LocalTime now = LocalTime.now();
        long secondsUntilEnd = getSecondsUntilEnd(now, endTime);
        if (isRunning && secondsUntilEnd > 0) {
            data.put("Status", "\u001B[32m✓ Active\u001B[0m");
        } else if (!isRunning) {
            data.put("Status", "\u001B[31m✗ Stopped\u001B[0m");
        } else {
            data.put("Status", "\u001B[32m✓ Active\u001B[0m");
        }
        
        // Show time remaining
        if (secondsUntilEnd > 0) {
            long hours = secondsUntilEnd / 3600;
            long minutes = (secondsUntilEnd % 3600) / 60;
            data.put("Time Remaining", String.format("%dh %dm", hours, minutes));
        } else {
            data.put("Time Remaining", "Completed - Restarting...");
        }
        
        return data;
    }

    /**
     * Returns signed seconds until end time. For large negative deltas, treat end time as next-day.
     */
    private long getSecondsUntilEnd(LocalTime now, LocalTime configuredEndTime) {
        long seconds = java.time.Duration.between(now, configuredEndTime).getSeconds();

        // If end time looks nearly a full day behind, it is likely intended for the next day.
        if (seconds < 0 && Math.abs(seconds) > 12 * 60 * 60) {
            return seconds + (24 * 60 * 60);
        }

        return seconds;
    }
    
    /**
     * Stop the timer and prevent restart
     */
    public void stopTimer() {
        shouldRestart = false;
        isRunning = false;
        logger.info("Keep-alive timer stopped");
    }
    
    /**
     * Check if the timer is currently running
     */
    public boolean isRunning() {
        return isRunning;
    }

    // Getters for testing
    public MonitorManager getMonitorManager() {
        return monitorManager;
    }

    public MousePositionTracker getPositionTracker() {
        return positionTracker;
    }
}

// Made with Bob
