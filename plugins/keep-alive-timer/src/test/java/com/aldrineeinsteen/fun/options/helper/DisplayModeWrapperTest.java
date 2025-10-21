package com.aldrineeinsteen.fun.options.helper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class DisplayModeWrapperTest {

    @Test
    public void testGetBounds() {
        // Mock objects
        DisplayMode mockDisplayMode = Mockito.mock(DisplayMode.class);
        GraphicsDevice mockDevice = Mockito.mock(GraphicsDevice.class);
        GraphicsConfiguration mockConfig = Mockito.mock(GraphicsConfiguration.class);
        Rectangle mockBounds = new Rectangle(100, 200, 1920, 1080);
        
        // Setup mocks
        when(mockDisplayMode.getWidth()).thenReturn(1920);
        when(mockDisplayMode.getHeight()).thenReturn(1080);
        when(mockDevice.getDefaultConfiguration()).thenReturn(mockConfig);
        when(mockConfig.getBounds()).thenReturn(mockBounds);
        
        // Create wrapper
        DisplayModeWrapper wrapper = new DisplayModeWrapper(mockDisplayMode, mockDevice);
        
        // Test
        assertEquals(1920, wrapper.getWidth());
        assertEquals(1080, wrapper.getHeight());
        assertEquals(mockDevice, wrapper.getDevice());
        assertEquals(mockBounds, wrapper.getBounds());
        assertEquals(100, wrapper.getX());
        assertEquals(200, wrapper.getY());
    }
    
    @Test
    public void testEquals() {
        // Mock objects
        DisplayMode mockDisplayMode1 = Mockito.mock(DisplayMode.class);
        GraphicsDevice mockDevice1 = Mockito.mock(GraphicsDevice.class);
        DisplayMode mockDisplayMode2 = Mockito.mock(DisplayMode.class);
        GraphicsDevice mockDevice2 = Mockito.mock(GraphicsDevice.class);
        
        // Setup mocks
        when(mockDisplayMode1.getWidth()).thenReturn(1920);
        when(mockDisplayMode1.getHeight()).thenReturn(1080);
        when(mockDisplayMode2.getWidth()).thenReturn(1920);
        when(mockDisplayMode2.getHeight()).thenReturn(1080);
        
        GraphicsConfiguration mockConfig1 = Mockito.mock(GraphicsConfiguration.class);
        GraphicsConfiguration mockConfig2 = Mockito.mock(GraphicsConfiguration.class);
        when(mockDevice1.getDefaultConfiguration()).thenReturn(mockConfig1);
        when(mockDevice2.getDefaultConfiguration()).thenReturn(mockConfig2);
        when(mockConfig1.getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        when(mockConfig2.getBounds()).thenReturn(new Rectangle(1920, 0, 1920, 1080));
        
        // Create wrappers
        DisplayModeWrapper wrapper1 = new DisplayModeWrapper(mockDisplayMode1, mockDevice1);
        DisplayModeWrapper wrapper2 = new DisplayModeWrapper(mockDisplayMode2, mockDevice2);
        DisplayModeWrapper wrapper3 = new DisplayModeWrapper(mockDisplayMode1, mockDevice1);
        
        // Test
        assertEquals(wrapper1, wrapper3);
        assertNotEquals(wrapper1, wrapper2);
        assertNotEquals(wrapper1, null);
        assertNotEquals(wrapper1, "string");
    }
}

// Made with Bob
