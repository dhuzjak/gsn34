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
 * This abstract class represents abstract mqtt client with
 * callback function for asynchronous events
 */
public abstract class AbstractMqttClient extends AbstractWrapper{

    protected static final String MQTT_CONFIG_FILE = "mqtt/config.xml";

    protected final static transient Logger logger = Logger
            .getLogger(AbstractWrapper.class);

    protected String brokerAddress;
    protected String brokerCertificatePath;
    protected int brokerPort;
    protected int securePort;
    protected int keepAliveInterval = 300;

    protected String username;
    protected String password;

    protected boolean isConnected = false;
    protected boolean anonymous = false;
    protected boolean mqttSecurity = false;

    protected String infoMessage = null;

    protected MqttClient client;
    protected MqttConnectOptions connOpt = new MqttConnectOptions();
    protected MqttCallback callback;

    public abstract boolean initialize();

    /**
     * This method reads general parameters from Mqtt config file
     */
    public boolean readMqttConfig(){

        try {
            SAXBuilder builder = new SAXBuilder();
            File xmlFile = new File(MQTT_CONFIG_FILE);
            Document doc = (Document) builder.build(xmlFile);
            Element root = doc.getRootElement();

            //get parameters from config file
            Element connectionParameters = root.getChild("connection-params");

            brokerAddress = connectionParameters.getChild("broker-url").getValue();  
            brokerPort = Integer.valueOf(connectionParameters.getChild("broker-port").getValue());

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

    /**
     * Connection to broker implementation.
     *
     * @throws MqttException, GeneralSecurityException, IOException
     */
    protected void connectToBroker() throws MqttException, GeneralSecurityException, IOException{

        // initial values
        String connectProtocol = "tcp://";
        int connectPort = brokerPort;

        // Set clean session
        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(keepAliveInterval);

        // Enable Automatic Reconnect
        connOpt.setAutomaticReconnect(true);  

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
            client.setCallback(callback);
            subscribeToTopics();

    }

    /**
     * This method returns connection status
     *
     * @return - true if client is connected, else false
     */
    public boolean isConnected(){
        if(client != null){
            isConnected = client.isConnected();
        }
        else{
            isConnected = false;
        }
        return isConnected;
    }

    /**
     * This method adds client subscription to specific topics
     *
     * @throws MqttException
     */

    protected abstract void subscribeToTopics() throws MqttException;

    /**
     * This method sets callback listener for mqtt events 
     * that happen asynchronously.
     *
     */

    protected abstract MqttCallback setCallback();

    public abstract DataField[] getOutputFormat();

    public abstract String getWrapperName();

    public abstract void dispose();

    
}


