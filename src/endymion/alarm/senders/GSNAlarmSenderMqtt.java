package endymion.alarm.senders;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import hr.fer.rasip.mqtt.service.MQTTService;

/**
 * Helper class - used for debugging only
 */
public class GSNAlarmSenderMqtt extends GSNAlarmSender {



    private List<String> endymionAlarmTopicList;
    private String endymionAlarmTopic = "RASIP/Endymion/alarms";

    /**
     * Constructor
     */
    public GSNAlarmSenderMqtt () {
        endymionAlarmTopicList = new ArrayList<String>();
    }


    @Override
    public void setSendParameters(String... parameters) throws EndymionException {
        
        
        if (parameters.length == 1) {
            
            String[] endymionAlarmTopicArray = parameters[0].split(";");
            //System.out.println(Arrays.toString(endymionAlarmTopicArray));

            Collections.addAll(endymionAlarmTopicList, endymionAlarmTopicArray);

            /*
            for (String publishTo : endymionAlarmTopicList) {
                

                //MQTTService.sendMqttMessage(publishTo, "alarm set for");
            }
            */
        } else {
            throw new EndymionException("Wrong parameter number!", EndymionLoggerEnum.ERROR);
        }
    }

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

        System.out.println(alarmMessage);
    }
}
