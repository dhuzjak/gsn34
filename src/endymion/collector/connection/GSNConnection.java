package endymion.collector.connection;

import endymion.exception.EndymionException;

import java.util.List;
/**
 * Created by Nikola on 23.02.2015.
 * Class that connects to GSN and fetches
 * configuration and data
 */
public abstract class GSNConnection {

    /**
     * A method which is used for setting up connection parameters
     * (e.g. host, port, username, password)
     * @param parameters - parameters essential for establishing a connection to GSN
     * @throws EndymionException - wrong number or wrong format of parameters
     */
    public abstract void setConnectionParameters (String... parameters) throws EndymionException;

    /**
     *
     * @return - An output returned by invoking /gsn
     * @throws EndymionException - establishing connection failed
     */
    public abstract String GSNOperation () throws EndymionException;

    /**
     *
     * @param vSensor - Name of the virtual sensor
     * @param dataFields - Field names of the virtual sensor
     * @param number - number of entries to fetch
     * @return -  An output returned by invoking /data with query
     * @throws EndymionException - establishing connection failed
     */
    public abstract String DataOperation (String vSensor, List<String> dataFields, int number) throws EndymionException;

    /**
     *
     * @param vSensor - Name of the virtual sensor
     * @param dataFields - Field names of the virtual sensor
     * @param lastTimestamp - Timestamp of the most recently fetched data
     * @param number - number of entries to fetch
     * @return -  An output returned by invoking /multidata with query
     * @throws EndymionException - establishing connection failed
     */
    public abstract String MultiDataOperation (String vSensor, List<String> dataFields, String lastTimestamp, int number) throws EndymionException;

    /**
     * This method is used for getting binary data (photos etc.) from GSN
     * The query is fetch via multidata operation.
     * @param query - query which is received from GSN
     * @return - binary element
     */
    public abstract String FieldOperation (String query) throws EndymionException;

}
