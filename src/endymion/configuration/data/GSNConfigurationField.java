package endymion.configuration.data;

import java.util.HashMap;

/**
 * Class that represents field from configuration
 */
public class GSNConfigurationField {

    /**
     * Field parameters
     */
    protected HashMap<String, String> parameters;

    /**
     * Constructor
     * @param name - field name
     */
    public GSNConfigurationField (String name) {
        parameters = new HashMap<String, String>();
        parameters.put("name" , name);
    }

    /**
     * Method for adding parameter
     * @param key - parameter name
     * @param value - parameter value
     */
    public void addParameter (String key, String value) {
        parameters.put(key, value);
    }

    /**
     * Getter for field parameters
     * @param key
     * @return - parameter value
     */
    public String getParameter (String key) {
        return parameters.get(key);
    }


}
