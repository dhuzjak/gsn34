package hr.fer.rasip.wrappers;
import java.io.*;
import gsn.beans.AddressBean;
import gsn.beans.DataField;
import gsn.wrappers.AbstractWrapper;

import java.io.Serializable;

import org.apache.log4j.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MQTTSubscribeTest extends AbstractWrapper implements MqttCallback{
	
  private DataField[] collection = new DataField[] {new DataField("temperature", "double", "MQTT temperature")};
  private final transient Logger logger = Logger.getLogger(MQTTSubscribeTest.class);
  private int counter;
  private AddressBean params;
  private long rate = 1000;
  private String brokerAddress;
  private String topicName;
  
  MqttClient client;
  
  public boolean initialize() {
    setName("MQTTSubscribetest" + counter++);
    
    boolean[] flags = {false, false, false};

    params = getActiveAddressBean();
    
    if ( params.getPredicateValue( "sampling-rate" ) != null ){

    	rate = (long) Integer.parseInt( params.getPredicateValue( "sampling-rate"));
    	logger.info("Sampling rate set to " + params.getPredicateValue( "sampling-rate") + " msec.");
      flags[0] = true;
    }
    //broker address
    if ( params.getPredicateValue( "MQTT-broker-address" ) != null ){

      brokerAddress= params.getPredicateValue("MQTT-broker-address");
      logger.info("Broker address set to " + params.getPredicateValue( "MQTT-broker-address"));
      flags[1] = true;
      //System.out.println(brokerAddress);
    }

    if ( params.getPredicateValue( "MQTT-topic" ) != null ){

      topicName = params.getPredicateValue( "MQTT-topic");
      logger.info("Topic set to " + params.getPredicateValue( "MQTT-topic"));
      flags[2] = true;
      //System.out.println(topicName);
    }
    
    
    return flags[0] & flags[1] & flags[2];
  }

  public void run() {
    try {

      client = new MqttClient(brokerAddress, client.generateClientId(), new MemoryPersistence());
      client.connect();
      //System.out.println(client.isConnected());
      client.setCallback(this);
      client.subscribe(topicName);
      
    } catch (MqttException e) {
      e.printStackTrace();
    }
  
  }

  public DataField[] getOutputFormat() {
    return collection;
  }

  public String getWrapperName() {
    return "MQTT temperature";
  }  

  public void dispose() {
    counter--;
    try {
      client.disconnect();
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
  }

  @Override
  public void connectionLost(Throwable cause) {
    System.out.println("connection lost");

  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    double temperature = 0;
    String newMessage = message.toString();
    if (isDouble(newMessage)){
      
          temperature = Double.parseDouble(newMessage);
          
          postStreamElement(new Serializable[] {temperature}); 
          //System.out.println(temperature);
      
    }
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // TODO Auto-generated method stub

  }

}


