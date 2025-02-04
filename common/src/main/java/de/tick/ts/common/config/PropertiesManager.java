package de.tick.ts.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * A Manger class to maintain properties for every application
 */
public class PropertiesManager {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesManager.class);
    private static final Properties properties = new Properties();
    private static volatile boolean initialized = false;

    public static void initialize(String... configFiles) {

        if (initialized) return;

        synchronized (PropertiesManager.class) {
            if (initialized) return;

            for (String configFile : configFiles) {
                loadConfigFile(configFile);
            }

            properties.putAll(System.getProperties());
            initialized = true;
        }
    }

    private static void loadConfigFile(String configPath) {

        try (InputStream input = PropertiesManager.class.getClassLoader().getResourceAsStream(configPath)) {

            if (input != null) {
                Properties fileProps = new Properties();
                fileProps.load(input);
                synchronized (PropertiesManager.class) {
                    properties.putAll(fileProps);
                }
            }
        } catch (Exception e) {
            logger.error("Error loading config: {} - {}", configPath, e.getMessage());
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
}