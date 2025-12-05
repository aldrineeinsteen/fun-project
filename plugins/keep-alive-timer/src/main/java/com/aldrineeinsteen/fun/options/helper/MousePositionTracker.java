package com.aldrineeinsteen.fun.options.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Tracks mouse position and detects user movements.
 */
public class MousePositionTracker {
    private static final Logger logger = LoggerFactory.getLogger(MousePositionTracker.class);
    
    private Point lastAutoPosition = null;
    private Point lastKnownPosition = null;
    private boolean userMovedMouse = false;

    /**
     * Update tracking with current mouse position
     * @return true if user moved the mouse, false otherwise
     */
    public boolean updatePosition(Point currentPosition) {
        // Check if user has moved the mouse since our last automatic movement
        if (lastAutoPosition != null && lastKnownPosition != null) {
            // If current position is different from last auto position but also different from last known position,
            // then user has moved the mouse
            if (!currentPosition.equals(lastAutoPosition) && !currentPosition.equals(lastKnownPosition)) {
                userMovedMouse = true;
                logger.debug("User moved mouse to {},{}", currentPosition.x, currentPosition.y);
            }
        }
        
        // Update last known position
        lastKnownPosition = new Point(currentPosition.x, currentPosition.y);
        
        return userMovedMouse;
    }

    /**
     * Check if user moved the mouse and reset the flag
     */
    public boolean checkAndResetUserMovement() {
        boolean moved = userMovedMouse;
        userMovedMouse = false;
        return moved;
    }

    /**
     * Record the position we automatically moved to
     */
    public void recordAutoPosition(Point position) {
        lastAutoPosition = new Point(position.x, position.y);
    }

    /**
     * Calculate new mouse position with small incremental movement
     */
    public Point calculateNewPosition(int monitorRelativeX, int monitorRelativeY,
                                     int screenWidth, int screenHeight) {
        // Check if coordinates need constraining
        boolean xOutOfBounds = monitorRelativeX < 0 || monitorRelativeX >= screenWidth;
        boolean yOutOfBounds = monitorRelativeY < 0 || monitorRelativeY >= screenHeight;
        
        // Constrain coordinates to monitor bounds if needed
        if (xOutOfBounds) {
            monitorRelativeX = constrainToMonitor(monitorRelativeX, screenWidth);
        }
        if (yOutOfBounds) {
            monitorRelativeY = constrainToMonitor(monitorRelativeY, screenHeight);
        }
        
        // Only apply incremental movement if both coordinates were within bounds
        if (!xOutOfBounds && !yOutOfBounds) {
            int increment = monitorRelativeX % 2 == 0 ? 1 : -1;
            monitorRelativeX += increment;
            monitorRelativeY += increment;
        }
        
        return new Point(monitorRelativeX, monitorRelativeY);
    }

    /**
     * Constrain coordinate to monitor bounds with margin
     */
    private int constrainToMonitor(int coordinate, int maxBound) {
        if (coordinate >= maxBound) {
            return maxBound - 50;
        } else if (coordinate < 0) {
            return 50;
        }
        return coordinate;
    }

    // Getters
    public Point getLastAutoPosition() {
        return lastAutoPosition;
    }

    public Point getLastKnownPosition() {
        return lastKnownPosition;
    }
}

// Made with Bob
