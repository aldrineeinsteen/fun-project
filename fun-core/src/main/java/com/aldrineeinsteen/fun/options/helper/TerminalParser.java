package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.PluginTemplate;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The core executor which is responsible for the delegation.
 * Uses dynamic plugin discovery instead of hardcoded plugin dependencies.
 * The {@link com.aldrineeinsteen.fun.Main} class will be deprecated in future.
 */
public class TerminalParser implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(TerminalParser.class);
    private final Terminal globalTerminal;

    public TerminalParser(Terminal terminal) {
        this.globalTerminal = terminal;
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
                    // Dynamically find and execute signature plugin action
                    PluginTemplate signaturePlugin = findPluginByAction("getRandomSignature");
                    if (signaturePlugin != null) {
                        try {
                            signaturePlugin.executeAction("getRandomSignature");
                            writeTips("Signature selected and copied to clipboard");
                        } catch (Exception e) {
                            logger.error("Error executing signature selection: {}", e.getMessage());
                            writeErrorNTips();
                        }
                    } else {
                        logger.error("No signature plugin available. Please ensure signature-selector plugin is loaded.");
                        writeErrorNTips();
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

    /**
     * Dynamically find a plugin that supports the given action.
     * This scans through all registered plugins to find one that can execute the action.
     */
    private PluginTemplate findPluginByAction(String actionName) {
        // Search through shortcut actions to find plugins that support this action
        for (PluginRepository.ShortcutAction shortcutAction : PluginRepository.getShortcutActions().values()) {
            if (actionName.equals(shortcutAction.getAction())) {
                PluginTemplate plugin = PluginRepository.getPlugin(shortcutAction.getPlugin());
                if (plugin != null && plugin.isReady()) {
                    logger.debug("Found plugin '{}' that supports action '{}'", shortcutAction.getPlugin(), actionName);
                    return plugin;
                }
            }
        }
        
        logger.debug("No plugin found supporting action: {}", actionName);
        return null;
    }

    private void writeTips(String message) {
        if (message != null)
            logger.info(message);
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
