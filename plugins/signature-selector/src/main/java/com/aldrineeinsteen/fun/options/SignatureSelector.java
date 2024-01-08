package com.aldrineeinsteen.fun.options;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
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
        Yaml yaml = new Yaml();
        InputStream signatureFile = getClass().getClassLoader().getResourceAsStream("signatures.yaml");
        Map<String, Object> yamlData = yaml.load(signatureFile);
        Object options = yamlData.get("options");
        if (options instanceof List) {
            List<?> list = (List<?>) options;
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                List<Signature> signatures = list.stream()
                        .map(option -> {
                            Map<String, Object> map = (Map<String, Object>) option;
                            return new Signature(map.get("signature").toString(),
                                    map.get("tag").toString(),
                                    Double.parseDouble(map.get("weight").toString()));
                        })
                        .collect(Collectors.toList());

                // Populate the weightedSignatures list based on the weights
                for (Signature signature : signatures) {
                    int weight = (int) (signature.getWeight() * 10); // Assuming weight is a double
                    weightedSignatures.addAll(Collections.nCopies(weight, signature));
                }

            } else {
                logger.error("list is either not an instance of Map or is Empty");
            }
        } else {
            logger.error("Options is not an instance of List.");
        }
    }

    public String getRandomSignature() {
        if (!weightedSignatures.isEmpty()) {
            String selectedSignature = weightedSignatures.get(random.nextInt(weightedSignatures.size())).getText();
            clipboard.setContents(new StringSelection(selectedSignature), clipboardOwner);
            return selectedSignature;
        } else {
            logger.error("The Signature collection is empty");
        }
        return null;
    }

    @Override
    public void executeAction(String actionName) {
        getRandomSignature();
    }
}
