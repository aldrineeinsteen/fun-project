package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalTime;

public class KeepAliveTimer extends UtilityTemplate {
    private static KeepAliveTimer instance; //used for singleton design pattern
    private final static Logger logger = LoggerFactory.getLogger(KeepAliveTimer.class);
    private static final int DEFAULT_DELAY_MILLISECONDS = 30 * 1000;

    private int delayMilliseconds = DEFAULT_DELAY_MILLISECONDS;
    private LocalTime endTime;
    private final Robot robot = new Robot();
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private final DisplayModeWrapper displayMode = new DisplayModeWrapper(gd.getDisplayMode());

    public KeepAliveTimer() throws AWTException {
        this(DEFAULT_DELAY_MILLISECONDS, LocalTime.parse("17:30"));
    }

    public KeepAliveTimer(LocalTime endTime) throws AWTException {
        this(DEFAULT_DELAY_MILLISECONDS, endTime);
    }

    public KeepAliveTimer(int delayMilliseconds, LocalTime endTime) throws AWTException {
        this.delayMilliseconds = delayMilliseconds;
        this.endTime = endTime;
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

    public void runUtility() {
        if (robot == null || endTime == null || displayMode == null) {
            logger.error("KeepAliveTimer is not properly initialized");
            return;
        }

        int screenHeight = displayMode.getHeight() - 1;
        int screenWidth = displayMode.getWidth() - 1;

        logger.debug("Current screen resolution - {}x{}p", displayMode.getWidth(), displayMode.getHeight());

        while (LocalTime.now().isBefore(endTime)) {
            robot.delay(delayMilliseconds);

            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            int xPosition = pointerInfo.getLocation().x;
            int yPosition = pointerInfo.getLocation().y;

            if (xPosition < screenWidth && yPosition < screenHeight) {
                int increment = xPosition % 2 == 0 ? 1 : -1;
                xPosition += increment;
                yPosition += increment;
            } else {
                yPosition = 0;
                xPosition = 0;
            }

            robot.mouseMove(xPosition, yPosition);
            logger.info("Updated position - {}, {}", xPosition, yPosition);
        }
    }

    @Override
    protected void logStart() {
        logger.info("Utility: '{}' started successfully", KeepAliveTimer.class.getSimpleName());
    }
}
