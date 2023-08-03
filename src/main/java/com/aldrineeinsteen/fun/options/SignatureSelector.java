package com.aldrineeinsteen.fun.options;

import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class SignatureSelector implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(SignatureSelector.class);
    private final Random random = new Random();
    private final Terminal globalTerminal;
    private List<String> signatures = new ArrayList<>();

    public SignatureSelector(Terminal terminal) {
        globalTerminal = terminal;
        Yaml yaml = new Yaml();
        InputStream signatureFile = getClass().getClassLoader().getResourceAsStream("signatures.yaml");
        Map<String, Object> yamlData = yaml.load(signatureFile);
        Object options = yamlData.get("options");
        if (options instanceof List) {
            List<?> list = (List<?>) options;
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                //noinspection unchecked
                signatures = list.stream()
                        .map(option -> ((Map<String, String>) option).get("signature"))
                        .collect(Collectors.toList());
            } else {
                logger.error("list is either not an instance of Map or is Empty");
            }
        } else {
            logger.error("Options is not an instance of List.");
        }
    }

    public String getRandomSignature() {
        if (signatures != null && !signatures.isEmpty()) {
            return signatures.get(random.nextInt(signatures.size()));
        } else {
            if (signatures == null) {
                logger.error("The Signature collection is empty");
            }
        }
        return null;
    }

    @Override
    public void run() {
        globalTerminal.writer().println("Starting the terminal...");
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
                    String signature = getRandomSignature();
                    if (signature != null) {
                        logger.info("Selecting random signature: {}.", signature);

                        // Copy the signature to the clipboard
                        StringSelection stringSelection = new StringSelection(signature);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                        writeTips(signature);
                    } else {
                        logger.error("No signature is shortlisted.");
                    }
                    break;
                }
                //Adding support to support windows subsystem. addressing issue #24
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
            globalTerminal.writer().println("The selected signature '" + signature + "' is copied to the clipboard for easy access. \n ---");
        writeTips();
    }

    private void writeTips() {
        globalTerminal.writer().print("Quick tool enabled: 's' + Enter gives a random signature:");
        globalTerminal.writer().flush();
    }

    private void writeErrorNTips() {
        globalTerminal.writer().println("No other operations supported at this moment. Please raise any feature request at https://github.com/aldrineeinsteen/fun-project.");
        globalTerminal.writer().flush();
        writeTips();
    }

}
