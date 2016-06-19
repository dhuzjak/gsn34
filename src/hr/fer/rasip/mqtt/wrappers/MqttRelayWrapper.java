package hr.fer.rasip.mqtt.wrappers;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import gsn.beans.AddressBean;
import gsn.beans.DataField;
import gsn.wrappers.AbstractWrapper;

import java.io.Serializable;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
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
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
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

public class MqttRelayWrapper extends AbstractWrapper implements MqttCallback {

	private static final String MQTT_CONFIG_FILE = "mqtt/config.xml";

    private DataField[] collection = new DataField[]{new DataField("Relay_1","varchar(10)", "Relay State" ),
                                                                new DataField("Relay_2","varchar(10)", "Relay State" ),
                                                                new DataField("Relay_3","varchar(10)", "Relay State" ),
                                                                new DataField("Relay_4","varchar(10)", "Relay State" )    };

    private final transient Logger logger = Logger.getLogger(MqttRelayWrapper.class);
    private int counter;
    private AddressBean params;
    private long rate = 1000;

    private String brokerAddress;
    private String brokerCertificatePath;
    private String mqttRelayTopic;
    private String mqttRelayTopicAck;

    private int brokerPort;
    private int securePort;
    private int keepAliveInterval = 300;

    private String username;
    private String password;

    private boolean isConnected = false;
    private boolean anonymous = false;
    private boolean mqttSecurity = false;

    private String infoMessage = null;
    
    private MqttClient client;
    private MqttConnectOptions connOpt;

    private GpioController gpio;
    private GpioPinDigitalOutput in_1;
    private GpioPinDigitalOutput in_2;
    private GpioPinDigitalOutput in_3;
    private GpioPinDigitalOutput in_4;
    private List<GpioPinDigitalOutput> relays = new ArrayList<GpioPinDigitalOutput>();

    public boolean initialize() {

        setName("MQTT relay" + counter++);

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
            mqttRelayTopic = connectionParameters.getChild("mqtt-topic-relay").getValue(); 
            mqttRelayTopicAck = connectionParameters.getChild("mqtt-topic-relay-ack").getValue();

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

        gpio = GpioFactory.getInstance();
        in_1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "Input_1", PinState.HIGH);
        in_2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10, "Input_2", PinState.HIGH);
        in_3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, "Input_3", PinState.HIGH);
        in_4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, "Input_4", PinState.HIGH);
        
        in_1.setShutdownOptions(Boolean.FALSE, PinState.HIGH);
        in_2.setShutdownOptions(Boolean.FALSE, PinState.HIGH);
        in_3.setShutdownOptions(Boolean.FALSE, PinState.HIGH);
        in_4.setShutdownOptions(Boolean.FALSE, PinState.HIGH);
        
        relays.add(in_1);
        relays.add(in_2);
        relays.add(in_3);
        relays.add(in_4);
	
	return true;
    }

    public void run() {
        
        // try to reconnect while wrapper is active
        while(isActive()){

            if(!isConnected()){
              try {

                // connect with username and without security
                if(!anonymous && !mqttSecurity)
                {
                    client = new MqttClient("tcp://" + brokerAddress + ":" + brokerPort, getWrapperName() + client.generateClientId(), new MemoryPersistence());
                    connOpt = new MqttConnectOptions();

                    connOpt.setCleanSession(true);
                    connOpt.setKeepAliveInterval(keepAliveInterval);
                    connOpt.setUserName(username);
                    connOpt.setPassword(password.toCharArray());
                    client.connect(connOpt);
                    infoMessage = getWrapperName() + ": Connected to: tcp://" + brokerAddress + ":" + brokerPort;

                }
                // connect with security, username optional
                if(mqttSecurity)
                {

                    client = new MqttClient("ssl://" + brokerAddress + ":" + securePort, getWrapperName() + client.generateClientId(), new MemoryPersistence());
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
                    connOpt = new MqttConnectOptions();

                    // if anonymous in configuration is set to false, connect with username and pass
                    if(!anonymous){
                        connOpt.setUserName(username);
                        connOpt.setPassword(password.toCharArray());
                    }
                    connOpt.setCleanSession(true);
                    connOpt.setKeepAliveInterval(keepAliveInterval);
                    connOpt.setSocketFactory(sslContext.getSocketFactory());
                    client.connect(connOpt);
                    infoMessage = getWrapperName() + ": Connected to: ssl://" + brokerAddress + ":" + securePort;

                }
                // connection without user authentification and no encryption
                if(anonymous && !mqttSecurity){
                    client = new MqttClient("tcp://" + brokerAddress + ":" + brokerPort, getWrapperName() + client.generateClientId(), new MemoryPersistence());
                    connOpt = new MqttConnectOptions();

                    connOpt.setCleanSession(true);
                    connOpt.setKeepAliveInterval(keepAliveInterval);
                    client.connect(connOpt);
                    infoMessage = getWrapperName() + ": Connected to: tcp://" + brokerAddress + ":" + brokerPort;
                }

                
                    logger.warn(infoMessage);
                    client.setCallback(this);
                    client.subscribe(mqttRelayTopic);
                
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
        return "MQTTrelay";
    }

    public void dispose() {
        --counter;
        try {
            client.disconnect();
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
        gpio.shutdown();
        gpio.unprovisionPin(new GpioPin[]{in_1});
        gpio.unprovisionPin(new GpioPin[]{in_2});
        gpio.unprovisionPin(new GpioPin[]{in_3});
        gpio.unprovisionPin(new GpioPin[]{in_4});
    }

    public void connectionLost(Throwable cause) {
        System.out.println(getWrapperName() + " disconnected");

    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String newMessage = message.toString();
        JSONParser parser = new JSONParser();
        
        try {
            Object obj = parser.parse(newMessage);
            JSONArray array = (JSONArray)obj;
            for (int i = 0; i < array.size(); ++i) {
                JSONObject relayState = (JSONObject)array.get(i);

                if (relays.get(i).getState() != relayState.get((Object)"status")){
                	relays.get(i).setState((Boolean)relayState.get((Object)"status") == false);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // send same message as acknowledgement that this message was received by relay wrapper
        MqttMessage status = new MqttMessage();
        status.setPayload(newMessage.getBytes());
        status.setRetained(true);
        client.publish(mqttRelayTopicAck, status);

        // update status of relays
        postStreamElement(new Serializable[]{   String.valueOf(in_1.isLow()),
                                                String.valueOf(in_2.isLow()),
                                                String.valueOf(in_3.isLow()),
                                                String.valueOf(in_4.isLow())
                                                    });
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
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
