package hr.fer.rasip.mobile;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;

public class MobileSensors extends AbstractVirtualSensor {

	private final transient Logger logger = Logger.getLogger(this.getClass());
	
	private static final String DEFAULT_DEVICE_ID_DELIMITER = "android_id";
	private static final String DEFAULT_ACCELERATION_DELIMITER = "ACCELERATION";
	private static final String DEFAULT_GRAVITY_DELIMITER = "GRAVITY";
	private static final String DEFAULT_GYROSCOPE_DELIMITER = "GYROSCOPE";
	private static final String DEFAULT_LINEAR_ACC_DELIMITER = "LINEAR_ACC";
	private static final String DEFAULT_TEMPERATURE_DELIMITER = "TEMPERATURE";
	private static final String DEFAULT_PRESSURE_DELIMITER = "PRESSURE";
	private static final String DEFAULT_HUMIDITY_DELIMITER = "HUMIDITY";
	private static final String DEFAULT_LIGHT_DELIMITER = "LIGHT";
	private static final String DEFAULT_PROXIMITY_DELIMITER = "PROXIMITY";
	private static final String DEFAULT_MAGNETIC_FIELD_DELIMITER = "MAGNETIC_FIELD";
	
	private DataField[] outputStructure;
	
	private String deviceId;
	private String deviceIdDelimiter;
	
	//delimiters
	private String accelerometerDelimiter;
	private String temperatureDelimiter;
	private String gravityDelimiter;
	private String gyroscopeDelimiter;
	private String lightDelimiter;
	private String linearAccDelimiter;
	private String magneticFieldDelimiter;
	private String pressureDelimiter;
	private String proximityDelimiter;
	private String relativeHumidityDelimiter;
	
	//accelerometer
	private Double accelerometerX;
	private Double accelerometerY;
	private Double accelerometerZ;
	
	//temeperature
	private Double temperature;
	
	//gravity
	private Double gravityX;
	private Double gravityY;
	private Double gravityZ;
	
	//gyroscope
	private Double gyroscopeX;
	private Double gyroscopeY;
	private Double gyroscopeZ;
	
	//light
	private Double light;
	
	//linear acceleration
	private Double linearAccX;
	private Double linearAccY;
	private Double linearAccZ;
	
	//magnetic field
	private Double magneticFieldX;
	private Double magneticFieldY;
	private Double magneticFieldZ;
	
	//pressure
	private Double pressure;
	
	//proximity
	private Integer proximity;
	
	//relative humidity
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
		
		//accelerometer delimiter
		accelerometerDelimiter = params.get("accelerometer-delimiter");
		if(accelerometerDelimiter == null || accelerometerDelimiter.trim().isEmpty()) {
			accelerometerDelimiter = DEFAULT_ACCELERATION_DELIMITER;
			logger.warn("Default value " + DEFAULT_ACCELERATION_DELIMITER + " is used for acceleration-delimiter");
		}
		accelerometerDelimiter = "!" + accelerometerDelimiter.trim() + "!";
		
		//temperature delimiter
		temperatureDelimiter = params.get("temperature-delimiter");
		if(temperatureDelimiter == null || temperatureDelimiter.trim().isEmpty()) {
			temperatureDelimiter = DEFAULT_TEMPERATURE_DELIMITER;
			logger.warn("Default value " + DEFAULT_TEMPERATURE_DELIMITER + " is used for temperature-delimiter");
		}
		temperatureDelimiter = "!" + temperatureDelimiter.trim() + "!";
		
		//gravity delimiter
		gravityDelimiter = params.get("gravity-delimiter");
		if(gravityDelimiter == null || gravityDelimiter.trim().isEmpty()) {
			gravityDelimiter = DEFAULT_GRAVITY_DELIMITER;
			logger.warn("Default value " + DEFAULT_GRAVITY_DELIMITER + " is used for gravity-delimiter");
		}
		gravityDelimiter = "!" + gravityDelimiter.trim() + "!";
		
		//gyroscope delimiter
		gyroscopeDelimiter = params.get("gyroscope-delimiter");
		if(gyroscopeDelimiter == null || gyroscopeDelimiter.trim().isEmpty()) {
			gyroscopeDelimiter = DEFAULT_GYROSCOPE_DELIMITER;
			logger.warn("Default value " + DEFAULT_GYROSCOPE_DELIMITER + " is used for gyroscope-delimiter");
		}
		gyroscopeDelimiter = "!" + gyroscopeDelimiter.trim() + "!";
		
		//light delimiter
		lightDelimiter = params.get("light-delimiter");
		if(lightDelimiter == null || lightDelimiter.trim().isEmpty()) {
			lightDelimiter = DEFAULT_LIGHT_DELIMITER;
			logger.warn("Default value " + DEFAULT_LIGHT_DELIMITER + " is used for light-delimiter");
		}
		lightDelimiter = "!" + lightDelimiter.trim() + "!";
		
		//linear acceleration delimiter
		linearAccDelimiter = params.get("linear-acc-delimiter");
		if(linearAccDelimiter == null || linearAccDelimiter.trim().isEmpty()) {
			linearAccDelimiter = DEFAULT_LINEAR_ACC_DELIMITER;
			logger.warn("Default value " + DEFAULT_LINEAR_ACC_DELIMITER + " is used for linear-acc-delimiter");
		}
		linearAccDelimiter = "!" + linearAccDelimiter.trim() + "!";
		
		//magnetic field delimiter
		magneticFieldDelimiter = params.get("magnetic-field-delimiter");
		if(magneticFieldDelimiter == null || magneticFieldDelimiter.trim().isEmpty()) {
			magneticFieldDelimiter = DEFAULT_MAGNETIC_FIELD_DELIMITER;
			logger.warn("Default value " + DEFAULT_MAGNETIC_FIELD_DELIMITER + " is used for magnetic-field-delimiter");
		}
		magneticFieldDelimiter = "!" + magneticFieldDelimiter.trim() + "!";
		
		//pressure delimiter
		pressureDelimiter = params.get("pressure-delimiter");
		if(pressureDelimiter == null || pressureDelimiter.trim().isEmpty()) {
			pressureDelimiter = DEFAULT_PRESSURE_DELIMITER;
			logger.warn("Default value " + DEFAULT_PRESSURE_DELIMITER + " is used for pressure-delimiter");
		}
		pressureDelimiter = "!" + pressureDelimiter.trim() + "!";
		
		//proximity delimiter
		proximityDelimiter = params.get("proximity-delimiter");
		if(proximityDelimiter == null || proximityDelimiter.trim().isEmpty()) {
			proximityDelimiter = DEFAULT_PROXIMITY_DELIMITER;
			logger.warn("Default value " + DEFAULT_PROXIMITY_DELIMITER + " is used for proximity-delimiter");
		}
		proximityDelimiter = "!" + proximityDelimiter.trim() + "!";
		
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
		
		//check if any of data is changed
		String[] tmpSplit = null;
		String tmp = null;
			try {
				//accelerometer
			if(data.contains(accelerometerDelimiter)) {
				tmpSplit = data.split(accelerometerDelimiter)[1].replace(',', '.').split(";");
				accelerometerX = Double.parseDouble(tmpSplit[0]);
				accelerometerY = Double.parseDouble(tmpSplit[1]);
				accelerometerZ = Double.parseDouble(tmpSplit[2]);
				//temperature
			} else if(data.contains(temperatureDelimiter)) {
				tmp = data.split(temperatureDelimiter)[1];
				temperature = Double.parseDouble(tmp);
				//gravity
			} else if(data.contains(gravityDelimiter)) {
				tmpSplit = data.split(gravityDelimiter)[1].replace(',', '.').split(";");
				gravityX = Double.parseDouble(tmpSplit[0]);
				gravityY = Double.parseDouble(tmpSplit[1]);
				gravityZ = Double.parseDouble(tmpSplit[2]);
				//gyroscope
			} else if(data.contains(gyroscopeDelimiter)) {
				tmpSplit = data.split(gyroscopeDelimiter)[1].replace(',', '.').split(";");
				gyroscopeX = Double.parseDouble(tmpSplit[0]);
				gyroscopeY = Double.parseDouble(tmpSplit[1]);
				gyroscopeZ = Double.parseDouble(tmpSplit[2]);
				//light
			} else if(data.contains(lightDelimiter)) {
				tmp = data.split(lightDelimiter)[1].replace(',', '.');
				light = Double.parseDouble(tmp);
				//linear acceleration
			} else if(data.contains(linearAccDelimiter)) {
				tmpSplit = data.split(linearAccDelimiter)[1].replace(',', '.').split(";");
				linearAccX = Double.parseDouble(tmpSplit[0]);;
				linearAccY = Double.parseDouble(tmpSplit[1]);
				linearAccZ = Double.parseDouble(tmpSplit[2]);
				//magnetic field
			} else if(data.contains(magneticFieldDelimiter)) {
				tmpSplit = data.split(magneticFieldDelimiter)[1].replace(',', '.').split(";");
				magneticFieldX = Double.parseDouble(tmpSplit[0]);
				magneticFieldY = Double.parseDouble(tmpSplit[1]);
				magneticFieldZ = Double.parseDouble(tmpSplit[2]);
				//pressure
			} else if(data.contains(pressureDelimiter)) {
				tmp = data.split(pressureDelimiter)[1].replace(',', '.');
				pressure = Double.parseDouble(tmp);
				//proximity
			} else if(data.contains(proximityDelimiter)) {
				tmp = data.split(proximityDelimiter)[1];
				if(tmp.equalsIgnoreCase("NEAR")) {
					proximity = 1;
				} else if(tmp.equalsIgnoreCase("FAR")) {
					proximity = 0;
				} else {
					logger.error("Invalid value for proximity recieved");
				}
				//relative humidity
			} else if(data.contains(relativeHumidityDelimiter)) {
				tmp = data.split(relativeHumidityDelimiter)[1];
				relativeHumidity = Double.parseDouble(tmp);
			} else {
				return;//didn't find any data that matches this virtual sensor
			}
		} catch (Exception e) {
			logger.error("Error occurred in " + this.getClass().getSimpleName() + ": " + e);
			return;
		}
		
		
		dataProduced(new StreamElement(outputStructure, new Serializable[] {
				accelerometerX, accelerometerY, accelerometerZ,
				temperature,
				gravityX, gravityY, gravityZ,
				gyroscopeX, gyroscopeY, gyroscopeZ,
				light, 
				linearAccX, linearAccY, linearAccZ,
				magneticFieldX, magneticFieldY, magneticFieldZ,
				pressure,
				proximity,
				relativeHumidity
		}));
	}
}
