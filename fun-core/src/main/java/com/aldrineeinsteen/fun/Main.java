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
package com.aldrineeinsteen.fun;

import com.aldrineeinsteen.fun.options.GlobalInputListener;
import com.aldrineeinsteen.fun.options.PluginTemplate;
import com.aldrineeinsteen.fun.options.helper.DashboardInitializer;
import com.aldrineeinsteen.fun.options.helper.PluginRepository;
import org.apache.commons.cli.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static final PluginRepository pluginRepository = new PluginRepository();
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, ParseException {
        // Initialize plugin repository
        pluginRepository.init();

        // Parse command line arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(PluginRepository.getOptions(), args);
            
            // Handle help option - exit immediately after showing structured help
            if (cmd.hasOption("h")) {
                System.out.println(PluginRepository.generateStructuredHelp());
                return;
            }
            
        } catch (ParseException e) {
            System.out.println(PluginRepository.generateStructuredHelp());
            logger.error("Invalid command line arguments: ", e);
            throw e;
        }

        // Set up global input listener for keyboard shortcuts (always needed)
        GlobalInputListener globalInputListener = new GlobalInputListener();
        globalInputListener.registerHook();
        
        // Set up terminal if not in dashboard mode
        boolean dashboardEnabled = cmd.hasOption("dash");
        if (!dashboardEnabled) {
            Terminal terminal = TerminalBuilder.builder()
                    .system(false)
                    .streams(System.in, System.out)
                    .build();
            terminal.enterRawMode();
        }

        // Load and start plugins based on command line options
        loadAndStartPlugins(cmd);
        
        // Initialize and start dashboard if enabled
        if (dashboardEnabled) {
            DashboardInitializer dashboardInitializer = new DashboardInitializer();
            dashboardInitializer.initialize();
            dashboardInitializer.registerPlugins();
            dashboardInitializer.start();
        }
    }
    
    /**
     * Load and start plugins based on command line options.
     * This method normalizes plugin loading regardless of dashboard mode.
     */
    private static void loadAndStartPlugins(CommandLine cmd) {
        PluginRepository.getLoadedPlugins().forEach(pluginName -> {
            // Determine if this plugin should be started based on command line options
            boolean shouldStart = shouldStartPlugin(pluginName, cmd);
            
            if (!shouldStart) {
                logger.debug("Plugin {} not started - option not provided", pluginName);
                return;
            }
            
            logger.info("Starting plugin: {}", pluginName);
            
            // Start the utility thread if it's a utility
            Runnable utility = PluginRepository.getUtility(pluginName);
            if (utility != null) {
                new Thread(utility).start();
            }
            
            // Start regular plugins (call their start() method)
            PluginTemplate plugin = PluginRepository.getPlugin(pluginName);
            if (plugin != null) {
                plugin.start();
            }
        });
    }
    
    /**
     * Determine if a plugin should be started based on command line options.
     * Add new plugin checks here as needed.
     */
    private static boolean shouldStartPlugin(String pluginName, CommandLine cmd) {
        // KeepAliveTimer: check for -k or --keep-alive
        if (pluginName.contains("KeepAliveTimer") && cmd.hasOption("k")) {
            return true;
        }
        
        // SignatureSelector: check for --sign
        if (pluginName.contains("SignatureSelector") && cmd.hasOption("sign")) {
            return true;
        }
        
        // Add more plugin checks here as needed
        
        return false;
    }
}

// Made with Bob
