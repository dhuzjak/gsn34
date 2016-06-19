package hr.fer.rasip.mqtt.processingclasses;

import gsn.beans.DataField;
import gsn.beans.StreamElement;

import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.TreeMap;

import hr.fer.rasip.mqtt.service.MQTTService;

public class MqttPIRPublisher extends AbstractVirtualSensor{
	
	//postavi ime kolone za podatkovni dio poruke

	private static final String DATA_FIELD_NAME = "motion";

    private static final String PUBLISH_TOPIC = "publish-topic";
	private String dataFieldName;
	private int motion;

    private String publishTopic;
	
	
    private static final transient Logger logger = Logger.getLogger(MqttPIRPublisher.class);
    
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
				
                motion = (Integer) data.getData()[i];

                // send message to topic
                MQTTService.sendMqttMessage(publishTopic, Integer.toString(motion));

            }
                	
		}

        StreamElement out = new StreamElement(new DataField[] {new DataField("motion","int", "Motion detector" )}, 
                                          
                                          new Serializable[]{motion});
        dataProduced(out);
    	
		
    }

    public void dispose(){

    }
}

