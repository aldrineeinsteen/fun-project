package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.DashboardRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles the actual rendering of dashboard content with ANSI formatting.
 */
public class DashboardDisplayRenderer {
    private static final Logger logger = LoggerFactory.getLogger(DashboardDisplayRenderer.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // ANSI escape codes for terminal control
    private static final String CLEAR_SCREEN = "\033[H\033[2J";
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String CYAN = "\033[36m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";

    private final DashboardGridLayout gridLayout = new DashboardGridLayout();

    /**
     * Render the complete dashboard
     */
    public String renderDashboard(String projectVersion, Map<String, DashboardRenderer> renderers) {
        StringBuilder dashboard = new StringBuilder();
        
        // Clear screen and header
        dashboard.append(CLEAR_SCREEN);
        appendHeader(dashboard, projectVersion);
        
        // Organize and render grid
        Map<Integer, Map<Integer, DashboardRenderer>> grid = gridLayout.organizeRenderersIntoGrid(renderers);
        
        logger.debug("Rendering dashboard with {} registered renderers in grid layout", renderers.size());
        
        if (!grid.isEmpty()) {
            renderGrid(dashboard, grid);
        }
        
        // Footer
        appendFooter(dashboard);
        
        return dashboard.toString();
    }

    /**
     * Append dashboard header
     */
    private void appendHeader(StringBuilder dashboard, String projectVersion) {
        dashboard.append(BOLD).append(CYAN);
        dashboard.append("FunProject v").append(projectVersion);
        dashboard.append(RESET).append("\n");
        dashboard.append("─".repeat(80)).append("\n");
        
        // Current time
        dashboard.append(YELLOW).append("Current Time: ").append(RESET);
        dashboard.append(LocalDateTime.now().format(TIME_FORMATTER)).append("\n\n");
    }

    /**
     * Append dashboard footer
     */
    private void appendFooter(StringBuilder dashboard) {
        dashboard.append("\n");
        dashboard.append("─".repeat(80)).append("\n");
        dashboard.append(YELLOW).append("Press Ctrl+C to exit").append(RESET).append("\n");
    }

    /**
     * Render the grid with box-drawing characters and dividers
     */
    private void renderGrid(StringBuilder dashboard, Map<Integer, Map<Integer, DashboardRenderer>> grid) {
        int columnWidth = 38; // Width for each column
        
        for (Map.Entry<Integer, Map<Integer, DashboardRenderer>> rowEntry : grid.entrySet()) {
            Map<Integer, DashboardRenderer> columns = rowEntry.getValue();
            int numColumns = columns.size();
            
            renderGridBorders(dashboard, numColumns, columnWidth, true);
            renderPluginHeaders(dashboard, columns, columnWidth);
            renderGridBorders(dashboard, numColumns, columnWidth, false);
            renderPluginData(dashboard, columns, columnWidth);
            renderGridBorders(dashboard, numColumns, columnWidth, true, true);
        }
    }

    /**
     * Render grid borders (top, middle, or bottom)
     */
    private void renderGridBorders(StringBuilder dashboard, int numColumns, int columnWidth, 
                                 boolean isTopOrBottom) {
        renderGridBorders(dashboard, numColumns, columnWidth, isTopOrBottom, false);
    }

    private void renderGridBorders(StringBuilder dashboard, int numColumns, int columnWidth, 
                                 boolean isTopOrBottom, boolean isBottom) {
        if (isBottom) {
            dashboard.append("└");
        } else if (isTopOrBottom) {
            dashboard.append("┌");
        } else {
            dashboard.append("├");
        }
        
        for (int i = 1; i <= numColumns; i++) {
            dashboard.append("─".repeat(columnWidth));
            if (i < numColumns) {
                if (isBottom) {
                    dashboard.append("┴");
                } else if (isTopOrBottom) {
                    dashboard.append("┬");
                } else {
                    dashboard.append("┼");
                }
            }
        }
        
        if (isBottom) {
            dashboard.append("┘\n");
        } else if (isTopOrBottom) {
            dashboard.append("┐\n");
        } else {
            dashboard.append("┤\n");
        }
    }

    /**
     * Render plugin name headers
     */
    private void renderPluginHeaders(StringBuilder dashboard, Map<Integer, DashboardRenderer> columns, 
                                   int columnWidth) {
        dashboard.append("│");
        for (Map.Entry<Integer, DashboardRenderer> colEntry : columns.entrySet()) {
            DashboardRenderer renderer = colEntry.getValue();
            String pluginName = renderer.getDashboardPluginName();
            dashboard.append(" ").append(BOLD).append(CYAN).append(pluginName).append(RESET);
            int padding = columnWidth - pluginName.length() - 1;
            dashboard.append(" ".repeat(Math.max(0, padding))).append("│");
        }
        dashboard.append("\n");
    }

    /**
     * Render plugin data content
     */
    private void renderPluginData(StringBuilder dashboard, Map<Integer, DashboardRenderer> columns, 
                                int columnWidth) {
        // Collect data from all columns
        List<List<String>> columnData = new ArrayList<>();
        int maxRows = 0;
        
        for (Map.Entry<Integer, DashboardRenderer> colEntry : columns.entrySet()) {
            DashboardRenderer renderer = colEntry.getValue();
            List<String> lines = collectRendererData(renderer);
            columnData.add(lines);
            maxRows = Math.max(maxRows, lines.size());
        }
        
        // Render data rows
        for (int lineIdx = 0; lineIdx < maxRows; lineIdx++) {
            dashboard.append("│");
            for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
                List<String> lines = columnData.get(colIdx);
                String line = lineIdx < lines.size() ? lines.get(lineIdx) : "";
                
                // Calculate visible length (excluding ANSI codes)
                int visibleLength = line.replaceAll("\\033\\[[0-9;]+m", "").length();
                int padding = columnWidth - visibleLength - 1;
                
                dashboard.append(" ").append(line);
                dashboard.append(" ".repeat(Math.max(0, padding))).append("│");
            }
            dashboard.append("\n");
        }
    }

    /**
     * Collect data from a single renderer
     */
    private List<String> collectRendererData(DashboardRenderer renderer) {
        List<String> lines = new ArrayList<>();
        
        try {
            Map<String, String> data = renderer.getDashboardData();
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, String> dataEntry : data.entrySet()) {
                    String line = GREEN + dataEntry.getKey() + ": " + RESET + dataEntry.getValue();
                    lines.add(line);
                }
            }
        } catch (Exception e) {
            logger.error("Error rendering dashboard for plugin", e);
            lines.add(GREEN + "Error: " + RESET + "Failed to render");
        }
        
        return lines;
    }
}

// Made with Bob
