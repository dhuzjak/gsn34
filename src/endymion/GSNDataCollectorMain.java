package endymion;

import endymion.alarm.manager.GSNAlarmManager;
import endymion.collector.GSNDataCollector;
import endymion.configuration.manager.GSNConfigurationManager;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.logger.EndymionLoggerManager;
import endymion.logger.timestamp.EndymionTimestampManager;
import endymion.processor.GSNDataProcessor;
import endymion.processor.data.GSNStorageElement;
import endymion.sensor.GSNConfigurationSensor;
import endymion.sensor.GSNSensor;
import endymion.sensor.data.GSNSensorData;
import endymion.storage.GSNStorageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 23.02.2015.
 * This is the main class which serves as interface to the systems using Endymion
 * It also serves as a "glue logic" for whole system (to avoid subsystems being
 * dependant of each other)
 */
public class GSNDataCollectorMain {

    /**
     * An interface to a configuration subsystem
     */
    GSNConfigurationManager configurationManager;

    /**
     * An interface to collector subsystem
     */
    GSNDataCollector dataCollector;

    /**
     * An interface to processing subsystem
     */
    GSNDataProcessor dataProcessor;

    /**
     * Data ready to be stored into a database
     */
    List<GSNStorageElement> storageElements;

    /**
     * Logger class instance (singleton)
     */
    EndymionLoggerManager logger;

    /**
     * An interface to alarm subsystem
     */
    GSNAlarmManager alarmManager;

    EndymionTimestampManager timestampManager;

    GSNStorageManager storageManager = null;


    /**
     * The constructor - instantiates all subsystems
     */
    public GSNDataCollectorMain() {

        configurationManager = new GSNConfigurationManager();
        dataCollector = new GSNDataCollector();
        dataProcessor = new GSNDataProcessor();
        storageElements = new ArrayList<GSNStorageElement>();
        alarmManager = new GSNAlarmManager();
        timestampManager = EndymionTimestampManager.getTimestampManager();
    }


    /**
     * This method should be called only once at the system start.
     * It reads the configuration file and initializes logger, alarms and connections
     * @throws Exception - if any of the initializations fails
     */
    public void initialize () throws Exception {
        configurationManager.readConfiguration();
        if (configurationManager.useOwnStorage()) {
            initializeStorage();
        }
        logger = EndymionLoggerManager.getLoggerManager();
        initializeAlarms();
        setupConnection();
        setupTimeSampling();
    }

    /**
     * This method should be called periodically.
     * It collects data from GSNs and processes them thus creating
     * storage elements.
     */
    public void run () {

        /* Setting the initial number of data for storage */
        dataCollector.setNumberOfRows(0);

        for (String id : configurationManager.getGSNs()) {
            try {
                collectData(id);
                dataCollector.outputSensorData(dataCollector.getSensorData());
                processSensorData();
            } catch (EndymionException e) {
                logger.logMessage(e);
            }

            /* Checks if the data limit was reached */
            if (dataCollector.isDataLimitReached()) {
                break;
            }
        }

        /* Checking alarms*/
        checkAlarms();

        /* Sorting storage elements for easier storing*/
        if (!storageElements.isEmpty()) {
            Collections.sort(storageElements);
            //outputStorageElements(storageElements);
        }

        if (storageManager != null && !storageElements.isEmpty()) {
            storageManager.storeDataElements(storageElements);
            storageElements.clear();
        }



    }

    /**
     * This method sets connection for each GSN defined in configuration
     * @throws EndymionException - Setting connection failed
     */
    private void setupConnection () throws EndymionException {
        for (String GSNId : configurationManager.getGSNs()) {
            setupConnection(GSNId);
        }
    }

    /**
     * This method sets connection for a specific GSN.
     * @param GSNid - ID of the GSN
     * @throws EndymionException - Setting connection failed
     */
    private void setupConnection(String GSNid) throws EndymionException {
        String ipAddress = configurationManager.getConfigurationParameter(GSNid, "ipAddress");
        String port = configurationManager.getConfigurationParameter(GSNid, "port");
        String username = configurationManager.getConfigurationParameter(GSNid, "username");
        String password = configurationManager.getConfigurationParameter(GSNid, "password");
        String connectionType = configurationManager.getConfigurationParameter(GSNid, "connectionType");

        dataCollector.setConnectionParameters(GSNid, connectionType, ipAddress, port, username, password);
    }

    /**
     * This method sets up time sampling defined in configuration
     */
    private void setupTimeSampling () {

        for (String id : configurationManager.getGSNs()) {
            dataCollector.initializeTimeSampling(id, configurationManager.getConfigurationParameter(id, "sampling-time"));

            for (String vSensor : configurationManager.getVSensors(id)) {
                String samplingTime = configurationManager.getConfigurationParameter(id, "vsensor", vSensor, "sampling-time");
                if (samplingTime != null) {
                    dataCollector.initializeTimeSampling(id, vSensor, samplingTime);
                }
            }
        }
    }

    /**
     * This method collects data from GSN with GSNId
     * @param GSNid - ID of the GSN
     * @throws EndymionException
     */
    private void collectData (String GSNid) throws EndymionException {

        List<String> vsensors = configurationManager.getVSensors(GSNid);

        /* If there are no vSensors defined - all vSensors' data is collected*/
        HashMap<String, List<String>> dataFieldsConfiguration = new HashMap<String, List<String>>();

        for (String sensor : vsensors) {
            dataFieldsConfiguration.put(sensor, new ArrayList<String>());

            /* If there are no fields defined and read-data-fields is set to all - all fields' data is collected*/
            if (!configurationManager.getConfigurationParameter(GSNid, "vsensor", sensor, "read-data-fields")
                    .equalsIgnoreCase("all")) {
                for (String field : configurationManager.getVSensorFields(GSNid, sensor)) {
                    dataFieldsConfiguration.get(sensor).add(field);
                }
            }
        }


        dataCollector.initialize(GSNid, dataFieldsConfiguration);
        setConfigurationSensorData();

        dataCollector.collectData(GSNid);
    }

    /**
     * This method processes collected data and creates storage elements
     */
    protected void processSensorData () {
        List<GSNStorageElement> tempStorage;

        for (GSNSensor sensor : dataCollector.getSensorData()) {
            HashMap<String, String> parameters = setProcessingParameters(sensor);
            parameters.put("timestamp", dataCollector.getLastTimestamp(sensor.getGSNId(), sensor.getvSensor()));
            dataProcessor.setFilterParameters(parameters);
            try {
                tempStorage = dataProcessor.processData(sensor);
                if (!tempStorage.isEmpty()) {
                    for (GSNStorageElement tempElement : tempStorage) {
                        storageElements.add(tempElement);
                    }
                }
            } catch (EndymionException e) {
                logger.logMessage(e);
            }
        }
    }

    /**
     * Helper function which outputs created storage elements
     * @param storageElements - list of storage elements
     */
    private void outputStorageElements (List<GSNStorageElement> storageElements) {

        StringBuilder builder = new StringBuilder();
        builder.append("----------------Storage Elements----------------------\n");
        for (GSNStorageElement storageElement : storageElements) {
            builder.append(storageElement.getGSNId() + " - " +
                    storageElement.getvSensorName() + " - " +
                    storageElement.getFieldName() + " - " +
                    storageElement.getTimed() + " - " +
                    storageElement.getValue() + " - " +
                    storageElement.getUnit());
            builder.append("\n");
        }

        logger.logMessage(builder.toString());
    }

    /**
     * This method sets parameters needed for processing sensor data
     * @param sensor - collected sensor data
     * @return
     */
    private HashMap<String, String> setProcessingParameters (GSNSensor sensor) {
        HashMap<String, String> processingParameters = new HashMap<String, String>();

        List<String> processingKeys = new ArrayList<String>();
        processingKeys.add("sampling-rate");
        //processingKeys.add("only-last-value");

        for (String key : processingKeys) {
            String value;
            value = configurationManager.getConfigurationParameter(sensor.getGSNId(),
                    "vsensor", sensor.getvSensor(), key);
            if (value == null || value.trim().isEmpty()) {
                value = configurationManager.getConfigurationParameter(sensor.getGSNId(), key);
            }


            processingParameters.put(key, value);
        }

        return processingParameters;
    }

    /**
     * This method is used for getting storage data from last run()
     * @return - sensor data storage elements
     */
    public List<GSNStorageElement> getStorageElements () {
        return storageElements;
    }

    /**
     * This method clears storage data
     */
    public void clearStorageElements () {
        storageElements.clear();
    }

    /**
     * This method is used for initializing alarms defined in configuration
     * file
     */
    protected void initializeAlarms () {
        List<String> gsnIds = configurationManager.getGSNs();


        for (String GSN : gsnIds) {

            /* Setting alarms on GSN level */
            for (String alarm : configurationManager.getAlarms(GSN)) {
                String after = configurationManager.getConfigurationParameter(GSN, "alarm", alarm, "after");
                String onlyOnce = configurationManager.getConfigurationParameter(GSN, "alarm", alarm, "only-once");
                String sendToList = configurationManager.getConfigurationParameter(GSN, "alarm", alarm, "send-to");

                //  added with mqtt implementatio
                String alarmType = configurationManager.getConfigurationParameter(GSN, "alarm", alarm, "alarm-type");
                String alarmId = configurationManager.getConfigurationParameter(GSN, "alarm", alarm, "alarm-id");

                boolean repeat = true;

                if (onlyOnce.equalsIgnoreCase("true")) {
                    repeat = false;
                }

                try {
                    
                    alarmManager.addAlarmHandler(GSN, alarm, alarmType);
                    alarmManager.setRepeat(GSN, alarm, repeat);
                    alarmManager.setTimePeriod(GSN, alarm, after);
                    alarmManager.setAlarmId(GSN, alarm, alarmId);

                    for (String sendTo : sendToList.split(";")) {
                        alarmManager.addSendTo(GSN, alarm, sendTo);
                    }

                    alarmManager.initializeSender(GSN, alarm);

                } catch (EndymionException e) {
                    logger.logMessage(e);
                }
            }

            List<String> vSensors = configurationManager.getVSensors(GSN);

            /* Setting alarms on vSensor level */
            for (String vSensor : vSensors) {
                for (String alarm : configurationManager.getAlarms(GSN, vSensor)) {

                    String after = configurationManager.getConfigurationParameter(GSN, "alarm", vSensor ,alarm, "after");
                    String onlyOnce = configurationManager.getConfigurationParameter(GSN, "alarm", vSensor, alarm, "only-once");
                    String sendToList = configurationManager.getConfigurationParameter(GSN, "alarm", vSensor, alarm, "send-to");

                    //added
                    String alarmType = configurationManager.getConfigurationParameter(GSN, "alarm", vSensor, alarm, "alarm-type");
                    String alarmId = configurationManager.getConfigurationParameter(GSN, "alarm", vSensor, alarm, "alarm-id");

                    boolean repeat = true;

                    if (onlyOnce.equalsIgnoreCase("true")) {
                        repeat = false;
                    }
                    try {
                        alarmManager.addAlarmHandler(GSN, vSensor ,alarm, alarmType);
                        alarmManager.setRepeat(GSN, vSensor,alarm, repeat);
                        alarmManager.setTimePeriod(GSN, vSensor, alarm, after);
                        alarmManager.setAlarmId(GSN, vSensor, alarm, alarmId);

                        for (String sendTo : sendToList.split(";")) {
                            alarmManager.addSendTo(GSN, vSensor, alarm, sendTo);
                        }

                        alarmManager.initializeSender(GSN, vSensor, alarm);
                        // sends message if alarm sender type is MQTT
                        //alarmManager.okMessage(GSN, vSensor, alarm);
                        
                    } catch (EndymionException e) {
                        logger.logMessage(e);
                    }
                }
            }


        }

    }

    /**
     *  Checking if the alarm should be raised
     */
    protected void checkAlarms () {
        alarmManager.checkAlarms();
    }

    /**
     *  Setting configuration sensors with only-last-value parameter
     */
    protected void setConfigurationSensorData () {
        for (GSNConfigurationSensor configurationSensor : dataCollector.getConfigurationSensors()) {
            String GSNId = configurationSensor.getGSNId();
            String vSensor = configurationSensor.getvSensor();

            String value = configurationManager.getConfigurationParameter(GSNId, "vsensor", vSensor, "only-last-value");
            if (value == null || value.trim().isEmpty()) {
                value = configurationManager.getConfigurationParameter(GSNId, "only-last-value");
            }

            if (value.equalsIgnoreCase("true")) {
                configurationSensor.setOnly_last_value(true);
            } else {
                configurationSensor.setOnly_last_value(false);
            }

            value = configurationManager.getConfigurationParameter(GSNId, "vsensor", vSensor, "sampling-rate");
            if (value == null || value.trim().isEmpty()) {
                value = configurationManager.getConfigurationParameter(GSNId, "sampling-rate");
            }

            configurationSensor.setSampling_rate(Double.parseDouble(value));
        }
    }

    private void initializeStorage() throws EndymionException {
        String url = configurationManager.getConfigurationParameter("storage", "url");
        String username = configurationManager.getConfigurationParameter("storage", "username");
        String password = configurationManager.getConfigurationParameter("storage", "password");

        storageManager = new GSNStorageManager(url, username, password);
        storageManager.initializeStorage();
    }

}
