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
 * Unit tests for DashboardGridLayout class
 */
public class DashboardGridLayoutTest {

    private DashboardGridLayout gridLayout;

    @BeforeEach
    void setUp() {
        gridLayout = new DashboardGridLayout();
    }

    @Test
    void testOrganizeRenderersIntoGrid_EmptyRenderers() {
        Map<String, DashboardRenderer> emptyRenderers = new HashMap<>();
        
        Map<Integer, Map<Integer, DashboardRenderer>> result =
            gridLayout.organizeRenderersIntoGrid(emptyRenderers);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testOrganizeRenderersIntoGrid_SingleRenderer() {
        DashboardRenderer renderer = mock(DashboardRenderer.class);
        when(renderer.getDashboardRow()).thenReturn(0);
        when(renderer.getDashboardColumn()).thenReturn(0);
        
        Map<String, DashboardRenderer> renderers = new HashMap<>();
        renderers.put("Renderer1", renderer);
        
        Map<Integer, Map<Integer, DashboardRenderer>> result =
            gridLayout.organizeRenderersIntoGrid(renderers);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(0));
        assertEquals(1, result.get(0).size());
        assertSame(renderer, result.get(0).get(0));
    }

    @Test
    void testOrganizeRenderersIntoGrid_MultipleRenderersInSameRow() {
        DashboardRenderer renderer1 = mock(DashboardRenderer.class);
        when(renderer1.getDashboardRow()).thenReturn(0);
        when(renderer1.getDashboardColumn()).thenReturn(0);
        
        DashboardRenderer renderer2 = mock(DashboardRenderer.class);
        when(renderer2.getDashboardRow()).thenReturn(0);
        when(renderer2.getDashboardColumn()).thenReturn(1);
        
        Map<String, DashboardRenderer> renderers = new LinkedHashMap<>();
        renderers.put("Renderer1", renderer1);
        renderers.put("Renderer2", renderer2);
        
        Map<Integer, Map<Integer, DashboardRenderer>> result =
            gridLayout.organizeRenderersIntoGrid(renderers);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).size());
        assertSame(renderer1, result.get(0).get(0));
        assertSame(renderer2, result.get(0).get(1));
    }

    @Test
    void testOrganizeRenderersIntoGrid_MultipleRows() {
        DashboardRenderer renderer1 = mock(DashboardRenderer.class);
        when(renderer1.getDashboardRow()).thenReturn(0);
        when(renderer1.getDashboardColumn()).thenReturn(0);
        
        DashboardRenderer renderer2 = mock(DashboardRenderer.class);
        when(renderer2.getDashboardRow()).thenReturn(1);
        when(renderer2.getDashboardColumn()).thenReturn(0);
        
        Map<String, DashboardRenderer> renderers = new LinkedHashMap<>();
        renderers.put("Renderer1", renderer1);
        renderers.put("Renderer2", renderer2);
        
        Map<Integer, Map<Integer, DashboardRenderer>> result =
            gridLayout.organizeRenderersIntoGrid(renderers);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(0));
        assertTrue(result.containsKey(1));
        assertSame(renderer1, result.get(0).get(0));
        assertSame(renderer2, result.get(1).get(0));
    }

    @Test
    void testOrganizeRenderersIntoGrid_ComplexGrid() {
        // Create a 2x2 grid
        DashboardRenderer r00 = mock(DashboardRenderer.class);
        when(r00.getDashboardRow()).thenReturn(0);
        when(r00.getDashboardColumn()).thenReturn(0);
        
        DashboardRenderer r01 = mock(DashboardRenderer.class);
        when(r01.getDashboardRow()).thenReturn(0);
        when(r01.getDashboardColumn()).thenReturn(1);
        
        DashboardRenderer r10 = mock(DashboardRenderer.class);
        when(r10.getDashboardRow()).thenReturn(1);
        when(r10.getDashboardColumn()).thenReturn(0);
        
        DashboardRenderer r11 = mock(DashboardRenderer.class);
        when(r11.getDashboardRow()).thenReturn(1);
        when(r11.getDashboardColumn()).thenReturn(1);
        
        Map<String, DashboardRenderer> renderers = new LinkedHashMap<>();
        renderers.put("R00", r00);
        renderers.put("R01", r01);
        renderers.put("R10", r10);
        renderers.put("R11", r11);
        
        Map<Integer, Map<Integer, DashboardRenderer>> result =
            gridLayout.organizeRenderersIntoGrid(renderers);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).size());
        assertEquals(2, result.get(1).size());
        
        assertSame(r00, result.get(0).get(0));
        assertSame(r01, result.get(0).get(1));
        assertSame(r10, result.get(1).get(0));
        assertSame(r11, result.get(1).get(1));
    }

    @Test
    void testOrganizeRenderersIntoGrid_SortsByRowAndColumn() {
        // Add renderers in random order
        DashboardRenderer r11 = mock(DashboardRenderer.class);
        when(r11.getDashboardRow()).thenReturn(1);
        when(r11.getDashboardColumn()).thenReturn(1);
        
        DashboardRenderer r00 = mock(DashboardRenderer.class);
        when(r00.getDashboardRow()).thenReturn(0);
        when(r00.getDashboardColumn()).thenReturn(0);
        
        DashboardRenderer r10 = mock(DashboardRenderer.class);
        when(r10.getDashboardRow()).thenReturn(1);
        when(r10.getDashboardColumn()).thenReturn(0);
        
        Map<String, DashboardRenderer> renderers = new LinkedHashMap<>();
        renderers.put("R11", r11);
        renderers.put("R00", r00);
        renderers.put("R10", r10);
        
        Map<Integer, Map<Integer, DashboardRenderer>> result =
            gridLayout.organizeRenderersIntoGrid(renderers);
        
        // TreeMap should sort by keys
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify rows are sorted
        var rowIterator = result.keySet().iterator();
        assertEquals(0, rowIterator.next());
        assertEquals(1, rowIterator.next());
        
        // Verify columns are sorted within rows
        var row1Columns = result.get(1).keySet().iterator();
        assertEquals(0, row1Columns.next());
        assertEquals(1, row1Columns.next());
    }
}

// Made with Bob