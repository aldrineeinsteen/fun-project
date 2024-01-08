package com.aldrineeinsteen.fun.options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SignatureSelector implements PluginTemplate {

    private final static Logger logger = LoggerFactory.getLogger(SignatureSelector.class);
    private final Random random = new Random();
    private final List<Signature> weightedSignatures = new ArrayList<>();
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private final ClipboardOwner clipboardOwner = null;

    public SignatureSelector() {
        loadSignatures();
    }

    private void loadSignatures() {
        try {
            Yaml yaml = new Yaml();
            InputStream signatureFile = getClass().getClassLoader().getResourceAsStream("signatures.yaml");
            if (signatureFile == null) {
                logger.error("Cannot find 'signatures.yaml' file");
                return;
            }

            Map<String, Object> yamlData = yaml.load(signatureFile);
            List<?> options = (List<?>) yamlData.get("options");
            processSignatureOptions(options);
        } catch (Exception e) {
            logger.error("Error loading signatures: ", e);
        }
    }

    private void processSignatureOptions(List<?> options) {
        if (options == null || options.isEmpty() || !(options.get(0) instanceof Map)) {
            logger.error("Signature options are not properly configured");
            return;
        }

        List<Signature> signatures = options.stream()
                .map(option -> {
                    Map<?, ?> map = (Map<?, ?>) option;
                    return new Signature(
                            map.get("signature").toString(),
                            map.get("tag").toString(),
                            Double.parseDouble(map.get("weight").toString())
                    );
                })
                .collect(Collectors.toList());

        // Populate the weightedSignatures list
        for (Signature signature : signatures) {
            int weight = (int) (signature.getWeight() * 10); // Assuming weight is a double
            weightedSignatures.addAll(Collections.nCopies(weight, signature));
        }
    }

    public String getRandomSignature() {
        if (weightedSignatures.isEmpty()) {
            logger.error("The Signature collection is empty");
            return null;
        }

        String selectedSignature = weightedSignatures.get(random.nextInt(weightedSignatures.size())).getText();
        clipboard.setContents(new StringSelection(selectedSignature), clipboardOwner);
        return selectedSignature;
    }

    @Override
    public void executeAction(String actionName) {
        if ("getRandomSignature".equals(actionName)) {
            getRandomSignature();
        } else {
            logger.error("Unrecognized action: {}", actionName);
        }
    }
}
