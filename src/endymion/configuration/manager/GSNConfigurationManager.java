package endymion.configuration.manager;

import endymion.configuration.data.GSNConfiguration;
import endymion.configuration.data.GSNStorageConfiguration;
import endymion.configuration.reader.GSNConfigurationReader;
import endymion.configuration.reader.GSNConfigurationReaderXML;
import endymion.configuration.processor.GSNConfigurationDataProcessor;
import endymion.configuration.processor.GSNConfigurationDataProcessorXML;
import endymion.configuration.validator.GSNConfigurationValidator;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.logger.EndymionLoggerManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikola on 23.02.2015.
 * This class is used as an interface for fetching
 * endymion.configuration data
 */
public class GSNConfigurationManager {

    /**
     * GSN configuration objects
     */
    List <GSNConfiguration> configurations;

    /**
     * Configuration data processor
     */
    GSNConfigurationDataProcessor dataProcessor;

    /**
     * Configuration reader object
     */
    GSNConfigurationReader configurationReader;

    /**
     * Configuration validator object
     */
    GSNConfigurationValidator configurationValidator;

    /**
     * Constructor
     */
    public GSNConfigurationManager () {
        configurationReader = new GSNConfigurationReaderXML("endymion_configuration/gsn_config.xml",
                "endymion_configuration/gsn_config.dtd");
        dataProcessor = new GSNConfigurationDataProcessorXML();
        configurationValidator = new GSNConfigurationValidator();
    }

    /**
     * Method for reading configuration
     * @throws EndymionException
     */
    public void readConfiguration () throws EndymionException {
        List<String> configuration;
        configuration = configurationReader.readConfiguration();
        this.configurations = dataProcessor.processConfiguration(configuration);
        for (GSNConfiguration gsnConfiguration : this.configurations) {
            configurationValidator.validateGSNConfiguration(gsnConfiguration);
        }

        outputConfig(this.configurations);
    }

    /**
     * Getter for GSNs
     * @return - list of GSN IDs
     */
    public List <String> getGSNs () {
        List<String> gsnIDs = new ArrayList<String>();

        for (GSNConfiguration gsn : configurations) {
            if (!gsn.getID().equals(GSNStorageConfiguration.STORAGE_ID))
                gsnIDs.add(gsn.getID());
        }

        return gsnIDs;

    }

    /**
     * Getter for configuration parameters
     * @param GSNId - GSN ID
     * @param parameters - keys for fetching parameters
     * @return - parameter value
     */
    public String getConfigurationParameter (String GSNId, String... parameters) {
        String configParam = null;
        GSNConfiguration gsnConfig;
        try {
            gsnConfig = getGSNConfigurationById(GSNId);
            configParam = gsnConfig.getConfigurationParameter(parameters);
        } catch (EndymionException e) {

        }

        return configParam;
    }

    /**
     * Getter for vSensors
     * @param GSNId - GSN ID
     * @return list of vSensor names
     */
    public List<String> getVSensors (String GSNId) {
        try {
            return getGSNConfigurationById(GSNId).getVSensors();
        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
            return null;
        }
    }

    /**
     * Getter for sensor field
     * @param GSNId - GSN ID
     * @param vSensor - vSensor name
     * @return list of sensor field names
     */
    public List<String> getVSensorFields (String GSNId, String vSensor) {
        try {
            return getGSNConfigurationById(GSNId).getSensorFields(vSensor);
        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
            return null;
        }
    }

    /**
     * Getter for alarms
     * @param GSNId - GSN ID
     * @return list of alarm names
     */
    public List<String> getAlarms (String GSNId) {
        try {
            return getGSNConfigurationById(GSNId).getAlarms();
        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
            return null;
        }
    }

    /**
     * Getter for vSensor alarms
     * @param GSNId - GSN ID
     * @param vSensor - sensor name
     * @return list of sensor alarms
     */
    public List<String> getAlarms (String GSNId, String vSensor) {
        try {
            return getGSNConfigurationById(GSNId).getAlarms(vSensor);
        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
            return null;
        }
    }

    /**
     * Getter for configuration object
     * @param GSNId - GSN ID
     * @return configuration object
     * @throws EndymionException
     */
    protected GSNConfiguration getGSNConfigurationById (String GSNId) throws EndymionException {
        for (GSNConfiguration gsn : configurations) {
            if (gsn.getID().equals(GSNId)) {
                return gsn;
            }
        }

        throw new EndymionException("GSNConfiguration with ID " + GSNId + " not found", EndymionLoggerEnum.WARNING);
    }

    public boolean useOwnStorage () {
        try {
            getGSNConfigurationById(GSNStorageConfiguration.STORAGE_ID);
            return true;
        } catch (EndymionException e) {
            return false;
        }
    }

    /**
     * Helper method for debugging
     * @param configurations
     */
    private void outputConfig (List<GSNConfiguration> configurations) {

        StringBuilder builder = new StringBuilder();

        try {

            for (GSNConfiguration config : configurations) {

                if (config.getID().equals(GSNStorageConfiguration.STORAGE_ID)) {
                    builder.append("\n");
                    builder.append("----------Storage------------\n");
                    builder.append("ID: " + config.getID() + "\n");
                    builder.append("URL: " + config.getConfigurationParameter("url") + "\n");
                    builder.append("Username: " + config.getUsername() + "\n");
                    builder.append("Password: " + config.getPassword() + "\n");

                } else {
                    builder.append("\n");
                    builder.append("----------GSN------------\n");
                    builder.append("ID: " + config.getID() + "\n");
                    builder.append("Username: " + config.getUsername() + "\n");
                    builder.append("Password: " + config.getPassword() + "\n");
                    builder.append("Connection type: " + config.getConfigurationParameter("connectionType") + "\n");
                    builder.append("Sampling-rate:" + config.getConfigurationParameter("sampling-rate") + "\n");
                    builder.append("Sampling-time: " + config.getConfigurationParameter("sampling-time") + "\n");
                    builder.append("Only-last-value:" + config.getConfigurationParameter("only-last-value") + "\n");
                    builder.append("Read-sensors:" + config.getConfigurationParameter("read-sensors") + "\n");

                    List<String> alarms = config.getAlarms();

                    for (String alarm : alarms) {
                        builder.append("\n");
                        builder.append("----------ALARM------------\n");
                        builder.append("Alarm name: " + alarm + "\n");
                        builder.append("Type: " + config.getConfigurationParameter("alarm", alarm, "alarm-type") + "\n");
                        builder.append("After: " + config.getConfigurationParameter("alarm", alarm, "after") + "\n");
                        builder.append("Only-once: " + config.getConfigurationParameter("alarm", alarm, "only-once") + "\n");
                        builder.append("Send-to: " + config.getConfigurationParameter("alarm", alarm, "send-to") + "\n");
                    }

                    List<String> vsensors = config.getVSensors();

                    for (String vs : vsensors) {
                        builder.append("\n");
                        builder.append("----------VSENSOR------------\n");
                        builder.append("VSname: " + config.getConfigurationParameter("vsensor", vs, "vsname") + "\n");
                        builder.append("Sampling-rate:" + config.getConfigurationParameter("vsensor", vs, "sampling-rate") + "\n");
                        builder.append("Sampling-time: " + config.getConfigurationParameter("vsensor", vs, "sampling-time") + "\n");
                        builder.append("Only-last-value:" + config.getConfigurationParameter("vsensor", vs, "only-last-value") + "\n");
                        builder.append("Read-data-fields:" + config.getConfigurationParameter("vsensor", vs, "read-data-fields") + "\n");

                        List<String> fields = config.getSensorFields(vs);

                        for (String field : fields) {
                            builder.append("\n");
                            builder.append("----------FIELD------------\n");
                            builder.append("Field name: " + config.getConfigurationParameter("field", vs, field, "name") + "\n");
                        }

                        alarms = config.getAlarms(vs);

                        for (String alarm : alarms) {
                            builder.append("\n");
                            builder.append("----------ALARM------------\n");
                            builder.append("Alarm name: " + config.getConfigurationParameter("alarm", vs, alarm, "name") + "\n");
                            builder.append("After:" + config.getConfigurationParameter("alarm", vs, alarm, "after") + "\n");
                            builder.append("Only-once: " + config.getConfigurationParameter("alarm", vs, alarm, "only-once") + "\n");
                            builder.append("Send-to: " + config.getConfigurationParameter("alarm", vs, alarm, "send-to") + "\n");
                        }


                    }
                }
            }
            builder.append("\n");

        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
        }

        EndymionLoggerManager.getLoggerManager().logMessage(builder.toString());
    }


}
