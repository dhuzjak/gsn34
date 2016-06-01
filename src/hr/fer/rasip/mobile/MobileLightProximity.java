package hr.fer.rasip.mobile;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

public class MobileLightProximity extends AbstractVirtualSensor {
	
	private final transient Logger logger = Logger.getLogger(this.getClass());
	
	private static final String DEFAULT_DEVICE_ID_DELIMITER = "android_id";
	private static final String DEFAULT_LIGHT_DELIMITER = "LIGHT";
	private static final String DEFAULT_PROXIMITY_DELIMITER = "PROXIMITY";
	
	private DataField[] outputStructure;
	
	private String deviceId;
	private String deviceIdDelimiter;
	
	//delimiters
	private String lightDelimiter;
	private String proximityDelimiter;
	
	private Double light;
	private Integer proximity;

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
		
		//light delimiter
		lightDelimiter = params.get("light-delimiter");
		if(lightDelimiter == null || lightDelimiter.trim().isEmpty()) {
			lightDelimiter = DEFAULT_LIGHT_DELIMITER;
			logger.warn("Default value " + DEFAULT_LIGHT_DELIMITER + " is used for light-delimiter");
		}
		lightDelimiter = "!" + lightDelimiter.trim() + "!";
		
		//proximity delimiter
		proximityDelimiter = params.get("proximity-delimiter");
		if(proximityDelimiter == null || proximityDelimiter.trim().isEmpty()) {
			proximityDelimiter = DEFAULT_PROXIMITY_DELIMITER;
			logger.warn("Default value " + DEFAULT_PROXIMITY_DELIMITER + " is used for proximity-delimiter");
		}
		proximityDelimiter = "!" + proximityDelimiter.trim() + "!";
		
		return true;
	}

	@Override
	public void dispose() {}

	@Override
	public void dataAvailable(String inputStreamName,
			StreamElement streamElement) {
		String data = (String)streamElement.getData("last_data_received");
		if(!data.contains(deviceIdDelimiter)) {
			return;
		}
		
		String id = data.split("!" + deviceIdDelimiter + "!")[1];
		if(!id.equals(deviceId)) {
			return;
		}
		
		if(data.contains(lightDelimiter)) {
			light = Double.parseDouble(data.split(lightDelimiter)[1].replace(',', '.'));
		} else if(data.contains(proximityDelimiter)) {
			String prox = data.split(proximityDelimiter)[1];
			if(prox.equalsIgnoreCase("NEAR")) {
				proximity = 1;
			} else if(prox.equalsIgnoreCase("FAR")) {
				proximity = 0;
			} else {
				logger.error("Invalid value for proximity recieved");
			}
		} else {
			return; //data doesn't belong to this VS
		}
		
		dataProduced(new StreamElement(outputStructure, new Serializable[] { light, proximity }, streamElement.getTimeStamp()));
	}
}
