
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
import java.lang.Math;




public class WaspCurrBlueAvgDay extends AbstractVirtualSensor{
	
	//postavi ime kolone za podatkovni dio poruke
	//predpostavljeni naziv je "data"
	private static final String DATA_FIELD_NAME = "power";
	private String dataFieldName;
	
	// varijable vezane za mjereni podatak
	private double value;
	private double averageValue = 0;
	
	// varijable vezane uz vrijeme
	private long TIMEBLOCK = 24*60 * 60 * 1000;				// 24 hours
	private long time_division = 0;

	// ostale neovisne varijable
	private Boolean flag = false;
	private double previousValue = 0;
	private long valueAccumulator = 0;  
	private long timeAccumulator = 0; 

	private long startTimeBlock = 0;
	private long endTimeBlock = 0;
	private long timeDifferenceUntilEndTimeBlock = 0;
	
	private long timeDifference  = 0;
	private long[] timestamp = new long[2];
	private long lastTimestamp = 0;
	
	private long previousDataTimestamp;	

    private static final transient Logger logger = Logger.getLogger(WaspCurrBlueAvgDay.class);
    
    public boolean initialize() {
    	VSensorConfig vsensor = getVirtualSensorConfiguration();
        TreeMap<String, String> params = vsensor.getMainClassInitialParams();

        String predicateKey = null;

        //postavi pocetak i kraj bloka
        java.util.Date date= new java.util.Date();	
        startTimeBlock = date.getTime();
        endTimeBlock = startTimeBlock + TIMEBLOCK;

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
				

				// ovisno koji podatak dohvacamo treba ili parsirati ili dobro pretvoriti tip podatka
				value = (Double) data.getData()[i];


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
					continue;
				}
				else{
					timeDifference = Math.abs(timestamp[0]-timestamp[1]);		// pogledaj kolika je razlika u vremenima (trenutna - zadnja primljena)
				}


				// dijeli sa 5 ms manje od time divisiona, ukoliko je timeDifference par ms manji od timedivisiona, n bi ispao 0
                n = timeDifference/time_division;				// koliko vremenskih odsjecaka je proslo 
                n = (n==0)?1:n;									// ako je podatak dosao prije time divisiona, postavi da je n=1, inace n=n
                //unutar intervala akumuliraj podatke
                if (lastTimestamp < (endTimeBlock)){
					if (n == 1){
						valueAccumulator+= value ;			
					}
					else if (n > 1){
						valueAccumulator+= previousValue * (n-1);	// toliko vremenskih odsjecaka pomnozi s proslom vrijednosti (zbrajanje)
						valueAccumulator+= value;					// + trenutna vrijednost
					}
					
					timeAccumulator += n;
				}
				//isteklo vrijeme
				else{
					// ako je proslo vise vremenskih odsjecaka izvan predviđog bloka a podatak se tek onda pojavio
					if(n>1) {
						// rekonstruraj vrijednosti od pojave prethodnog podatka do kraja intervala, zatim izracunaj avg
						previousDataTimestamp = lastTimestamp - timeDifference;
						timeDifferenceUntilEndTimeBlock = endTimeBlock - previousDataTimestamp;		//koliko je vremena proslo od prethodnog podatka do kraja tekuceg bloka
						
						n = timeDifferenceUntilEndTimeBlock/time_division;		//nadopuni podatke iz prethodnog bloka
						
						valueAccumulator+= previousValue * n;
						timeAccumulator += n;
						

						averageValue = (double)valueAccumulator / timeAccumulator;		// izracunaj avg

						// buduci da je podatak dosao izvan intervala, rekonstruiraj vrijednosti za sljedeci interval mjerenja i ubaci u novi akumulator
						valueAccumulator = 0;
						timeAccumulator = 0;

						timeDifference = lastTimestamp - endTimeBlock;
						n = timeDifference/time_division;
						n = (n==0)?1:n;

						if (n == 1){
							valueAccumulator+= value ;			
						}
						else if (n > 1){
							valueAccumulator+= previousValue * (n-1);	// toliko vremenskih odsjecaka pomnozi s proslom vrijednosti (zbrajanje)
							valueAccumulator+= value;					// + trenutna vrijednost
						}
						timeAccumulator += n;

						startTimeBlock = endTimeBlock;					//start blok je tamo gdje je trenutni blok zavrsava
						n = timeDifference / TIMEBLOCK;					//koliko vremenskih blokova je preskocio zadnji podatak?
						endTimeBlock = startTimeBlock + (n+1) * TIMEBLOCK;		//postavi endTimeBlock za toliko blokova
					}
					// ako je bilo sve regularno
					else{
						valueAccumulator+= value ;							// dodaj jos zadnju vrijednost
						timeAccumulator += n;
						startTimeBlock = lastTimestamp;
						endTimeBlock = startTimeBlock + TIMEBLOCK;
						averageValue = (double)valueAccumulator / timeAccumulator;		// izracunaj avg
						valueAccumulator = 0;
						timeAccumulator = 0;
					}

					// zaokruzi na dvije decimale
					averageValue = Math.round(averageValue * 100);
					averageValue /= 100;
					

					// objavi podatke (promijeniti ime varijable ovisno kako je u XML opisniku)
					StreamElement out = new StreamElement(
								new DataField[] {new DataField("powerAvgDay", "double", "PowerAvgDay")},	
								new Serializable[]{averageValue});
    				dataProduced(out);

				}
				previousValue = value;
			}
		}	
    }

    public void dispose(){

    }
}

