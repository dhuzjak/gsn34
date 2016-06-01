package endymion.collector;

import endymion.collector.connection.GSNConnection;
import endymion.collector.connection.GSNConnectionFactory;
import endymion.collector.processor.GSNConnectionDataProcessor;
import endymion.collector.processor.GSNConnectionDataProcessorXML;
import endymion.sensor.GSNConfigurationSensor;
import endymion.time.GSNTimeManager;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.logger.EndymionLoggerManager;
import endymion.sensor.GSNSensor;
import endymion.sensor.data.GSNSensorData;

import java.text.DateFormat;
import java.util.*;

/**
 * Created by Nikola on 23.02.2015.
 * Class that collects data from GSN and
 * stores it in predefined elements
 */
public class GSNDataCollector {

    /**
     * Data limit - memory management
     */
    public static final int DATA_LIMIT = 10000;

    /**
     * ConnectionDataProcessor object
     */
    protected GSNConnectionDataProcessor connectionDataProcessor;

    /**
     * A map which stores connections based on GSN ID
     */
    protected HashMap<String, GSNConnection> connections;

    /**
     * The sensor which contain data collected from GSN
     */
    protected List<GSNSensor> sensors;

    /**
     * Configuration sensors which contain "instructions" on how
     * to collect data (e.g only last value or all)
     */
    protected List<GSNConfigurationSensor> configurationSensors;

    /**
     * GSNTimeManager instance
     */
    protected GSNTimeManager timeManager;

    /**
     * Projected number of rows in database which sensor data will occupy
     */
    protected long numberOfRows;

    /**
     * Constructor
     */
    public GSNDataCollector () {
        sensors = new ArrayList<GSNSensor>();
        configurationSensors = new ArrayList<GSNConfigurationSensor>();
        connectionDataProcessor = new GSNConnectionDataProcessorXML();
        connections = new HashMap<String, GSNConnection>();
        timeManager = GSNTimeManager.getTimeManager();
        numberOfRows = 0;
    }

    /**
     * Collects data from GSN with id
     * @param id - GSN ID
     */
    public void collectData (String id) {

        sensors.clear();

        /* Getting connection from map */
        GSNConnection connection = getConnectionById(id);

        for (GSNConfigurationSensor configSensor : configurationSensors) {

            /* Checking if the data should be fetched (based on time sampling) */
            if (!timeManager.isSensorReady(id, configSensor.getvSensor())) continue;

            if (isDataLimitReached()) {
                break;
            }

            try {
                String connectionData;

                /* Checking if the only last value should be fetched */
                if (configSensor.isOnly_last_value()) {
                    connectionData = connection.MultiDataOperation(configSensor.getvSensor(), configSensor.getDataFields(),
                            timeManager.getLastTimestamp(id, configSensor.getvSensor()), 1);
                } else {
                    connectionData = connection.MultiDataOperation(configSensor.getvSensor(), configSensor.getDataFields(),
                            timeManager.getLastTimestamp(id, configSensor.getvSensor()), 0);
                }

                /* Processing collected data */
                GSNSensor sensor = connectionDataProcessor.processSensorData(connectionData, configSensor);

                //EndymionLoggerManager.getLoggerManager().logMessage(outputSensorData(sensor));

                /* Clearing data older than timestamp - extra checking */
                sensor = clearObsoleteSensorData(sensor, timeManager.getLastTimestamp(id, sensor.getvSensor()));

                /* Checking if the limit is reached */
                sensor = limitNumberOfDataEntries(sensor, configSensor.getSampling_rate());

                sensor = fetchImages (connection, sensor);

                sensors.add(sensor);

                /* Setting new last received timestamp */
                if (!getLastTimestamp(sensor).isEmpty())
                    timeManager.setLastTimestamp(id, sensor.getvSensor(), getLastTimestamp(sensor));



            } catch (EndymionException e) {
                EndymionLoggerManager.getLoggerManager().logMessage(e);

            }
        }

    }

    /**
     * This method initializes configuration sensors based on endymion config file and GSN configuration
     * @param id - GSN ID
     * @param dataFields - a map containing vSensors as keys and list of fields as values
     * @throws EndymionException
     */
    public void initialize (String id, HashMap<String, List<String>> dataFields) throws EndymionException {

        GSNConnection connection = getConnectionById(id);
        if (connection == null) throw new EndymionException("No connection with ID: " + id, EndymionLoggerEnum.FATAL_ERROR);
        String configuration = connection.GSNOperation();
        configurationSensors = connectionDataProcessor.processSensorConfiguration(configuration, dataFields);
        for (GSNConfigurationSensor configurationSensor : configurationSensors) {
            configurationSensor.setGSNId(id);
        }
    }

    /**
     * Initialises time sampling on GSN level
     * @param id - GSN ID
     * @param timeSampling - time sampling value (e.g 30m or 2h)
     */
    public void initializeTimeSampling (String id, String timeSampling) {
        timeManager.setGsnTimeSampling(id, timeSampling);
    }

    /**
     * Initialises time sampling on vSensor level
     * @param id - GSN ID
     * @param vSensorName - vSensor name
     * @param timeSampling - time sampling value (e.g 30m or 2h)
     */
    public void initializeTimeSampling (String id, String vSensorName, String timeSampling) {
        timeManager.setTimeSampling(id, vSensorName, timeSampling);
    }

    /**
     * Fetches the last timestamp received from vSensor
     * @param id - GSN ID
     * @param vSensorName - vSensor name
     * @return
     */
    public String getLastTimestamp (String id, String vSensorName) {
        return timeManager.getLastTimestamp(id, vSensorName);
    }

    /**
     * Instantiates a new connection for GSN
     * @param gsnId -GSN ID
     * @param connectionType - http, https
     * @param connectionParameters - ipAddress, port...
     * @throws EndymionException - wrong connection type or unexpected connection parameters
     */
    public void setConnectionParameters (String gsnId, String connectionType, String... connectionParameters) throws EndymionException {
        if (connections.containsKey(gsnId)) return;

        GSNConnection connection = GSNConnectionFactory.getConnection(connectionType);
        connection.setConnectionParameters(connectionParameters);
        connections.put(gsnId, connection);
    }

    /**
     * Getter for sensor data
     * @return - GSNSensor list
     */
    public List<GSNSensor> getSensorData () {
        return sensors;
    }

    /**
     * Getter for configuration sensor data
     * @return - GSNConfigurationSensor list
     */
    public List<GSNConfigurationSensor> getConfigurationSensors () { return configurationSensors; }

    /**
     * Gets connection by ID
     * @param id - GSN ID
     * @return - GSNConnection object
     */
    protected GSNConnection getConnectionById (String id) {
        return connections.get(id);
    }

    /**
     * Extracts the last timestamp (chronologically) from collected sensor data
     * @param sensor - GSNSensor object
     * @return - timestamp
     */
    protected String getLastTimestamp (GSNSensor sensor) {

        List<String> fields = sensor.getDataFields();

        if (!fields.isEmpty()) {
            try {
                HashMap<String, String> timeAndValue = sensor.getSensorData(fields.get(0)).getTimeAndValue();
                String maxDate = "";
                DateFormat format = GSNTimeManager.dateFormat;

                for (String time : timeAndValue.keySet()) {
                    if (maxDate.isEmpty() || format.parse(maxDate).before(format.parse(time))) {
                        maxDate = time;
                    }
                }

                return maxDate;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    /**
     * Clears data that is older than last timestamp
     * @param sensor - GSNSensor object
     * @param lastTimestamp - last received timestamp before this collection
     * @return - GSNSensor object without "old" data
     * @throws EndymionException - wrong timestamp format
     */
    protected GSNSensor clearObsoleteSensorData (GSNSensor sensor, String lastTimestamp) throws EndymionException {

        List<String> fields = sensor.getDataFields();

        if (lastTimestamp == null ||lastTimestamp.trim().isEmpty()) return sensor;

        DateFormat format = GSNTimeManager.dateFormat;
        List<String> obsoleteValues = new ArrayList<String>();

        for (String field : fields) {
            GSNSensorData sensorData = sensor.getSensorData(field);
            HashMap<String, String> timeAndValue = sensorData.getTimeAndValue();

            Set<String> keyset = timeAndValue.keySet();
            for (String time : keyset) {
                try {
                    if (format.parse(time).before(format.parse(lastTimestamp))
                            || format.parse(time).equals(format.parse(lastTimestamp))) {
                        obsoleteValues.add(time);
                    }
                } catch (Exception e) {
                    throw new EndymionException(e.getMessage(), EndymionLoggerEnum.WARNING);
                }
            }

            for (String time : obsoleteValues) {
                timeAndValue.remove(time);
            }

            obsoleteValues.clear();

            sensorData.setTimeAndValue(timeAndValue);
        }

        return sensor;
    }

    /**
     *
     * @param numberOfRows
     */
    public void setNumberOfRows (long numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    /**
     *
     * @return
     */
    public boolean isDataLimitReached () {

        return (this.numberOfRows >= DATA_LIMIT);

    }

    /**
     * Method which limits the number of data entries
     * @param sensor - object containg data
     * @param sampling_rate - projected percentage of data which will be left after processing
     * @return - sensor object with all or less data if the limit is reached
     * @throws EndymionException
     */
    protected GSNSensor limitNumberOfDataEntries (GSNSensor sensor, double sampling_rate) throws EndymionException {

        String field = sensor.getDataFields().get(0);
        if (field == null) return sensor;

        /* Extract timestamps */
        Set<String> timestamps = sensor.getSensorData(field).getTimeAndValue().keySet();

        /* If limit is not reached */
        if ((timestamps.size() * sampling_rate) + this.numberOfRows < DATA_LIMIT) {
            this.numberOfRows += Math.round(timestamps.size() * sampling_rate);
        } else if (!isDataLimitReached()) {
            List<Date> timestampDates = new ArrayList<Date>();

            /* Setting timestamps in Date list */
            for (String timestamp : timestamps) {
                try {
                    timestampDates.add(GSNTimeManager.dateFormat.parse(timestamp));
                } catch (Exception e) {
                    throw new EndymionException("Wrong date format : " + timestamp);
                }
            }

            /* Sorting dates and reversing them so the latest come first */
            Collections.sort(timestampDates);
            Collections.reverse(timestampDates);

            List<String> cleanupList = new ArrayList<String>();


            long allowedLimit = DATA_LIMIT - this.numberOfRows;
            allowedLimit = Math.round(allowedLimit / sampling_rate);
            long cleanupSize = timestampDates.size() - allowedLimit;

            /* Setting timestamps that will be removed from sensor data */
            for (int i = 0; i < cleanupSize; ++i) {
                cleanupList.add(GSNTimeManager.dateFormat.format(timestampDates.get(i)));
            }


            /* Removing values with timestamp from all fields */
            for (String field1 : sensor.getDataFields()) {

                HashMap<String, String> timeAndValue = sensor.getSensorData(field1).getTimeAndValue();
                for (String timestamp : cleanupList) {
                    timeAndValue.remove(timestamp);
                }

            }

            this.numberOfRows = DATA_LIMIT;

            EndymionLoggerManager.getLoggerManager().logMessage("Endymion data limit reached!" +
                    " Other data shall be fetched in the next collection cycle.\n" +
                    "Data limit: " + DATA_LIMIT);

        }

        return sensor;
    }

    /**
     * Helper function for fetching images
     * @param connection - connection object
     * @param sensor - sensor containg data
     * @return
     */
    protected GSNSensor fetchImages (GSNConnection connection, GSNSensor sensor) {

        for (String field : sensor.getDataFields()) {
            try {
                GSNSensorData sensorData = sensor.getSensorData(field);
                if (sensorData.getUnit().toLowerCase().contains("binary")) {
                    fetchImage(connection, sensorData);
                }
            } catch (EndymionException e) {
                EndymionLoggerManager.getLoggerManager().logMessage(e);
            }
        }

        return sensor;
    }

    /**
     * Helper method for fetching images
     * @param connection - connection object
     * @param sensorData - data which contains URL to an image
     */
    protected void fetchImage (GSNConnection connection, GSNSensorData sensorData) {

        for (String timestamp : sensorData.getTimeAndValue().keySet()) {
            try {
                String imageS = connection.FieldOperation(sensorData.getTimeAndValue().get(timestamp));
                sensorData.getTimeAndValue().put(timestamp, imageS);
            } catch (EndymionException e) {
                EndymionLoggerManager.getLoggerManager().logMessage(e);
            }
        }
    }

    /**
     * Helper function which outputs collected sensor data
     * @param sensors - list of collected sensor data
     */
    public void outputSensorData (List<GSNSensor> sensors) {

        StringBuilder builder = new StringBuilder();

        for (GSNSensor sensor : sensors) {
            builder.append(outputSensorData(sensor));
        }

        if (!builder.toString().isEmpty()) {
            EndymionLoggerManager.getLoggerManager().logMessage(builder.toString());
        }
    }

    /**
     * Helper function for debugging
     * @param sensor
     * @return
     */
    private String outputSensorData (GSNSensor sensor) {

        StringBuilder builder = new StringBuilder();

        builder.append("\n");
        builder.append("-----------------VS-----------------\n");
        builder.append("VSName: " + sensor.getvSensor() + "\n");

        for (String dataField : sensor.getDataFields()) {
            try {
                GSNSensorData sensorData = sensor.getSensorData(dataField);
                builder.append("\n");
                builder.append("-----------------FIELD-----------------\n");
                builder.append("Field name: " + sensorData.getDataField() + "\n");
                builder.append("Unit: " + sensorData.getUnit() + "\n");

                builder.append("Number of data entries: " + sensorData.getTimeAndValue().size() + "\n");
            } catch (Exception e) {
                EndymionLoggerManager.getLoggerManager().logMessage(e.getMessage(), EndymionLoggerEnum.WARNING);
            }

        }

        return builder.toString();
    }




}
