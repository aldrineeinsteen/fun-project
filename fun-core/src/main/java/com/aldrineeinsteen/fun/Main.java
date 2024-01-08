package com.aldrineeinsteen.fun;

import com.aldrineeinsteen.fun.options.GlobalInputListener;
import com.aldrineeinsteen.fun.options.PluginTemplate;
import com.aldrineeinsteen.fun.options.helper.PluginRepository;
import org.apache.commons.cli.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

public class Main {

    private static final PluginRepository pluginRepository = new PluginRepository();

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, AWTException, ParseException {
        pluginRepository.init();

        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(System.in, System.out)
                .build();
        terminal.enterRawMode();
        GlobalInputListener globalInputListener = new GlobalInputListener();
        globalInputListener.registerHook();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(PluginRepository.getOptions(), args);
        } catch (ParseException e) {
            formatter.printHelp("fun project", PluginRepository.getOptions());
            logger.error("Invalid command line arguments: ", e);
            throw e;
        }

        // Execute the action for each loaded plugin if needed
        pluginRepository.getLoadedPlugins().forEach(pluginName -> {
            //Object plugin = PluginRepository.getPlugin(pluginName);
            Runnable plugin = PluginRepository.getUtility(pluginName);
            if (plugin instanceof Runnable) {
                new Thread((Runnable) plugin).start();
            }
        });

        // Additional command line options processing
    }
}
