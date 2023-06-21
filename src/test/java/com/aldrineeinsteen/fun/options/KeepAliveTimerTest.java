package com.aldrineeinsteen.fun.options;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;
import java.time.LocalTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeepAliveTimerTest {

    @Mock
    Robot robot;

    @Mock
    DisplayMode displayMode;

    @Test
    public void testRun() {
        when(displayMode.getWidth()).thenReturn(800);
        when(displayMode.getHeight()).thenReturn(600);

        KeepAliveTimer keepAliveTimer = new KeepAliveTimer(LocalTime.now().plusSeconds(1), robot, displayMode);
        keepAliveTimer.run();

        verify(robot, atLeastOnce()).delay(anyInt());
        verify(robot, atLeastOnce()).mouseMove(anyInt(), anyInt());
    }
}
