package hr.fer.rasip.mqtt.wrappers;
import java.io.*;
import gsn.beans.AddressBean;
import gsn.beans.DataField;
import gsn.wrappers.AbstractWrapper;

import java.io.Serializable;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.security.GeneralSecurityException; 
import java.io.IOException; 

public class MqttGateway extends AbstractMqttClient{

    private static final String MQTT_CONFIG_FILE = "mqtt/config.xml";

    private DataField[] collection = new DataField[] {new DataField("Topic", "varchar(150)", "MQTT topic"),
                                                      new DataField("Message", "varchar(1000)", "message")};


    private final transient Logger logger = Logger.getLogger(MqttGateway.class);

    private int counter;
    private AddressBean params;

    private long rate = 1000;

    protected String mqttGatewayTopic;

    public boolean initialize() {
        setName("MqttGateway" + counter++);

        params = getActiveAddressBean();

        // get general mqtt config, if failed return false
        boolean mqttConfigStatus = readMqttConfig();
        if (mqttConfigStatus == false){
            return false;
        }

        // get wrapper specific mqtt config
        try {
            SAXBuilder builder = new SAXBuilder();
            File xmlFile = new File(MQTT_CONFIG_FILE);
            Document doc = (Document) builder.build(xmlFile);
            Element root = doc.getRootElement();

            //get parameters from config file
            Element connectionParameters = root.getChild("connection-params");

            mqttGatewayTopic = connectionParameters.getChild("mqtt-gateway").getValue();


        }
        catch(Exception e){
            logger.error(e.getMessage(), e);
            return false; 
        }

        // set callback function on Mqtt events
        callback = setCallback();
        return true;
    }

    /**
     * Try to connect to broker.
     */
    public void run() {

        if(!isConnected()){
          try {

            connectToBroker();
            
          } catch (MqttException | GeneralSecurityException | IOException e) {
                logger.error(e.getMessage(), e);
                //e.printStackTrace();
                
            }
        }
        
    }

    public DataField[] getOutputFormat() {
        return collection;
    }

    public String getWrapperName() {
        return "MQTTGateway";
    }  


    // disconnect after removal
    public void dispose() {
        counter--;

        if(isConnected()){

          try {
            client.disconnect();
          } catch (MqttException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
          }
        }
    }

    @Override
    protected void subscribeToTopics() throws MqttException{

        client.subscribe(mqttGatewayTopic);

    }

    @Override
    protected MqttCallbackExtended setCallback() {

        return new MqttCallbackExtended(){  

            @Override
            public void connectionLost(Throwable cause) {
                isConnected = false;
                System.out.println(getWrapperName() + ": connection to " + brokerAddress + " lost. Reconnecting...");

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                String incomingData = message.toString();
                postStreamElement(new Serializable[] {topic, incomingData}); 
              
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // TODO Auto-generated method stub

            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                System.out.println(getWrapperName() + ": reconnected to " + serverURI);

            }
        };

    }

}


