package gsn.wrappers.rasip;

import gsn.beans.DataField;
import gsn.beans.DataTypes;
import gsn.beans.StreamElement;
import gsn.beans.AddressBean;

import jssc.SerialPort;
import com.sun.org.apache.xpath.internal.operations.Equals;
import java.nio.Buffer;
import jssc.SerialPort;
import jssc.SerialPortException;

import java.io.File;
import java.io.Serializable;

import org.apache.log4j.Logger;

import gsn.wrappers.*;
import gsn.electricity_1.*;

public class ElectricityMeterWrapper_1 extends AbstractWrapper{
    
    private static final int            DEFAULT_SAMPLING_RATE	= 60000;
    private int                         samplingRate			= DEFAULT_SAMPLING_RATE;
    private static int                  threadCounter			= 0;
    private final transient Logger      logger					= Logger.getLogger(ElectricityMeterWrapper.class);
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
	private static final String       CURRENT_PHASE_1			= "CURRENT_PHASE_1";
	private static final String       CURRENT_PHASE_2			= "CURRENT_PHASE_2";
	private static final String       CURRENT_PHASE_3			= "CURRENT_PHASE_3";
	

	
    
    private transient DataField [ ] outputStructureCache = new DataField[] { new DataField( FIELD_MAX_CUMM_POWER_T1, "double" , "Maximum cummulative poweer tarrife one." ), new DataField( FIELD_MAX_CUMM_POWER_T2, "double" , "Maximum cummulative poweer tarrife two" ), new DataField( FIELD_POWER, "double", "Electricity Power" ),
            new DataField( FIELD_POWER_T1, "double", "Electricity Power tarrife one" ), new DataField( FIELD_POWER_T2, "double", "Electricity Power tarrife two" ), new DataField( CURRENT_PHASE_1, "double", "Current in phase 1" ),new DataField( CURRENT_PHASE_2, "double", "Current in phase 2" ),new DataField( CURRENT_PHASE_3, "double", "Current in phase 3" )};
    
    private static final String [ ] FIELD_NAMES = new String [ ] { FIELD_MAX_CUMM_POWER_T1, FIELD_MAX_CUMM_POWER_T2, FIELD_POWER, FIELD_POWER_T1, FIELD_POWER_T2, CURRENT_PHASE_1, CURRENT_PHASE_2, CURRENT_PHASE_3 };
    
	
	double max_cumm_power_t1, max_cumm_power_t2, power, power_t1, power_t2, current_phase1, current_phase2, current_phase3;
	
    public boolean initialize() {
        logger.info("Initializing ElectricityMeterWrapper_1 Class");
        String javaVersion = System.getProperty("java.version");
        if(!javaVersion.startsWith("1.7")){
            logger.error("Error in initializing DiskSpaceWrapper because of incompatible jdk version: " + javaVersion + " (should be 1.7.x)");
            return false;
        }
        setName("ElectricityMeterWrapper_1-Thread" + (++threadCounter));
		
		addressBean = getActiveAddressBean();
		
		//citanje parametara iz XML datoteke
		serialPort = addressBean.getPredicateValue("serial-port");
		
		
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
			/*
            System.out.println(power);
            System.out.println(current_phase1);
            System.out.println(max_cumm_power_t1);
			System.out.println(max_cumm_power_t2);
            System.out.println(power_t1);
            System.out.println(power_t2);
			System.out.println(current_phase2);
            System.out.println(current_phase3);
            */
            //System.out.println(current_power_t1);
			//System.out.println(current_power_t2);
			
        }
        catch (Exception ex){
            System.out.println(ex);
            continue;
        }
            
            StreamElement streamElement = new StreamElement( FIELD_NAMES , new Byte [ ] { DataTypes.DOUBLE, DataTypes.DOUBLE, DataTypes.DOUBLE, DataTypes.DOUBLE, DataTypes.DOUBLE,DataTypes.DOUBLE,DataTypes.DOUBLE,DataTypes.DOUBLE } , new Serializable [ ] { max_cumm_power_t1, max_cumm_power_t2, power, power_t1, power_t2, current_phase1, current_phase2, current_phase3 }, System.currentTimeMillis( ) );
            
            postStreamElement(streamElement);
        }
    }
    
	
    
    public void dispose() {
        threadCounter--;
    }
    
    public String getWrapperName() {
        return "Electricity Meter 1";
    }
    
    public DataField[] getOutputFormat() {
        return outputStructureCache;
    }
    
}
