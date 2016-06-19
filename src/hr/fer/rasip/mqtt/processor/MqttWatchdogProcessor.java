package hr.fer.rasip.mqtt.processor;

import gsn.beans.DataField;
import gsn.beans.DataTypes;
import gsn.beans.StreamElement;
import gsn.vsensor.AbstractVirtualSensor;
import org.apache.log4j.Logger;

import gsn.beans.VSensorConfig;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.format.ISODateTimeFormat;
import java.util.Arrays;

import java.io.File;
import java.io.FileInputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;

import org.json.simple.JSONObject;
import hr.fer.rasip.mqtt.service.MQTTService;


public class MqttWatchdogProcessor extends AbstractVirtualSensor {

    private static final transient Logger logger = Logger.getLogger(MqttWatchdogProcessor.class);

    private static final String MQTT_CONFIG_FILE = "mqtt/config.xml";

    private static final String NOTIFICATION_DELAY = "delay";
    private static final String CRITICAL_PERIOD = "critical-period";

    private static final String SENSOR_NAME = "sensor-name";

    private static final int CHECK_PERIOD = 1000;  // check every 1 second

    private DataField[] outputStructure;

    private long notificationDelay = 0;
    private long criticalPeriod = 0;

    private String sensorName = null;

    private long startTime;
    private long notificationTime;

    private String gsnName;
    private Integer gsnId;
    private String watchdogTopic;

    private boolean alarmFlag = false;
    private Timer timer0;
    private TimerTask timerTask;

    protected StreamElement dataItem;
    
    
    public boolean initialize() {
        VSensorConfig vsensor = getVirtualSensorConfiguration();
        TreeMap<String, String> params = vsensor.getMainClassInitialParams();

        outputStructure = vsensor.getOutputStructure();

        startTime = System.currentTimeMillis();
        
                            
        if (params.get(NOTIFICATION_DELAY) != null)
        {
            try {
                notificationDelay = Long.parseLong(params.get(NOTIFICATION_DELAY));
            }
            catch (Exception e) {
                logger.error(getVirtualSensorConfiguration().getName()+ " parameter not Integer: " + NOTIFICATION_DELAY);
                return false;
            }
        }
        else
        {
            logger.warn(getVirtualSensorConfiguration().getName()+ " parameter not set: " + NOTIFICATION_DELAY);
            return false;
        }

        if (params.get(CRITICAL_PERIOD) != null)
        {
            try {
                criticalPeriod = Long.parseLong(params.get(CRITICAL_PERIOD));
            }
            catch (Exception e) {
                logger.error(getVirtualSensorConfiguration().getName()+ " parameter not Integer: " + CRITICAL_PERIOD);
                return false;
            }
        }
        else
        {
            logger.warn(getVirtualSensorConfiguration().getName()+ " parameter not set: " + CRITICAL_PERIOD);
            return false;
        }

        if (params.get(SENSOR_NAME) != null)
        {
                sensorName = params.get(SENSOR_NAME);
        }
        else
        {
            logger.warn(getVirtualSensorConfiguration().getName()+ " parameter not set: " + SENSOR_NAME);
            return false;
        }


        //fetch info from mqtt config
        try {
          SAXBuilder builder = new SAXBuilder();
          File xmlFile = new File(MQTT_CONFIG_FILE);
          Document doc = (Document) builder.build(xmlFile);
          Element root = doc.getRootElement();
          
          //get parameters from config file
          Element connectionParameters = root.getChild("connection-params");
          
          gsnName = connectionParameters.getChild("gsn-name").getValue();  
          gsnId = Integer.valueOf(connectionParameters.getChild("gsn-id").getValue());
          watchdogTopic = connectionParameters.getChild("mqtt-topic-watchdog").getValue(); 

         }
       catch(Exception e){
          logger.error(e.getMessage(), e);
          return false; 
       }

        // startTime is used in the virtual sensor class to start a timer
        timer0 = new Timer();

        timerTask = new MyTimerTask();
        timer0.schedule(timerTask, 0, CHECK_PERIOD);


        return true;
    }

    public void dataAvailable(String inputStreamName, StreamElement data) {
            
            dataItem = data;
            startTime = dataItem.getTimeStamp();

            // little workaround, purpose of this is that first notification must
            // start immediately after expiration of critical period, not after
            // some delay
            notificationTime = startTime - notificationDelay;
            
    }
    
    public void dispose(){
        timer0.cancel();
        
    }

    // task for checking watchdog time
    class MyTimerTask extends TimerTask {

        public void run() {

            sendNotification(dataItem);

        }

        public void sendNotification(StreamElement data){

            long endPeriod = System.currentTimeMillis();
            long dataTimestamp;

            // if data is not received for critical period
            if ((startTime + criticalPeriod) < endPeriod ){

                // repeat notification every notificationDelay parameter
                if(notificationTime + notificationDelay < endPeriod ){

                    // if data == null, there was no data from start of watchdog
                    // else get timestamp of last data
                    if(data == null)
                        dataTimestamp = startTime;
                    else
                        dataTimestamp = data.getTimeStamp();

                    System.out.println(generateJSON(dataTimestamp, "alarm"));
                    MQTTService.sendMqttMessage(watchdogTopic, generateJSON(dataTimestamp, "alarm"));

                    notificationTime = endPeriod ;

                    // set flag to true so if new data is received watcdog will send "Ok" message
                    alarmFlag = true;
                }    
            }
            // if new data is received
            else{
                // if alarmFlag is set, send ok
                if(alarmFlag){

                    if(data == null)
                        dataTimestamp = startTime;
                    else
                        dataTimestamp = data.getTimeStamp();

                    //System.out.println(generateJSON(dataTimestamp, "Ok"));
                    MQTTService.sendMqttMessage(watchdogTopic, generateJSON(dataTimestamp, "Ok"));

                    alarmFlag = false;

                }
            }
        }

        public String generateJSON(long timed, String info){

            // notification delay and critical period are in ms, convert to seconds
            JSONObject jsonOutput = new JSONObject();
            String str = ISODateTimeFormat.dateTime().print(timed);
            jsonOutput.put("watchdogInfo", info);
            jsonOutput.put("gsnName", gsnName);
            jsonOutput.put("gsnId", gsnId);
            jsonOutput.put("sensorName", sensorName);
            jsonOutput.put("notificationDelay", notificationDelay/1000);
            jsonOutput.put("criticalPeriod", criticalPeriod/1000);
            jsonOutput.put("timeUnit", "second");
            jsonOutput.put("timestamp", str);

            return jsonOutput.toJSONString();
        

        }

    }
}
