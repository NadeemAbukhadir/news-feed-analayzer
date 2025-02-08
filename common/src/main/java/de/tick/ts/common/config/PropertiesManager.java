package de.tick.ts.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

import static de.tick.ts.common.Constants.EXTERNAL_CONFIG_DIR;

/**
 * A Manger class to maintain properties for every application
 */
public final class PropertiesManager {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesManager.class);
    private static final Properties properties = new Properties();
    private static volatile boolean initialized = false;

    private PropertiesManager() {
    }

    public static void initialize(String... configFiles) {

        if (initialized) {
            return;
        }

        synchronized (PropertiesManager.class) {

            if (initialized) {
                return;
            }

            for (String configFile : configFiles) {
                loadExternalConfig(configFile);
            }

            for (String configFile : configFiles) {
                loadClasspathConfig(configFile);
            }

            properties.putAll(System.getProperties());
            initialized = true;
        }
    }

    private static void loadExternalConfig(String configFile) {

        File externalConfig = new File(EXTERNAL_CONFIG_DIR + configFile);
        if (externalConfig.exists()) {
            try (InputStream input = Files.newInputStream(externalConfig.toPath())) {
                Properties fileProps = new Properties();
                fileProps.load(input);
                synchronized (PropertiesManager.class) {
                    properties.putAll(fileProps);
                }
                logger.info("Loaded external config: {}", externalConfig.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Error loading external config {}: {}", configFile, e.getMessage());
            }
        }
    }

    private static void loadClasspathConfig(String configFile) {

        try (InputStream input = PropertiesManager.class.getClassLoader().getResourceAsStream(configFile)) {
            if (input != null) {
                Properties fileProps = new Properties();
                fileProps.load(input);
                synchronized (PropertiesManager.class) {
                    fileProps.forEach(properties::putIfAbsent);
                }
                logger.info("Loaded classpath config: {}", configFile);
            }
        } catch (Exception e) {
            logger.error("Error loading classpath config {}: {}", configFile, e.getMessage());
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