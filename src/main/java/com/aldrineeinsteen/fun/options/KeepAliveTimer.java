package com.aldrineeinsteen.fun.options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalTime;

public class KeepAliveTimer implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(KeepAliveTimer.class);

    // Using a constant for the delay time
    private static final int DELAY_MILLISECONDS = 1000;

    private final LocalTime endTime;
    private final Robot robot;
    private final DisplayMode displayMode;

    public KeepAliveTimer(LocalTime endTime, Robot robot, DisplayMode displayMode) {
        this.endTime = endTime;
        this.robot = robot;
        this.displayMode = displayMode;
    }

    @Override
    public void run() {
        int screenHeight = displayMode.getHeight() - 1;
        int screenWidth = displayMode.getWidth() - 1;

        logger.info("Current screen resolution - {}x{}p", displayMode.getWidth(), displayMode.getHeight());

        while (LocalTime.now().isBefore(endTime)) {
            robot.delay(DELAY_MILLISECONDS);

            // Stored pointer info in a variable for reuse
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            int xPosition = pointerInfo.getLocation().x;
            int yPosition = pointerInfo.getLocation().y;

            if (xPosition < screenWidth && yPosition < screenHeight) {
                // Toggle between increasing and decreasing by 1 for X and Y position
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
