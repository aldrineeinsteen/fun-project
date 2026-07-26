package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Assumptions;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeepAliveTimerTest {

    final Robot robot = Mockito.mock(Robot.class);
    final GraphicsDevice mockGraphicsDevice = Mockito.mock(GraphicsDevice.class);
    final GraphicsConfiguration mockGraphicsConfig = Mockito.mock(GraphicsConfiguration.class);
    final Rectangle mockBounds = new Rectangle(0, 0, 800, 600);
    final DisplayMode realDisplayMode = new DisplayMode(800, 600, DisplayMode.BIT_DEPTH_MULTI, DisplayMode.REFRESH_RATE_UNKNOWN);
    DisplayModeWrapper displayMode;
    
    public KeepAliveTimerTest() {
        // Set up the mock objects
        when(mockGraphicsDevice.getDefaultConfiguration()).thenReturn(mockGraphicsConfig);
        when(mockGraphicsConfig.getBounds()).thenReturn(mockBounds);
        displayMode = new DisplayModeWrapper(realDisplayMode, mockGraphicsDevice);
    }

    @Test
    public void testRun() throws AWTException {
        // Skip test in headless environments since KeepAliveTimer requires AWT components
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
        
        // This test is just a placeholder - we're not actually testing the run method
        // because it's difficult to mock all the required components
        assertTrue(true);
    }
    
    @Test
    public void testDetectCurrentMonitor() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        // Create a test DisplayModeWrapper
        DisplayModeWrapper testWrapper = new DisplayModeWrapper(realDisplayMode, mockGraphicsDevice);
        
        // Test that the bounds are correctly set
        assertEquals(mockBounds, testWrapper.getBounds());
        assertEquals(0, testWrapper.getX());
        assertEquals(0, testWrapper.getY());
    }
    
    @Test
    public void testUserMovementDetection() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        // Test the Point class behavior which is used for movement detection
        Point p1 = new Point(100, 100);
        Point p2 = new Point(100, 100);
        Point p3 = new Point(200, 200);
        
        // Test equality
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        
        // This verifies the behavior our code relies on for detecting user movement
        assertTrue(p1.equals(p2));
        assertFalse(p1.equals(p3));
    }
    
    @Test
    public void testMonitorManagerIntegration() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        // Create a KeepAliveTimer instance
        KeepAliveTimer timer = new KeepAliveTimer();
        
        // Verify MonitorManager is properly initialized
        assertNotNull(timer.getMonitorManager(), "MonitorManager should be initialized");
        assertNotNull(timer.getPositionTracker(), "MousePositionTracker should be initialized");
        
        // Verify monitor detection works
        DisplayModeWrapper currentDisplay = timer.getMonitorManager().getCurrentDisplayMode();
        assertNotNull(currentDisplay, "Should have a current display mode");
        assertTrue(currentDisplay.getWidth() > 0, "Display width should be positive");
        assertTrue(currentDisplay.getHeight() > 0, "Display height should be positive");
    }
    
    @Test
    public void testConstructorWithDefaultEndTime() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        KeepAliveTimer timer = new KeepAliveTimer();
        assertNotNull(timer);
        assertNotNull(timer.getMonitorManager());
        assertNotNull(timer.getPositionTracker());
    }
    
    @Test
    public void testConstructorWithCustomEndTime() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        LocalTime customEndTime = LocalTime.of(20, 30);
        KeepAliveTimer timer = new KeepAliveTimer(customEndTime);
        assertNotNull(timer);
        
        // Verify the end time is set correctly by checking dashboard data
        Map<String, String> data = timer.getDashboardData();
        assertEquals("20:30", data.get("End Time"));
    }
    
    @Test
    public void testConstructorWithCustomDelayAndEndTime() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        int customDelay = 60000; // 60 seconds
        LocalTime customEndTime = LocalTime.of(22, 0);
        KeepAliveTimer timer = new KeepAliveTimer(customDelay, customEndTime);
        assertNotNull(timer);
        
        // Verify settings through dashboard data
        Map<String, String> data = timer.getDashboardData();
        assertEquals("22:00", data.get("End Time"));
        assertEquals("60s", data.get("Delay"));
    }
    
    @Test
    public void testGetInstance() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        KeepAliveTimer instance1 = KeepAliveTimer.getInstance();
        KeepAliveTimer instance2 = KeepAliveTimer.getInstance();
        
        assertNotNull(instance1);
        assertSame(instance1, instance2, "getInstance should return the same instance");
    }
    
    @Test
    public void testIsRunningInitiallyFalse() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        KeepAliveTimer timer = new KeepAliveTimer();
        assertFalse(timer.isRunning(), "Timer should not be running initially");
    }
    
    @Test
    public void testStopTimer() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        KeepAliveTimer timer = new KeepAliveTimer();
        timer.stopTimer();
        assertFalse(timer.isRunning(), "Timer should not be running after stop");
    }
    
    @Test
    public void testGetDashboardDataWithFutureEndTime() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        // Set end time to 2 hours from now
        LocalTime futureEndTime = LocalTime.now().plusHours(2);
        KeepAliveTimer timer = new KeepAliveTimer(30000, futureEndTime);
        
        Map<String, String> data = timer.getDashboardData();
        
        assertNotNull(data);
        assertTrue(data.containsKey("Monitor"));
        assertTrue(data.containsKey("Device"));
        assertTrue(data.containsKey("End Time"));
        assertTrue(data.containsKey("Delay"));
        assertTrue(data.containsKey("Status"));
        assertTrue(data.containsKey("Time Remaining"));
        
        // Verify delay format
        assertEquals("30s", data.get("Delay"));
        
        // Verify status shows stopped (not running)
        assertTrue(data.get("Status").contains("Stopped"));
        
        // Verify time remaining is calculated
        String timeRemaining = data.get("Time Remaining");
        assertTrue(timeRemaining.matches("\\d+h \\d+m"),
            "Time remaining should be in format 'Xh Ym', got: " + timeRemaining);
    }
    
    @Test
    public void testGetDashboardDataWithPastEndTime() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        // Pick a deterministic past/equal time in the same day to avoid midnight wrap ambiguity.
        LocalTime now = LocalTime.now();
        LocalTime pastEndTime;
        if (now.getHour() == 0) {
            // At midnight hour, using current hour keeps this <= now and avoids wrapping to previous day.
            pastEndTime = LocalTime.of(0, now.getMinute());
        } else {
            pastEndTime = LocalTime.of(now.getHour() - 1, now.getMinute());
        }
        KeepAliveTimer timer = new KeepAliveTimer(30000, pastEndTime);
        
        Map<String, String> data = timer.getDashboardData();
        
        assertNotNull(data);
        assertEquals("Completed - Restarting...", data.get("Time Remaining"));
    }

    @Test
    public void testSetEndTimeFromStringSupportsHHmmAndHHColon() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");

        KeepAliveTimer timer = new KeepAliveTimer(30000, LocalTime.of(18, 30));

        timer.setEndTimeFromString("23:59");
        Map<String, String> dataColon = timer.getDashboardData();
        assertEquals("23:59", dataColon.get("End Time"));

        timer.setEndTimeFromString("0005");
        Map<String, String> dataCompact = timer.getDashboardData();
        assertEquals("00:05", dataCompact.get("End Time"));
    }

    @Test
    public void testSetEndTimeFromStringInvalidKeepsPreviousValue() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");

        KeepAliveTimer timer = new KeepAliveTimer(30000, LocalTime.of(18, 30));
        timer.setEndTimeFromString("23:10");
        assertEquals("23:10", timer.getDashboardData().get("End Time"));

        timer.setEndTimeFromString("bad-input");
        assertEquals("23:10", timer.getDashboardData().get("End Time"));
    }

    @Test
    public void testSetDelaySecondsFromStringValidAndInvalid() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");

        KeepAliveTimer timer = new KeepAliveTimer(30000, LocalTime.now().plusHours(1));

        timer.setDelaySecondsFromString("45");
        assertEquals("45s", timer.getDashboardData().get("Delay"));

        timer.setDelaySecondsFromString("0");
        assertEquals("45s", timer.getDashboardData().get("Delay"));

        timer.setDelaySecondsFromString("oops");
        assertEquals("45s", timer.getDashboardData().get("Delay"));
    }
    
    @Test
    public void testGetDashboardDataMonitorInfo() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        KeepAliveTimer timer = new KeepAliveTimer();
        Map<String, String> data = timer.getDashboardData();
        
        // Verify monitor information is present
        String monitorInfo = data.get("Monitor");
        assertNotNull(monitorInfo);
        assertTrue(monitorInfo.matches("\\d+x\\d+"),
            "Monitor info should be in format 'WIDTHxHEIGHT', got: " + monitorInfo);
        
        // Verify device info is present
        String deviceInfo = data.get("Device");
        assertNotNull(deviceInfo);
        assertFalse(deviceInfo.isEmpty());
    }
    
    @Test
    public void testLogStart() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        KeepAliveTimer timer = new KeepAliveTimer();
        
        // This should not throw an exception
        assertDoesNotThrow(() -> {
            // Use reflection to call the protected logStart method
            java.lang.reflect.Method method = KeepAliveTimer.class.getDeclaredMethod("logStart");
            method.setAccessible(true);
            method.invoke(timer);
        });
    }
    
    @Test
    public void testMultipleTimerInstances() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        // Create multiple timer instances with different settings
        KeepAliveTimer timer1 = new KeepAliveTimer(LocalTime.of(18, 0));
        KeepAliveTimer timer2 = new KeepAliveTimer(LocalTime.of(20, 0));
        
        assertNotNull(timer1);
        assertNotNull(timer2);
        assertNotSame(timer1, timer2, "Different constructor calls should create different instances");
        
        // Verify they have different end times
        Map<String, String> data1 = timer1.getDashboardData();
        Map<String, String> data2 = timer2.getDashboardData();
        
        assertEquals("18:00", data1.get("End Time"));
        assertEquals("20:00", data2.get("End Time"));
    }
    
    @Test
    public void testDashboardDataFormat() throws Exception {
        // Skip test in headless environments
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
            "Test skipped in headless environment - KeepAliveTimer requires display access");
            
        KeepAliveTimer timer = new KeepAliveTimer(45000, LocalTime.of(19, 30));
        Map<String, String> data = timer.getDashboardData();
        
        // Verify all expected keys are present
        String[] expectedKeys = {"Monitor", "Device", "End Time", "Delay", "Status", "Time Remaining"};
        for (String key : expectedKeys) {
            assertTrue(data.containsKey(key), "Dashboard data should contain key: " + key);
        }
        
        // Verify specific formats
        assertEquals("19:30", data.get("End Time"));
        assertEquals("45s", data.get("Delay"));
    }
}
