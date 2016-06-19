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
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

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


public class MqttGateway extends AbstractWrapper implements MqttCallback{

  private static final String MQTT_CONFIG_FILE = "mqtt/config.xml";
	
  private DataField[] collection = new DataField[] {new DataField("Topic", "varchar(150)", "MQTT topic"),
                                                      new DataField("Message", "varchar(1000)", "message")};


  private final transient Logger logger = Logger.getLogger(MqttGateway.class);

  private int counter;
  private AddressBean params;

  private long rate = 1000;

  private String brokerAddress;
  private String brokerCertificatePath;
  private int brokerPort;
  private int securePort;
  private int keepAliveInterval = 300;

  private String mqttGatewayTopic;
  private String username;
  private String password;

  private boolean isConnected = false;
  private boolean anonymous = false;
  private boolean mqttSecurity = false;

  private String infoMessage = null;
  
  private MqttClient client;
  private MqttConnectOptions connOpt;
  
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

        username = connectionParameters.getChild("mqtt-username").getValue(); 
        password = connectionParameters.getChild("mqtt-password").getValue();
        brokerCertificatePath = connectionParameters.getChild("broker-ca-certificate").getValue(); 
        anonymous = Boolean.parseBoolean(connectionParameters.getChild("mqtt-anonymous").getValue()); 
        mqttSecurity = Boolean.parseBoolean(connectionParameters.getChild("mqtt-security").getValue());
        securePort = Integer.valueOf(connectionParameters.getChild("secure-port").getValue());

    }
    catch(Exception e){
        logger.error(e.getMessage(), e);
        return false; 
    }

        return true;
  }

  public void run() {

    // initial values
    String connectProtocol = "tcp://";
    int connectPort = brokerPort;

  	// try to reconnect while wrapper is active
  	while(isActive()){

	    if(!isConnected()){
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

                infoMessage = getWrapperName() + ": Connected to: tcp://" + brokerAddress + ":" + brokerPort;

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

                infoMessage = getWrapperName() + ": Connected to: ssl://" + brokerAddress + ":" + securePort;

            }
            
            // connection without user authentification and no encryption
            if(anonymous && !mqttSecurity){

                connectProtocol = "tcp://";
                connectPort = brokerPort;

                infoMessage = getWrapperName() + ": Connected to: tcp://" + brokerAddress + ":" + brokerPort;
            }

                client = new MqttClient(connectProtocol + brokerAddress + ":" + connectPort, getWrapperName() + client.generateClientId(), new MemoryPersistence());
                client.connect(connOpt);

		        logger.warn(infoMessage);
		        client.setCallback(this);
		        client.subscribe(mqttGatewayTopic);
	        
	      } catch (MqttException | GeneralSecurityException | IOException e) {
				logger.error(e.getMessage(), e);
				//e.printStackTrace();
				logger.warn(getWrapperName() + ": Reconnect in 10 sec");

				// sleep
	          	try{
					Thread.sleep(10000);
				} catch (InterruptedException ex) {
		        	logger.error(e.getMessage(), ex);

		     	}	
			}
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
  public void connectionLost(Throwable cause) {
  	isConnected = false;
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


