package com.aldrineeinsteen.fun.options;

import java.awt.*;

public class DisplayModeWrapper {

    private final int width;
    private final int height;

    public DisplayModeWrapper(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public DisplayModeWrapper(DisplayMode dp) {
        this.width = dp.getWidth();
        this.height = dp.getHeight();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
