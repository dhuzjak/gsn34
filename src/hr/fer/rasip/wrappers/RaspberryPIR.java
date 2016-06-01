package hr.fer.rasip.wrappers;
import java.io.*;
import gsn.beans.AddressBean;
import gsn.beans.DataField;
import gsn.wrappers.AbstractWrapper;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;


public class RaspberryPIR extends AbstractWrapper {
	
  private DataField[] collection = new DataField[] {new DataField("Motion", "int", "RPi PIR sensor")};
  private final transient Logger logger = Logger.getLogger(RaspberryPIR.class);
  private int counter;
  private AddressBean params;
  private long rate = 1000;

	private GpioController gpio;
	private GpioPinDigitalInput sensorPIR;

  
  
  public boolean initialize() {
    setName("RpiPIR" + counter++);
    
    params = getActiveAddressBean();
    
    if ( params.getPredicateValue( "sampling-rate" ) != null ){

    	rate = (long) Integer.parseInt( params.getPredicateValue( "sampling-rate"));
      //System.out.println(rate);
    	logger.info("Sampling rate set to " + params.getPredicateValue( "sampling-rate") + " msec.");
    }

		gpio = GpioFactory.getInstance();

      // provision gpio pin #11 as an input pin with its internal pull down resistor enabled
    sensorPIR = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);

    // create and register gpio pin listener
    sensorPIR.addListener(new GpioPinListenerDigital() {
      @Override
      public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
          // display pin state on console
          // post the data to GSN
          //motion = event.getState();

          // 1 ili 0 za stanja jer se lakse prati u gsnu
          if(event.getState().isHigh()){  
            postStreamElement(new Serializable[] { 1 });
          }   

          if(event.getState().isLow()){   
           postStreamElement(new Serializable[] { 0 });
          }  
          
          //System.out.println( event.getState());
          //System.out.println(" --> PIR STATE CHANGE: " + event.getPin() + " = " + event.getState());
      }
      
    });
    
    
    return true;
  }

  public void run() {
    
    while (isActive()) {
      
        
      try { 
        Thread.sleep(rate);

      } catch (InterruptedException e) {
        logger.error(e.getMessage(), e);
         // stop all GPIO activity/threads by shutting down the GPIO controller
         // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
          
        gpio.shutdown();
      }
            
    }
  }

  public DataField[] getOutputFormat() {
    return collection;
  }

  public String getWrapperName() {
    return "Rpi temperature";
  }  

  public void dispose() {
    counter--;
		gpio.shutdown();
		gpio.unprovisionPin(sensorPIR);
	
  }
}
