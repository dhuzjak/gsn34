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

import hr.fer.rasip.AdafruitLCD.*;


public class MqttAdafruitLcd extends AbstractVirtualSensor{
	
    private static final String DATA_FIELD_NAME = "message";
    private static final String DATA_FIELD_NAME_TOPIC = "topic";

    private String dataFieldName;
    private String topicFieldName;

    private final transient Logger logger = Logger.getLogger(MqttAdafruitLcd.class);
    
  	static final int BUS_NO = 1;
  	static final int BUS_ADDRESS = 0x20;

    	
    private Lcd lcd;

    public boolean initialize() {

        VSensorConfig vsensor = getVirtualSensorConfiguration();
        TreeMap<String, String> params = vsensor.getMainClassInitialParams();
        

        if (params.get("data-field-name") ==  null){
            dataFieldName = DATA_FIELD_NAME;
        }
        else{
            dataFieldName = params.get("data-field-name");
        }

        topicFieldName = DATA_FIELD_NAME_TOPIC;
        		  

        // get LCD object
        try {
          
          lcd = new AdafruitLcdPlate(BUS_NO, BUS_ADDRESS);
      	
        }catch( IOException | InterruptedException e){
                logger.error(e.getMessage(), e);
                e.printStackTrace();
                return false;
        }

        // init LCD
      	lcd.setDisplay(true);
      	lcd.setBacklight(lcd.BACKLIGHT_RED);
      	lcd.write(0, "Hello");

        return true;
    }

    public void dataAvailable(String inputStreamName, StreamElement data) {
        
        String [] fieldNames = data.getFieldNames();
        String newMessage = null;
        String topicName = null;

        lcd.clear();

        for(int i=0; i < fieldNames.length; i++) {
           //get topic
          if(fieldNames[i].equals(topicFieldName.toUpperCase())) {
            topicName = data.getData()[i].toString();

            // output first 16 (or less) characters to LCD row 0
            topicName = topicName.substring(0, Math.min(topicName.length(), 15));
            lcd.write(0, topicName);

          }
          //get message
          if(fieldNames[i].equals(dataFieldName.toUpperCase())) {
            newMessage = data.getData()[i].toString();

            // output first 16 (or less) characters to LCD row 1
            newMessage = newMessage.substring(0, Math.min(newMessage.length(), 15));
            lcd.write(1, newMessage);

          }
         
        }

        if (newMessage != null && topicName != null){

          StreamElement out = new StreamElement(new DataField[] {new DataField("topic","varchar(16)", "Topic info" ),
                                                                  new DataField("message","varchar(16)", "Message" )
                                                                }, 
                                    
                                    new Serializable[]{   topicName,
                                                          newMessage
                                                      });
          dataProduced(out);
        }


    }

    public void dispose(){

        lcd.clear();
        lcd.shutdown();

    }
}



