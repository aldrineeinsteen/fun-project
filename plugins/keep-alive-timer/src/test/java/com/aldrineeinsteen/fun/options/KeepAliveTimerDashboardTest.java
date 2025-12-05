package com.aldrineeinsteen.fun.options;

import org.junit.jupiter.api.Test;
import java.awt.AWTException;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KeepAliveTimerDashboardTest {
    
    @Test
    public void testGetDashboardData() throws AWTException {
        // Create a KeepAliveTimer instance with end time in the future
        LocalTime futureTime = LocalTime.now().plusHours(2);
        KeepAliveTimer timer = new KeepAliveTimer(30000, futureTime);
        
        // Get dashboard data
        Map<String, String> data = timer.getDashboardData();
        
        // Verify data is not null and not empty
        assertNotNull(data, "Dashboard data should not be null");
        assertFalse(data.isEmpty(), "Dashboard data should not be empty");
        
        // Verify expected keys are present
        assertTrue(data.containsKey("End Time"), "Should contain End Time");
        assertTrue(data.containsKey("Time Remaining"), "Should contain Time Remaining");
        assertTrue(data.containsKey("Next Move"), "Should contain Next Move");
        assertTrue(data.containsKey("Mode"), "Should contain Mode");
        assertTrue(data.containsKey("Status"), "Should contain Status");
        assertTrue(data.containsKey("Activity"), "Should contain Activity");
        
        // Verify some values
        assertEquals("jitter", data.get("Mode"));
        assertEquals("Stopped", data.get("Status")); // Should be stopped initially
        
        System.out.println("Dashboard Data:");
        data.forEach((key, value) -> System.out.println("  " + key + ": " + value));
    }
    
    @Test
    public void testIsDashboardEnabled() throws AWTException {
        KeepAliveTimer timer = new KeepAliveTimer();
        
        // Initially should be disabled (default)
        assertFalse(timer.isDashboardEnabled(), "Dashboard should be disabled by default");
        
        // Enable it
        timer.setDashboardEnabled(true);
        assertTrue(timer.isDashboardEnabled(), "Dashboard should be enabled after setting");
        
        // Disable it
        timer.setDashboardEnabled(false);
        assertFalse(timer.isDashboardEnabled(), "Dashboard should be disabled after setting");
    }
}

// Made with Bob
