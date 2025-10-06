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
    
    // Multi-monitor support
    private final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private final GraphicsDevice[] allGraphicsDevices = graphicsEnvironment.getScreenDevices();
    private final List<DisplayModeWrapper> allDisplayModes;
    private final DisplayModeWrapper primaryDisplayMode;
    
    // Current active display mode (can be switched between monitors)
    private DisplayModeWrapper currentDisplayMode;

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
        this.allDisplayModes = Arrays.stream(allGraphicsDevices)
                .map(device -> new DisplayModeWrapper(device.getDisplayMode(), device))
                .collect(Collectors.toList());
        
        // Set primary display mode (default screen device)
        GraphicsDevice primaryDevice = graphicsEnvironment.getDefaultScreenDevice();
        this.primaryDisplayMode = new DisplayModeWrapper(primaryDevice.getDisplayMode(), primaryDevice);
        this.currentDisplayMode = primaryDisplayMode;
        
        // Log all available monitors
        logAvailableMonitors();
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
            
            logger.info("Monitor {}: {}x{} {} - Device ID: {} {}",
                    i + 1, 
                    display.getWidth(), 
                    display.getHeight(),
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

        int screenHeight = currentDisplayMode.getHeight() - 1;
        int screenWidth = currentDisplayMode.getWidth() - 1;

        logger.info("Starting keep-alive on monitor: {}x{} (Device: {})", 
                currentDisplayMode.getWidth(), 
                currentDisplayMode.getHeight(),
                currentDisplayMode.getDevice().getIDstring());

        while (LocalTime.now().isBefore(endTime)) {
            robot.delay(delayMilliseconds);

            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            int xPosition = pointerInfo.getLocation().x;
            int yPosition = pointerInfo.getLocation().y;

            // Detect which monitor the mouse is currently on
            DisplayModeWrapper currentMonitor = detectCurrentMonitor(xPosition, yPosition);
            if (currentMonitor != null && !currentMonitor.equals(currentDisplayMode)) {
                logger.info("Mouse moved to different monitor: {} - Updating active display", 
                        currentMonitor.getDevice().getIDstring());
                currentDisplayMode = currentMonitor;
                screenHeight = currentDisplayMode.getHeight() - 1;
                screenWidth = currentDisplayMode.getWidth() - 1;
            }

            if (xPosition < screenWidth && yPosition < screenHeight) {
                int increment = xPosition % 2 == 0 ? 1 : -1;
                xPosition += increment;
                yPosition += increment;
            } else {
                yPosition = 0;
                xPosition = 0;
            }

            robot.mouseMove(xPosition, yPosition);
            logger.info("Updated position - {}, {} on monitor: {}", 
                    xPosition, yPosition, currentDisplayMode.getDevice().getIDstring());
        }
    }
    
    /**
     * Detect which monitor contains the given coordinates
     */
    private DisplayModeWrapper detectCurrentMonitor(int x, int y) {
        for (DisplayModeWrapper display : allDisplayModes) {
            GraphicsDevice device = display.getDevice();
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            if (bounds.contains(x, y)) {
                return display;
            }
        }
        return primaryDisplayMode; // fallback to primary
    }

    @Override
    protected void logStart() {
        logger.info("Utility: '{}' started successfully", KeepAliveTimer.class.getSimpleName());
    }
}
