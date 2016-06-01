package hr.fer.rasip.mqtt.processingclasses;

import gsn.beans.DataField;
import gsn.beans.StreamElement;

import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.TreeMap;


public class TestMqttProcessing extends AbstractVirtualSensor{
	
	//postavi ime kolone za podatkovni dio poruke
	//predpostavljeni naziv je "data"
	private static final String DATA_FIELD_NAME = "message";
	private String dataFieldName;

	
	
    private static final transient Logger logger = Logger.getLogger(TestMqttProcessing.class);
    
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
    	
    	String message;
		for(int i=0; i < fieldNames.length; i++) {
			System.out.println(fieldNames[i].toUpperCase());
			if(fieldNames[i].toUpperCase().equals(dataFieldName.toUpperCase())) {

				message  = (String) data.getData()[i];
				System.out.println(message);
				//procitaj data dio primljene poruke

				StreamElement out = new StreamElement(new DataField[] {
												new DataField("temp","varchar(40)", "temperature" )
											
											  },
											  new Serializable[]{
													message
													
													}
											);
				dataProduced(out);
				
			}
		}
    	
		
       
    }

    public void dispose(){

    }
}