package hr.fer.rasip.mobile;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.vsensor.AbstractVirtualSensor;

public class MobileBattery extends AbstractVirtualSensor {

	private final transient Logger logger = Logger.getLogger(this.getClass());
	
	private static final String DEFAULT_DEVICE_ID_DELIMITER = "android_id";
	private static final String DEFAULT_DELIMITER = "BATTERY_LEVEL";
	
	private String deviceId;
	private String deviceIdDelimiter;
	private String delimiter;
	private DataField[] outputStructure;
	
	private int plugged;
	private int status;
	private int health;
	private double temperature;
	private double voltage;
	private int level; 
	
	@Override
	public boolean initialize() {
		outputStructure = getVirtualSensorConfiguration().getOutputStructure();
		Map<String, String> params = getVirtualSensorConfiguration().getMainClassInitialParams();
		delimiter = params.get("delimiter");
		if(delimiter == null || delimiter.isEmpty()) {
			delimiter = DEFAULT_DELIMITER;
			logger.warn("Default value " + DEFAULT_DELIMITER + " used for delimiter");
		}
		delimiter = "!" + delimiter.trim() + "!";
		
		deviceId = params.get("device-id");
		if(deviceId == null || deviceId.trim().isEmpty()) {
			logger.error("Device id must be entered. " + this.getClass().getSimpleName()
					+ " is unable to start.");
			return false;
		}
		deviceId = deviceId.trim();
		
		deviceIdDelimiter = params.get("device-id-delimiter");
		if(deviceIdDelimiter == null || deviceIdDelimiter.trim().isEmpty()) {
			deviceIdDelimiter = DEFAULT_DEVICE_ID_DELIMITER;
			logger.warn("Default value " + DEFAULT_DEVICE_ID_DELIMITER + " is used for device-id-delimiter");
		}
		deviceIdDelimiter = deviceIdDelimiter.trim();
		
		return true;
	}

	@Override
	public void dispose() {}

	@Override
	public void dataAvailable(String inputStreamName,
			StreamElement streamElement) {
		String data = (String)streamElement.getData("last_data_received");
		if(!data.contains(delimiter) || !data.contains(deviceIdDelimiter)) {
			return;
		}
		
		String id = data.split("!" + deviceIdDelimiter + "!")[1];
		if(!id.equals(deviceId)) {
			return;
		}
		
		data = data.split(delimiter)[1];
		String[] pieces = data.split(";");
	
		plugged = Integer.parseInt(pieces[0]);//setPlugged(Integer.parseInt(pieces[0]));
		status = Integer.parseInt(pieces[1]);//setStatus(Integer.parseInt(pieces[1]));
		health = Integer.parseInt(pieces[2]);//setHealth(Integer.parseInt(pieces[2]));
		temperature = Double.parseDouble(pieces[3])/10;//setTemperature(pieces[3]);
		voltage = Double.parseDouble(pieces[4])/1000;//setVoltage(pieces[4]);
		level = Integer.parseInt(pieces[5]);//setLevel(pieces[5]);
		
		StreamElement se = new StreamElement(outputStructure, new Serializable[] { plugged, status, health, temperature, voltage, level }, streamElement.getTimeStamp());
		dataProduced(se);
	}
	
	/*private void setPlugged(int data) {
		switch (data) {
		case 0:
			plugged = "UNPLUGGED";
			break;

		case 1:
			plugged = "AC POWER";
			break;
			
		case 2:
			plugged = "USB";
			break;
			
		case 4:
			plugged = "WIRELESS";
			break;
		
		default:
			logger.error("Inavlid value for battery plugged status");
			break;
		}
	}
	
	private void setStatus(int data) {
		switch (data) {
		case 1:
			status = "UNKNOWN";
			break;
			
		case 2:
			status = "CHARGING"; 
			break;

		case 3:
			status = "DISCHARGING";
			break;
			
		case 4:
			status = "NOT CHARGING";
			break;
			
		case 5:
			status = "FULL";
			break;
			
		default:
			logger.error("Invalid value for battery status");
			break;
		}
	}
	
	private void setHealth(int data) {
		switch (data) {
		case 1:
			health = "UNKNOWN";
			break;
			
		case 2:
			health = "GOOD";
			break;
			
		case 3:
			health = "OVERHEATH";
			break;
			
		case 4:
			health = "DEAD";
			break;

		case 5:
			health = "OVER VOLTAGE";
			break;
			
		case 6:
			health = "FAILURE";
			break;
			
		case 7:
			health = "COLD";
			break;
			
		default:
			logger.error("Invalid value for battery health");
			break;
		}
	}
	
	private void setTemperature(String data) {
		temperature = Double.parseDouble(data)/10;
	}
	
	private void setVoltage(String data) {
		voltage = Double.parseDouble(data)/1000;
	}
	
	private void setLevel(String data) {
		level = Integer.parseInt(data);
	}*/
}
