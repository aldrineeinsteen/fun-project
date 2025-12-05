package com.aldrineeinsteen.fun.options;

import java.util.HashMap;
import java.util.Map;

public abstract class UtilityTemplate implements Runnable, DashboardRenderer {
    
    private boolean dashboardEnabled = false;
    private int dashboardPosition = 100;
    private int dashboardColumn = 1;
    private int dashboardRow = 1;

    @Override
    public void run() {
        logStart();
        runUtility();
    }

    protected abstract void logStart();

    protected abstract void runUtility();
    
    /**
     * Enable or disable dashboard rendering for this utility
     */
    public void setDashboardEnabled(boolean enabled) {
        this.dashboardEnabled = enabled;
    }
    
    /**
     * Set the dashboard display position
     */
    public void setDashboardPosition(int position) {
        this.dashboardPosition = position;
    }
    
    /**
     * Set the dashboard column for grid layout
     */
    public void setDashboardColumn(int column) {
        this.dashboardColumn = column;
    }
    
    /**
     * Set the dashboard row for grid layout
     */
    public void setDashboardRow(int row) {
        this.dashboardRow = row;
    }
    
    @Override
    public boolean isDashboardEnabled() {
        return dashboardEnabled;
    }
    
    @Override
    public int getDashboardPosition() {
        return dashboardPosition;
    }
    
    @Override
    public int getDashboardColumn() {
        return dashboardColumn;
    }
    
    @Override
    public int getDashboardRow() {
        return dashboardRow;
    }
    
    @Override
    public String getDashboardPluginName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Default implementation returns empty map.
     * Subclasses should override to provide actual dashboard data.
     */
    @Override
    public Map<String, String> getDashboardData() {
        return new HashMap<>();
    }
}
