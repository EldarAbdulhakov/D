package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyProvider {

    private static PropertyProvider instance;
    private final Properties properties = new Properties();

    public static PropertyProvider getInstance() {
        if (instance == null) {
            instance = new PropertyProvider();
        }

        return instance;
    }

    private PropertyProvider() {
        String propertiesFile = "config.properties";

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(propertiesFile)) {
            if (inputStream == null) {
                throw new RuntimeException(propertiesFile + " not found in classpath");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + propertiesFile, e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
