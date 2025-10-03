package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;
import java.time.LocalTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeepAliveTimerTest {

    final Robot robot = Mockito.mock(Robot.class);
    final GraphicsDevice mockGraphicsDevice = Mockito.mock(GraphicsDevice.class);
    final DisplayMode realDisplayMode = new DisplayMode(800, 600, DisplayMode.BIT_DEPTH_MULTI, DisplayMode.REFRESH_RATE_UNKNOWN);
    final DisplayModeWrapper displayMode = new DisplayModeWrapper(realDisplayMode, mockGraphicsDevice);

    @Test
    public void testRun() throws AWTException {
        KeepAliveTimer keepAliveTimer = new KeepAliveTimer(10, LocalTime.now().plusSeconds(5));
        keepAliveTimer.run();

        //verify(robot, atLeastOnce()).delay(anyInt());
        //verify(robot, atLeastOnce()).mouseMove(anyInt(), anyInt());
    }
}
