package endymion.collector.processor;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.logger.EndymionLoggerManager;
import endymion.sensor.GSNConfigurationSensor;
import endymion.time.GSNTimeManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.apache.commons.lang.StringEscapeUtils;
import endymion.sensor.GSNSensor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;

/**
 * Created by Nikola on 28.02.2015.
 * This class is used for processing XML connection data
 */
public class GSNConnectionDataProcessorXML extends GSNConnectionDataProcessor {

    /**
     *
     * @param connectionData - sensor data fetched from GSN server
     * @param sensor - the GSNSensor object which corresponds to a sensor data which is
     *               held in connectionData
     * @return - A sensor object with its data
     * @throws EndymionException
     */
    @Override
    public GSNSensor processSensorData(String connectionData, GSNSensor sensor) throws EndymionException {

        /**
         * Building a DOM object from connection data
         */
        Node data = buildDOM(connectionData);

        /**
         * In case of /multidata format
         */
        if (data.getNodeName().equals("result")) {
            data = data.getFirstChild();
            while (!data.getNodeName().equals("data")) {
                data = data.getNextSibling();
            }
        }

        if (data.getFirstChild() == null) {
            throw new EndymionException("No data received!", EndymionLoggerEnum.WARNING);
        }

        List<String> fields = new ArrayList<String>();
        Node firstLine = data.getFirstChild().getNextSibling();

        if (firstLine == null) {
            throw new EndymionException("No data received!", EndymionLoggerEnum.WARNING);
        }
        /**
         * Iterating through all the field values
         */
        for (Node field = firstLine.getFirstChild(); field != null; field = field.getNextSibling()) {
            if (!field.getTextContent().equals("#text") && !field.getTextContent().trim().isEmpty() &&
                    field.getTextContent() != null) {
                //System.out.println("Field: " + field.getTextContent());
                fields.add(field.getTextContent());
            }
        }

        List<String> values = new ArrayList<String>();

        /**
         * Iterating through measurement results
         */
        for (Node line = firstLine.getNextSibling(); line != null; line = line.getNextSibling()) {
            if (line.getNodeName().equals("#text")) continue;
            values.clear();

            /**
             * Iterating through field values
             */
            for (Node field = line.getFirstChild(); field != null; field = field.getNextSibling()) {

                if (field.getNodeName().equals("#text")) continue;
                String value = StringEscapeUtils.unescapeXml(field.getTextContent());
                values.add(value);
            }

            /**
             * Timestamp
             */
            String timed = getFormattedDate(fields, values);

            for (int i = 0; i < fields.size(); ++i) {
                if (fields.get(i).equalsIgnoreCase("timed")) continue;
                sensor.setSensorData(fields.get(i), timed, values.get(i));
            }
        }

        return sensor;
    }

    /**
     *
     * @param connectionData - configuration data fetched from GSN server
     * @param configuration - A map which contains sensor names and their respective fields
     * @return - A list of sensor objects
     * @throws EndymionException
     */
    @Override
    public List<GSNConfigurationSensor> processSensorConfiguration(String connectionData,
                                                      HashMap<String, List<String>> configuration) throws EndymionException {

        List<GSNConfigurationSensor> sensors = new ArrayList<GSNConfigurationSensor>();

        /**
         * Building a DOM object from connection data
         */
        Node gsn = buildDOM(connectionData);

        /**
         * Iterating through sensors
         */
        for (Node vsensor = gsn.getFirstChild(); vsensor != null; vsensor = vsensor.getNextSibling()) {
            if (vsensor.getNodeName().equalsIgnoreCase("#text") || vsensor.getChildNodes().getLength() == 0) continue;
            String vsName = vsensor.getAttributes().getNamedItem("name").getNodeValue();

            /**
             * Check if the sensor matches a sensor from the configuration
             */
            if (vSensorCondition(vsName, configuration)) {
                GSNConfigurationSensor sensor = createGSNSensor(vsensor, configuration);

                if (!sensor.getDataFields().isEmpty())
                    sensors.add(sensor);
            }
        }

        return sensors;
    }

    /**
     * This function is used to create a sensor object
     * @param node - The node from the DOM which matches the sensor
     * @param configuration - A map containing sensors and their fields
     * @return - A GSNSensor object
     * @throws EndymionException - Failed parsing
     */
    protected GSNConfigurationSensor createGSNSensor (Node node, HashMap<String, List<String>> configuration) throws EndymionException {

        GSNConfigurationSensor sensor = new GSNConfigurationSensor();

        /**
         * Extracting virtual sensor name
         */
        String vsName = node.getAttributes().getNamedItem("name").getNodeValue();
        sensor.setvSensor(vsName);

        /**
         * Iterating through sensor fields
         */
        for (Node field = node.getFirstChild(); field != null; field = field.getNextSibling()) {
            if (field.getNodeName().equals("#text")) continue;
            String fieldName = field.getAttributes().getNamedItem("name").getNodeValue();

            /**
             *  If the field has attribute category=predicate the iteration is continued
             */
            Node category = field.getAttributes().getNamedItem("category");
            if (category != null && category.getNodeValue().equalsIgnoreCase("predicate")) continue;

            /**
             * Check if the sensor field matches a sensor field from the configuration
             */
            if (vSensorFieldCondition(vsName, fieldName, configuration)) {
                String fieldType;
                try {
                    fieldType = field.getAttributes().getNamedItem("type").getNodeValue();
                } catch (Exception e) {
                    fieldType = "string";
                }
                sensor.addDataField(fieldName, fieldType);
            }
        }

        return sensor;
    }

    /**
     *
     * @param vsName - the name of the sensor
     * @param configuration - A map containing sensors and their fields
     * @return - true if the configuration contains vsName or if the configuration is empty
     *         - false otherwise
     */
    protected boolean vSensorCondition (String vsName, HashMap<String, List<String>> configuration) {

        try {
            if ((configuration.keySet().isEmpty())
                    || configuration.keySet().contains(vsName)) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    /**
     *
     * @param vsName - The name of the sensor
     * @param fieldName - The name of the sensor's field
     * @param configuration - A map containing sensors and their fields
     * @return - true if the configuration contains vsName and its fieldName or if the configuration is empty
     *         - false otherwise
     */
    protected boolean vSensorFieldCondition (String vsName, String fieldName, HashMap<String, List<String>> configuration) {

        try {
            if (fieldName.equalsIgnoreCase("timed")) return false;
            if ((configuration.keySet().isEmpty()) || (configuration.get(vsName).isEmpty())
                    || configuration.get(vsName).contains(fieldName)) {
                return true;
            }
        } catch (Exception e) {

        }

        return false;
    }

    /**
     * This is a helper function for building DOM
     * @param connectionData - data in XML format
     * @return - The root node of the DOM
     * @throws EndymionException - Building DOM failed
     */
    protected Node buildDOM (String connectionData) throws EndymionException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder documentBuilder;
        Document document;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(new InputSource(new StringReader(connectionData)));
        }  catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.ERROR);
        }

        return document.getDocumentElement();
    }

    /**
     * Method which formats date
     * @param fields - attribute names
     * @param values - attribute values
     * @return String which contains formatted date in dateFormat
     * @throws EndymionException
     */
    protected String getFormattedDate (List<String> fields, List<String> values) throws EndymionException {

        for (int i = 0; i < fields.size(); ++i) {
            if (fields.get(i).equalsIgnoreCase("timed")) {
                try {
                    return GSNTimeManager.getTimeManager().getUsedTimeFormat(values.get(i));
                } catch (EndymionException e) {
                    EndymionLoggerManager.getLoggerManager().logMessage(e);
                }
            }
        }

        throw new EndymionException("Unknown date format received!", EndymionLoggerEnum.ERROR);
    }

}
