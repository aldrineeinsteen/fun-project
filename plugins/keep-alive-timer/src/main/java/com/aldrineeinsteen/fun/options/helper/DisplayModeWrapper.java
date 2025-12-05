package com.aldrineeinsteen.fun.options.helper;

import java.awt.*;

public class DisplayModeWrapper {

    private final int width;
    private final int height;
    private final GraphicsDevice device; // Store the reference to the device
    private final Rectangle bounds; // Store the monitor's position in virtual screen

    public DisplayModeWrapper(DisplayMode dp, GraphicsDevice device) {
        this.width = dp.getWidth();
        this.height = dp.getHeight();
        this.device = device; // Store the GraphicsDevice
        this.bounds = device.getDefaultConfiguration().getBounds(); // Store the bounds
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public GraphicsDevice getDevice() {
        return device;
    }
    
    /**
     * Get the bounds of this monitor in the virtual screen space
     * @return Rectangle representing the monitor's position and size
     */
    public Rectangle getBounds() {
        return bounds;
    }
    
    /**
     * Get the x-coordinate of the monitor's top-left corner in virtual screen space
     * @return x-coordinate
     */
    public int getX() {
        return bounds.x;
    }
    
    /**
     * Get the y-coordinate of the monitor's top-left corner in virtual screen space
     * @return y-coordinate
     */
    public int getY() {
        return bounds.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DisplayModeWrapper that = (DisplayModeWrapper) obj;
        return device.equals(that.device);
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }
    
    @Override
    public String toString() {
        return "DisplayModeWrapper{" +
                "width=" + width +
                ", height=" + height +
                ", device=" + (device != null ? device.getIDstring() : "null") +
                ", bounds=" + bounds +
                '}';
    }
}