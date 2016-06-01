package gsn.processor;

import gsn.beans.DataField;
import gsn.beans.DataTypes;
import gsn.beans.StreamElement;
import gsn.vsensor.AbstractVirtualSensor;
import org.apache.log4j.Logger;

import gsn.beans.VSensorConfig;

import java.io.Serializable;
import java.util.TreeMap;

import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.DateTime;

import gsn.utils.services.MQTTService;
import java.util.Arrays;

import java.io.File;
import java.io.FileInputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;


public class MqttNotificationProcessor extends AbstractVirtualSensor {

    private static final transient Logger logger = Logger.getLogger(MqttNotificationProcessor.class);

    private static final String MQTT_CONFIG_FILE = "mqtt/config.xml";


    private static final String PARAM_PERIOD = "period";
    private static final String DELAY = "delay";
    private static final String CRITICAL_VALUE = "critical-value";
    private static final String SENSOR_NAME = "sensor-name";
    private static final String CRITICAL_TYPE = "critical-type";
    private static final String MONITORED_FIELD = "monitored-field";

    private static final String WARNING = "Warning";
    private static final String OK = "OK";
    private static final String CRITICAL = "Critical";

    private String gsnName;
    private Integer gsnId;
    private String notificationsTopic;


    private DataField[] outputStructure;
    private StreamElement dataItem    ;   //Buffer for most recent stream element

    private long period = 0;
    private long delay = 0;
    private double criticalValue = 0;
    private String sensorName = null;
    private String criticalType = null;
    private String monitoredField = null;

    private double sensorData = 0;

    private double previousSensorData = 0;

    private Long timed;

    private String outputNotification;

    private boolean notificationFlag = false;

    private NotifyTimer timer;
    
    public boolean initialize() {
        VSensorConfig vsensor = getVirtualSensorConfiguration();
        TreeMap<String, String> params = vsensor.getMainClassInitialParams();

        outputStructure = vsensor.getOutputStructure();

        if (params.get(PARAM_PERIOD) != null)
        {
            try {
                period = Long.parseLong(params.get(PARAM_PERIOD));
            }
            catch (Exception e) {
                logger.error(getVirtualSensorConfiguration().getName()+ " parameter not Integer: " + PARAM_PERIOD);
                return false;
            }
        }
        else
        {
            logger.warn(getVirtualSensorConfiguration().getName()+ " parameter not set: " + PARAM_PERIOD);
            return false;
        }
                            
        if (params.get(DELAY) != null)
        {
            try {
                delay = Long.parseLong(params.get(DELAY));
            }
            catch (Exception e) {
                logger.error(getVirtualSensorConfiguration().getName()+ " parameter not Integer: " + DELAY);
                return false;
            }
        }
        else
        {
            logger.warn(getVirtualSensorConfiguration().getName()+ " parameter not set: " + DELAY);
            return false;
        }

        if (params.get(CRITICAL_VALUE) != null)
        {
            try {
                criticalValue = Double.parseDouble(params.get(CRITICAL_VALUE));
            }
            catch (Exception e) {
                logger.error(getVirtualSensorConfiguration().getName()+ " parameter not Double: " + CRITICAL_VALUE);
                return false;
            }
        }
        else
        {
            logger.warn(getVirtualSensorConfiguration().getName()+ " parameter not set: " + CRITICAL_VALUE);
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

        if (params.get(CRITICAL_TYPE) != null)
        {
                criticalType = params.get(CRITICAL_TYPE);

                if (!criticalType.equals("above") && !criticalType.equals("below"))
                {
                    logger.error(getVirtualSensorConfiguration().getName()+ " wrong parameter: " + CRITICAL_TYPE + "\n\t\t\t" + 
                        "should be {above || below}\n\t\t\tActual: " + criticalType);
                    return false;
                }
        }
        else
        {
            logger.warn(getVirtualSensorConfiguration().getName()+ " parameter not set: " + CRITICAL_TYPE);
            return false;
        }

        if (params.get(MONITORED_FIELD) != null)
        {
                monitoredField = params.get(MONITORED_FIELD);

        }
        else
        {
            logger.warn(getVirtualSensorConfiguration().getName()+ " parameter not set: " + MONITORED_FIELD);
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
          notificationsTopic = connectionParameters.getChild("mqtt-topic-notifications").getValue(); 

         }
       catch(Exception e){
          logger.error(e.getMessage(), e);
          return false; 
       }

        timer = new NotifyTimer();

        return true;
    }

    public String generateJSON(Double data, long timed, String info){

        JSONObject jsonOutput = new JSONObject();
        String str = ISODateTimeFormat.dateTime().print(timed);
        jsonOutput.put("notificationInfo", info);
        jsonOutput.put("gsnName", gsnName);
        jsonOutput.put("gsnId", gsnId);
        jsonOutput.put("sensorName", sensorName);
        jsonOutput.put("periodBetweenNotifications", period/1000);
        jsonOutput.put("sensorName", sensorName);
        jsonOutput.put("criticalType", criticalType);
        jsonOutput.put("criticalValue", criticalValue);
        jsonOutput.put("monitoredField", monitoredField);
        jsonOutput.put("sensorData", data);
        jsonOutput.put("timeUnit", "second");
        jsonOutput.put("timestamp", str);

        return jsonOutput.toJSONString();
    

    }

    public void dataAvailable(String inputStreamName, StreamElement data) {
        

        dataItem = data;
        
        String [] fieldNames = dataItem.getFieldNames();
            
        for(int i=0; i < fieldNames.length; i++) {
            if(fieldNames[i].equals(monitoredField.toUpperCase())) {
                

                timed = dataItem.getTimeStamp();
                try {
                    sensorData = Double.parseDouble(data.getData()[i].toString());
                }
                catch (Exception e) {
                    logger.error(getVirtualSensorConfiguration().getName() + ": message not Number");
                    return;
                }

                if (sensorData == criticalValue){
                    
                    notificationFlag = true;
                    
                    outputNotification = generateJSON(sensorData, timed, WARNING);
                    if(!timer.isRunning()){
                        timer.resume();
                        
                    }

                }

                if (criticalType.equals("above")){
                    if (sensorData > criticalValue){
                        
                        notificationFlag = true;
                        outputNotification = generateJSON(sensorData, timed, CRITICAL);

                        if(!timer.isRunning()){
                            timer.resume();                            
                        }

                    }
                    // if previous condition is not satisfied, then everything is ok
                    else if(notificationFlag == true && sensorData < criticalValue){
                        
                        MQTTService.sendMqttMessage(notificationsTopic, generateJSON(sensorData, timed, OK));
                        notificationFlag = false;
                        timer.pause();
                        
                    }

                }

                if (criticalType.equals("below")){
                    if (sensorData < criticalValue){
                        
                        notificationFlag = true;
                        outputNotification = generateJSON(sensorData, timed, CRITICAL);

                        if(!timer.isRunning()){
                            timer.resume();    
                        }

                    }
                    else if(notificationFlag == true && (sensorData > criticalValue)){

                        MQTTService.sendMqttMessage(notificationsTopic, generateJSON(sensorData, timed, OK));

                        notificationFlag = false;
                        timer.pause();
                       
                    }

                }
                
                

            }     
        }
    } 

    
    public void dispose(){
        timer.shutdown();
    }

    public class NotifyTimer {

        private boolean isRunning = false;
        private ScheduledExecutorService execService = Executors.newSingleThreadScheduledExecutor();
        private Future<?> future = null;

        NotifyTimer() {
        }

        public void start() {
            if (isRunning)
                return;

            isRunning = true;
            future = execService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {   
                    try{
                        MQTTService.sendMqttMessage(notificationsTopic, outputNotification);
                        //System.out.println(outputNotification);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, period, TimeUnit.MILLISECONDS);
        }

        public void pause() {
            if(!isRunning) return;
            future.cancel(false);
            isRunning = false;
        }

        public void resume() {
            this.start();
        }

        public void shutdown() {
            execService.shutdown();
            future.cancel(true);
        }

        public boolean isRunning(){
            return isRunning;
        }

    }

}