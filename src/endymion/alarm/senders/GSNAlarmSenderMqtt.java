package endymion.alarm.senders;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import hr.fer.rasip.mqtt.service.MQTTService;

import java.io.File;
import java.io.FileInputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;

/**
 * This class extends GSNAlarmSender and implements MQTT alarm sender
 */
public class GSNAlarmSenderMqtt extends GSNAlarmSender {

    private static final String MQTT_CONFIG_FILE = "mqtt/config.xml";

    private List<String> endymionAlarmTopicList;
    private String endymionAlarmTopic = null;

    /**
     * Constructor
     */
    public GSNAlarmSenderMqtt () {
        endymionAlarmTopicList = new ArrayList<String>();
    }

    /**
     * Sets a list of topics to publish alarm messages
     * @param parameters - topic parameters
     * @throws EndymionException
     */
    @Override
    public void setSendParameters(String... parameters) throws EndymionException {
        
        if (parameters.length == 1) {
            
            // check if send-to parameter in config is set
            if(parameters[0] != null && !parameters[0].equals("")){

                String[] endymionAlarmTopicArray = parameters[0].split(";");

                Collections.addAll(endymionAlarmTopicList, endymionAlarmTopicArray);

                /*
                // for testing purposes
                for (String publishTo : endymionAlarmTopicList) {
                    System.out.println("topic list:");
                    System.out.println(publishTo);
                    System.out.println("");
                    //MQTTService.sendMqttMessage(publishTo, "alarm set for");
                }
                */

            }
            
            
        } else {
            throw new EndymionException("Wrong parameter number!", EndymionLoggerEnum.ERROR);
        }

        // get failsafe topic parameter from mqtt config
        try {
            SAXBuilder builder = new SAXBuilder();
            File xmlFile = new File(MQTT_CONFIG_FILE);
            Document doc = (Document) builder.build(xmlFile);
            Element root = doc.getRootElement();

            //get parameters from config file
            Element connectionParameters = root.getChild("connection-params");

            endymionAlarmTopic = connectionParameters.getChild("mqtt-topic-endymion-alarm").getValue();  


        }
        catch(Exception e){
            throw new EndymionException (e.getMessage(), EndymionLoggerEnum.ERROR);

        }
    }

    /**
     * This method publishes mqtt alarm message to topics that were set as senders
     * @param subjectLine - not used therefore equals null
     * @param alarmMessage - message regarding the concrete alarm
     * @throws EndymionException
     */
    @Override
    public void sendAlarm(String subjectLine, String alarmMessage) throws EndymionException {
        
        // failsafe publish topic
        if (endymionAlarmTopicList.isEmpty()) {
            MQTTService.sendMqttMessage(endymionAlarmTopic, alarmMessage);
        }
        else{
            for (String publishTo : endymionAlarmTopicList) {
                //System.out.println(publishTo);
                MQTTService.sendMqttMessage(publishTo, alarmMessage);
            }
        }
        // console output for testing purposes
        System.out.println("Mqtt Alarm sent");
        //System.out.println(alarmMessage);
    }
}
