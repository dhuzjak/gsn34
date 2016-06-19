package hr.fer.rasip.mqtt.processingclasses;


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




public class MqttJsonLedTest extends AbstractVirtualSensor{
	
    private static final String DATA_FIELD_NAME = "message";


    private String dataFieldName;

    private final transient Logger logger = Logger.getLogger(MqttJsonLedTest.class);
    
    public boolean initialize() {

        VSensorConfig vsensor = getVirtualSensorConfiguration();
        TreeMap<String, String> params = vsensor.getMainClassInitialParams();
        

        if (params.get("data-field-name") ==  null){
            dataFieldName = DATA_FIELD_NAME;
        }
        else{
            dataFieldName = params.get("data-field-name");
        }

        
        		  System.out.println("\n JSON test init\n");

        

        return true;
    }

    public void dataAvailable(String inputStreamName, StreamElement data) {
        
        String [] fieldNames = data.getFieldNames();
        String newMessage = null;

        JSONParser parser = new JSONParser();

        System.out.println("\n JSON data recv\n");

        int alarmId;
        for(int i=0; i < fieldNames.length; i++) {
          
          //get message
          if(fieldNames[i].equals(dataFieldName.toUpperCase())) {
            newMessage = data.getData()[i].toString();
            try {
                  Object obj = parser.parse(newMessage);

                  System.out.println(obj);
                  
                  
                  JSONObject alarm = (JSONObject)obj;

                  //alarmId = (Integer) alarm.get("alarmId");
                  if(alarm.containsKey("alarmId")) {
                      alarmId = ((Number)alarm.get("alarmId")).intValue();

                      System.out.print("alarmID is: ");
                      System.out.println(alarmId);
                  }
                  else{
                    System.out.println("no alarmId");
                  }
                  
                          
                      
                  
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


    }
}



