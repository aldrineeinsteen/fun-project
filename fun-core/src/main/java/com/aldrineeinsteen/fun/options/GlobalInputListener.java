package com.aldrineeinsteen.fun.options;

import com.aldrineeinsteen.fun.options.helper.PluginRepository;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalInputListener implements NativeKeyListener {

    private final static Logger logger = LoggerFactory.getLogger(GlobalInputListener.class);

    public void registerHook() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        String keyCombination = getKeyCombination(e);
        logger.debug("Key pressed: {}", keyCombination);

        // Retrieve the ShortcutAction object
        PluginRepository.ShortcutAction actionInfo = PluginRepository.getShortcutActions().get(keyCombination);
        if (actionInfo != null) {
            logger.debug("Executing action: {} in plugin: {}", actionInfo.getAction(), actionInfo.getPlugin());
            // Execute the action for the specific plugin
            executePluginAction(actionInfo.getAction(), actionInfo.getPlugin());
        } else {
            logger.debug("No action registered for this key combination.");
        }
    }

    private String getKeyCombination(NativeKeyEvent e) {
        StringBuilder combination = new StringBuilder();
        
        // Handle modifier keys in consistent order
        if ((e.getModifiers() & NativeInputEvent.CTRL_MASK) != 0) {
            combination.append("CTRL + ");
        }
        if ((e.getModifiers() & NativeInputEvent.SHIFT_MASK) != 0) {
            combination.append("SHIFT + ");
        }
        if ((e.getModifiers() & NativeInputEvent.ALT_MASK) != 0) {
            combination.append("ALT + ");
        }
        if ((e.getModifiers() & NativeInputEvent.META_MASK) != 0) {
            combination.append("META + ");
        }

        // Get the key text and normalize it
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        if (keyText != null && !keyText.isEmpty()) {
            // Normalize key text (uppercase for consistency)
            keyText = keyText.toUpperCase();
            
            // Handle special key mappings
            keyText = normalizeKeyText(keyText);
            
            combination.append(keyText);
        } else {
            logger.warn("Unknown key code: {}", e.getKeyCode());
            combination.append("UNKNOWN_KEY_").append(e.getKeyCode());
        }

        String result = combination.toString();
        logger.trace("Key combination parsed: {} (keyCode: {}, modifiers: {})", 
            result, e.getKeyCode(), e.getModifiers());
        
        return result;
    }

    /**
     * Normalize key text for consistent mapping across platforms
     */
    private String normalizeKeyText(String keyText) {
        if (keyText == null) return "UNKNOWN";
        
        // Handle common key text variations
        switch (keyText.toUpperCase()) {
            case "SPACE":
            case "SPACEBAR":
                return "SPACE";
            case "ENTER":
            case "RETURN":
                return "ENTER";
            case "ESCAPE":
            case "ESC":
                return "ESCAPE";
            case "DELETE":
            case "DEL":
                return "DELETE";
            case "BACKSPACE":
            case "BACK":
                return "BACKSPACE";
            default:
                return keyText.toUpperCase();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Implement logic for key release events
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Implement logic for key typed events
    }

    private void executePluginAction(String action, String pluginName) {
        if (action == null || action.trim().isEmpty()) {
            logger.error("Action name is null or empty for plugin: {}", pluginName);
            return;
        }
        
        if (pluginName == null || pluginName.trim().isEmpty()) {
            logger.error("Plugin name is null or empty for action: {}", action);
            return;
        }

        logger.debug("Attempting to execute action '{}' on plugin '{}'", action, pluginName);
        
        try {
            PluginTemplate plugin = PluginRepository.getPlugin(pluginName);
            if (plugin == null) {
                logger.error("Plugin '{}' not found in registry. Available plugins: {}", 
                    pluginName, PluginRepository.getLoadedPlugins());
                return;
            }

            // Validate plugin state before execution
            if (!plugin.validate()) {
                logger.error("Plugin '{}' failed validation check", pluginName);
                return;
            }

            if (!plugin.isReady()) {
                logger.error("Plugin '{}' is not ready for execution. Current state: {}", 
                    pluginName, plugin.getState());
                return;
            }

            // Execute the action
            long startTime = System.currentTimeMillis();
            plugin.executeAction(action);
            long executionTime = System.currentTimeMillis() - startTime;
            
            logger.info("Successfully executed action '{}' on plugin '{}' in {}ms", 
                action, plugin.getPluginName(), executionTime);
                
        } catch (IllegalArgumentException e) {
            logger.error("Invalid action '{}' for plugin '{}': {}", action, pluginName, e.getMessage());
        } catch (SecurityException e) {
            logger.error("Security error executing action '{}' on plugin '{}': {}", action, pluginName, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error executing action '{}' on plugin '{}': {} - {}", 
                action, pluginName, e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }
}
