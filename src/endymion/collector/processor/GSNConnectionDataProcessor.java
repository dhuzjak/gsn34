package endymion.collector.processor;

import endymion.exception.EndymionException;
import endymion.sensor.GSNConfigurationSensor;
import endymion.sensor.GSNSensor;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 23.02.2015.
 * This abstract class is used for parsing the data
 * fetched from server
 */
public abstract class GSNConnectionDataProcessor {

    /**
     * This function is used for parsing the data which is the result of the query for GSN sensor data
     * @param connectionData - sensor data fetched from GSN server
     * @param sensor - the GSNSensor object which corresponds to a sensor data which is
     *               held in connectionData
     * @return GSNSensor object with new data
     * @throws EndymionException - Data parsing failed
     */
    public abstract GSNSensor processSensorData (String connectionData, GSNSensor sensor) throws EndymionException;

    /**
     *
     * @param connectionData - configuration data fetched from GSN server
     * @param configuration - A map which contains sensor names and their respective fields
     * @return - A list of GSNSensor objects which were part of the configuration and returned by a GSN server
     * @throws EndymionException - Data parsing failed
     */
    public abstract List <GSNConfigurationSensor> processSensorConfiguration (String connectionData,
                                                     HashMap<String, List<String>> configuration) throws EndymionException;
}
