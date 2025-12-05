package com.aldrineeinsteen.fun.options.helper;

import com.aldrineeinsteen.fun.options.PluginTemplate;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PluginRepositoryTest {

    @BeforeEach
    public void setUp() {
        // Note: PluginRepository uses static state which persists between tests
        // This is by design for the singleton plugin architecture
    }

    @Test
    public void testGetOptions_ReturnsValidOptionsObject() {
        Options options = PluginRepository.getOptions();
        assertNotNull(options, "Options object should not be null");
    }

    @Test
    public void testGetShortcutActions_ReturnsValidMap() {
        Map<String, PluginMetadata.ShortcutAction> shortcuts = PluginRepository.getShortcutActions();
        assertNotNull(shortcuts, "Shortcuts map should not be null");
    }

    @Test
    public void testGetLoadedPlugins_ReturnsValidSet() {
        Set<String> loadedPlugins = PluginRepository.getLoadedPlugins();
        assertNotNull(loadedPlugins, "Loaded plugins set should not be null");
    }

    @Test
    public void testAddLoadedPlugin_AddsPluginToSet() {
        String pluginName = "com.test.TestPlugin";
        PluginRepository.addLoadedPlugin(pluginName);
        
        assertTrue(PluginRepository.getLoadedPlugins().contains(pluginName), 
            "Plugin should be added to loaded plugins set");
    }

    @Test
    public void testRegisterPlugin_StoresPluginInstance() {
        MockPlugin mockPlugin = new MockPlugin();
        String pluginName = "TestPlugin";
        
        PluginRepository.registerPlugin(pluginName, mockPlugin);
        
        PluginTemplate retrievedPlugin = PluginRepository.getPlugin(pluginName);
        assertSame(mockPlugin, retrievedPlugin, "Retrieved plugin should be the same instance");
    }

    @Test
    public void testRegisterUtility_StoresUtilityInstance() {
        MockUtility mockUtility = new MockUtility();
        String utilityName = "TestUtility";
        
        PluginRepository.registerUtility(utilityName, mockUtility);
        
        Runnable retrievedUtility = PluginRepository.getUtility(utilityName);
        assertSame(mockUtility, retrievedUtility, "Retrieved utility should be the same instance");
    }

    @Test
    public void testShortcutAction_CreationAndGetters() {
        String action = "testAction";
        String plugin = "TestPlugin";
        String keyCombination = "CTRL + SHIFT + ALT + T";
        
        PluginRepository.ShortcutAction shortcutAction = new PluginRepository.ShortcutAction(action, plugin, keyCombination);
        
        assertEquals(action, shortcutAction.getAction(), "Action should match");
        assertEquals(plugin, shortcutAction.getPlugin(), "Plugin should match");
        assertEquals(keyCombination, shortcutAction.getKeyCombination(), "Key combination should match");
    }

    @Test
    public void testShortcutAction_ToString() {
        String action = "testAction";
        String plugin = "TestPlugin";
        String keyCombination = "CTRL + SHIFT + ALT + T";
        
        PluginRepository.ShortcutAction shortcutAction = new PluginRepository.ShortcutAction(action, plugin, keyCombination);
        String toString = shortcutAction.toString();
        
        assertNotNull(toString, "toString should not return null");
        assertTrue(toString.contains(action), "toString should contain action");
        assertTrue(toString.contains(plugin), "toString should contain plugin");
    }

    @Test
    public void testMockPlugin_ExecuteAction() {
        MockPlugin mockPlugin = new MockPlugin();
        String testAction = "testAction";
        
        assertFalse(mockPlugin.isActionExecuted(), "Plugin should not have executed action initially");
        
        mockPlugin.executeAction(testAction);
        
        assertTrue(mockPlugin.isActionExecuted(), "Plugin should have executed action");
        assertEquals(testAction, mockPlugin.getLastAction(), "Last action should match executed action");
    }

    @Test
    public void testMockUtility_Run() {
        MockUtility mockUtility = new MockUtility();
        
        assertFalse(mockUtility.hasRun(), "Utility should not have run initially");
        
        mockUtility.run();
        
        assertTrue(mockUtility.hasRun(), "Utility should have run");
    }

    /**
     * Mock plugin class for testing
     */
    private static class MockPlugin extends PluginTemplate {
        private boolean actionExecuted = false;
        private String lastAction;

        @Override
        public void executeAction(String actionName) {
            this.actionExecuted = true;
            this.lastAction = actionName;
        }

        public boolean isActionExecuted() {
            return actionExecuted;
        }

        public String getLastAction() {
            return lastAction;
        }
    }

    /**
     * Mock utility class for testing
     */
    private static class MockUtility implements Runnable {
        private boolean hasRun = false;

        @Override
        public void run() {
            hasRun = true;
        }

        public boolean hasRun() {
            return hasRun;
        }
    }
}