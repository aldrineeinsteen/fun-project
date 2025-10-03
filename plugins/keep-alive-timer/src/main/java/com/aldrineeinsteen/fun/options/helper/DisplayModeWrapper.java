package com.aldrineeinsteen.fun.options.helper;

import java.awt.*;

public class DisplayModeWrapper {

    private final int width;
    private final int height;
    private final GraphicsDevice device; // Store the reference to the device

    public DisplayModeWrapper(DisplayMode dp, GraphicsDevice device) {
        this.width = dp.getWidth();
        this.height = dp.getHeight();
        this.device = device; // Store the GraphicsDevice
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

    @Override
    public String toString() {
        return "DisplayModeWrapper{" +
                "width=" + width +
                ", height=" + height +
                ", device=" + (device != null ? device.getIDstring() : "null") +
                '}';
    }
}