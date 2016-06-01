package hr.fer.rasip.mobile;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

public class MobileGravity extends AbstractVirtualSensor {

	private final transient Logger logger = Logger.getLogger(this.getClass());
	
	private static final String DEFAULT_DEVICE_ID_DELIMITER = "android_id";
	private static final String DEFAULT_DELIMITER = "GRAVITY";
	
	private DataField[] outputStructure;
	
	private String deviceId;
	private String deviceIdDelimiter;
	private String delimiter;
	
	//gravity
	private Double gravityX;
	private Double gravityY;
	private Double gravityZ;
	
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
		
		//gravity delimiter
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
		
		gravityX = Double.parseDouble(pieces[0].replace(',', '.'));
		gravityY = Double.parseDouble(pieces[1].replace(',', '.'));
		gravityZ = Double.parseDouble(pieces[2].replace(',', '.'));
		
		dataProduced(new StreamElement(outputStructure, new Serializable[] { gravityX, gravityY, gravityZ }, streamElement.getTimeStamp()));
	}
}
