package hr.fer.rasip.processingclasses;

import gsn.beans.DataField;
import gsn.beans.StreamElement;

import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.TreeMap;


public class WaspCurrBlack extends AbstractVirtualSensor{
	
	//postavi ime kolone za podatkovni dio poruke
	//predpostavljeni naziv je "data"
	private static final String DATA_FIELD_NAME = "data";
	private String dataFieldName;
	private double current;
	private double power;
	
    private static final transient Logger logger = Logger.getLogger(WaspCurrBlack.class);
    
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
				current = Double.parseDouble(message.split("!cm!")[1]);
				power = current * 230.0;
			}
		}
    	
		StreamElement out = new StreamElement(new DataField[] {new DataField("current","double", "Measured current" ), 
											  new DataField("power", "double", "Measured power")},
											  new Serializable[]{current,power});
        dataProduced(out);
    }

    public void dispose(){

    }
}

