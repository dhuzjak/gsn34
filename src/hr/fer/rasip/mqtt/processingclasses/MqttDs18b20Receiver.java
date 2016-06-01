package hr.fer.rasip.mqtt.processingclasses;

import gsn.beans.DataField;
import gsn.beans.StreamElement;

import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.TreeMap;

public class MqttDs18b20Receiver extends AbstractVirtualSensor{
    
    //postavi ime kolone za podatkovni dio poruke
    //predpostavljeni naziv je "data"
    private static final String DATA_FIELD_NAME = "message";

    private String dataFieldName;
    private double temperature;


    
    
    private static final transient Logger logger = Logger.getLogger(MqttDs18b20Receiver.class);
    
    public boolean initialize() {

        VSensorConfig vsensor = getVirtualSensorConfiguration();
        TreeMap<String, String> params = vsensor.getMainClassInitialParams();
        

        if (params.get("data-field-name") ==  null){
            dataFieldName = DATA_FIELD_NAME;
        }
        else{
            dataFieldName = params.get("data-field-name");
        }

        return true;
    }

    public void dataAvailable(String inputStreamName, StreamElement data) {
        
        String [] fieldNames = data.getFieldNames();
        
        for(int i=0; i < fieldNames.length; i++) {
            if(fieldNames[i].equals(dataFieldName.toUpperCase())) {

                //procitaj data dio primljene poruke
                try {
                    temperature = Double.parseDouble(data.getData()[i].toString());

                    StreamElement out = new StreamElement(new DataField[] {new DataField("temperature","double", "Measured temperature" )}, 
                                              
                                              new Serializable[]{temperature});
                    dataProduced(out);

                }
                catch (Exception e) {
                    logger.error(getVirtualSensorConfiguration().getName() + ": message not Double");
                }
                
            }
        }
        
        
    }

    public void dispose(){

    }
}

