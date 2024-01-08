package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalTime;

public class KeepAliveTimer implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(KeepAliveTimer.class);
    private static final int DEFAULT_DELAY_MILLISECONDS = 30 * 1000;

    private int delayMilliseconds = DEFAULT_DELAY_MILLISECONDS;
    private LocalTime endTime;
    private Robot robot;
    private DisplayModeWrapper displayMode;

    public KeepAliveTimer() {
        // Default constructor for plugin initialization
    }

    public KeepAliveTimer(LocalTime endTime, Robot robot, DisplayModeWrapper displayMode) {
        this(DEFAULT_DELAY_MILLISECONDS, endTime, robot, displayMode);
    }

    public KeepAliveTimer(int delayMilliseconds, LocalTime endTime, Robot robot, DisplayModeWrapper displayMode) {
        this.delayMilliseconds = delayMilliseconds;
        this.endTime = endTime;
        this.robot = robot;
        this.displayMode = displayMode;
    }

    // Setter methods
    public void setDelayMilliseconds(int delayMilliseconds) {
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
    }

    @Override
    public void run() {
        if (robot == null || endTime == null || displayMode == null) {
            logger.error("KeepAliveTimer is not properly initialized");
            return;
        }

        int screenHeight = displayMode.getHeight() - 1;
        int screenWidth = displayMode.getWidth() - 1;

        logger.info("Current screen resolution - {}x{}p", displayMode.getWidth(), displayMode.getHeight());

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
}
