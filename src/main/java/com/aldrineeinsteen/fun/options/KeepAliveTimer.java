package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalTime;

public class KeepAliveTimer implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(KeepAliveTimer.class);

    //private static final int DELAY_MILLISECONDS = 1000;
    private final int DELAY_MILLISECONDS;

    private final LocalTime endTime;
    private final Robot robot;
    private final DisplayModeWrapper displayMode;

    public KeepAliveTimer(LocalTime endTime, Robot robot, DisplayModeWrapper displayMode) {
        this(30*1000, endTime, robot, displayMode);
    }

    public KeepAliveTimer(Integer delayMilliseconds, LocalTime endTime, Robot robot, DisplayModeWrapper displayMode) {
        this.DELAY_MILLISECONDS = delayMilliseconds;
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
