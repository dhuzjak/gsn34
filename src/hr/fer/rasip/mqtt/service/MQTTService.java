package hr.fer.rasip.mqtt.service;

import gsn.utils.Utils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;

/**
 * This class provides an access to MQTT Notification services.
 */
public class MQTTService {

    private static final transient Logger logger = Logger.getLogger(MQTTService.class);

    private static final String MQTT_CONFIG_FILE = "mqtt/config.xml";
    
    private String gsnName;
    private Integer gsnId;
    private String topicName;

    private static String brokerAddress;
    private static Integer port = 0;
    private static String username;
    private static String password;

    private static boolean isConnected = false;
    private static boolean anonymous = false;
    
    private static MqttClient client;
    private static MqttConnectOptions connOpt;

    public static boolean init(){

      try {
          SAXBuilder builder = new SAXBuilder();
          File xmlFile = new File(MQTT_CONFIG_FILE);
          Document doc = (Document) builder.build(xmlFile);
          Element root = doc.getRootElement();
          
          //get parameters from config file
          Element connectionParameters = root.getChild("connection-params");
          
          //gsnName = connectionParameters.getChild("gsn-name").getValue();  
          //gsnId = Integer.valueOf(connectionParameters.getChild("gsn-id").getValue());

          brokerAddress = connectionParameters.getChild("broker-url").getValue();
          if(brokerAddress == null){
            logger.error("MQTTService: no broker-url parameter");
            return false;
          }
          port = Integer.valueOf(connectionParameters.getChild("broker-port").getValue());
          if(port == 0){
            logger.error("MQTTService: no broker-port parameter");
            return false;
          }
          
          username = connectionParameters.getChild("mqtt-username").getValue(); 
          if(username == null){
            logger.error("MQTTService: no username parameter");
            return false;
          }
          password = connectionParameters.getChild("mqtt-password").getValue(); 
          if(password == null){
            logger.error("MQTTService: no password parameter");
            return false;
          }
          String anonym = connectionParameters.getChild("mqtt-anonymous").getValue();
          if(anonym == null){
            logger.error("MQTTService: no mqtt-anonymous parameter");
            return false;
          }
          anonymous = Boolean.parseBoolean(anonym); 
          
          return true;
         }
       catch(Exception e){
          logger.error(e.getMessage(), e);
          return false; 
       }

    }

    public static boolean connectToBroker(){


      if(!init()){
        return false;
      }


      try {
              client = new MqttClient("tcp://" + brokerAddress + ":" + port, "MqttService" + client.generateClientId(), new MemoryPersistence());
              if(!anonymous)
              {
                connOpt = new MqttConnectOptions();
          
                connOpt.setCleanSession(true);
                connOpt.setKeepAliveInterval(300);
                connOpt.setUserName(username);
                connOpt.setPassword(password.toCharArray());
                client.connect(connOpt);

              }
              else
              {
                client.connect();
              }

              return true;
        } catch (MqttException e) {
            logger.warn("Could not connect to broker: " + brokerAddress);
            logger.error(e.getMessage(), e);
            return false;
        }

    }


    public static boolean sendMqttMessage(String topicName, String data) {
        
        boolean output;
        
        if(!isConnected()){
          output = connectToBroker();
        }

        
        MqttMessage message = new MqttMessage();
        message.setPayload(data.getBytes());


      try {
        client.publish(topicName, message);
        return true;
      } catch (MqttException e) {
            logger.warn("Mqtt publish failed");
            logger.error(e.getMessage(), e);
            return false;
      }

    }

    public static boolean isConnected(){
      if(client != null){
        isConnected = client.isConnected();
      }
      else{
        isConnected = false;
      }
      return isConnected;
    }

}
