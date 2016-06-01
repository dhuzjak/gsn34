package gsn.electricity;

import java.util.HashMap;

import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialCommunication {

	SerialPort serialPort;
	SerialPortReader serialPortReader;
	
	
	public SerialCommunication (String portName) {
		this.serialPort = new SerialPort (portName);
		this.serialPortReader = new SerialPortReader(this.serialPort);
	}

	public void openPort () {
		try {
			this.serialPort.openPort();
			this.serialPort.setParams(300, 7, 1, 2);
			/*this.serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
			this.serialPort.addEventListener(this.serialPortReader);*/
		} catch (SerialPortException ex) {
			ex.printStackTrace();
		}
	}
	
	public void closePort () {
		try {
			this.serialPort.closePort();
		} catch (SerialPortException ex) {
			ex.printStackTrace();
		} 
	}
	
	public HashMap <String, String> getResults () {
		
		
		return this.serialPortReader.getValues();
		
		
	}
	
	public void clearResults () {
		this.serialPortReader.clearValues();
	}
	
	public void write (String output, String key) {
		byte[] outputBytes = output.getBytes();
		char stopChar;
		try {
			this.serialPortReader.setKey(key);
			if (!this.serialPort.writeBytes(outputBytes)) {
				System.out.println("Greska kod upisa: " + key);
			} else {
				switch (key) {
				case "start": stopChar = 0x0A; break;
				case "ack": stopChar = 0x03; break;
				default: stopChar = 0x03; break;
				}
				this.serialPortReader.read(stopChar, 1000);
			}
			
		} catch (SerialPortException ex) {
			ex.printStackTrace();
		} 
	}
	
	
}
	


