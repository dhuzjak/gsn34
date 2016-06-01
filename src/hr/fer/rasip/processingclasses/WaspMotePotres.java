package hr.fer.rasip.processingclasses;

import gsn.beans.DataField;
import gsn.beans.StreamElement;

import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.TreeMap;


public class WaspMotePotres extends AbstractVirtualSensor{
	
	//postavi ime kolone za podatkovni dio poruke
	//predpostavljeni naziv je "data"
	private static final String DATA_FIELD_NAME = "data";
	private String dataFieldName;
	private int temperature;
	private int battery;
	private int ax;
	private int ay;
	private int az;
	
    private static final transient Logger logger = Logger.getLogger(WaspMotePotres.class);
    
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
				temperature = Integer.parseInt(message.split("!t!")[1]);
				battery = Integer.parseInt(message.split("!b!")[1]);
				ax = Integer.parseInt(message.split("!ax!")[1]);
				ay = Integer.parseInt(message.split("!ay!")[1]);
				az = Integer.parseInt(message.split("!az!")[1]);
				
			}
		}
    	
		StreamElement out = new StreamElement(new DataField[] {new DataField("temperature","int", "Measured temperature" ),
								new DataField("battery","int", "Measured battery" ), 
								new DataField("ax","int", "Measured ax" ), 
								new DataField("ay","int", "Measured ay" ),
								new DataField("az","int", "Measured az" )},
								new Serializable[]{temperature,battery, ax, ay, az});
        dataProduced(out);
    }

    public void dispose(){

    }
}

