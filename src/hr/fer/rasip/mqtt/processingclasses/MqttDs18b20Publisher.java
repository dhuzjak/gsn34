package hr.fer.rasip.mqtt.processingclasses;

import gsn.beans.DataField;
import gsn.beans.StreamElement;

import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.TreeMap;

import hr.fer.rasip.mqtt.service.MQTTService;

public class MqttDs18b20Publisher extends AbstractVirtualSensor{
	
	//postavi ime kolone za podatkovni dio poruke
	//predpostavljeni naziv je "temperature"
	private static final String DATA_FIELD_NAME = "temperature";

    private static final String PUBLISH_TOPIC = "publish-topic";
	private String dataFieldName;
	private double temperature;

    private String publishTopic;
	
	
    private static final transient Logger logger = Logger.getLogger(MqttDs18b20Publisher.class);
    
    public boolean initialize() {
        
    	VSensorConfig vsensor = getVirtualSensorConfiguration();
        TreeMap<String, String> params = vsensor.getMainClassInitialParams();
        
        if (params.get(PUBLISH_TOPIC) != null)
        {
                publishTopic = params.get(PUBLISH_TOPIC);
        }
        else
        {
            logger.error(getVirtualSensorConfiguration().getName()+ " parameter not set: " + PUBLISH_TOPIC);
            return false;
        }

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
				
                temperature = (Double) data.getData()[i];

                // send message to topic
                MQTTService.sendMqttMessage(publishTopic, Double.toString(temperature));

            }
                	
		}

        StreamElement out = new StreamElement(new DataField[] {new DataField("temperature","double", "Measured temperature" )}, 
                                          
                                          new Serializable[]{temperature});
        dataProduced(out);
    	
		
    }

    public void dispose(){

    }
}

