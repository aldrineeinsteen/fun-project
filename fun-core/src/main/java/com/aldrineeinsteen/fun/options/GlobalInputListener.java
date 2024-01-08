package com.aldrineeinsteen.fun.options;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import com.aldrineeinsteen.fun.options.helper.PluginRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

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
        if ((e.getModifiers() & NativeInputEvent.CTRL_MASK) != 0) {
            combination.append("CTRL + ");
        }
        if ((e.getModifiers() & NativeInputEvent.SHIFT_MASK) != 0) {
            combination.append("SHIFT + ");
        }
        if ((e.getModifiers() & NativeInputEvent.ALT_MASK) != 0) {
            combination.append("ALT + ");
        }

        // Append the key character (you might need to map the keycode to a string)
        combination.append(NativeKeyEvent.getKeyText(e.getKeyCode()));

        return combination.toString();
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
        try {
            Object plugin = PluginRepository.getPlugin(pluginName);
            if (plugin == null) {
                logger.error("Plugin not found: {}", pluginName);
                return;
            }

            // Assuming the method to be invoked has a single String parameter
            Method method = plugin.getClass().getDeclaredMethod("executeAction", String.class);
            method.setAccessible(true);
            method.invoke(plugin, action);
        } catch (NoSuchMethodException e) {
            logger.error("Method not found: {}", action, e);
        } catch (Exception e) {
            logger.error("Error executing action: {}", action, e);
        }
    }
}
