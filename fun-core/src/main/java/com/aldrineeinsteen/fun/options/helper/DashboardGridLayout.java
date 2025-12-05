package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.DashboardRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

/**
 * Manages grid layout organization for dashboard renderers.
 */
public class DashboardGridLayout {
    private static final Logger logger = LoggerFactory.getLogger(DashboardGridLayout.class);

    /**
     * Organize renderers into a grid structure based on their column and row positions
     */
    public Map<Integer, Map<Integer, DashboardRenderer>> organizeRenderersIntoGrid(
            Map<String, DashboardRenderer> renderers) {
        Map<Integer, Map<Integer, DashboardRenderer>> grid = new TreeMap<>();
        
        for (Map.Entry<String, DashboardRenderer> entry : renderers.entrySet()) {
            DashboardRenderer renderer = entry.getValue();
            int row = renderer.getDashboardRow();
            int column = renderer.getDashboardColumn();
            
            grid.computeIfAbsent(row, k -> new TreeMap<>()).put(column, renderer);
        }
        
        logger.debug("Organized {} renderers into grid with {} rows", 
            renderers.size(), grid.size());
        
        return grid;
    }
}

// Made with Bob
