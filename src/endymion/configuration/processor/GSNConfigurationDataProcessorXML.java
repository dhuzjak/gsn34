package endymion.configuration.processor;

import endymion.configuration.data.GSNConfiguration;
import endymion.configuration.data.GSNConfigurationItem;
import endymion.configuration.data.GSNStorageConfiguration;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Nikola on 26.02.2015.
 * Configuration data processor for XML configuration
 */
public class GSNConfigurationDataProcessorXML extends GSNConfigurationDataProcessor {

    @Override
    public List<GSNConfiguration> processConfiguration(List<String> configuration) throws EndymionException {

        List<GSNConfiguration> configurations = new ArrayList<GSNConfiguration>();

        Node rootItem = buildDOM(configuration.get(0));

        for (Node gsn = rootItem.getFirstChild(); gsn != null; gsn = gsn.getNextSibling()) {
            if (gsn.getNodeName().equals("gsn"))
                configurations.add(createGSNConfiguration(gsn));
            else if (gsn.getNodeName().equals("storage")) {
                configurations.add(createStorageConfiguration(gsn));
            }
        }

        return configurations;
    }

    protected Node buildDOM (String configuration) throws EndymionException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder documentBuilder;
        Document document;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(new InputSource(new StringReader(configuration)));
        }  catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.ERROR);
        }

        return document.getDocumentElement();

    }

    protected GSNConfiguration createGSNConfiguration (Node gsn) throws EndymionException{
        HashMap<String, String> parameters = new HashMap<String, String>();
        Node vsensors = null;
        Node alarms = null;
        for (Node node = gsn.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (!node.getNodeName().equalsIgnoreCase("vsensors") && !node.getNodeName().equalsIgnoreCase("alarms")) {
                parameters.put(node.getNodeName(), node.getTextContent());
            } else if (node.getNodeName().equalsIgnoreCase("vsensors")) {
               vsensors = node;
            } else if (node.getNodeName().equalsIgnoreCase("alarms")) {
                alarms = node;
            }
        }

        GSNConfiguration config = new GSNConfigurationItem(parameters.get("ipAddress"), parameters.get("port"),
                parameters.get("username"), parameters.get("password"), parameters.get("connectionType"));

        if (vsensors == null) {
            throw new EndymionException("Vsensors element is missing!", EndymionLoggerEnum.FATAL_ERROR);
        }
        NamedNodeMap attributes = vsensors.getAttributes();

        for (int i = 0; i < attributes.getLength(); ++i) {
            config.setConfigurationParameter(attributes.item(i).getNodeName(), attributes.getNamedItem
                    (attributes.item(i).getNodeName()).getNodeValue());
        }

        getVSensorConfiguration(vsensors, config);
        if (alarms != null)
            getAlarmsConfiguration (alarms, config);
        return config;
    }

    protected void getVSensorConfiguration(Node vsensors, GSNConfiguration gsnConfiguration) throws EndymionException {

        HashMap<String, String> parameters = new HashMap<String, String>();

        for (Node node = vsensors.getFirstChild(); node != null; node = node.getNextSibling()) {
            NamedNodeMap attributes = node.getAttributes();

            if (attributes == null) continue;

            for (int i = 0; i < attributes.getLength(); ++i) {
                parameters.put(attributes.item(i).getNodeName(), attributes.getNamedItem
                        (attributes.item(i).getNodeName()).getNodeValue());
            }

            gsnConfiguration.addVSensor(parameters.get("vsname"), parameters);
            getVSensorFieldConfiguration(node, gsnConfiguration);
            getVSensorAlarmsConfiguration(node, gsnConfiguration);
            parameters.clear();
        }


    }

    protected void getVSensorFieldConfiguration (Node vsensor, GSNConfiguration gsnConfiguration) throws EndymionException{

        HashMap<String, String> parameters = new HashMap<String, String>();

        Node fields = null;
        for (Node node = vsensor.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeName().equalsIgnoreCase("fields")) {
                fields = node;
                break;
            }
        }

        if (fields == null) return;

        for (Node node = fields.getFirstChild(); node != null; node = node.getNextSibling()) {
            NamedNodeMap attributes = node.getAttributes();

            for (int i = 0; attributes != null && i < attributes.getLength(); ++i) {
                parameters.put(attributes.item(i).getNodeName(), attributes.getNamedItem
                        (attributes.item(i).getNodeName()).getNodeValue());
            }

            gsnConfiguration.addField(vsensor.getAttributes().getNamedItem("vsname").getNodeValue(),
                    parameters);
        }
    }

    protected void getAlarmsConfiguration (Node alarms, GSNConfiguration gsnConfiguration) throws EndymionException {

        HashMap<String, String> parameters = new HashMap<String, String>();
        List<String> sendToList = new ArrayList<String>();

        for (Node node = alarms.getFirstChild(); node != null; node = node.getNextSibling()) {
            NamedNodeMap attributes = node.getAttributes();

            if (attributes == null) continue;

            for (int i = 0; i < attributes.getLength(); ++i) {
                parameters.put(attributes.item(i).getNodeName(), attributes.getNamedItem
                        (attributes.item(i).getNodeName()).getNodeValue());
            }

            for (Node sendToNode = node.getFirstChild(); sendToNode != null; sendToNode = sendToNode.getNextSibling()) {
                sendToList.add(sendToNode.getTextContent());
            }

            gsnConfiguration.addAlarm(parameters.get("name"), parameters, sendToList);
            parameters.clear();
            sendToList.clear();

        }
    }

    protected void getVSensorAlarmsConfiguration (Node vsensor, GSNConfiguration gsnConfiguration) throws EndymionException {

        HashMap<String, String> parameters = new HashMap<String, String>();
        List<String> sendToList = new ArrayList<String>();

        Node alarms = null;
        for (Node node = vsensor.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeName().equalsIgnoreCase("alarms")) {
                alarms = node;
                break;
            }
        }

        if (alarms == null) return;

        for (Node node = alarms.getFirstChild(); node != null; node = node.getNextSibling()) {
            NamedNodeMap attributes = node.getAttributes();

            for (int i = 0; attributes != null && i < attributes.getLength(); ++i) {
                parameters.put(attributes.item(i).getNodeName(), attributes.getNamedItem
                        (attributes.item(i).getNodeName()).getNodeValue());
            }


            for (Node sendToNode = node.getFirstChild(); sendToNode != null; sendToNode = sendToNode.getNextSibling()) {
                sendToList.add(sendToNode.getTextContent());
            }

            gsnConfiguration.addAlarm(vsensor.getAttributes().getNamedItem("vsname").getNodeValue(),
                    parameters.get("name"), parameters, sendToList);

            parameters.clear();
            sendToList.clear();


        }
    }

    protected GSNConfiguration createStorageConfiguration (Node storage) {

        NamedNodeMap attributes = storage.getAttributes();

        String ipAddress = attributes.getNamedItem("url").getNodeValue();
        String username = attributes.getNamedItem("username").getNodeValue();
        String password = attributes.getNamedItem("password").getNodeValue();

        return new GSNStorageConfiguration(ipAddress, username, password);

    }
}


