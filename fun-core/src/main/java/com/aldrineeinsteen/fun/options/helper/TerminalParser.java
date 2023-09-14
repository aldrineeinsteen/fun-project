package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.SignatureSelector;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The core executor which is responsible for the delegation.
 * The {@link com.aldrineeinsteen.fun.Main} class will be deprecated in future.
 */
public class TerminalParser implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(TerminalParser.class);
    private final Terminal globalTerminal;
    private final SignatureSelector signatureSelector;

    public TerminalParser(Terminal terminal, SignatureSelector signatureSelector) {
        this.globalTerminal = terminal;
        this.signatureSelector = signatureSelector;
    }

    @Override
    public void run() {
        logger.info("Starting the terminal...");
        writeTips();
        int read;
        while (true) {
            try {
                if ((read = globalTerminal.reader().read()) == -1) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            char ch = (char) read;

            switch (ch) {
                case 's': {
                    String signature = signatureSelector.getRandomSignature();
                    if (signature != null) {
                        logger.debug("Selecting random signature: {}.", signature);
                        writeTips(signature);
                    } else {
                        logger.error("No signature is shortlisted.");
                    }
                    break;
                }
                // Adding support to support windows subsystem. addressing issue #24
                case '\r':
                case '\n': {
                    break;
                }
                default: {
                    writeErrorNTips();
                    break;
                }
            }
        }
    }

    private void writeTips(String signature) {
        if (signature != null)
            logger.info("The selected signature '{}' is copied to the clipboard for easy access.", signature);
        writeTips();
    }

    private void writeTips() {
        globalTerminal.writer().print("Quick tool enabled: 's' + Enter gives a random signature: ");
        globalTerminal.writer().flush();
    }

    private void writeErrorNTips() {
        globalTerminal.writer().println("No other operations supported at this moment. Please raise any feature request at https://github.com/aldrineeinsteen/fun-project.");
        globalTerminal.writer().flush();
        writeTips();
    }
}
