package endymion.collector.connection;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;

/**
 * Created by Nikola on 24.03.2015.
 * The Factory class which returns appropriate HTTPConnection instance
 * based on connectionType
 */
public class GSNConnectionFactory {

    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    /**
     *
     * @param connectionType - The parameter which defines the type of connection
     * @return - GSNConnection object
     * @throws EndymionException - Wrong parameter given
     */
    public static GSNConnection getConnection (String connectionType) throws EndymionException {
        if (connectionType.equalsIgnoreCase(HTTP)) {
            return new GSNConnectionHTTP();
        } else if (connectionType.equalsIgnoreCase(HTTPS)) {
            return new GSNConnectionHTTPS();
        } else {
            throw new EndymionException("Wrong connection type given: " + connectionType, EndymionLoggerEnum.FATAL_ERROR);
        }
    }
}
