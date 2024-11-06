package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeepAliveTimerTest {

    final Robot robot = Mockito.mock(Robot.class);
    final DisplayModeWrapper displayMode = Mockito.mock(DisplayModeWrapper.class);

    @Test
    public void testRun() {
        // Mock display mode behavior
        when(displayMode.getWidth()).thenReturn(800);
        when(displayMode.getHeight()).thenReturn(800);

        // Create a list of display modes
        List<DisplayModeWrapper> displayModes = Collections.singletonList(displayMode);

        // Initialize KeepAliveTimer with the list of display modes
        KeepAliveTimer keepAliveTimer = new KeepAliveTimer(100, LocalTime.now().plusSeconds(1), robot, displayModes);
        keepAliveTimer.run();

        // Verify that the delay and mouseMove were called at least once
        verify(robot, atLeastOnce()).delay(anyInt());
        verify(robot, atLeastOnce()).mouseMove(anyInt(), anyInt());
    }
}