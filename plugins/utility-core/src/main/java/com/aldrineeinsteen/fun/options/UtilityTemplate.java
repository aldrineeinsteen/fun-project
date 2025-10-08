package com.aldrineeinsteen.fun.options;

import org.apache.commons.cli.CommandLine;

public abstract class UtilityTemplate implements Runnable {

    protected CommandLine commandLine;

    @Override
    public void run() {
        logStart();
        runUtility();
    }

    /**
     * Configure utility from parsed command line parameters.
     * This method is called before run() to provide access to CLI parameters.
     */
    public void configureFromCLI(CommandLine commandLine) {
        this.commandLine = commandLine;
        processCommandLineOptions();
    }

    /**
     * Process command line options specific to this utility.
     * Subclasses should override this to handle their specific CLI parameters.
     */
    protected void processCommandLineOptions() {
        // Default implementation - subclasses can override
    }

    protected abstract void logStart();

    protected abstract void runUtility();
}
