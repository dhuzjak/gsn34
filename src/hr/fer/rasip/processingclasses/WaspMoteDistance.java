package hr.fer.rasip.processingclasses;

import gsn.beans.DataField;
import gsn.beans.StreamElement;

import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.TreeMap;


public class WaspMoteDistance extends AbstractVirtualSensor{
	
	//postavi ime kolone za podatkovni dio poruke
	//predpostavljeni naziv je "data"
	private static final String DATA_FIELD_NAME = "data";
	private String dataFieldName;
	private double distance;
	private double light;
	
	
	
    private static final transient Logger logger = Logger.getLogger(WaspMoteDistance.class);
    
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
			if(fieldNames[i].equals(dataFieldName.toUpperCase())) {
				//procitaj data dio primljene poruke
				message = (String) data.getData()[i];
				//ovdje dodati za citanje drugih parametara
				distance = Double.parseDouble(message.split("!u!")[1]);
				light = Double.parseDouble(message.split("!o!")[1]);
				
			}
		}
    	
		StreamElement out = new StreamElement(new DataField[] {new DataField("distance","double", "Measured distance" ),
								new DataField("light","double", "Measured light" )},
								new Serializable[]{distance,light});
        dataProduced(out);
    }

    public void dispose(){

    }
}

