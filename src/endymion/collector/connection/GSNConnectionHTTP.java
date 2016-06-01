package endymion.collector.connection;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import endymion.time.GSNTimeManager;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.logger.EndymionLoggerManager;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Nikola on 28.02.2015.
 * This class extends GSNConnection and is used
 * for connecting to GSN via HTTP
 */
public class GSNConnectionHTTP extends GSNConnection {

    protected String ipAddress;
    protected String port;
    protected String username;
    protected String password;

    /**
     *
     * @param parameters - parameters essential for establishing a connection to GSN
     * @throws EndymionException
     */
    @Override
    public void setConnectionParameters(String... parameters) throws EndymionException {
        if (parameters.length == 4) {
            this.ipAddress = parameters[0];
            this.port = parameters[1];
            this.username = parameters[2];
            this.password = parameters[3];
        } else {
            throw new EndymionException("Wrong number of connection parameters");
        }
    }

    /**
     * HTTP /gsn query building
     * @return output of the /gsn operation as returned by a GSN server
     * @throws EndymionException - HTTP connection failed
     */
    @Override
    public String GSNOperation() throws EndymionException {
        URL url;
        try {
            url = new URL(getProtocol() + "://" + ipAddress + ":" + port + "/gsn");
        } catch (Exception e) {
            throw new EndymionException(e.getMessage() + ". URL:" + getProtocol() + "://" + ipAddress + ":" + port + "/gsn" ,
                    EndymionLoggerEnum.ERROR);
        }

        return readFromConnection(url, username + ":" + password);

    }

    /**
     * /data operation
     * @param vSensor - Name of the virtual sensor
     * @param dataFields - Field names of the virtual sensor
     * @param number - number of entries to fetch
     * @return
     * @throws EndymionException
     */
    @Override
    public String DataOperation(String vSensor, List<String> dataFields, int number) throws EndymionException {

        String host = getProtocol() + "://" + ipAddress + ":" + port + "/data";
        String query = "?vsname=" + vSensor;

        for (String field : dataFields) {
            query+= "&fields=" + field;
        }

        /**
         * Field "timed" represents timestamp and is required in every
         * /data operation
         */
        if (!dataFields.contains("timed"))
            query+= "&fields=timed";

        if (number > 0) {
            query+= "&nb=" + String.valueOf(number);
        }

        URL url;
        try {
            url = new URL(host + query);
        } catch (Exception e) {
            throw new EndymionException(e.getMessage() + ". URL: " + host + query, EndymionLoggerEnum.ERROR);
        }
        EndymionLoggerManager.getLoggerManager().logMessage("URL: " + host + query + "\n");

        return readFromConnection(url, username + ":" + password);
    }

    /**
     * /multidata operation
     * @param vSensor - Name of the virtual sensor
     * @param dataFields - Field names of the virtual sensor
     * @param lastTimestamp - Timestamp of the most recently fetched data
     * @param number - number of entries to fetch
     * @return
     * @throws EndymionException
     */
    @Override
    public String MultiDataOperation(String vSensor, List<String> dataFields, String lastTimestamp, int number) throws EndymionException {

        String host = getProtocol() + "://" + ipAddress + ":" + port + "/multidata";
        String query = "?vs[0]=" + vSensor;

        query += "&field[0]=";
        for (String field : dataFields) {
            query += field + ",";
        }

        if (query.endsWith(",")) {
            query = query.substring(0, query.length() - 1);
        }

        /**
         * Download format also be csv (CSV connection data processor is needed in that case)
         */
        String download_format = "&download_format=xml";
        String time_format="&time_format=iso";
        String nb = "";
        if (number > 0) {
            nb = "&nb=SPECIFIED&nb_value=" + number;
        }


        String from = "";
        URL url;
        try {
            /**
             * From parameter is used for getting only "new" data
             * Its format is specified in GSNTimeManager.multidataDateFormat
             */
            if (lastTimestamp != null && !lastTimestamp.trim().isEmpty()) {
                from = "&from=" + GSNTimeManager.multidataDateFormat.format(GSNTimeManager.dateFormat.parse(lastTimestamp));
            }
            url = new URL(host + query + time_format + download_format + nb + from);
        } catch (Exception e) {
            throw new EndymionException(e.getMessage() + ". URL: " + host + query + time_format + download_format + nb + from,
                    EndymionLoggerEnum.ERROR);
        }

        EndymionLoggerManager.getLoggerManager().logMessage("URL: " + host + query + time_format + download_format + nb + from + "\n");

        return readFromConnection(url, username + ":" + password);

    }

    /**
     * HTTP /field operation
     * @param query - query which is received from GSN
     * @return - String encoded image
     * @throws EndymionException
     */
    public String FieldOperation (String query) throws EndymionException {

        String host = getProtocol() + "://" + ipAddress + ":" + port;

        if (query.startsWith("field")) {
            query = "/" + query;
        }

        URL url;
        try {
            url = new URL(host + query);
        } catch (Exception e) {
            throw new EndymionException(e.getMessage() + ". URL: " + host + query,
                    EndymionLoggerEnum.ERROR);
        }

        EndymionLoggerManager.getLoggerManager().logMessage("URL: " + host + query + "\n");
        return readImageFromConnection(url, username + ":" + password);

    }

    /**
     *
     * @param url - HTTP url for wanted operation
     * @param authorization - username:password
     * @return The response (body) to a HTTP request
     * @throws EndymionException - HTTP connection failed
     */
    protected String readFromConnection (URL url, String authorization) throws EndymionException {

        try {

            InputStream stream = establishConnection(url, authorization);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder stringBuilder = new StringBuilder();

            String data;
            String line;

            /**
             * Reading from connection
             */
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            /**
             * Closing the connection
             */
            stream.close();

            data = stringBuilder.toString();
            return data;
        } catch (Exception e) {
            throw new EndymionException(e.getMessage() + url.toExternalForm(), EndymionLoggerEnum.ERROR);
        }
    }

    /**
     * Reading image from connection - Base64 encoded
     * @param url - URL to image
     * @param authorization - username:password Base64 encoded
     * @return String of Base64 encoded image
     * @throws EndymionException
     */
    protected String readImageFromConnection (URL url, String authorization) throws EndymionException {
        try {

            InputStream stream = establishConnection(url, authorization);

            byte[] bytes = IOUtils.toByteArray(stream);
            String output = Base64.encode(bytes);

            stream.close();
            return output;

        } catch (Exception e) {
            throw new EndymionException(e.getMessage() + url.toExternalForm(), EndymionLoggerEnum.ERROR);
        }

    }

    /**
     * Method which establishes connection
     * @param url - connection
     * @param authorization - username:password Base64 encoded
     * @return InputStream from connection
     * @throws Exception
     */
    protected InputStream establishConnection (URL url, String authorization) throws Exception {
        HttpURLConnection connection = getConnection(url);
        connection.setRequestMethod("GET");

        /**
         * Basic authorization is needed for GSN HTTP request
         */
        connection.setRequestProperty("Authorization", Base64.encode(authorization.getBytes()));
        connection.connect();

        return connection.getInputStream();
    }

    /**
     *
     * @return "http"
     */
    protected String getProtocol () {
        return "http";
    }

    /**
     * Gets HTTPUrlConnection object
     * @param url - URL object
     * @return - HTTPConnection object
     * @throws Exception
     */
    protected HttpURLConnection getConnection (URL url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setConnectTimeout(60000); // timeouts after a minute
        return connection;
    }
}
