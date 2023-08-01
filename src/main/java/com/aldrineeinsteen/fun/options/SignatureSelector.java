package com.aldrineeinsteen.fun.options;

import org.jline.terminal.Terminal;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class SignatureSelector {
    private final List<String> signatures;
    private final Random random = new Random();

    public SignatureSelector(Terminal terminal) throws IOException {
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(getClass().getClassLoader().getResourceAsStream("signatures.yaml"));
        signatures = ((List<Map<String, String>>) yamlData.get("options")).stream()
                .map(option -> option.get("signature"))
                .collect(Collectors.toList());

        int read;
        while ((read = terminal.reader().read()) != -1) {
            char ch = (char) read;
            System.out.println("Key pressed: " + ch);
        }
//
//        robot.addKeyListener(new KeyListener() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_S && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
//                    if (signatures != null && !signatures.isEmpty()) {
//                        String signature = signatures.get(random.nextInt(signatures.size()));
//                        System.out.println(signature);
//                    }
//                }
//            }
//
//            @Override
//            public void keyReleased(KeyEvent e) {}
//
//            @Override
//            public void keyTyped(KeyEvent e) {}
//        });
    }

    public String getRandomSignature() {
        if (signatures != null && !signatures.isEmpty()) {
            return signatures.get(random.nextInt(signatures.size()));
        }
        return null;
    }
}
