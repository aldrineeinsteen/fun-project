package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

public class KeepAliveTimer implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(KeepAliveTimer.class);

    private final int DELAY_MILLISECONDS;
    private final LocalTime endTime;
    private final Robot robot;
    private final List<DisplayModeWrapper> displayModes;

    public KeepAliveTimer(LocalTime endTime, Robot robot) {
        this(30 * 1000, endTime, robot, getDisplayModes());
    }

    public KeepAliveTimer(Integer delayMilliseconds, LocalTime endTime, Robot robot, List<DisplayModeWrapper> displayModes) {
        this.DELAY_MILLISECONDS = delayMilliseconds;
        this.endTime = endTime;
        this.robot = robot;
        this.displayModes = displayModes;
    }

    private static List<DisplayModeWrapper> getDisplayModes() {
        List<DisplayModeWrapper> displayModes = new ArrayList<>();
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        for (GraphicsDevice device : devices) {
            if (device != null) { // Check if the device is not null
                DisplayMode mode = device.getDisplayMode();
                displayModes.add(new DisplayModeWrapper(mode, device));
            } else {
                logger.warn("Null GraphicsDevice encountered and skipped.");
            }
        }
        return displayModes;
    }

    public void addDevice(GraphicsDevice device) {
        DisplayMode mode = device.getDisplayMode();
        displayModes.add(new DisplayModeWrapper(mode, device));
        logger.info("Device added. Total devices: {}", displayModes.size());
    }

    public void removeDevice(GraphicsDevice device) {
        displayModes.removeIf(displayModeWrapper -> displayModeWrapper.getDevice().equals(device));
        logger.info("Device removed. Total devices: {}", displayModes.size());
    }

    @Override
    public void run() {
        int screenIndex = 0;
        logger.info("Number of screens detected: {}", displayModes.size());

        while (LocalTime.now().isBefore(endTime)) {
            robot.delay(DELAY_MILLISECONDS);

            DisplayModeWrapper currentDisplay = displayModes.get(screenIndex);
            int screenWidth = currentDisplay.getWidth() - 1;
            int screenHeight = currentDisplay.getHeight() - 1;

            logger.info("Current screen resolution for screen index {} - {}x{}p", screenIndex, currentDisplay.getWidth(), currentDisplay.getHeight());

            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            Point location = pointerInfo.getLocation();
            int xPosition = location.x;
            int yPosition = location.y;

            if (xPosition < screenWidth && yPosition < screenHeight) {
                int increment = xPosition % 2 == 0 ? 1 : -1;
                xPosition += increment;
                yPosition += increment;
            } else {
                xPosition = 0;
                yPosition = 0;
            }

            // Move the cursor only if it's currently on the active screen
            if (currentDisplay.getDevice().getDefaultConfiguration().getBounds().contains(location)) {
                robot.mouseMove(xPosition, yPosition);
                logger.info("Updated position on screen index {} - {}, {}", screenIndex, xPosition, yPosition);
            } else {
                logger.info("Cursor is not on the active screen index {}. No movement applied.", screenIndex);
            }

            // Move to the next screen in sequence
            screenIndex = (screenIndex + 1) % displayModes.size();
        }
    }
}