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

import java.io.*;
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
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.security.GeneralSecurityException; 
import java.io.IOException; 

public class MqttRelayWrapper extends AbstractMqttClient {

    private DataField[] collection = new DataField[]{new DataField("Relay_1","varchar(10)", "Relay State" ),
                                                                new DataField("Relay_2","varchar(10)", "Relay State" ),
                                                                new DataField("Relay_3","varchar(10)", "Relay State" ),
                                                                new DataField("Relay_4","varchar(10)", "Relay State" )    };

    private final transient Logger logger = Logger.getLogger(MqttRelayWrapper.class);
    private int counter;
    private AddressBean params;
    private long rate = 1000;

    protected String mqttRelayTopic;
    protected String mqttRelayTopicAck;


    private final String lastWillPayload = "{\"relays\":" + "[{\"name\":\"Relay 1\",\"id\":1,\"status\":false}," + 
                                                            "{\"name\":\"Relay 2\",\"id\":2,\"status\":false}," +
                                                            "{\"name\":\"Relay 3\",\"id\":3,\"status\":false}," +
                                                            "{\"name\":\"Relay 4\",\"id\":4,\"status\":false}]," +
                                            "\"relayControllerState\": false}";

    private GpioController gpio;
    private GpioPinDigitalOutput in_1;
    private GpioPinDigitalOutput in_2;
    private GpioPinDigitalOutput in_3;
    private GpioPinDigitalOutput in_4;
    private List<GpioPinDigitalOutput> relays = new ArrayList<GpioPinDigitalOutput>();

    public boolean initialize() {

        setName("MQTT relay" + counter++);

        params = getActiveAddressBean();

        gpio = GpioFactory.getInstance();

        // claim pins for usage in this wrapper
        in_1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "Input_1", PinState.HIGH);
        in_2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10, "Input_2", PinState.HIGH);
        in_3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, "Input_3", PinState.HIGH);
        in_4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, "Input_4", PinState.HIGH);
        
        // set state of relays after shutdown (HIGH = OFF)
        in_1.setShutdownOptions(Boolean.FALSE, PinState.HIGH);
        in_2.setShutdownOptions(Boolean.FALSE, PinState.HIGH);
        in_3.setShutdownOptions(Boolean.FALSE, PinState.HIGH);
        in_4.setShutdownOptions(Boolean.FALSE, PinState.HIGH);
        
        // add relays to list for batch state checking 
        relays.add(in_1);
        relays.add(in_2);
        relays.add(in_3);
        relays.add(in_4);

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

            mqttRelayTopic = connectionParameters.getChild("mqtt-topic-relay").getValue(); 
            mqttRelayTopicAck = connectionParameters.getChild("mqtt-topic-relay-ack").getValue();


        }
        catch(Exception e){
            logger.error(e.getMessage(), e);
            return false; 
        }

        // set callback function on Mqtt events
        callback = setCallback();

        // set last will (qos = 1, retain = true)
        connOpt.setWill(mqttRelayTopicAck, lastWillPayload.getBytes(), 1, true);
    
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

    @Override
    protected void subscribeToTopics() throws MqttException{

        client.subscribe(mqttRelayTopic);

    }

    @Override
    protected MqttCallbackExtended setCallback() {

        return new MqttCallbackExtended(){ 

            @Override
            public void connectionLost(Throwable cause) {
                isConnected = false;
                System.out.println(getWrapperName() + ": connection to " + brokerAddress + " lost. Reconnecting...");

            }

            // parse relay status
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String newMessage = message.toString();
                JSONParser parser = new JSONParser();
                
                try {
                    
                    JSONObject jsonObject = (JSONObject) parser.parse(newMessage);

                    JSONArray array = (JSONArray) jsonObject.get("relays");
                    for (int i = 0; i < array.size(); ++i) {
                        JSONObject relayState = (JSONObject)array.get(i);

                        if (relays.get(i).getState() != relayState.get((Object)"status")){
                            relays.get(i).setState((Boolean)relayState.get((Object)"status") == false);
                        }
                    }

                    // Set relayControllerState to true
                    jsonObject.put("relayControllerState", true);
                    newMessage = jsonObject.toJSONString();

                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                // send message as acknowledgement that this message was received by relay wrapper
                // relayControllerState set to true
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

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                System.out.println(getWrapperName() + ": reconnected to " + serverURI);

            }
        };
    }

}