package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeepAliveTimerTest {

    @Mock
    private Robot robot;

    @Mock
    private DisplayModeWrapper displayMode;

    @Mock
    private GraphicsDevice graphicsDevice;

    @Mock
    private GraphicsConfiguration graphicsConfiguration;

    @Test
    public void testRun() {
        // Set up mock return values for DisplayModeWrapper and GraphicsDevice
        when(displayMode.getWidth()).thenReturn(800);
        when(displayMode.getHeight()).thenReturn(800);
        when(displayMode.getDevice()).thenReturn(graphicsDevice); // Link DisplayModeWrapper to GraphicsDevice
        when(graphicsDevice.getDefaultConfiguration()).thenReturn(graphicsConfiguration);

        // Set up GraphicsConfiguration to return a valid Rectangle
        when(graphicsConfiguration.getBounds()).thenReturn(new Rectangle(0, 0, 800, 800));

        // Create a list of display modes with the mocked DisplayModeWrapper
        List<DisplayModeWrapper> displayModes = Collections.singletonList(displayMode);

        // Initialize KeepAliveTimer with the list of display modes and the mocked Robot
        KeepAliveTimer keepAliveTimer = new KeepAliveTimer(100, LocalTime.now().plusSeconds(1), robot, displayModes);

        // Run the KeepAliveTimer
        keepAliveTimer.run();

        // Verify that delay and mouseMove methods were called on the Robot mock
        verify(robot, atLeastOnce()).delay(anyInt());
        verify(robot, atLeastOnce()).mouseMove(anyInt(), anyInt());
    }
}