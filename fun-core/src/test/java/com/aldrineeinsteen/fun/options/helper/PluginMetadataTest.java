package com.aldrineeinsteen.fun.options.helper;

import org.apache.commons.cli.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginMetadataTest {

    private PluginMetadata pluginMetadata;

    @BeforeEach
    void setUp() {
        pluginMetadata = new PluginMetadata("TestPlugin", "com.test.TestPlugin", "Test Description");
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("TestPlugin", pluginMetadata.getName());
        assertEquals("com.test.TestPlugin", pluginMetadata.getClassName());
        assertEquals("Test Description", pluginMetadata.getDescription());
        assertNotNull(pluginMetadata.getOptions());
        assertNotNull(pluginMetadata.getParams());
        assertNotNull(pluginMetadata.getShortcuts());
        assertTrue(pluginMetadata.getOptions().isEmpty());
        assertTrue(pluginMetadata.getParams().isEmpty());
        assertTrue(pluginMetadata.getShortcuts().isEmpty());
    }

    @Test
    void testAddOption() {
        Option option = Option.builder("t").longOpt("test").desc("Test option").build();
        pluginMetadata.addOption(option);
        
        assertEquals(1, pluginMetadata.getOptions().size());
        assertEquals(option, pluginMetadata.getOptions().get(0));
    }

    @Test
    void testAddParam() {
        Option param = Option.builder("p").longOpt("param").hasArg().desc("Test param").build();
        pluginMetadata.addParam(param);
        
        assertEquals(1, pluginMetadata.getParams().size());
        assertEquals(param, pluginMetadata.getParams().get(0));
    }

    @Test
    void testAddShortcut() {
        PluginMetadata.ShortcutAction shortcut = new PluginMetadata.ShortcutAction(
            "testAction", "com.test.TestPlugin", "CTRL + T");
        pluginMetadata.addShortcut(shortcut);
        
        assertEquals(1, pluginMetadata.getShortcuts().size());
        assertEquals(shortcut, pluginMetadata.getShortcuts().get(0));
    }

    @Test
    void testShortcutAction() {
        PluginMetadata.ShortcutAction shortcut = new PluginMetadata.ShortcutAction(
            "testAction", "com.test.TestPlugin", "CTRL + T");
        
        assertEquals("testAction", shortcut.getAction());
        assertEquals("com.test.TestPlugin", shortcut.getPlugin());
        assertEquals("CTRL + T", shortcut.getKeyCombination());
        
        String toString = shortcut.toString();
        assertTrue(toString.contains("testAction"));
        assertTrue(toString.contains("com.test.TestPlugin"));
        assertTrue(toString.contains("CTRL + T"));
    }

    @Test
    void testMultipleOptionsAndParams() {
        Option option1 = Option.builder("a").longOpt("alpha").build();
        Option option2 = Option.builder("b").longOpt("beta").build();
        Option param1 = Option.builder("x").longOpt("xray").hasArg().build();
        Option param2 = Option.builder("y").longOpt("yankee").hasArg().build();
        
        pluginMetadata.addOption(option1);
        pluginMetadata.addOption(option2);
        pluginMetadata.addParam(param1);
        pluginMetadata.addParam(param2);
        
        assertEquals(2, pluginMetadata.getOptions().size());
        assertEquals(2, pluginMetadata.getParams().size());
    }
}

// Made with Bob
