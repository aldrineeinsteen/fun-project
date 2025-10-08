package com.aldrineeinsteen.fun.options.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Configuration loader for KeepAliveTimer plugin settings
 */
public class KeepAliveConfig {
    private static final Logger logger = LoggerFactory.getLogger(KeepAliveConfig.class);
    private static final String CONFIG_FILE = "keep-alive-config.yaml";
    
    // Default values
    private LocalTime endTime = LocalTime.parse("18:30");
    private int delayMilliseconds = 30000; // 30 seconds
    private boolean resumeNextDay = false;
    private boolean multiMonitorEnabled = true;
    private boolean logMonitorSwitches = true;
    
    public KeepAliveConfig() {
        loadConfiguration();
    }
    
    @SuppressWarnings("unchecked")
    private void loadConfiguration() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                logger.warn("Configuration file {} not found, using defaults", CONFIG_FILE);
                return;
            }
            
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(is);
            
            if (config == null) {
                logger.warn("Empty configuration file {}, using defaults", CONFIG_FILE);
                return;
            }
            
            // Load end time
            String endTimeStr = (String) config.get("endTime");
            if (endTimeStr != null) {
                try {
                    this.endTime = LocalTime.parse(endTimeStr);
                    logger.info("Loaded end time from config: {}", this.endTime);
                } catch (DateTimeParseException e) {
                    logger.error("Invalid end time format in config: {}, using default: {}", endTimeStr, this.endTime);
                }
            }
            
            // Load delay seconds
            Integer delaySeconds = (Integer) config.get("delaySeconds");
            if (delaySeconds != null && delaySeconds > 0) {
                this.delayMilliseconds = delaySeconds * 1000;
                logger.info("Loaded delay from config: {} seconds", delaySeconds);
            }
            
            // Load resume next day
            Boolean resumeNextDay = (Boolean) config.get("resumeNextDay");
            if (resumeNextDay != null) {
                this.resumeNextDay = resumeNextDay;
                logger.info("Loaded resume next day from config: {}", this.resumeNextDay);
            }
            
            // Load multi-monitor settings
            Map<String, Object> multiMonitorConfig = (Map<String, Object>) config.get("multiMonitor");
            if (multiMonitorConfig != null) {
                Boolean enabled = (Boolean) multiMonitorConfig.get("enabled");
                if (enabled != null) {
                    this.multiMonitorEnabled = enabled;
                    logger.info("Loaded multi-monitor enabled from config: {}", this.multiMonitorEnabled);
                }
                
                Boolean logSwitches = (Boolean) multiMonitorConfig.get("logSwitches");
                if (logSwitches != null) {
                    this.logMonitorSwitches = logSwitches;
                    logger.info("Loaded log monitor switches from config: {}", this.logMonitorSwitches);
                }
            }
            
            logger.info("Successfully loaded configuration from {}", CONFIG_FILE);
            
        } catch (IOException e) {
            logger.error("Error loading configuration file {}: {}", CONFIG_FILE, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error loading configuration: {}", e.getMessage(), e);
        }
    }
    
    // Getters
    public LocalTime getEndTime() { return endTime; }
    public int getDelayMilliseconds() { return delayMilliseconds; }
    public boolean isResumeNextDay() { return resumeNextDay; }
    public boolean isMultiMonitorEnabled() { return multiMonitorEnabled; }
    public boolean isLogMonitorSwitches() { return logMonitorSwitches; }
    
    // Setters for CLI parameter overrides
    public void setEndTime(LocalTime endTime) { 
        this.endTime = endTime; 
        logger.info("End time overridden via CLI: {}", endTime);
    }
    
    public void setDelayMilliseconds(int delayMilliseconds) { 
        this.delayMilliseconds = delayMilliseconds; 
        logger.info("Delay overridden via CLI: {} ms", delayMilliseconds);
    }
    
    public void setResumeNextDay(boolean resumeNextDay) { 
        this.resumeNextDay = resumeNextDay; 
        logger.info("Resume next day overridden via CLI: {}", resumeNextDay);
    }
}