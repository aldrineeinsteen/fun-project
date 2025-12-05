package com.aldrineeinsteen.fun.options.helper;

import org.apache.commons.cli.Option;

import java.util.Map;

/**
 * Generates structured help text for plugins and their options.
 */
public class HelpTextGenerator {

    /**
     * Generate a structured help message showing plugins and their individual documentation
     */
    public static String generateStructuredHelp(Map<String, PluginMetadata> pluginInfos) {
        StringBuilder help = new StringBuilder();
        help.append("Fun Project - Java Console Application with Multi-Monitor Support\n");
        help.append("========================================================================\n\n");
        
        // Global options first
        help.append("GLOBAL OPTIONS:\n");
        help.append("  -h, --help                Show this help message\n\n");
        
        // Plugin-specific sections
        if (pluginInfos.isEmpty()) {
            help.append("No plugins loaded.\n");
        } else {
            help.append("AVAILABLE PLUGINS:\n");
            help.append("------------------\n\n");
            
            for (PluginMetadata pluginInfo : pluginInfos.values()) {
                appendPluginHelp(help, pluginInfo);
            }
        }
        
        appendExamples(help);
        
        return help.toString();
    }

    private static void appendPluginHelp(StringBuilder help, PluginMetadata pluginInfo) {
        help.append(String.format("Plugin: %s\n", pluginInfo.getName()));
        help.append(String.format("Description: %s\n", pluginInfo.getDescription()));
        help.append(String.format("Class: %s\n", pluginInfo.getClassName()));
        
        appendOptions(help, pluginInfo);
        appendParams(help, pluginInfo);
        appendShortcuts(help, pluginInfo);
        
        help.append("\n");
    }

    private static void appendOptions(StringBuilder help, PluginMetadata pluginInfo) {
        if (!pluginInfo.getOptions().isEmpty()) {
            help.append("  Main Options:\n");
            for (Option option : pluginInfo.getOptions()) {
                help.append(String.format("    -%s, --%s%s%s\n", 
                    option.getOpt(), 
                    option.getLongOpt(),
                    option.hasArg() ? " <arg>" : "",
                    option.getDescription() != null && !option.getDescription().isEmpty() 
                        ? "  " + option.getDescription() : ""));
            }
        }
    }

    private static void appendParams(StringBuilder help, PluginMetadata pluginInfo) {
        if (!pluginInfo.getParams().isEmpty()) {
            help.append("  Parameters:\n");
            for (Option param : pluginInfo.getParams()) {
                help.append(String.format("    -%s, --%s%s%s\n", 
                    param.getOpt(), 
                    param.getLongOpt(),
                    param.hasArg() ? " <arg>" : "",
                    param.getDescription() != null && !param.getDescription().isEmpty() 
                        ? "  " + param.getDescription() : ""));
            }
        }
    }

    private static void appendShortcuts(StringBuilder help, PluginMetadata pluginInfo) {
        if (!pluginInfo.getShortcuts().isEmpty()) {
            help.append("  Global Shortcuts:\n");
            for (PluginMetadata.ShortcutAction shortcut : pluginInfo.getShortcuts()) {
                help.append(String.format("    %s  Trigger: %s\n", 
                    shortcut.getKeyCombination(), 
                    shortcut.getAction()));
            }
        }
    }

    private static void appendExamples(StringBuilder help) {
        help.append("EXAMPLES:\n");
        help.append("---------\n");
        help.append("  java -cp \"target/lib/*:target/plugins/*:target/fun-project.jar\" com.aldrineeinsteen.fun.Main -k\n");
        help.append("    Start keep-alive timer with multi-monitor support\n\n");
        help.append("  java -cp \"target/lib/*:target/plugins/*:target/fun-project.jar\" com.aldrineeinsteen.fun.Main -k -e 17:30\n");
        help.append("    Start keep-alive timer until 5:30 PM\n\n");
        help.append("  java -cp \"target/lib/*:target/plugins/*:target/fun-project.jar\" com.aldrineeinsteen.fun.Main -k --multi-monitor --cross-monitors\n");
        help.append("    Start with multi-monitor mode allowing cross-monitor movement\n\n");
    }
}

// Made with Bob
