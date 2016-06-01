package endymion.configuration.validator;

import java.util.Arrays;
import endymion.configuration.data.GSNConfiguration;
import endymion.configuration.data.GSNStorageConfiguration;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;

import java.util.List;

/**
 * Created by Nikola on 06.03.2015.
 */
public class GSNConfigurationValidator {

    private static final List<String> allowedConnectionTypes = Arrays.asList("http", "https");

    public void validateGSNConfiguration (GSNConfiguration configuration) throws EndymionException {

        if (configuration.getID().equals(GSNStorageConfiguration.STORAGE_ID)) {
            return;
        }

        checkConnectionType(configuration);
        checkReadParameter(configuration);
        checkSamplingRateParameter(configuration);
        checkSamplingTimeParameter(configuration);
        checkAlarmAfterParameter(configuration);


    }

    protected void checkConnectionType (GSNConfiguration configuration) throws EndymionException {
        String connectionType = configuration.getConfigurationParameter("connectionType");

        if (!allowedConnectionTypes.contains(connectionType)) {
            throw new EndymionException("Unsupported connection type: " + connectionType, EndymionLoggerEnum.ERROR);
        }

    }

    protected void checkReadParameter (GSNConfiguration configuration) throws EndymionException {
        String read_sensors = configuration.getConfigurationParameter("read-sensors");

        if (read_sensors.equals("part") && configuration.getVSensors().isEmpty()) {
            throw new EndymionException("In GSN: " + configuration.getID() +
                    ". Parameter read-sensors is 'part', at least one vsensor element expected", EndymionLoggerEnum.ERROR);
        }

        List<String> vsensors = configuration.getVSensors();

        for (String vsensor : vsensors) {
            String read_fields = configuration.getConfigurationParameter("vsensor", vsensor, "read-data-fields");
            if (read_fields.equalsIgnoreCase("part") && configuration.getSensorFields(vsensor).isEmpty()) {
                throw new EndymionException("In GSN: " + configuration.getID() +
                        ". Parameter read-data-fields is 'part' in vsensor " + vsensor +
                        ", at least one field element expected", EndymionLoggerEnum.ERROR);
            }
        }
    }

    protected void checkSamplingRateParameter (GSNConfiguration configuration) throws EndymionException {

        String sampling_rate_s = configuration.getConfigurationParameter("sampling-rate");
        double sampling_rate;
        try {
            sampling_rate = Double.parseDouble(sampling_rate_s);
        } catch (Exception e) {
            throw new EndymionException("In GSN: " + configuration.getID() +
                    ". Parameter sampling-rate must be a double between 0 and 1.", EndymionLoggerEnum.ERROR);
        }

        if (sampling_rate > 1 || sampling_rate < 0) {
            throw new EndymionException("In GSN: " + configuration.getID() +
                    ". Parameter sampling-rate must be a double between 0 and 1.", EndymionLoggerEnum.ERROR);
        }

        List<String> vsensors = configuration.getVSensors();

        for (String vsensor : vsensors) {
            sampling_rate_s = configuration.getConfigurationParameter("vsensor", vsensor, "sampling-rate");

            if (sampling_rate_s == null) continue;

            try {
                sampling_rate = Double.parseDouble(sampling_rate_s);
            } catch (Exception e) {
                throw new EndymionException("In GSN: " + configuration.getID() + " and vsensor " + vsensor +
                        ". Parameter sampling-rate must be a double between 0 and 1.", EndymionLoggerEnum.ERROR);
            }

            if (sampling_rate > 1 || sampling_rate < 0) {
                throw new EndymionException("In GSN: " + configuration.getID() + " and vsensor " + vsensor +
                        ". Parameter sampling-rate must be a double between 0 and 1.", EndymionLoggerEnum.ERROR);
            }
        }
    }

    protected void checkSamplingTimeParameter (GSNConfiguration configuration) throws EndymionException {

        String samplingTimeS = configuration.getConfigurationParameter("sampling-time");

        if (!samplingTimeS.endsWith("h") && !samplingTimeS.endsWith("m")) {
            throw new EndymionException("In GSN: " + configuration.getID() +
                    ". Parameter sampling-time must end with h (hours) or m (minutes).", EndymionLoggerEnum.ERROR);
        }

        samplingTimeS = samplingTimeS.substring(0, samplingTimeS.length() - 1);

        try {
            Integer.parseInt(samplingTimeS);
        } catch (Exception e) {
            throw new EndymionException("In GSN: " + configuration.getID() +
                    ". Parameter sampling-time must be an integer ending with h (hours) or m (minutes): "
                    + samplingTimeS, EndymionLoggerEnum.ERROR);
        }

        List<String> vsensors = configuration.getVSensors();

        for (String vsensor : vsensors) {
            samplingTimeS = configuration.getConfigurationParameter("vsensor", vsensor, "sampling-time");

            if (samplingTimeS == null || samplingTimeS.trim().isEmpty()) continue;

            if (!samplingTimeS.endsWith("h") && !samplingTimeS.endsWith("m")) {
                throw new EndymionException("In GSN: " + configuration.getID() + " and vsensor " + vsensor +
                        ". Parameter sampling-time must end with h (hours) or m (minutes): " + samplingTimeS,
                        EndymionLoggerEnum.ERROR);
            }

            samplingTimeS = samplingTimeS.substring(0, samplingTimeS.length() - 1);

            try {
                Integer.parseInt(samplingTimeS);
            } catch (Exception e) {
                throw new EndymionException("In GSN: " + configuration.getID() + " and vsensor " + vsensor +
                        ".Parameter sampling-time must be an integer ending with h (hours) or m (minutes): "
                        + samplingTimeS, EndymionLoggerEnum.ERROR);
            }
        }

    }

    protected void checkAlarmAfterParameter (GSNConfiguration configuration) throws EndymionException {

        for (String alarm : configuration.getAlarms()) {

            String afterParamS = configuration.getConfigurationParameter("alarm", alarm, "after");

            if (!afterParamS.endsWith("h") && !afterParamS.endsWith("m")) {
                throw new EndymionException("In GSN: " + configuration.getID() + " alarm " + alarm +
                        ". Parameter after must end with h (hours) or m (minutes): " + afterParamS,
                        EndymionLoggerEnum.ERROR);
            }

            afterParamS = afterParamS.substring(0, afterParamS.length() - 1);

            try {
                Integer.parseInt(afterParamS);
            } catch (Exception e) {
                throw new EndymionException("In GSN: " + configuration.getID() + " alarm " + alarm +
                        ". Parameter after must be an integer ending with h (hours) or m (minutes): "
                        + afterParamS, EndymionLoggerEnum.ERROR);
            }
        }

        List<String> vsensors = configuration.getVSensors();

        for (String vsensor : vsensors) {
            for (String alarm : configuration.getAlarms(vsensor)) {

                String afterParamS = configuration.getConfigurationParameter("alarm", vsensor, alarm, "after");

                if (!afterParamS.endsWith("h") && !afterParamS.endsWith("m")) {
                    throw new EndymionException("In GSN: " + configuration.getID() + " alarm " + alarm + "in vsensor " + vsensor +
                            ". Parameter after must end with h (hours) or m (minutes): " + afterParamS,
                            EndymionLoggerEnum.ERROR);
                }

                afterParamS = afterParamS.substring(0, afterParamS.length() - 1);

                try {
                    Integer.parseInt(afterParamS);
                } catch (Exception e) {
                    throw new EndymionException("In GSN: " + configuration.getID() + " alarm " + alarm + "in vsensor " + vsensor +
                            ". Parameter after must be an integer ending with h (hours) or m (minutes): "
                            + afterParamS, EndymionLoggerEnum.ERROR);
                }
            }
        }
    }


}
