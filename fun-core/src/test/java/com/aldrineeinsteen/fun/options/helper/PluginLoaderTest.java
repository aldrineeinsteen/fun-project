package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.PluginTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PluginLoader class
 */
public class PluginLoaderTest {

    private PluginLoader pluginLoader;

    @BeforeEach
    void setUp() {
        pluginLoader = new PluginLoader();
    }

    @Test
    void testInstantiateAndRegisterPlugin_WithNullClassName() {
        // Should handle null gracefully
        pluginLoader.instantiateAndRegisterPlugin(null);
        
        // Verify no plugin was registered
        assertTrue(pluginLoader.getPlugins().isEmpty());
    }

    @Test
    void testInstantiateAndRegisterPlugin_WithEmptyClassName() {
        // Should handle empty string gracefully
        pluginLoader.instantiateAndRegisterPlugin("");
        
        // Verify no plugin was registered
        assertTrue(pluginLoader.getPlugins().isEmpty());
    }

    @Test
    void testInstantiateAndRegisterPlugin_WithWhitespaceClassName() {
        // Should handle whitespace gracefully
        pluginLoader.instantiateAndRegisterPlugin("   ");
        
        // Verify no plugin was registered
        assertTrue(pluginLoader.getPlugins().isEmpty());
    }

    @Test
    void testInstantiateAndRegisterPlugin_WithNonExistentClass() {
        // Should handle ClassNotFoundException gracefully
        pluginLoader.instantiateAndRegisterPlugin("com.nonexistent.FakePlugin");
        
        // Verify no plugin was registered
        assertTrue(pluginLoader.getPlugins().isEmpty());
    }

    @Test
    void testInstantiateAndRegisterPlugin_WithInvalidClass() {
        // Should handle classes that don't implement required interfaces
        pluginLoader.instantiateAndRegisterPlugin("java.lang.String");
        
        // Verify no plugin was registered
        assertTrue(pluginLoader.getPlugins().isEmpty());
    }

    @Test
    void testRegisterPlugin() {
        MockPlugin plugin = new MockPlugin();
        String pluginName = "TestPlugin";
        
        pluginLoader.registerPlugin(pluginName, plugin);
        
        PluginTemplate retrieved = pluginLoader.getPlugin(pluginName);
        assertSame(plugin, retrieved);
    }

    @Test
    void testRegisterUtility() {
        MockUtility utility = new MockUtility();
        String utilityName = "TestUtility";
        
        pluginLoader.registerUtility(utilityName, utility);
        
        Runnable retrieved = pluginLoader.getUtility(utilityName);
        assertSame(utility, retrieved);
    }

    @Test
    void testGetPlugin_NonExistent() {
        PluginTemplate plugin = pluginLoader.getPlugin("NonExistent");
        assertNull(plugin);
    }

    @Test
    void testGetUtility_NonExistent() {
        Runnable utility = pluginLoader.getUtility("NonExistent");
        assertNull(utility);
    }

    @Test
    void testGetPlugins_ReturnsDefensiveCopy() {
        MockPlugin plugin = new MockPlugin();
        pluginLoader.registerPlugin("Test", plugin);
        
        var plugins1 = pluginLoader.getPlugins();
        var plugins2 = pluginLoader.getPlugins();
        
        // Should return different map instances (defensive copy)
        assertNotSame(plugins1, plugins2);
        assertEquals(plugins1.size(), plugins2.size());
    }

    @Test
    void testGetUtilities_ReturnsDefensiveCopy() {
        MockUtility utility = new MockUtility();
        pluginLoader.registerUtility("Test", utility);
        
        var utilities1 = pluginLoader.getUtilities();
        var utilities2 = pluginLoader.getUtilities();
        
        // Should return different map instances (defensive copy)
        assertNotSame(utilities1, utilities2);
        assertEquals(utilities1.size(), utilities2.size());
    }

    @Test
    void testMultiplePluginRegistration() {
        MockPlugin plugin1 = new MockPlugin();
        MockPlugin plugin2 = new MockPlugin();
        
        pluginLoader.registerPlugin("Plugin1", plugin1);
        pluginLoader.registerPlugin("Plugin2", plugin2);
        
        assertEquals(2, pluginLoader.getPlugins().size());
        assertSame(plugin1, pluginLoader.getPlugin("Plugin1"));
        assertSame(plugin2, pluginLoader.getPlugin("Plugin2"));
    }

    @Test
    void testMultipleUtilityRegistration() {
        MockUtility utility1 = new MockUtility();
        MockUtility utility2 = new MockUtility();
        
        pluginLoader.registerUtility("Utility1", utility1);
        pluginLoader.registerUtility("Utility2", utility2);
        
        assertEquals(2, pluginLoader.getUtilities().size());
        assertSame(utility1, pluginLoader.getUtility("Utility1"));
        assertSame(utility2, pluginLoader.getUtility("Utility2"));
    }

    @Test
    void testPluginOverwrite() {
        MockPlugin plugin1 = new MockPlugin();
        MockPlugin plugin2 = new MockPlugin();
        
        pluginLoader.registerPlugin("Test", plugin1);
        pluginLoader.registerPlugin("Test", plugin2);
        
        // Should overwrite with second plugin
        assertEquals(1, pluginLoader.getPlugins().size());
        assertSame(plugin2, pluginLoader.getPlugin("Test"));
    }

    /**
     * Mock plugin for testing
     */
    private static class MockPlugin extends PluginTemplate {
        @Override
        public void executeAction(String actionName) {
            // Mock implementation
        }
    }

    /**
     * Mock utility for testing
     */
    private static class MockUtility implements Runnable {
        @Override
        public void run() {
            // Mock implementation
        }
    }
}

// Made with Bob