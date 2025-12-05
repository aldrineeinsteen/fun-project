package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.DashboardRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardDisplayRenderer class
 */
public class DashboardDisplayRendererTest {

    private DashboardDisplayRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new DashboardDisplayRenderer();
    }

    @Test
    void testRenderDashboard_EmptyRenderers() {
        Map<String, DashboardRenderer> emptyRenderers = new HashMap<>();
        
        String result = renderer.renderDashboard("1.0.0", emptyRenderers);
        
        assertNotNull(result);
        assertTrue(result.contains("FunProject v1.0.0"));
        assertTrue(result.contains("Current Time:"));
        assertTrue(result.contains("Press Ctrl+C to exit"));
        assertTrue(result.startsWith("\033[H\033[2J")); // Clear screen
    }

    @Test
    void testRenderDashboard_SingleRenderer() {
        DashboardRenderer mockRenderer = mock(DashboardRenderer.class);
        when(mockRenderer.getDashboardRow()).thenReturn(0);
        when(mockRenderer.getDashboardColumn()).thenReturn(0);
        when(mockRenderer.getDashboardPluginName()).thenReturn("TestPlugin");
        
        Map<String, String> data = new LinkedHashMap<>();
        data.put("Status", "Active");
        data.put("Count", "5");
        when(mockRenderer.getDashboardData()).thenReturn(data);
        
        Map<String, DashboardRenderer> renderers = new HashMap<>();
        renderers.put("TestPlugin", mockRenderer);
        
        String result = renderer.renderDashboard("1.0.0", renderers);
        
        assertNotNull(result);
        assertTrue(result.contains("TestPlugin"));
        assertTrue(result.contains("Status"));
        assertTrue(result.contains("Active"));
        assertTrue(result.contains("Count"));
        assertTrue(result.contains("5"));
        
        // Verify grid borders are present
        assertTrue(result.contains("┌"));
        assertTrue(result.contains("└"));
        assertTrue(result.contains("│"));
    }

    @Test
    void testRenderDashboard_MultipleRenderersInRow() {
        DashboardRenderer renderer1 = mock(DashboardRenderer.class);
        when(renderer1.getDashboardRow()).thenReturn(0);
        when(renderer1.getDashboardColumn()).thenReturn(0);
        when(renderer1.getDashboardPluginName()).thenReturn("Plugin1");
        when(renderer1.getDashboardData()).thenReturn(Map.of("Key1", "Value1"));
        
        DashboardRenderer renderer2 = mock(DashboardRenderer.class);
        when(renderer2.getDashboardRow()).thenReturn(0);
        when(renderer2.getDashboardColumn()).thenReturn(1);
        when(renderer2.getDashboardPluginName()).thenReturn("Plugin2");
        when(renderer2.getDashboardData()).thenReturn(Map.of("Key2", "Value2"));
        
        Map<String, DashboardRenderer> renderers = new LinkedHashMap<>();
        renderers.put("Plugin1", renderer1);
        renderers.put("Plugin2", renderer2);
        
        String result = this.renderer.renderDashboard("1.0.0", renderers);
        
        assertNotNull(result);
        assertTrue(result.contains("Plugin1"));
        assertTrue(result.contains("Plugin2"));
        assertTrue(result.contains("Value1"));
        assertTrue(result.contains("Value2"));
        
        // Verify column dividers
        assertTrue(result.contains("┬")); // Top divider
        assertTrue(result.contains("┴")); // Bottom divider
    }

    @Test
    void testRenderDashboard_MultipleRows() {
        DashboardRenderer renderer1 = mock(DashboardRenderer.class);
        when(renderer1.getDashboardRow()).thenReturn(0);
        when(renderer1.getDashboardColumn()).thenReturn(0);
        when(renderer1.getDashboardPluginName()).thenReturn("Row0Plugin");
        when(renderer1.getDashboardData()).thenReturn(Map.of("Data", "Row0"));
        
        DashboardRenderer renderer2 = mock(DashboardRenderer.class);
        when(renderer2.getDashboardRow()).thenReturn(1);
        when(renderer2.getDashboardColumn()).thenReturn(0);
        when(renderer2.getDashboardPluginName()).thenReturn("Row1Plugin");
        when(renderer2.getDashboardData()).thenReturn(Map.of("Data", "Row1"));
        
        Map<String, DashboardRenderer> renderers = new LinkedHashMap<>();
        renderers.put("Row0Plugin", renderer1);
        renderers.put("Row1Plugin", renderer2);
        
        String result = this.renderer.renderDashboard("1.0.0", renderers);
        
        assertNotNull(result);
        assertTrue(result.contains("Row0Plugin"));
        assertTrue(result.contains("Row1Plugin"));
        assertTrue(result.contains("Row0"));
        assertTrue(result.contains("Row1"));
    }

    @Test
    void testRenderDashboard_WithNullData() {
        DashboardRenderer mockRenderer = mock(DashboardRenderer.class);
        when(mockRenderer.getDashboardRow()).thenReturn(0);
        when(mockRenderer.getDashboardColumn()).thenReturn(0);
        when(mockRenderer.getDashboardPluginName()).thenReturn("NullDataPlugin");
        when(mockRenderer.getDashboardData()).thenReturn(null);
        
        Map<String, DashboardRenderer> renderers = new HashMap<>();
        renderers.put("NullDataPlugin", mockRenderer);
        
        String result = this.renderer.renderDashboard("1.0.0", renderers);
        
        assertNotNull(result);
        assertTrue(result.contains("NullDataPlugin"));
        // Should not crash, just render empty data section
    }

    @Test
    void testRenderDashboard_WithEmptyData() {
        DashboardRenderer mockRenderer = mock(DashboardRenderer.class);
        when(mockRenderer.getDashboardRow()).thenReturn(0);
        when(mockRenderer.getDashboardColumn()).thenReturn(0);
        when(mockRenderer.getDashboardPluginName()).thenReturn("EmptyDataPlugin");
        when(mockRenderer.getDashboardData()).thenReturn(new HashMap<>());
        
        Map<String, DashboardRenderer> renderers = new HashMap<>();
        renderers.put("EmptyDataPlugin", mockRenderer);
        
        String result = this.renderer.renderDashboard("1.0.0", renderers);
        
        assertNotNull(result);
        assertTrue(result.contains("EmptyDataPlugin"));
    }

    @Test
    void testRenderDashboard_WithException() {
        DashboardRenderer mockRenderer = mock(DashboardRenderer.class);
        when(mockRenderer.getDashboardRow()).thenReturn(0);
        when(mockRenderer.getDashboardColumn()).thenReturn(0);
        when(mockRenderer.getDashboardPluginName()).thenReturn("ErrorPlugin");
        when(mockRenderer.getDashboardData()).thenThrow(new RuntimeException("Test error"));
        
        Map<String, DashboardRenderer> renderers = new HashMap<>();
        renderers.put("ErrorPlugin", mockRenderer);
        
        String result = this.renderer.renderDashboard("1.0.0", renderers);
        
        assertNotNull(result);
        assertTrue(result.contains("ErrorPlugin"));
        assertTrue(result.contains("Error:") || result.contains("Failed to render"));
    }

    @Test
    void testRenderDashboard_ContainsAnsiCodes() {
        DashboardRenderer mockRenderer = mock(DashboardRenderer.class);
        when(mockRenderer.getDashboardRow()).thenReturn(0);
        when(mockRenderer.getDashboardColumn()).thenReturn(0);
        when(mockRenderer.getDashboardPluginName()).thenReturn("ColorPlugin");
        when(mockRenderer.getDashboardData()).thenReturn(Map.of("Key", "Value"));
        
        Map<String, DashboardRenderer> renderers = new HashMap<>();
        renderers.put("ColorPlugin", mockRenderer);
        
        String result = this.renderer.renderDashboard("1.0.0", renderers);
        
        assertNotNull(result);
        // Check for ANSI color codes
        assertTrue(result.contains("\033["));
        assertTrue(result.contains("\033[0m")); // Reset
        assertTrue(result.contains("\033[1m")); // Bold
        assertTrue(result.contains("\033[36m")); // Cyan
    }

    @Test
    void testRenderDashboard_VersionInHeader() {
        Map<String, DashboardRenderer> emptyRenderers = new HashMap<>();
        
        String result = renderer.renderDashboard("2.5.10", emptyRenderers);
        
        assertNotNull(result);
        assertTrue(result.contains("FunProject v2.5.10"));
    }

    @Test
    void testRenderDashboard_ComplexGrid() {
        // Create a 2x2 grid
        DashboardRenderer r00 = createMockRenderer(0, 0, "Plugin00", Map.of("A", "1"));
        DashboardRenderer r01 = createMockRenderer(0, 1, "Plugin01", Map.of("B", "2"));
        DashboardRenderer r10 = createMockRenderer(1, 0, "Plugin10", Map.of("C", "3"));
        DashboardRenderer r11 = createMockRenderer(1, 1, "Plugin11", Map.of("D", "4"));
        
        Map<String, DashboardRenderer> renderers = new LinkedHashMap<>();
        renderers.put("Plugin00", r00);
        renderers.put("Plugin01", r01);
        renderers.put("Plugin10", r10);
        renderers.put("Plugin11", r11);
        
        String result = this.renderer.renderDashboard("1.0.0", renderers);
        
        assertNotNull(result);
        assertTrue(result.contains("Plugin00"));
        assertTrue(result.contains("Plugin01"));
        assertTrue(result.contains("Plugin10"));
        assertTrue(result.contains("Plugin11"));
        
        // Verify all data is present
        assertTrue(result.contains("1"));
        assertTrue(result.contains("2"));
        assertTrue(result.contains("3"));
        assertTrue(result.contains("4"));
    }

    @Test
    void testRenderDashboard_UnevenDataRows() {
        // One renderer with more data than the other
        Map<String, String> data1 = new LinkedHashMap<>();
        data1.put("Key1", "Value1");
        data1.put("Key2", "Value2");
        data1.put("Key3", "Value3");
        
        Map<String, String> data2 = new LinkedHashMap<>();
        data2.put("KeyA", "ValueA");
        
        DashboardRenderer renderer1 = createMockRenderer(0, 0, "Plugin1", data1);
        DashboardRenderer renderer2 = createMockRenderer(0, 1, "Plugin2", data2);
        
        Map<String, DashboardRenderer> renderers = new LinkedHashMap<>();
        renderers.put("Plugin1", renderer1);
        renderers.put("Plugin2", renderer2);
        
        String result = this.renderer.renderDashboard("1.0.0", renderers);
        
        assertNotNull(result);
        assertTrue(result.contains("Value1"));
        assertTrue(result.contains("Value2"));
        assertTrue(result.contains("Value3"));
        assertTrue(result.contains("ValueA"));
    }

    private DashboardRenderer createMockRenderer(int row, int col, String name, Map<String, String> data) {
        DashboardRenderer mock = mock(DashboardRenderer.class);
        when(mock.getDashboardRow()).thenReturn(row);
        when(mock.getDashboardColumn()).thenReturn(col);
        when(mock.getDashboardPluginName()).thenReturn(name);
        when(mock.getDashboardData()).thenReturn(data);
        return mock;
    }
}

// Made with Bob