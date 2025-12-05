/*
 * Copyright 2017-2025 Aldrine Einsteen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aldrineeinsteen.fun.options;

import java.util.Map;

/**
 * Interface for plugins to contribute to the dashboard display.
 * Each plugin can provide key-value pairs that will be rendered in the dashboard.
 */
public interface DashboardRenderer {
    
    /**
     * Get the dashboard data to be displayed.
     * The map keys represent the field names and values represent the current values.
     *
     * @return Map of field names to their current values
     */
    Map<String, String> getDashboardData();
    
    /**
     * Get the display position/order for this plugin's dashboard section.
     * Lower numbers appear first.
     *
     * @return position value (default: 100)
     */
    default int getDashboardPosition() {
        return 100;
    }
    
    /**
     * Get the column number for grid layout (1-based).
     * Column 1 is leftmost, column 2 is to the right, etc.
     *
     * @return column number (default: 1)
     */
    default int getDashboardColumn() {
        return 1;
    }
    
    /**
     * Get the row number for grid layout (1-based).
     * Row 1 is topmost, row 2 is below, etc.
     *
     * @return row number (default: 1)
     */
    default int getDashboardRow() {
        return 1;
    }
    
    /**
     * Get the plugin name for dashboard display.
     *
     * @return plugin display name
     */
    default String getDashboardPluginName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Check if this plugin should be shown in the dashboard.
     *
     * @return true if plugin should be displayed
     */
    default boolean isDashboardEnabled() {
        return true;
    }
}

// Made with Bob
