package gsn.wrappers.rasip;

import gsn.beans.DataField;
import gsn.beans.DataTypes;
import gsn.beans.StreamElement;
import gsn.beans.AddressBean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import gsn.wrappers.*;
import gsn.electricity.*;

public class ElectricityMeterWrapper_v2 extends AbstractWrapper{
    
    private static final int            DEFAULT_SAMPLING_RATE	= 60000;
    private int                         samplingRate			= DEFAULT_SAMPLING_RATE;
    private static int                  threadCounter			= 0;
    private final transient Logger      logger					= Logger.getLogger(ElectricityMeterWrapper_v2.class);
	private String                  serialPort;
	private String[] str = new String[8];
	
	private AddressBean             addressBean;
	
    //private static final String       FIELD_CURRENT_POWER_T1	= "CURRENT_POWER_T1";
	//private static final String       FIELD_CURRENT_POWER_T2	= "CURRENT_POWER_T2";
    private static final String       FIELD_MAX_CUMM_POWER_T1	= "MAX_CUMM_POWER_T1";
	private static final String       FIELD_MAX_CUMM_POWER_T2	= "MAX_CUMM_POWER_T2";
    private static final String       FIELD_POWER				= "POWER";
    private static final String       FIELD_POWER_T1			= "POWER_T1";
	private static final String       FIELD_POWER_T2			= "POWER_T2";
	
	byte[] acknowledgeMessage = new byte[]{0x06,0x30,0x30,0x31,0x0D,0x0A};
    byte[] closeMessage=new byte[]{0x01,0x42,0x30,0x03,0x71};
    
    byte[] fullPowerMessage=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x38,0x2e,0x30,0x28,0x29,0x03,0x5a};
    byte[] fullPowerT1Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x38,0x2e,0x31,0x28,0x29,0x03,0x5b};
    byte[] fullPowerT2Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x38,0x2e,0x32,0x28,0x29,0x03,0x58};

    byte[] maxCummulativePowerT1Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x32,0x2e,0x31,0x28,0x29,0x03,0x51};
    byte[] maxCummulativePowerT2Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x32,0x2e,0x32,0x28,0x29,0x03,0x52};

    byte[] currentPowerT1Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x34,0x2e,0x31,0x28,0x29,0x03,0x57};
    byte[] currentPowerT2Message=new byte[]{0x01,0x52,0x31,0x02,0x31,0x2e,0x34,0x2e,0x32,0x28,0x29,0x03,0x54};

    /*byte[] currentPhase1=new byte[] {0x01, 0x52, 0x31, 0x02, 0x33, 0x31, 0x2e, 0x37, 0x2e, 0x30, 0x28, 0x29, 0x03, 0x66};
    byte[] currentPhase2=new byte[] {0x01, 0x52, 0x31, 0x02, 0x35, 0x31, 0x2e, 0x37, 0x2e, 0x30, 0x28, 0x29, 0x03, 0x60};
    byte[] currentPhase3=new byte[] {0x01, 0x52, 0x31, 0x02, 0x37, 0x31, 0x2e, 0x37, 0x2e, 0x30, 0x28, 0x29, 0x03, 0x62};*/
    
    private boolean errorOccured;
	

	
    
    private transient DataField [ ] outputStructureCache = new DataField[] { new DataField( FIELD_MAX_CUMM_POWER_T1, "double" , "Maximum cummulative poweer tarrife one." ), new DataField( FIELD_MAX_CUMM_POWER_T2, "double" , "Maximum cummulative poweer tarrife two" ), new DataField( FIELD_POWER, "double", "Electricity Power" ),
            new DataField( FIELD_POWER_T1, "double", "Electricity Power tarrife one" ), new DataField( FIELD_POWER_T2, "double", "Electricity Power tarrife two" )};
    
    private static final String [ ] FIELD_NAMES = new String [ ] { FIELD_MAX_CUMM_POWER_T1, FIELD_MAX_CUMM_POWER_T2, FIELD_POWER, FIELD_POWER_T1, FIELD_POWER_T2 };
    
	
	double max_cumm_power_t1, max_cumm_power_t2, power, power_t1, power_t2;
	
    public boolean initialize() {
        logger.info("Initializing ElectricityMeterWrapper_v2 Class");
        String javaVersion = System.getProperty("java.version");
        if(!javaVersion.startsWith("1.7")){
            logger.error("Error in initializing DiskSpaceWrapper because of incompatible jdk version: " + javaVersion + " (should be 1.7.x)");
            return false;
        }
        setName("ElectricityMeterWrapper_v2-Thread" + (++threadCounter));
		
		addressBean = getActiveAddressBean();
		
		//citanje parametara iz XML datoteke
		serialPort = addressBean.getPredicateValue("serial-port");
		
		errorOccured = false;
		return true;
		
    }
    
    public void run(){
        while(isActive()){
            try{
                Thread.sleep(samplingRate);
            }catch (InterruptedException e){
                logger.error(e.getMessage(), e);
            }
			
            try {
            	SerialCommunication comm = new SerialCommunication (serialPort);
            	
            	comm.openPort();
            	
            	comm.write("/?!\r\n", "start");
            	comm.write(new String (acknowledgeMessage), "ack");
            	comm.write(new String (fullPowerMessage), "power");
            	//comm.write(new String (currentPhase1), "current_phase1");
            	comm.write(new String (maxCummulativePowerT1Message), "max_cumm_power_t1");
            	comm.write(new String (maxCummulativePowerT2Message), "max_cumm_power_t2");
            	comm.write(new String (fullPowerT1Message), "power_t1");
            	comm.write(new String (fullPowerT2Message), "power_t2");
            	//comm.write(new String (currentPhase2), "current_phase2");
            	//comm.write(new String (currentPhase3), "current_phase3");
            	comm.write(new String (closeMessage), "stop");
            	
            	comm.closePort();
            	
            	HashMap <String, String> results = comm.getResults();
            	
            	for (String key : results.keySet()) {
            		System.out.println(key + " = " + results.get(key));
            	}
            	
            	//Pattern pattern = Pattern.compile("(?<=\\()(.*?)(?=\\s)");
            	power = extractValue(results.get("power"));
            	//current_phase1 = extractValue(results.get("current_phase1"));
            	max_cumm_power_t1 = extractValue(results.get("max_cumm_power_t1"));
            	max_cumm_power_t2 = extractValue(results.get("max_cumm_power_t2"));
            	power_t1 = extractValue(results.get("power_t1"));
            	power_t2 = extractValue(results.get("power_t2"));
            	//current_phase2 = extractValue(results.get("current_phase2"));
            	//current_phase3 = extractValue(results.get("current_phase3"));
            	
            	/* Wrong readings are not sent to client */
            	if (errorOccured) {
            		System.out.println("Dogodila se greska pri parsiranju rezultata!");
            		errorOccured = false;
            		continue;
            	}
            	
            	
            	
            } catch (Exception ex) {
            	ex.printStackTrace();
                continue;
            } 
            
			
            
			
          /*try {
				
				dohvati doh = new dohvati();
            str = doh.reading(serialPort);
            
            power = Double.parseDouble(str[0].substring(str[0].indexOf("(")+1,str[0].indexOf(" ")));
            current_phase1 = Double.parseDouble(str[1].substring(str[1].indexOf("(")+1,str[1].indexOf(" ")));
            max_cumm_power_t1 = Double.parseDouble(str[2].substring(str[2].indexOf("(")+1,str[2].indexOf(" ")));
			max_cumm_power_t2 = Double.parseDouble(str[3].substring(str[3].indexOf("(")+1,str[3].indexOf(" ")));
            power_t1 = Double.parseDouble(str[4].substring(str[4].indexOf("(")+1,str[4].indexOf(" ")));
            power_t2 = Double.parseDouble(str[5].substring(str[5].indexOf("(")+1,str[5].indexOf(" ")));
            current_phase2 = Double.parseDouble(str[6].substring(str[6].indexOf("(")+1,str[6].indexOf(" ")));
            current_phase3 = Double.parseDouble(str[7].substring(str[7].indexOf("(")+1,str[7].indexOf(" ")));
            //current_power_t1 = Double.parseDouble(str[8].substring(str[8].indexOf("(")+1,str[8].indexOf(" ")));
            //current_power_t2 = Double.parseDouble(str[9].substring(str[9].indexOf("(")+1,str[9].indexOf(" ")));
			
            System.out.println(power);
            System.out.println(current_phase1);
            System.out.println(max_cumm_power_t1);
			System.out.println(max_cumm_power_t2);
            System.out.println(power_t1);
            System.out.println(power_t2);
			System.out.println(current_phase2);
            System.out.println(current_phase3);
            
            //System.out.println(current_power_t1);
			//System.out.println(current_power_t2);
			
        }
        catch (Exception ex){
            System.out.println(ex);
        }*/
            
            StreamElement streamElement = new StreamElement( FIELD_NAMES , new Byte [ ] { DataTypes.DOUBLE, DataTypes.DOUBLE, DataTypes.DOUBLE, DataTypes.DOUBLE, DataTypes.DOUBLE } , new Serializable [ ] { max_cumm_power_t1, max_cumm_power_t2, power, power_t1, power_t2 }, System.currentTimeMillis( ) );
            
            postStreamElement(streamElement);
        }
    }
    
	
    
    public void dispose() {
        threadCounter--;
    }
    
    public String getWrapperName() {
        return "Electricity Meter v2";
    }
    
    public DataField[] getOutputFormat() {
        return outputStructureCache;
    }
    
   /* private Double extractValue (Pattern pattern, HashMap <String, String> results, String key) {
    	if (!results.containsKey(key)) {
    		return null;
    	}
    	Matcher match = pattern.matcher(results.get(key));
    	if (match.find()) {
    		return Double.parseDouble(match.group(1));
    	} else {
    		return null;
    	}
    }*/
    
    private Double extractValue (String value) {
    	if (value.isEmpty()) {
    		errorOccured = true;
    		return -1.0;
    	}
    	try {
    		return Double.parseDouble(value.substring(value.indexOf("(")+1,value.indexOf("*")));
    	} catch (Exception ex) {
    		errorOccured = true;
    		return -1.0;
    	}
    }
    
}