package com.aldrineeinsteen.fun.options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class SignatureSelector {

    private final static Logger logger = LoggerFactory.getLogger(SignatureSelector.class);
    private final Random random = new Random();
    private List<String> signatures = new ArrayList<>();

    public SignatureSelector() {
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
}
