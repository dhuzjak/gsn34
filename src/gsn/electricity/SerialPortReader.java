package gsn.electricity;

import java.util.HashMap;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class SerialPortReader implements SerialPortEventListener {
	 
	SerialPort serialPort;
	HashMap <String, String> values;
	String key;
	
	public SerialPortReader (SerialPort serialPort) {
		this.serialPort = serialPort;
		values = new HashMap<String, String>();
	}
	
    public void serialEvent(SerialPortEvent event) {
        //Object type SerialPortEvent carries information about which event occurred and a value.
        //For example, if the data came a method event.getEventValue() returns us the number of bytes in the input buffer.
        if(event.isRXCHAR()){
        	
            try {
                 byte[] buffer = serialPort.readBytes(event.getEventValue());
                 values.put(this.key, new String (buffer));
                 System.out.println("Procitao: " + key);
                 Thread.sleep(500);
            }
            catch (SerialPortException ex) {
                System.out.println(ex);
                values.put(this.key, "Error");
            } catch (Exception ex) {
    			ex.printStackTrace();
    		}          
        }
    }
    
    public HashMap <String, String> getValues () {
    	return values;
    }
    
    public void clearValues () {
    	values.clear();
    }
    
    public void setKey (String key) {
    	this.key = key;
    }
    
    public void read (char stopChar, int timeout) {
    	String buffer = "";
    	String c;
    	try {
    			while (true) {
	    			c = serialPort.readString(1, timeout);
	    			buffer += c;
	    			if (c.length() != 0 && (c.charAt(0) == stopChar || this.key == "stop")) {
	    				break;
	    			}
    			}
    			
    			values.put(this.key, buffer);
    	        System.out.println("Procitao: " + key);
    	        Thread.sleep(500);

    	} catch (Exception ex) {
    		values.put(this.key, new String (""));
    		ex.printStackTrace();
    		System.out.println("Istekao: " + key);
    	}
    }
}
