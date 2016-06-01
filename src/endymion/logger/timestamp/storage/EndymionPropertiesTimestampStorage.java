package endymion.logger.timestamp.storage;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by Nikola on 25.04.2015.
 */
public class EndymionPropertiesTimestampStorage extends EndymionAbstractTimestampStorage {


    private final String filename = "endymion_configuration/logger/timestamp.properties";
    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public String getTimestamp(String... keys) throws EndymionException {
        String propertyKey = generateKey(keys);
        Properties properties = loadProperties();

        return properties.getProperty(propertyKey, null);

    }

    @Override
    public void setTimestamp(String timestamp, String... keys) throws EndymionException {

        String propertyKey = generateKey(keys);

        Properties properties = loadProperties();
        properties.setProperty(propertyKey, timestamp);

        storeProperties(properties);

    }

    protected Properties loadProperties () throws EndymionException {
        Properties properties = new Properties();
        InputStream input;
        try {
            input = new FileInputStream(filename);
            properties.load(input);
            input.close();
        } catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.ERROR);
        }

        return properties;
    }

    protected void storeProperties (Properties properties) throws EndymionException {

        OutputStream output;
        try {
            output = new FileOutputStream(filename);
            properties.store(output, null);
            output.close();
        } catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.ERROR);
        }
    }

    protected String generateKey (String... keys) throws EndymionException {
        String propertyKey = "";

        for (String key : keys) {
            propertyKey += key + "_";
        }

        if (propertyKey.isEmpty()) {
            throw new EndymionException("Property key cannot be empty!" , EndymionLoggerEnum.ERROR);
        }

        propertyKey = propertyKey.substring(0, propertyKey.length()-1);

        return propertyKey;
    }
}
