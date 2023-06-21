package com.aldrineeinsteen.fun;

import com.aldrineeinsteen.fun.options.KeepAliveTimer;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws AWTException {
        Options options = new Options();

        Option endTimeOption = new Option("e", "end-time", true, "End Time in format of HH:mm");
        endTimeOption.setRequired(false);
        options.addOption(endTimeOption);

        Option keepAliveOption = new Option("k", "keep-alive", false, "Run the KeepAlive timer");
        keepAliveOption.setRequired(false);
        options.addOption(keepAliveOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            formatter.printHelp("fun project", options);
            System.exit(1);
            return;
        }

        LocalTime endTime = LocalTime.of(18, 0);  // default end time
        if (cmd.hasOption("end-time")) {
            try {
                endTime = LocalTime.parse(cmd.getOptionValue("end-time"), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e) {
                logger.error("Invalid end time format. Please use HH:mm format");
                System.exit(1);
            }
        }

        if (cmd.hasOption("keep-alive")) {
            java.awt.Robot robot = new java.awt.Robot();
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            DisplayMode displayMode = gd.getDisplayMode();
            KeepAliveTimer keepAliveTimer = new KeepAliveTimer(
                    endTime,
                    robot,
                    displayMode
            );
            Thread keepAliveThread = new Thread(keepAliveTimer);
            keepAliveThread.start();
        }
    }
}
