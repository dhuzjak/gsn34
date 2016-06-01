package hr.fer.rasip.mobile;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.vsensor.AbstractVirtualSensor;

public class MobileWifiNetwork extends AbstractVirtualSensor {

	private final transient Logger logger = Logger.getLogger(this.getClass());
	
	private static final String DEFAULT_DEVICE_ID_DELIMITER = "android_id";
	private static final String DEFAULT_DELIMITER = "NETWORK";
	
	private String deviceId;
	private String deviceIdDelimiter;
	private String delimiter;
	private String wifiName;
	private String ipAddress;
	private String macAddress;
	private String linkSpeed;
	private int signalLevel;
	private DataField[] outputStructure;
	
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

		if(!pieces[0].equalsIgnoreCase("WIFI")) {
			return;
		}

		wifiName = pieces[1];
		ipAddress = pieces[2];
		macAddress = pieces[3];
		linkSpeed = pieces[4];
		signalLevel = Integer.parseInt(pieces[5]);
		
		dataProduced(new StreamElement(outputStructure, new Serializable[]{ wifiName, ipAddress, macAddress, linkSpeed, signalLevel }, streamElement.getTimeStamp()));
	}
}
