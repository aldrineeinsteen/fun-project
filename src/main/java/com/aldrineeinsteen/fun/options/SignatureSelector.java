package com.aldrineeinsteen.fun.options;

import org.jline.terminal.Terminal;
import org.yaml.snakeyaml.Yaml;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class SignatureSelector implements Runnable{
    private final List<String> signatures;
    private final Random random = new Random();

    private Terminal globalTerminal;

    public SignatureSelector(Terminal terminal) throws IOException {
        globalTerminal = terminal;
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(getClass().getClassLoader().getResourceAsStream("signatures.yaml"));
        signatures = ((List<Map<String, String>>) yamlData.get("options")).stream()
                .map(option -> option.get("signature"))
                .collect(Collectors.toList());

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
                    System.out.println(signature);

                    // Copy the signature to the clipboard
                    StringSelection stringSelection = new StringSelection(signature);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

                }
            }
        }
    }

}
