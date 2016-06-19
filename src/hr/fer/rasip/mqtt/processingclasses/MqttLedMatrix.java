package hr.fer.rasip.mqtt.processingclasses;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

import java.io.*;

import java.io.Serializable;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.ArrayList;
import java.util.List;

import hr.fer.rasip.LedMatrix.LedMatrix;


public class MqttLedMatrix extends AbstractVirtualSensor{
	
    private static final String DATA_FIELD_NAME = "message";

    private static final String JSON_ALARM_ID_KEY = "alarmId";
    private static final String JSON_ALARM_TYPE_KEY = "alarmType";


    private String dataFieldName;

    private final transient Logger logger = Logger.getLogger(MqttLedMatrix.class);
    
  	static final int BUS_NO = 1;
  	static final int BUS_ADDRESS = 0x20;

    private int blinkRate = 1000;

    	
    private LedMatrix ledMatrix;
	private BlinkTimer timer;

	private List<Integer> ledList = new ArrayList<>();


    public boolean initialize() {

        VSensorConfig vsensor = getVirtualSensorConfiguration();
        TreeMap<String, String> params = vsensor.getMainClassInitialParams();
        

        if (params.get("data-field-name") ==  null){
            dataFieldName = DATA_FIELD_NAME;
        }
        else{
            dataFieldName = params.get("data-field-name");
        }

        ledMatrix = new LedMatrix();

		timer = new BlinkTimer();

        return true;
    }

    public void dataAvailable(String inputStreamName, StreamElement data) {
        
        String [] fieldNames = data.getFieldNames();
        String newMessage = null;

        JSONParser parser = new JSONParser();

        int alarmId;
        String alarmType;

        for(int i=0; i < fieldNames.length; i++) {
          
          //get message
          if(fieldNames[i].equals(dataFieldName.toUpperCase())) {
            newMessage = data.getData()[i].toString();

            // parse JSON message
            try {
                  Object obj = parser.parse(newMessage);
                  
                  JSONObject alarm = (JSONObject)obj;

                  if(alarm.containsKey(JSON_ALARM_ID_KEY)) {
                      alarmId = ((Number)alarm.get(JSON_ALARM_ID_KEY)).intValue();

                  }
                  else{
                    logger.error("Receieved JSON does not contain "+ JSON_ALARM_ID_KEY +" key!");
                    return;
                  }
                  if(alarm.containsKey(JSON_ALARM_TYPE_KEY )) {
                        alarmType = (String)alarm.get(JSON_ALARM_TYPE_KEY);

                  }
                  else{
                    logger.error("Receieved JSON does not contain "+ JSON_ALARM_TYPE_KEY +" key!");
                    return;
                  }

                  // if received message is alarmType = Ok
                  if(alarmType.equals("Ok")){

                    // remove alarmId from list
					if(!ledList.isEmpty()){
						if(ledList.contains(alarmId)){
							ledList.remove(ledList.indexOf(alarmId));
						}
					}
					else{
						timer.pause();					
					}
                    // turn On LED
					ledMatrix.drawPixel(alarmId, true);
					

                  }
                  // if received message is alarmType = alarm
                  else if(alarmType.equals("alarm")){
					
                    // add alarmId to alarm list
					if(!ledList.contains(alarmId)){
							ledList.add(alarmId);
							timer.start();
						}
					
					
                    //ledMatrix.drawPixel(alarmId, false);

                  }
				ledMatrix.writeDisplay();

            }
            catch (Exception e) {
                logger.error(getVirtualSensorConfiguration().getName() + ": Could not parse JSON");
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }
          }

      }


    }


    public void dispose(){

        try {
            ledMatrix.clearDisplay();
        }catch (IOException e) {
            e.printStackTrace();
        }
        timer.shutdown();

    }

    /**
     * This class is used for LED matrix LED toggling
     * as LED blinking represent alarm
     */
	public class BlinkTimer {

        private boolean isRunning = false;
        private ScheduledExecutorService execService = Executors.newSingleThreadScheduledExecutor();
        private Future<?> future = null;

		private int ledId;
        BlinkTimer() {
			
        }

        public void start() {
            if (isRunning)
                return;

            isRunning = true;
            future = execService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {   
					try{
                    
						for (Integer element : ledList) {
							ledMatrix.togglePixel(element);
						}
						ledMatrix.writeDisplay();

					}catch (IOException e) {
				        logger.error(getVirtualSensorConfiguration().getName() + ": Could not refresh Led Matrix");
				        logger.error(e.getMessage(), e);
				        e.printStackTrace();
				    }

                }
            }, 0, blinkRate, TimeUnit.MILLISECONDS);
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



