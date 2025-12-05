package com.aldrineeinsteen.fun.options.helper;

import org.apache.commons.cli.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HelpTextGeneratorTest {

    private Map<String, PluginMetadata> pluginInfos;

    @BeforeEach
    void setUp() {
        pluginInfos = new HashMap<>();
    }

    @Test
    void testGenerateStructuredHelpWithNoPlugins() {
        String help = HelpTextGenerator.generateStructuredHelp(pluginInfos);
        
        assertNotNull(help);
        assertTrue(help.contains("Fun Project"));
        assertTrue(help.contains("GLOBAL OPTIONS"));
        assertTrue(help.contains("No plugins loaded"));
        assertTrue(help.contains("EXAMPLES"));
    }

    @Test
    void testGenerateStructuredHelpWithOnePlugin() {
        PluginMetadata metadata = new PluginMetadata(
            "TestPlugin", 
            "com.test.TestPlugin", 
            "A test plugin");
        
        Option option = Option.builder("t")
            .longOpt("test")
            .desc("Test option")
            .build();
        metadata.addOption(option);
        
        pluginInfos.put("TestPlugin", metadata);
        
        String help = HelpTextGenerator.generateStructuredHelp(pluginInfos);
        
        assertNotNull(help);
        assertTrue(help.contains("TestPlugin"));
        assertTrue(help.contains("com.test.TestPlugin"));
        assertTrue(help.contains("A test plugin"));
        assertTrue(help.contains("-t, --test"));
        assertTrue(help.contains("Test option"));
    }

    @Test
    void testGenerateStructuredHelpWithOptionsAndParams() {
        PluginMetadata metadata = new PluginMetadata(
            "ComplexPlugin", 
            "com.test.ComplexPlugin", 
            "A complex plugin");
        
        Option option = Option.builder("c")
            .longOpt("complex")
            .desc("Complex option")
            .build();
        metadata.addOption(option);
        
        Option param = Option.builder("p")
            .longOpt("param")
            .hasArg()
            .desc("Parameter with argument")
            .build();
        metadata.addParam(param);
        
        pluginInfos.put("ComplexPlugin", metadata);
        
        String help = HelpTextGenerator.generateStructuredHelp(pluginInfos);
        
        assertTrue(help.contains("Main Options:"));
        assertTrue(help.contains("-c, --complex"));
        assertTrue(help.contains("Parameters:"));
        assertTrue(help.contains("-p, --param <arg>"));
        assertTrue(help.contains("Parameter with argument"));
    }

    @Test
    void testGenerateStructuredHelpWithShortcuts() {
        PluginMetadata metadata = new PluginMetadata(
            "ShortcutPlugin", 
            "com.test.ShortcutPlugin", 
            "Plugin with shortcuts");
        
        PluginMetadata.ShortcutAction shortcut = new PluginMetadata.ShortcutAction(
            "doSomething", 
            "com.test.ShortcutPlugin", 
            "CTRL + S");
        metadata.addShortcut(shortcut);
        
        pluginInfos.put("ShortcutPlugin", metadata);
        
        String help = HelpTextGenerator.generateStructuredHelp(pluginInfos);
        
        assertTrue(help.contains("Global Shortcuts:"));
        assertTrue(help.contains("CTRL + S"));
        assertTrue(help.contains("doSomething"));
    }

    @Test
    void testGenerateStructuredHelpWithMultiplePlugins() {
        PluginMetadata plugin1 = new PluginMetadata(
            "Plugin1", "com.test.Plugin1", "First plugin");
        PluginMetadata plugin2 = new PluginMetadata(
            "Plugin2", "com.test.Plugin2", "Second plugin");
        
        pluginInfos.put("Plugin1", plugin1);
        pluginInfos.put("Plugin2", plugin2);
        
        String help = HelpTextGenerator.generateStructuredHelp(pluginInfos);
        
        assertTrue(help.contains("Plugin1"));
        assertTrue(help.contains("Plugin2"));
        assertTrue(help.contains("First plugin"));
        assertTrue(help.contains("Second plugin"));
    }

    @Test
    void testHelpContainsExamples() {
        String help = HelpTextGenerator.generateStructuredHelp(pluginInfos);
        
        assertTrue(help.contains("EXAMPLES:"));
        assertTrue(help.contains("java -cp"));
        assertTrue(help.contains("keep-alive timer"));
    }

    @Test
    void testHelpFormatting() {
        String help = HelpTextGenerator.generateStructuredHelp(pluginInfos);
        
        // Check for proper section separators
        assertTrue(help.contains("========"));
        assertTrue(help.contains("EXAMPLES:"));
        assertTrue(help.contains("java -cp"));
    }
}

// Made with Bob
