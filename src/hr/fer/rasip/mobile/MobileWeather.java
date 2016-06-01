package hr.fer.rasip.mobile;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

public class MobileWeather extends AbstractVirtualSensor {
	
	private final transient Logger logger = Logger.getLogger(this.getClass());
	
	private static final String DEFAULT_DEVICE_ID_DELIMITER = "android_id";
	private static final String DEFAULT_TEMPERATURE_DELIMITER = "TEMPERATURE";
	private static final String DEFAULT_PRESSURE_DELIMITER = "PRESSURE";
	private static final String DEFAULT_HUMIDITY_DELIMITER = "HUMIDITY";
	
	private DataField[] outputStructure;
	
	private String deviceId;
	private String deviceIdDelimiter;
	
	//delimiters
	private String pressureDelimiter;
	private String temperatureDelimiter;
	private String relativeHumidityDelimiter;
	
	private Double pressure;
	private Double temperature;
	private Double relativeHumidity;

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
		
		//temperature delimiter
		temperatureDelimiter = params.get("temperature-delimiter");
		if(temperatureDelimiter == null || temperatureDelimiter.trim().isEmpty()) {
			temperatureDelimiter = DEFAULT_TEMPERATURE_DELIMITER;
			logger.warn("Default value " + DEFAULT_TEMPERATURE_DELIMITER + " is used for temperature-delimiter");
		}
		temperatureDelimiter = "!" + temperatureDelimiter.trim() + "!";
		
		//pressure delimiter
		pressureDelimiter = params.get("pressure-delimiter");
		if(pressureDelimiter == null || pressureDelimiter.trim().isEmpty()) {
			pressureDelimiter = DEFAULT_PRESSURE_DELIMITER;
			logger.warn("Default value " + DEFAULT_PRESSURE_DELIMITER + " is used for pressure-delimiter");
		}
		pressureDelimiter = "!" + pressureDelimiter.trim() + "!";
		
		//relative humidity delimiter
		relativeHumidityDelimiter = params.get("relative-humidity-delimiter");
		if(relativeHumidityDelimiter == null || relativeHumidityDelimiter.trim().isEmpty()) {
			relativeHumidityDelimiter = DEFAULT_HUMIDITY_DELIMITER;
			logger.warn("Default value " + DEFAULT_HUMIDITY_DELIMITER + " is used for relative-humidity-delimiter");
		}
		relativeHumidityDelimiter = "!" + relativeHumidityDelimiter.trim() + "!";
		
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
		
		try {
			if(data.contains(pressureDelimiter)) {
				pressure = Double.parseDouble(data.split(pressureDelimiter)[1].replace(',', '.'));
			} else if(data.contains(temperatureDelimiter)) {
				temperature = Double.parseDouble(data.split(temperatureDelimiter)[1].replace(',', '.'));
			} else if(data.contains(relativeHumidityDelimiter)) {
				relativeHumidity = Double.parseDouble(data.split(relativeHumidityDelimiter)[1].replace(',', '.'));
			} else {
				return; //data doesn't belong to this VS
			}
		} catch (NumberFormatException e) {
			logger.error("Error occurred in " + this.getClass().getSimpleName() + ": " + e.getMessage());
			return;
		}
		
		dataProduced(new StreamElement(outputStructure, new Serializable[] { pressure, temperature, relativeHumidity }, streamElement.getTimeStamp()));
	}
}
