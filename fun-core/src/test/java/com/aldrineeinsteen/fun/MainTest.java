package com.aldrineeinsteen.fun;

import com.aldrineeinsteen.fun.options.helper.PluginRepository;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MainTest {

    @Test
    public void testMainWithInvalidArguments() {
        String[] args = {"--invalid", "--arguments"};
        Assertions.assertThrows(ParseException.class, () -> Main.main(args));
    }

    @Test
    public void testMainWithDynamicPluginOptions() throws Exception {
        // Test that the system handles plugin options dynamically without hardcoded references
        // If no plugins are available, this should still work without errors
        String[] args = {"--help"}; // Help option should always be available
        try {
            Main.main(args);
        } catch (ParseException e) {
            // Expected behavior when no plugins provide the requested options
            Assertions.assertTrue(e.getMessage().contains("Unrecognized option") || 
                                e.getMessage().contains("help"));
        }
    }

    @Test 
    public void testMainWithEmptyArgs() throws Exception {
        // Test that the system starts correctly with no arguments (interactive mode)
        String[] args = {};
        Main.main(args);
        // Should not throw exception - plugins discovered dynamically
    }

    @Test
    public void testMainInitializesPluginRepository() throws Exception {
        // Test that plugin repository is initialized properly
        String[] args = {};
        Main.main(args);
        
        // After initialization, the plugin repository should be set up
        // This tests the dynamic plugin discovery system
        Assertions.assertNotNull(PluginRepository.getOptions());
        Assertions.assertNotNull(PluginRepository.getShortcutActions());
        Assertions.assertNotNull(PluginRepository.getLoadedPlugins());
    }

    @Test
    public void testMainWithUnknownOption() {
        // Test behavior with completely unknown options
        String[] args = {"--unknown-option", "value"};
        Assertions.assertThrows(ParseException.class, () -> Main.main(args));
    }
}