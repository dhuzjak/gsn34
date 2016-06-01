package endymion.configuration.data;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 12.04.2015.
 * This class represents configuration defined alarm
 */
public class GSNConfigurationAlarm {

    /**
     * Alarm name
     */
    protected String name;

    /**
     * Alarm parameters
     */
    protected HashMap<String, String> parameters;

    /**
     * List of recipients
     */
    protected List<String> sendToList;

    /**
     * Constructor
     * @param name - alarm name
     */
    public GSNConfigurationAlarm (String name) {
        this.name = name;
        this.parameters = new HashMap<String, String>();
        this.sendToList = new ArrayList<String>();
        this.parameters.put("name", name);
    }

    /**
     * Constructor
     * @param name - alarm name
     * @param parameters - initial parameters
     */
    public GSNConfigurationAlarm (String name, HashMap<String, String> parameters) {
        this.name = name;
        this.parameters = (HashMap<String, String>)parameters.clone();
        this.sendToList = new ArrayList<String>();
        this.parameters.put("name", name);
    }

    public String getName() {
        return name;
    }

    /**
     * Gets alarm parameter
     * @param parameterKey
     * @return parameter value
     */
    public String getParameter (String parameterKey) {

        /* If the parameterKey is send-to, return value is a
            string containing all recipients separated by ';'
         */
        if (parameterKey.equalsIgnoreCase("send-to")) {
            return StringUtils.join(this.sendToList.iterator(), ";");
        } else {
            return parameters.get(parameterKey);
        }
    }

    /**
     * Adds alarm parameter
     * @param parameterKey
     * @param parameter
     */
    public void addParameter (String parameterKey, String parameter) {

        /* If the parameterKey is send-to, the value is added
            to the list
         */
        if (parameterKey.equalsIgnoreCase("send-to")) {
            addSendTo(parameter);
        } else {
            this.parameters.put(parameterKey, parameter);
        }
    }

    /**
     * Method for adding recipient of alarm
     * @param sendTo
     */
    public void addSendTo (String sendTo) {
        this.sendToList.add(sendTo);
    }

    /**
     * Getter for recipient list
     * @return list of recipient
     */
    public List<String> getSendToList() {
        return sendToList;
    }
}
