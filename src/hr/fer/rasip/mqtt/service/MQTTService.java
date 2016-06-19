package hr.fer.rasip.mqtt.service;

import gsn.utils.Utils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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

import javax.net.ssl.SSLContext; 
import javax.net.ssl.SSLParameters; 
import javax.net.ssl.SSLSocketFactory; 
import javax.net.ssl.TrustManagerFactory.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.GeneralSecurityException; 

import java.io.FileNotFoundException; 
import java.io.IOException; 
import java.io.InputStreamReader; 
import java.security.cert.*;
import javax.net.ssl.*;

/**
 * This class provides an service for publishing Mqtt messages
 */
public class MQTTService {

    private static final transient Logger logger = Logger.getLogger(MQTTService.class);

    private static final String MQTT_CONFIG_FILE = "mqtt/config.xml";
    
    private static String gsnName;
    private static Integer gsnId;
    private static String topicName;

    private static String brokerAddress;
    private static String brokerCertificatePath;
    private static int brokerPort;
    private static int securePort;
    private static int keepAliveInterval = 300;

    private static String username;
    private static String password;

    private static boolean isConnected = false;
    private static boolean anonymous = false;
    private static Boolean mqttSecurity;

    private static String infoMessage = null;
    
    private static MqttClient client;
    private static MqttConnectOptions connOpt;

    /**
     * Get config from config file
     */
    public static boolean init(){

      try {
          SAXBuilder builder = new SAXBuilder();
          File xmlFile = new File(MQTT_CONFIG_FILE);
          Document doc = (Document) builder.build(xmlFile);
          Element root = doc.getRootElement();
          
          //get parameters from config file
          Element connectionParameters = root.getChild("connection-params");
          
          // not used for now
          //gsnName = connectionParameters.getChild("gsn-name").getValue();  
          //gsnId = Integer.valueOf(connectionParameters.getChild("gsn-id").getValue());

          brokerAddress = connectionParameters.getChild("broker-url").getValue();
          if(brokerAddress == null){
            logger.error("MQTTService: no broker-url parameter");
            return false;
          }
          brokerPort = Integer.valueOf(connectionParameters.getChild("broker-port").getValue());
          if(brokerPort == 0){
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

          mqttSecurity = Boolean.parseBoolean(connectionParameters.getChild("mqtt-security").getValue());
          if(mqttSecurity == null){
            logger.error("MQTTService: no mqtt-security parameter");
            return false;
          }
          securePort = Integer.valueOf(connectionParameters.getChild("secure-port").getValue()); 
          if(securePort == 0){
            logger.error("MQTTService: no secure-port parameter");
            return false;
          }

          brokerCertificatePath = connectionParameters.getChild("broker-ca-certificate").getValue();
          if(brokerCertificatePath == null){
            logger.error("MQTTService: no broker-ca-certificate parameter, Security disabled");
            mqttSecurity = false;
          }
          
          return true;
         }
       catch(Exception e){
          logger.error(e.getMessage(), e);
          return false; 
       }

    }

    /**
     * This method connects service to broker
     * @return true if client connected, else false
     */
    public static boolean connectToBroker(){

        // initial values
        String connectProtocol = "tcp://";
        int connectPort = brokerPort;

        // if initialization parameters are not correct return false
        if(!init()){
        return false;
        }



        try {

            connOpt = new MqttConnectOptions();
            connOpt.setCleanSession(true);
            connOpt.setKeepAliveInterval(keepAliveInterval);

            // connect with username and without security
            if(!anonymous && !mqttSecurity)
            {

                connectProtocol = "tcp://";
                connectPort = brokerPort;

                connOpt.setUserName(username);
                connOpt.setPassword(password.toCharArray());

                infoMessage = "MQTTService: Connected to: tcp://" + brokerAddress + ":" + brokerPort;

            }

            // connect with security, username optional
            if(mqttSecurity)
            {

                connectProtocol = "ssl://";
                connectPort = securePort;

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream certFile = new FileInputStream(brokerCertificatePath);
                Certificate ca = cf.generateCertificate(certFile);
             
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

                // if anonymous in configuration is set to false, connect with username and pass
                if(!anonymous){

                    connOpt.setUserName(username);
                    connOpt.setPassword(password.toCharArray());

                }
                
                connOpt.setSocketFactory(sslContext.getSocketFactory());

                infoMessage = "MQTTService: Connected to: ssl://" + brokerAddress + ":" + securePort;

            }
            
            // connection without user authentification and no encryption
            if(anonymous && !mqttSecurity){

                connectProtocol = "tcp://";
                connectPort = brokerPort;

                infoMessage = "MQTTService: Connected to: tcp://" + brokerAddress + ":" + brokerPort;
            }

                client = new MqttClient(connectProtocol + brokerAddress + ":" + connectPort, "MQTTService:" + client.generateClientId(), new MemoryPersistence());
                client.connect(connOpt);
                logger.warn(infoMessage);
                
        
        } catch (MqttException | GeneralSecurityException | IOException e) {
            logger.warn("MQTTService: Could not connect to broker: " + brokerAddress);
            logger.error(e.getMessage(), e);
            return false;
        }

        
        return true;

    }

    /**
     * Publish message to topic
     * @param topicName - name of publish topic
     * @param data - payload to publish
     * @return true if message is published, else false
     */
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
            logger.warn("MQTTService: Mqtt publish failed");
            logger.error(e.getMessage(), e);
            return false;
        }

    }
    /**
     * Check if client is connted
     * @return true if client is connected, else false
     */
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
