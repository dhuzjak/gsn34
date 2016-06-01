package hr.fer.rasip.wrappers;
import java.io.*;
import gsn.beans.AddressBean;
import gsn.beans.DataField;
import gsn.wrappers.AbstractWrapper;

import java.io.Serializable;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class ds18b20 extends AbstractWrapper {
	
  private DataField[] collection = new DataField[] {new DataField("temperature", "double", "Rpi temperature")};
  private final transient Logger logger = Logger.getLogger(ds18b20.class);
  private int counter;
  private AddressBean params;
  private long rate = 1000;

  static String w1DirPath = "/sys/bus/w1/devices";
  
  
  public boolean initialize() {
    setName("RpiTemp" + counter++);
    
    params = getActiveAddressBean();
    
    if ( params.getPredicateValue( "sampling-rate" ) != null ){

    	rate = (long) Integer.parseInt( params.getPredicateValue( "sampling-rate"));
      	//System.out.println(rate);
    	logger.info("Sampling rate set to " + params.getPredicateValue( "sampling-rate") + " msec.");
    }
    
    
    return true;
  }

  public void run() {
    //double temperature = 0;

    double tempC = 0;
	File dir = new File(w1DirPath);
    File[] files = dir.listFiles(new DirectoryFileFilter());
    //System.out.print("111");
    if (files != null) {
	while(isActive()) {
	    
		for(File file: files) {
			  //System.out.print(file.getName() + ": ");
			  // Device data in w1_slave file
	        String filePath = w1DirPath + "/" + file.getName() + "/w1_slave";
	        File f = new File(filePath);
	        try(BufferedReader br = new BufferedReader(new FileReader(f))) {
	            String output;
	            output = br.readLine();
	            if ( output.endsWith(" YES") ) {
	            	//System.out.print(output);
	            	output = br.readLine();
	            	int idx = output.indexOf("t=");	              
	            	if(idx > -1) {
	            		// get temperature 
	            		tempC = Double.parseDouble(output.substring(output.indexOf("t=") + 2));
	            		// Divide by 1000 to get degrees Celsius
	            		tempC /= 1000;	
	              }
	            }
	            else{
	            	tempC = -273.15;
	            }
	            //System.out.print(String.format("%.3f ", tempC));  
	        }
	        catch(Exception ex) {
	            System.out.println(ex.getMessage());
	        }
	    }
		try { 
			    postStreamElement(new Serializable[] {tempC}); 
		        //System.out.println(tempC);
				Thread.sleep(rate);	
		} catch (InterruptedException e) {
	        logger.error(e.getMessage(), e);
	    }

    	}
    }

    /*while (isActive()) {
      
        try{
          

          ProcessBuilder pb = new ProcessBuilder("python","temperature.py");
          Process proc = pb.start();
          //Process proc = Runtime.getRuntime().exec("python temperature.py");

          //Reader reader = new InputStreamReader(proc.getInputStream());
          BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
          while (reader == null);	//cekanje dok podatak nije spreman (!!!)
          String s = reader.readLine(); 

          try { 
            proc.waitFor();
          } catch (InterruptedException e) {
              logger.error(e.getMessage(), e);

          }

          proc.destroy();
          
          temperature = Double.parseDouble(s);

        }catch(IOException e){
          logger.error(e.getMessage(), e);
        }

      try { 
          
          //proc.waitFor();
            
            // post the data to GSN
          postStreamElement(new Serializable[] {temperature}); 
          System.out.println(temperature);
          Thread.sleep(rate); //delay



      } catch (InterruptedException e) {
        logger.error(e.getMessage(), e);

      }
	  
	  
            
    }*/
  }

  public DataField[] getOutputFormat() {
    return collection;
  }

  public String getWrapperName() {
    return "Rpi temperature";
  }  

  public void dispose() {
    counter--;
  }
}

class DirectoryFileFilter implements FileFilter
{
   public boolean accept(File file) {
     String dirName = file.getName();
     String startOfName = dirName.substring(0, 3);
     return (file.isDirectory() && startOfName.equals("28-"));
   }
}  
