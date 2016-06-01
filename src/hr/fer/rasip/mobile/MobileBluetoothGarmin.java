package hr.fer.rasip.mobile;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

public class MobileBluetoothGarmin extends AbstractVirtualSensor {

	private final transient Logger logger = Logger.getLogger(this.getClass());
	
	private static final String DEFAULT_DEVICE_ID_DELIMITER = "android_id";
	private static final String DEFAULT_DELIMITER = "BLUETOOTH";
	
	private DataField[] outputStructure;
	
	private String deviceId;
	private String deviceIdDelimiter;
	private String delimiter;
	
	@Override
	public boolean initialize() {
		VSensorConfig config = getVirtualSensorConfiguration();
		outputStructure = config.getOutputStructure();
		
		Map<String, String> params = config.getMainClassInitialParams();
		
		//device id
		deviceId = params.get("device-id");
		if(deviceId == null || deviceId.trim().isEmpty()) {
			logger.error("Device id must be entered. " + this.getClass().getSimpleName()
					+ " is unable to start.");
			return false;
		}
		deviceId = deviceId.trim();
		
		//device id delimiter
		deviceIdDelimiter = params.get("device-id-delimiter");
		if(deviceIdDelimiter == null || deviceIdDelimiter.trim().isEmpty()) {
			deviceIdDelimiter = DEFAULT_DEVICE_ID_DELIMITER;
			logger.warn("Default value " + DEFAULT_DEVICE_ID_DELIMITER + " is used for device-id-delimiter");
		}
		deviceIdDelimiter = deviceIdDelimiter.trim();
		
		//accelerometer delimiter
		delimiter = params.get("delimiter");
		if(delimiter == null || delimiter.trim().isEmpty()) {
			delimiter = DEFAULT_DELIMITER;
			logger.warn("Default value " + DEFAULT_DELIMITER + " used for delimiter");
		}
		delimiter = "!" + delimiter.trim() + "!";
		
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
		
		if(pieces.length != 7 || !pieces[0].startsWith("LAT")) {
			return;
		}
		
		double latitude = Double.parseDouble(pieces[0].replace(',', '.').substring(4, pieces[0].length()));
		double longitude = Double.parseDouble(pieces[1].replace(',', '.').substring(5, pieces[1].length()));
		double time = Double.parseDouble(pieces[2].replace(',', '.').substring(5, pieces[2].length()));
		double quality = Double.parseDouble(pieces[3].replace(',', '.').substring(8, pieces[3].length()));
		double direction = Double.parseDouble(pieces[4].replace(',', '.').substring(10, pieces[4].length()));
		double altitude = Double.parseDouble(pieces[5].replace(',', '.').substring(4, pieces[5].length()));
		double speed = Double.parseDouble(pieces[6].replace(',', '.').substring(6, pieces[6].length()));
		
		dataProduced(new StreamElement(outputStructure, new Serializable[] {
				latitude,
				longitude,
				latitude,
				longitude,
				time,
				quality,
				direction,
				altitude,
				speed
		}, streamElement.getTimeStamp()));
	}
}
