package endymion.configuration.data;

import endymion.exception.EndymionException;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 04.06.2015.
 */
public class GSNStorageConfiguration extends GSNConfiguration {

    public static final String STORAGE_ID = "storage";

    private HashMap<String, String> parameters;

    public GSNStorageConfiguration (String ipAddress, String username, String password) {
        super(ipAddress, "", username, password);
        parameters = new HashMap<String, String>();

        parameters.put("url", ipAddress);
        parameters.put("username", username);
        parameters.put("password", password);
    }

    @Override
    public String getID() {
        return STORAGE_ID;
    }

    @Override
    public String getConfigurationParameter(String... keys) throws EndymionException {
        return parameters.get(keys[0]);
    }

    @Override
    public void setConfigurationParameter(String... keys) throws EndymionException {

    }

    @Override
    public List<String> getVSensors() {
        return null;
    }

    @Override
    public List<String> getAlarms() {
        return null;
    }

    @Override
    public List<String> getSensorFields(String sensor) throws EndymionException {
        return null;
    }

    @Override
    public List<String> getAlarms(String sensor) throws EndymionException {
        return null;
    }

    @Override
    public void addVSensor(String vSensor, HashMap<String, String> parameters) throws EndymionException {

    }

    @Override
    public void addField(String vSensor, HashMap<String, String> parameters) throws EndymionException {

    }

    @Override
    public void addAlarm(String vSensor, String alarmName, HashMap<String, String> parameters, List<String> sendToList) throws EndymionException {

    }

    @Override
    public void addAlarm(String alarmName, HashMap<String, String> parameters, List<String> sendToList) throws EndymionException {

    }
}
