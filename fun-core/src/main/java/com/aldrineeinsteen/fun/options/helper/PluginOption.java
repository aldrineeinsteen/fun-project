package com.aldrineeinsteen.fun.options.helper;

import org.apache.commons.cli.Option;

import java.util.Map;

public class PluginOption {
    private final String shortOpt;
    private final String longOpt;
    private final String description;
    private final boolean hasArguments;
    private final boolean required;

    public PluginOption(Map<String, Object> optionConfig) {
        this.shortOpt = (String) optionConfig.get("shortOpt");
        this.longOpt = (String) optionConfig.get("longOpt");
        this.description = (String) optionConfig.get("description");
        this.hasArguments = (boolean) optionConfig.get("hasArguments");
        this.required = (boolean) optionConfig.get("required");
    }

    public Option toOption() {
        return Option.builder(shortOpt)
                .longOpt(longOpt)
                .desc(description)
                .hasArg(hasArguments)
                .required(required)
                .build();
    }

    public String getShortOpt() {
        return shortOpt;
    }

    public String getLongOpt() {
        return longOpt;
    }
}