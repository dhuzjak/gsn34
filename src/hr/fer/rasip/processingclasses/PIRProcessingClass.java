package hr.fer.rasip.processingclasses;

import gsn.beans.DataField;
import gsn.beans.StreamElement;

import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.TreeMap;


public class PIRProcessingClass extends AbstractVirtualSensor{
	
	//postavi ime kolone za podatkovni dio poruke
	//predpostavljeni naziv je "data"
	private static final String DATA_FIELD_NAME = "message";

	private String dataFieldName;
	private int motion;
	
	
    private static final transient Logger logger = Logger.getLogger(PIRProcessingClass.class);
    
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
				motion = (Integer) data.getData()[i];

                StreamElement out = new StreamElement(new DataField[] {new DataField("motion","int", "Mqtt motion detector" )}, 
                                          
                                          new Serializable[]{motion});
                dataProduced(out);
				
			}
		}
    	
		
    }

    public void dispose(){

    }
}