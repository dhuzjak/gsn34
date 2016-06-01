package endymion.configuration.data;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;

import java.util.*;

/**
 * Created by Nikola on 28.02.2015.
 * This class extends GSNConfiguration and is used as a basic
 * configuration class
 */
public class GSNConfigurationItem extends GSNConfiguration {

    /**
     * List of vSensor configurations
     */
    protected List<GSNConfigurationVSensor> vSensors;

    /**
     * GSN level parameters
     */
    protected HashMap<String, String> parameters;

    /**
     * List of GSN level alarms
     */
    protected List<GSNConfigurationAlarm> alarms;

    private final int PARAM_GET_NUMBER = 1;
    private final int PARAM_GET_NUMBER_VSENSOR = 3;
    private final int PARAM_GET_NUMBER_FIELD = 4;
    private final int PARAM_GET_NUMBER_ALARM = PARAM_GET_NUMBER_VSENSOR;

    private final int PARAM_SET_NUMBER = 2;

    private final String FIELD_ID = "field";
    private final String ALARM_ID = "alarm";
    private final String VSENSOR_ID = "vsensor";

    public GSNConfigurationItem (String ipAddress, String port, String username, String password, String connectionType) {
        super(ipAddress, port, username, password);
        vSensors = new ArrayList<GSNConfigurationVSensor>();
        alarms = new ArrayList<GSNConfigurationAlarm>();
        parameters = new HashMap<String, String>();
        parameters.put("ipAddress", ipAddress);
        parameters.put("port", port);
        parameters.put("username", username);
        parameters.put("password", password);
        parameters.put("connectionType", connectionType);
        this.GSNId = ipAddress + ":" + port;
    }

    /**
     * There are several cases of this method:
     * if the there is only one key - GSN level configuration parameter is returned
     *
     * in other cases the fist key represents "whose" parameter is fetched (vsensor, field, alarm)
     * the second parameter is the name of vsensor or alarm.
     * the third and in some cases fourth parameter are passed into getParameter method of that vsensor or alarm.
     * @param keys - identifier of the parameter
     * @return parameter value
     * @throws EndymionException
     */
    @Override
    public String getConfigurationParameter(String... keys) throws EndymionException {
        if (keys.length == PARAM_GET_NUMBER_VSENSOR && keys[0].equalsIgnoreCase(VSENSOR_ID)) {
            GSNConfigurationVSensor vSensor = getVSensorByName(keys[1]);
            return vSensor.getParameter(keys[2]);
        } else if (keys.length == PARAM_GET_NUMBER_ALARM && keys[0].equalsIgnoreCase(ALARM_ID)) {
            GSNConfigurationAlarm alarm = getAlarmByName(keys[1]);
            return alarm.getParameter(keys[2]);
        } else if (keys.length == PARAM_GET_NUMBER_FIELD) {
            GSNConfigurationVSensor vSensor = getVSensorByName(keys[1]);
            return vSensor.getParameter(keys[0], keys[2], keys[3]);
        } else if (keys.length == PARAM_GET_NUMBER) {
            return parameters.get(keys[0]);
        } else {
            throw new EndymionException("No parameter in GSN " + getID() + " with keys " + Arrays.toString(keys), EndymionLoggerEnum.WARNING);
        }
    }

    /**
     * This method only works for GSN level parameters
     * Other parameters must be set directly into their object representation (vsensor, field, alarm)
     * @param keys - identifier and value of parameter
     * @throws EndymionException
     */
    @Override
    public void setConfigurationParameter(String... keys) throws EndymionException {
        if (keys.length == PARAM_SET_NUMBER) {
            if (keys[0] != null)
                parameters.put(keys[0], keys[1]);
            else
                throw new EndymionException("Key cannot be null", EndymionLoggerEnum.WARNING);
        } else {
            throw new EndymionException("Invalid keys: " + Arrays.toString(keys), EndymionLoggerEnum.WARNING);
        }
    }

    /**
     * Getter for alarms
     * @return - list of alarm names
     */
    public List<String> getAlarms () {
        List<String> alarmsS = new ArrayList<String>();

        for (GSNConfigurationAlarm alarm : alarms) {
            alarmsS.add(alarm.getName());
        }

        return alarmsS;
    }

    /**
     * Getter for vSensors
     * @return - list of sensor names
     */
    @Override
    public List<String> getVSensors() {
        List<String> VSensorsS = new ArrayList<String>();

        for (GSNConfigurationVSensor vSensor : vSensors) {
            VSensorsS.add(vSensor.getName());
        }

        return VSensorsS;
    }

    /**
     * Getter for sensor fields
     * @param sensor - sensor name
     * @return - sensor field names
     * @throws EndymionException
     */
    @Override
    public List<String> getSensorFields(String sensor) throws EndymionException{
        GSNConfigurationVSensor vSensor = getVSensorByName(sensor);
        return vSensor.getAllFields();
    }

    /**
     * Getter for alarms
     * @param sensor - sensor name
     * @return - list of vSensor alarms
     * @throws EndymionException
     */
    @Override
    public List<String> getAlarms(String sensor) throws EndymionException {
        GSNConfigurationVSensor vSensor = getVSensorByName(sensor);
        return vSensor.getAllAlarms();
    }

    /**
     * Method for adding vSensor
     * @param vSensor - sensor name
     * @param parameters - sensor configuration parameters
     * @throws EndymionException
     */
    @Override
    public void addVSensor(String vSensor, HashMap<String, String> parameters) throws EndymionException{

        if (getVSensors().contains(vSensor)) {
            throw new EndymionException("VSensor with name " + vSensor + " already exists", EndymionLoggerEnum.WARNING);
        }

        GSNConfigurationVSensor vSensorObject = new GSNConfigurationVSensor(vSensor);
        for (String key : parameters.keySet()) {
            vSensorObject.setParameters(key, parameters.get(key));
        }
        vSensors.add(vSensorObject);
    }

    /**
     * Method for adding field
     * @param vSensor - sensor name
     * @param parameters - field configuration parameters
     * @throws EndymionException
     */
    @Override
    public void addField(String vSensor, HashMap<String, String> parameters) throws EndymionException{
        GSNConfigurationVSensor vSensorObject = getVSensorByName(vSensor);

        if (parameters.get("name") == null) {
            throw new EndymionException("No field name defined in " + getID(), EndymionLoggerEnum.WARNING);
        }

        vSensorObject.setParameters(FIELD_ID, parameters.get("name"));
    }

    /**
     * Method for adding alarm for vSensor
     * @param vSensor - sensor name
     * @param alarmName - alarm name
     * @param parameters - alarm parameters
     * @param sendToList - list of addresses to send alarm to
     * @throws EndymionException
     */
    @Override
    public void addAlarm(String vSensor, String alarmName, HashMap<String, String> parameters, List<String> sendToList) throws EndymionException {
        GSNConfigurationVSensor vSensorObject = getVSensorByName(vSensor);

        if (alarmName == null) {
            throw new EndymionException("No alarm name defined in " + getID(), EndymionLoggerEnum.WARNING);
        }

        vSensorObject.setParameters(ALARM_ID, alarmName);

        for (String key : parameters.keySet()) {
            vSensorObject.setParameters(ALARM_ID, alarmName, key, parameters.get(key));
        }

        for (String sendTo : sendToList) {
            vSensorObject.setParameters(ALARM_ID, alarmName, "send-to", sendTo);
        }
    }

    /**
     * Method for adding alarms for GSN
     * @param alarmName - alarm name
     * @param parameters - alarm parameters
     * @param sendToList - list of addresses to send alarm to
     * @throws EndymionException
     */
    @Override
    public void addAlarm(String alarmName, HashMap<String, String> parameters, List<String> sendToList) throws EndymionException {
        if (getAlarms().contains(alarmName)) {
            throw new EndymionException("Alarm with name " + alarmName + " already exists", EndymionLoggerEnum.WARNING);
        }

        GSNConfigurationAlarm alarm = new GSNConfigurationAlarm(alarmName, parameters);
        for (String sendTo : sendToList) {
            alarm.addSendTo(sendTo);
        }

        alarms.add(alarm);
    }

    /**
     * Getter for sensor object by name
     * @param name - sensor name
     * @return - sensor configuration object
     * @throws EndymionException
     */
    protected GSNConfigurationVSensor getVSensorByName (String name) throws EndymionException {
        for (GSNConfigurationVSensor vSensor : vSensors) {
            if (vSensor.getName().equalsIgnoreCase(name)) {
                return vSensor;
            }
        }

        throw new EndymionException("No sensor with name: " + name + " in GSN " + this.getID(), EndymionLoggerEnum.WARNING);
    }

    /**
     * Getter for alarm object by name
     * @param name - alarm name
     * @return - alarm configuration object
     * @throws EndymionException
     */
    protected GSNConfigurationAlarm getAlarmByName (String name) throws EndymionException {
        for (GSNConfigurationAlarm alarm : alarms) {
            if (alarm.getName().equalsIgnoreCase(name)) {
                return alarm;
            }
        }

        throw new EndymionException("No alarm with name: " + name + " in GSN " + this.getID(), EndymionLoggerEnum.WARNING);
    }


}
