
package hr.fer.rasip.processingclasses;

import gsn.beans.AddressBean;
import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.beans.StreamSource;
import gsn.beans.InputStream;

import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.TreeMap;





public class WaspCurrBlackAvg extends AbstractVirtualSensor{
	
	//postavi ime kolone za podatkovni dio poruke
	//predpostavljeni naziv je "data"
	private static final String DATA_FIELD_NAME = "data";
	private String dataFieldName;
	
	private double current;
	private double power;

	private double lastPowerValue = 0;

	private double valueAverage = 0;


	private long time_division = 0;
	private Boolean flag = false;



	private long timeDifference  = 0;
	private long  valueAccumulator = 0;  
	private long  timeAccumulator = 0; 
	
	private long[] timestamp = new long[2];

	// zadnji podatak
	private long lastTimestamp = 0;
	
    private static final transient Logger logger = Logger.getLogger(WaspCurrBlackAvg.class);
    
    public boolean initialize() {
    	VSensorConfig vsensor = getVirtualSensorConfiguration();
        TreeMap<String, String> params = vsensor.getMainClassInitialParams();

        String predicateKey = null;

        
       for ( final InputStream inputStream : vsensor.getInputStreams( ) ) {
                for ( final StreamSource ss : inputStream.getSources( ) ) {
                        for ( final AddressBean addressing : ss.getAddressing( ) ) {                            
                            if (addressing.getPredicateValue("time-division") != null);
                                predicateKey = addressing.getPredicateValue("time-division");
                        }
                }
        }
        if (predicateKey != null)
        	time_division = Long.parseLong(predicateKey);

		
        if (params.get("data-field-name") ==  null){
        	dataFieldName = DATA_FIELD_NAME;
        }
        else{
        	dataFieldName = params.get("data-field-name");
        }
        return true;
    }

    public void dataAvailable(String inputStreamName, StreamElement data) {

    	long n;
    	java.util.Date date= new java.util.Date();		// za timestamp

    	String [] fieldNames = data.getFieldNames();
    	String message;
		for(int i=0; i < fieldNames.length; i++) {
			if(fieldNames[i].equals(dataFieldName.toUpperCase())) {
				//procitaj data dio primljene poruke
				message = (String) data.getData()[i];
				//ovdje dodati za citanje drugih parametara
				current = Double.parseDouble(message.split("!cm!")[1]);
				power = current * 230.0;
				// racunanje srednje vrijednosti
				if (!flag){
					timestamp[0] = date.getTime();		// dohvati prvi timestamp
					lastTimestamp = timestamp[0];
					flag=true;
				}
				else{
					timestamp[1] = date.getTime();		// dohvati drugi timestamp
					lastTimestamp = timestamp[1];
					flag=false;
				}

				if ((timestamp[0] == 0 || timestamp[1]==0)){
					timeDifference = 0;
				}
				else{
					timeDifference = java.lang.Math.abs(timestamp[0]-timestamp[1]);		// pogledaj kolika je razlika u vremenima (trenutna - zadnja primljena)
				}
		
				// dijeli sa 50 ms manje od time divisiona, ukoliko je timeDifference par ms manji od timedivisiona, n bi ispao 0
                n = timeDifference/(time_division-50);				// koliko vremenskih odsjecaka je proslo

                if (n == 1){
					valueAccumulator+= power ;			
                }
				else if (n > 1){
					valueAccumulator+= lastPowerValue * (n-1);	// toliko vremenskih odsjecaka pomnozi s proslom vrijednosti (zbrajanje)
					valueAccumulator+= power;					// + trenutna vrijednost

				}
				
				timeAccumulator+= n;		// .getTime() je u ms, zelimo sekunde
				valueAverage = (double)valueAccumulator / timeAccumulator;	//izracunaj zbroj / ukupno vrijeme = srednja vrijednost

				valueAverage = Math.round(valueAverage * 100) / 100;		// na dvije decimale

				lastPowerValue = power;

				
			}
		}
		
		
		StreamElement out = new StreamElement(new DataField[] {new DataField("current", "double", "Current"),
								new DataField("power", "double", "PWR"),
								new DataField("powerAvg", "double", "average PWR")
								
								},
									new Serializable[]{current, power, valueAverage});
        dataProduced(out);

        
        
    }

    public void dispose(){

    }
}

