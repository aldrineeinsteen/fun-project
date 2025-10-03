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
        pluginRepository.init();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(PluginRepository.getOptions(), args);
            
            // Handle help option - exit immediately after showing help
            if (cmd.hasOption("h")) {
                formatter.printHelp("fun project", PluginRepository.getOptions());
                return;
            }
            
        } catch (ParseException e) {
            formatter.printHelp("fun project", PluginRepository.getOptions());
            logger.error("Invalid command line arguments: ", e);
            throw e;
        }

        // Only set up terminal and listeners if we're not just showing help
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(System.in, System.out)
                .build();
        terminal.enterRawMode();
        GlobalInputListener globalInputListener = new GlobalInputListener();
        globalInputListener.registerHook();

        // Execute the action for each loaded plugin if needed
        PluginRepository.getLoadedPlugins().forEach(pluginName -> {
            //Object plugin = PluginRepository.getPlugin(pluginName);
            Runnable plugin = PluginRepository.getUtility(pluginName);
            if (plugin instanceof Runnable) {
                new Thread(plugin).start();
            }
        });

        // Additional command line options processing
    }
}
