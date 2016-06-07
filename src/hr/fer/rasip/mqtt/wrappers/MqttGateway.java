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
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MqttGateway extends AbstractWrapper implements MqttCallback{

  private static final String MQTT_CONFIG_FILE = "mqtt/config.xml";
	
  private DataField[] collection = new DataField[] {new DataField("Topic", "varchar(150)", "MQTT topic"),
                                                      new DataField("Message", "varchar(1000)", "message")};


  private final transient Logger logger = Logger.getLogger(MqttGateway.class);

  private int counter;
  private AddressBean params;

  private long rate = 1000;

  private String brokerAddress;
  private int brokerPort;

  private String mqttGatewayTopic;

  private boolean isConnected = false;
  
  MqttClient client;
  
  public boolean initialize() {
    setName("MqttGateway" + counter++);

    params = getActiveAddressBean();

    //fetch info from mqtt config
    try {
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(MQTT_CONFIG_FILE);
        Document doc = (Document) builder.build(xmlFile);
        Element root = doc.getRootElement();

        //get parameters from config file
        Element connectionParameters = root.getChild("connection-params");

        brokerAddress = connectionParameters.getChild("broker-url").getValue();  
        brokerPort = Integer.valueOf(connectionParameters.getChild("broker-port").getValue());
        mqttGatewayTopic = connectionParameters.getChild("mqtt-gateway").getValue(); 

    }
    catch(Exception e){
        logger.error(e.getMessage(), e);
        return false; 
    }


        return true;
  }

  public void run() {


    if(!isConnected()){
      try {

        client = new MqttClient("tcp://" + brokerAddress + ":" + brokerPort, client.generateClientId(), new MemoryPersistence());
        client.connect();
        System.out.println(getWrapperName() + ": Connected to: " + brokerAddress + ":" + brokerPort);
        client.setCallback(this);
        client.subscribe(mqttGatewayTopic);
        
      } catch (MqttException e) {
          logger.error(e.getMessage(), e);
          e.printStackTrace();
      }

    }

  
  }

  public DataField[] getOutputFormat() {
    return collection;
  }

  public String getWrapperName() {
    return "MQTT Gateway";
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
  public void connectionLost(Throwable cause) {
    System.out.println("Mqtt Gateway connection lost");

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

  public boolean isConnected(){
      if(client != null){
        isConnected = client.isConnected();
      }
      else{
        isConnected = false;
      }
      return isConnected;
  }

}


