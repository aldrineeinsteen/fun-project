package com.aldrineeinsteen.fun.options;

import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class SignatureSelector implements Runnable{

    private final static Logger logger = LoggerFactory.getLogger(SignatureSelector.class);
    private List<String> signatures = new ArrayList<>();
    private final Random random = new Random();

    private final Terminal globalTerminal;

    public SignatureSelector(Terminal terminal) {
        globalTerminal = terminal;
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(getClass().getClassLoader().getResourceAsStream("signatures.yaml"));
        Object options = yamlData.get("options");
        if (options instanceof List) {
            List<?> list = (List<?>) options;
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                //noinspection unchecked
                signatures = list.stream()
                        .map(option -> ((Map<String, String>) option).get("signature"))
                        .collect(Collectors.toList());
            }
        }
    }

    public String getRandomSignature() {
        if (signatures != null && !signatures.isEmpty()) {
            return signatures.get(random.nextInt(signatures.size()));
        }
        return null;
    }

    @Override
    public void run() {
        globalTerminal.writer().println("Starting the terminal...");
        int read;
        while (true) {
            try {
                if ((read = globalTerminal.reader().read()) == -1) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            char ch = (char) read;
            if (ch == 's') {
                String signature = getRandomSignature();
                if (signature != null) {
                    logger.info("Selecting random signature: {}.", signature);

                    // Copy the signature to the clipboard
                    StringSelection stringSelection = new StringSelection(signature);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

                } {
                    logger.error("No signature is shortlisted.");
                }
            }
        }
    }

}
