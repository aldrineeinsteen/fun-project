package com.aldrineeinsteen.fun.options.helper;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class PluginRepository {
    private final static Logger logger = LoggerFactory.getLogger(PluginRepository.class);
    private final static Options options = new Options();

    public static Options getOptions() {
        return options;
    }

    public void init() {
        try {

            List<URL> urls = new ArrayList<>();

            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Enumeration<URL> resources = classLoader.getResources("plugin.yaml");

                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    urls.add(url);
                }
            } catch (IOException e) {
                logger.error("Exception when loading the yaml: ", e);
            }

            for (URL url : urls) {
                logger.debug("Found plugin.yaml in: {}", url.getPath());

                try (InputStream is = url.openConnection().getInputStream()) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> yamlData = yaml.load(is);

                    ArrayList<Map<String, Object>> commandOptions = (ArrayList<Map<String, Object>>) yamlData.get("options");

                    for (Map<String, Object> command : commandOptions) {
                        Option option = Option.builder((String) command.get("shortOpt"))
                                .hasArg((boolean) command.get("hasArguments"))
                                .longOpt((String) command.get("longOpt"))
                                .desc((String) command.get("description"))
                                .build();
                        options.addOption(option);
                    }
                }

            }
        } catch (Exception e) {
            logger.error("Plugin initialisation has failed.", e);
        }
    }
}
