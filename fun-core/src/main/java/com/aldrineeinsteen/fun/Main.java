package com.aldrineeinsteen.fun;

import com.aldrineeinsteen.fun.options.KeepAliveTimer;
import com.aldrineeinsteen.fun.options.SignatureSelector;
import com.aldrineeinsteen.fun.options.helper.DisplayModeWrapper;
import com.aldrineeinsteen.fun.options.helper.PluginRepository;
import com.aldrineeinsteen.fun.options.helper.TerminalParser;
import org.apache.commons.cli.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main {

    private static final PluginRepository pluginRepository = new PluginRepository();

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws AWTException, IOException, ParseException {
        pluginRepository.init();
        Terminal terminal = TerminalBuilder.builder()
                .system(false)
                .streams(System.in, System.out)
                .build();
        terminal.enterRawMode();
        Robot robot = new Robot();

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

        if (cmd.hasOption("signature")) {
            SignatureSelector signatureSelector = new SignatureSelector();
            TerminalParser terminalParser = new TerminalParser(terminal, signatureSelector);
            Thread terminalParserThread = new Thread(terminalParser);
            terminalParserThread.start();
        }

        if (cmd.hasOption("keep-alive")) {

            int seconds = 30;
            LocalTime endTime = LocalTime.of(17, 0);  // default end time
            if (cmd.hasOption("end-time")) {
                try {
                    endTime = LocalTime.parse(cmd.getOptionValue("end-time"), DateTimeFormatter.ofPattern("HH:mm"));
                } catch (DateTimeParseException e) {
                    logger.error("Invalid end time format. Please use HH:mm format", e);
                    throw e;
                }
            }

            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            KeepAliveTimer keepAliveTimer;
            if (cmd.hasOption("seconds")) {
                keepAliveTimer = new KeepAliveTimer(
                        seconds * 1000,
                        endTime,
                        robot,
                        new DisplayModeWrapper(gd.getDisplayMode())
                );
            } else {
                keepAliveTimer = new KeepAliveTimer(
                        endTime,
                        robot,
                        new DisplayModeWrapper(gd.getDisplayMode())
                );
            }
            Thread keepAliveThread = new Thread(keepAliveTimer);
            keepAliveThread.start();
        }
    }
}
