package com.aldrineeinsteen.fun.options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class PluginTemplate {
    private static final Map<Class<? extends PluginTemplate>, PluginTemplate> instances = new HashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(PluginTemplate.class);

    public static synchronized <T extends PluginTemplate> T getInstance(Class<T> clazz) {
        if (!instances.containsKey(clazz)) {
            try {
                T instance = clazz.getDeclaredConstructor().newInstance();
                instances.put(clazz, instance);
                logger.info("{} is initialised successfully", clazz.getSimpleName());
            } catch (Exception e) {
                throw new RuntimeException("Error creating instance for " + clazz.getName(), e);
            }
        }
        return clazz.cast(instances.get(clazz));
    }

    public abstract void executeAction(String actionName);

    protected PluginTemplate() {
    }

}
