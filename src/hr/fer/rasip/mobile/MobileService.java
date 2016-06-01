package hr.fer.rasip.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.beans.VSensorConfig;
import gsn.vsensor.AbstractVirtualSensor;
import hr.fer.rasip.genvsensor.VirtualSensorDescriptionFile;
import hr.fer.rasip.genvsensor.VirtualSensorException;

public class MobileService extends AbstractVirtualSensor {

	private final transient Logger logger = Logger.getLogger(this.getClass());
	
	private static final String DEFAULT_DEVICE_ID_DELIMITER = "android_id";
	
	private static final String DEFAULT_BATTERY_FILE_NAME = "MobileBatteryVS";
	private static final String DEFAULT_BATTERY_CONNECTED_FILE_NAME = "MobileBatteryConnectedVS";
	private static final String DEFAULT_BATTERY_OK_FILE_NAME = "MobileBatteryLevelVS";
	private static final String DEFAULT_LOCATION_FILE_NAME = "MobileLocationVS";
	private static final String DEFAULT_SIGNAL_FILE_NAME = "MobileSignalVS";
	private static final String DEFAULT_WIFI_FILE_NAME = "MobileWifiVS";
	private static final String DEFAULT_NETWORK_FILE_NAME = "MobileNetworkVS";
	private static final String DEFAULT_SENSORS_FILE_NAME = "MobileSensorsVS";
	private static final String DEFAULT_ORIENTATION_FILE_NAME = "MobileOrientationVS";
	private static final String DEFAULT_ACCELERATION_FILE_NAME = "MobileAccelerationVS";
	private static final String DEFAULT_GRAVITY_FILE_NAME = "MobileGravityVS";
	private static final String DEFAULT_GYROSCOPE_FILE_NAME = "MobileGyroscopeVS";
	private static final String DEFAULT_LINEAR_ACC_FILE_NAME = "MobileLinearAccVS";
	private static final String DEFAULT_WEATHER_FILE_NAME = "MobileWeatherVS";
	private static final String DEFAULT_LIGHT_PROXIMITY_FILE_NAME = "MobileLightProximityVS";
	private static final String DEFAULT_MAGNETIC_FIELD_FILE_NAME = "MobileMagneticFieldVS";
	private static final String DEFAULT_BLUETOOTH_GARMIN_FILE_NAME = "MobileBluetoothGarminVS";
	
	private static final String DEFAULT_BATTERY_DELIMITER = "BATTERY_LEVEL";
	private static final String DEFAULT_BATTERY_CONNECT_DELIMITER = "BATTERY_CONNECT";
	private static final String DEFAULT_BATTERY_OK_DELIMITER = "BATTERY_OK";
	private static final String DEFAULT_LOCATION_DELIMITER = "LOCATION";
	private static final String DEFAULT_SIGNAL_DELIMITER = "SIGNAL";
	private static final String DEFAULT_NETWORK_DELIMITER = "NETWORK";
	private static final String DEFAULT_ORIENTATION_DELIMITER = "ORIENTATION";
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
	private static final String DEFAULT_BLUETOOTH_DELIMITER = "BLUETOOTH";
	
	/**
	 * Android devices that exist in system.
	 */
	private Set<String> devicesIds;
	
	/**
	 * Key is device id and value is list of paths to generated
	 * virtual sensor description files.
	 */
	private Map<String, List<String>> pathToDevicesVS;
	
	/**
	 * Key is device id and value is timestamp taken last
	 * time data was received from device 
	 */
	private Map<String, Long> devicesLastTimestamp;
	private DataField[] outputStructure;
	
	private String deviceIdDelimiter;
	private File dictionary;
	private String pathToVirtualSensorsFolder;
	private boolean onlyRegisteredDevices;
	private boolean checkTimeout;
	private int timeout;//represents value in minutes
	private Timer timer;
	private boolean deleteOnExit;
	
	private boolean gatewayMode;//if true this VS only forwards data and does nothing except that
	
	//file names
	private String batteryFileName;
	private String batteryConnectFileName;
	private String batteryLevelFileName;
	private String locationFileName;
	private String signalFileName;
	private String wifiFileName;
	private String networkFileName;
	private String sensorsFileName;
	private String orientationFileName;
	private String accelerationFileName;
	private String gravityFileName;
	private String gyroscopeFileName;
	private String linearAccFileName;
	private String weatherFileName;
	private String lightProximityFileName;
	private String magneticFieldFileName;
	private String bluetoothGarminFileName;
	
	//delimiters
	private String batteryDelimiter;
	private String batteryConnectDelimiter;
	private String batteryLevelDelimiter;
	private String locationDelimiter;
	private String signalDelimiter;
	private String networkDelimiter;
	private String orientationDelimiter;
	private String accelerationDelimiter;
	private String gravityDelimiter;
	private String gyroscopeDelimiter;
	private String linearAccDelimiter;
	private String temperatureDelimiter;
	private String pressureDelimiter;
	private String humidityDelimiter;
	private String lightDelimiter;
	private String proximityDelimiter;
	private String magneticFieldDelimiter;
	private String bluetoothDelimiter;
	
	//whether to generate certain VS or not
	private boolean generateBattery;
	private boolean generateBatteryConnect;
	private boolean generateBatteryLevel;
	private boolean generateLocation;
	private boolean generateSignal;
	private boolean generateWifi;
	private boolean generateNetwork;
	private boolean generateSensors;
	private boolean generateOrientation;
	private boolean generateAcceleration;
	private boolean generateGravity;
	private boolean generateGyroscope;
	private boolean generateLinearAcc;
	private boolean generateWeather;
	private boolean generateLightProximity;
	private boolean generateMagnetic;
	private boolean generateBluetooth;
	
	/**
	 * Dictionary is key-value organized file.
	 * Key is device id and value is device label(which can be any string)
	 */
	private String currentDeviceLabel;
	
	@Override
	public boolean initialize() {
		VSensorConfig config = getVirtualSensorConfiguration();
		outputStructure = config.getOutputStructure();
		
		Map<String, String> initParams = config.getMainClassInitialParams();
		onlyRegisteredDevices = Boolean.parseBoolean(initParams.get("only-registered-devices"));
		
		deviceIdDelimiter = initParams.get("device-id-delimiter");
		if(deviceIdDelimiter == null || deviceIdDelimiter.trim().isEmpty()) {
			deviceIdDelimiter = DEFAULT_DEVICE_ID_DELIMITER;
			logger.warn("Default value \"" + DEFAULT_DEVICE_ID_DELIMITER + "\" is used for device-id-delimiter.");
		}
		deviceIdDelimiter = deviceIdDelimiter.trim();
		
		gatewayMode = Boolean.parseBoolean(initParams.get("gateway-mode"));
		if(gatewayMode) {
			logger.warn("Gateway mode activated, mobile service will only forward data to "
					+ "virtual sensors which are subscribed to this VS.");
			return true;
		}
		
		String path = initParams.get("path-to-dictionary");
		if(path != null) {
			dictionary = new File(path);
			if(!dictionary.isFile()) {
				if(onlyRegisteredDevices) {
					logger.error("\"path-to-dictionary\" parameter is not set to a proper file, " + this.getClass().getSimpleName()
							+ " won't start. Either enter path to correct file or set \"only-registered-devices\" parameter to \"false\".");
					return false;
				}
				dictionary = null;
			}
		} else {
			if(onlyRegisteredDevices) {
				logger.error("\"path-to-dictionary\" parameter must be entered when \"only-registered-devices\" is set to true. "
						+ this.getClass().getSimpleName() + " won't start.");
				return false;
			}
		}
		
		pathToVirtualSensorsFolder = initParams.get("path-to-virtual-sensors");
		if(pathToVirtualSensorsFolder != null) {
			File virtualSensorsFolder = new File(pathToVirtualSensorsFolder);
			if(!virtualSensorsFolder.isDirectory()) {
				logger.error("\"path-to-virtual-sensors\" parameter is not set to a proper directory, " + this.getClass().getSimpleName()
						+ " won't start.");
				return false;
			}
			if(pathToVirtualSensorsFolder.charAt(pathToVirtualSensorsFolder.length() - 1) != File.separatorChar) {
				pathToVirtualSensorsFolder += File.separator;
			}
		} else {
			logger.error("\"path-to-virtual-sensors\" parameter must be set!. "
					+ this.getClass().getSimpleName() + " won't start.");
			return false;
		}
		
		String tmp = initParams.get("timeout");
		if(tmp != null && !tmp.trim().equalsIgnoreCase("never")) {
			tmp = tmp.trim();
			try {
				timeout = Integer.parseInt(tmp);
				checkTimeout = true;
				timer = new Timer(true);
				timer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						checkTimeout();
					}
				}, 300000, 300000);//every five minutes
			} catch (NumberFormatException e) {
				checkTimeout = false;
				logger.warn("Invalid value for parameter \"timeout\", it is set to default value of \"never\"");
			}
		} else {
			checkTimeout = false;
		}
		
		deleteOnExit = Boolean.parseBoolean(initParams.get("delete-on-exit"));
		
		/** FILE NAMES FOR VIRTUAL SENSOR DESCRIPTION FILES  **/
		
		//BATTERY FILE NAME
		tmp = initParams.get("battery-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			batteryFileName = tmp.isEmpty() ? DEFAULT_BATTERY_FILE_NAME : tmp;
			generateBattery = true;
		} else {
			generateBattery = false;
		}
		
		//BATTERY CONNECTED FILE NAME
		tmp = initParams.get("battery-connected-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			batteryConnectFileName = tmp.isEmpty() ? DEFAULT_BATTERY_CONNECTED_FILE_NAME : tmp;
			generateBatteryConnect = true;
		} else {
			generateBatteryConnect = false;
		}
		
		//BATTERY LEVEL FILE NAME
		tmp = initParams.get("battery-level-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			batteryLevelFileName = tmp.isEmpty() ? DEFAULT_BATTERY_OK_FILE_NAME : tmp;
			generateBatteryLevel = true;
		} else {
			generateBatteryLevel = false;
		}
		
		//LOCATION FILE NAME
		tmp = initParams.get("location-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			locationFileName = tmp.isEmpty() ? DEFAULT_LOCATION_FILE_NAME : tmp;
			generateLocation = true;
		} else {
			generateLocation = false;
		}
		
		//SIGNAL FILE NAME
		tmp = initParams.get("signal-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			signalFileName = tmp.isEmpty() ? DEFAULT_SIGNAL_FILE_NAME : tmp;
			generateSignal = true;
		} else {
			generateSignal = false;
		}
		
		//WIFI FILE NAME
		tmp = initParams.get("wifi-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			wifiFileName = tmp.isEmpty() ? DEFAULT_WIFI_FILE_NAME : tmp;
			generateWifi = true;
		} else {
			generateWifi = false;
		}
		
		//NETWORK FILE NAME
		tmp = initParams.get("network-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			networkFileName = tmp.isEmpty() ? DEFAULT_NETWORK_FILE_NAME : tmp;
			generateNetwork = true;
		} else {
			generateNetwork = false;
		}
		
		//SENSORS FILE NAME
		tmp = initParams.get("sensors-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			sensorsFileName = tmp.isEmpty() ? DEFAULT_SENSORS_FILE_NAME : tmp;
			generateSensors = true;
		} else {
			generateSensors = false;
		}
		
		//ORIENTATION FILE NAME
		tmp = initParams.get("orientation-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			orientationFileName = tmp.isEmpty() ? DEFAULT_ORIENTATION_FILE_NAME : tmp;
			generateOrientation = true;
		} else {
			generateOrientation = false;
		}
		
		//ACCELERATION FILE NAME
		tmp = initParams.get("acceleration-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			accelerationFileName = tmp.isEmpty() ? DEFAULT_ACCELERATION_FILE_NAME : tmp;
			generateAcceleration = true;
		} else {
			generateAcceleration = false;
		}
		
		//GRAVITY FILE NAME
		tmp = initParams.get("gravity-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			gravityFileName = tmp.isEmpty() ? DEFAULT_GRAVITY_FILE_NAME : tmp;
			generateGravity = true;
		} else {
			generateGravity = false;
		}
		
		//GYROSCOPE FILE NAME
		tmp = initParams.get("gyroscope-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			gyroscopeFileName = tmp.isEmpty() ? DEFAULT_GYROSCOPE_FILE_NAME : tmp;
			generateGyroscope = true;
		} else {
			generateGyroscope = false;
		}
		
		//LINEAR ACC FILE NAME
		tmp = initParams.get("linear-acc-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			linearAccFileName = tmp.isEmpty() ? DEFAULT_LINEAR_ACC_FILE_NAME : tmp;
			generateLinearAcc = true;
		} else {
			generateLinearAcc = false;
		}
		
		//WEATHER FILE NAME
		tmp = initParams.get("weather-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			weatherFileName = tmp.isEmpty() ? DEFAULT_WEATHER_FILE_NAME : tmp;
			generateWeather = true;
		} else {
			generateWeather = false;
		}
		
		//LIGHT PROX FILE NAME
		tmp = initParams.get("light-prox-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			lightProximityFileName = tmp.isEmpty() ? DEFAULT_LIGHT_PROXIMITY_FILE_NAME : tmp;
			generateLightProximity = true;
		} else {
			generateLightProximity = false;
		}
		
		//MAGNETIC FIELD FILE NAME
		tmp = initParams.get("magnetic-field-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			magneticFieldFileName = tmp.isEmpty() ? DEFAULT_MAGNETIC_FIELD_FILE_NAME : tmp;
			generateMagnetic = true;
		} else {
			generateMagnetic = false;
		}
		
		//BLUETOOTH GARMIN FILE NAME
		tmp = initParams.get("garmin-file-name");
		if(tmp != null) {
			tmp = tmp.trim();
			bluetoothGarminFileName = tmp.isEmpty() ? DEFAULT_BLUETOOTH_GARMIN_FILE_NAME : tmp;
			generateBluetooth = true;
		} else {
			generateBluetooth = false;
		}
		
		
		/** DELIMITERS FOR SMART SENSE DATA **/
		
		// BATTERY DELIMITER
		tmp = initParams.get("battery-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			batteryDelimiter = tmp.isEmpty() ? DEFAULT_BATTERY_DELIMITER : tmp;
		} else {
			batteryDelimiter = DEFAULT_BATTERY_DELIMITER;
		}
		
		// BATTERY CONNECTED DELIMITER
		tmp = initParams.get("battery-connected-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			batteryConnectDelimiter = tmp.isEmpty() ? DEFAULT_BATTERY_CONNECT_DELIMITER : tmp;
		} else {
			batteryConnectDelimiter = DEFAULT_BATTERY_CONNECT_DELIMITER;
		}
		
		// BATTERY LEVEL DELIMITER
		tmp = initParams.get("battery-level-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			batteryLevelDelimiter = tmp.isEmpty() ? DEFAULT_BATTERY_OK_DELIMITER : tmp;
		} else {
			batteryLevelDelimiter = DEFAULT_BATTERY_OK_DELIMITER;
		}
		
		// LOCATION DELIMITER
		tmp = initParams.get("location-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			locationDelimiter = tmp.isEmpty() ? DEFAULT_LOCATION_DELIMITER : tmp;
		} else {
			locationDelimiter = DEFAULT_LOCATION_DELIMITER;
		}
		
		// NETWORK DELIMITER
		tmp = initParams.get("network-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			networkDelimiter = tmp.isEmpty() ? DEFAULT_NETWORK_DELIMITER : tmp;
		} else {
			networkDelimiter = DEFAULT_NETWORK_DELIMITER;
		}
		
		// SIGNAL DELIMITER
		tmp = initParams.get("signal-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			signalDelimiter = tmp.isEmpty() ? DEFAULT_SIGNAL_DELIMITER : tmp;
		} else {
			signalDelimiter = DEFAULT_SIGNAL_DELIMITER;
		}
		
		// ACCELEROMETER DELIMITER
		tmp = initParams.get("accelerometer-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			accelerationDelimiter = tmp.isEmpty() ? DEFAULT_ACCELERATION_DELIMITER : tmp;
		} else {
			accelerationDelimiter = DEFAULT_ACCELERATION_DELIMITER;
		}
		
		// TEMPERATURE DELIMITER
		tmp = initParams.get("temperature-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			temperatureDelimiter = tmp.isEmpty() ? DEFAULT_TEMPERATURE_DELIMITER : tmp;
		} else {
			temperatureDelimiter = DEFAULT_TEMPERATURE_DELIMITER;
		}
		
		// GRAVITY DELIMITER
		tmp = initParams.get("gravity-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			gravityDelimiter = tmp.isEmpty() ? DEFAULT_GRAVITY_DELIMITER : tmp;
		} else {
			gravityDelimiter = DEFAULT_GRAVITY_DELIMITER;
		}
		
		// GYROSCOPE DELIMITER
		tmp = initParams.get("gyroscope-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			gyroscopeDelimiter = tmp.isEmpty() ? DEFAULT_GYROSCOPE_DELIMITER : tmp;
		} else {
			gyroscopeDelimiter = DEFAULT_GYROSCOPE_DELIMITER;
		}
		
		// LIGHT DELIMITER
		tmp = initParams.get("light-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			lightDelimiter = tmp.isEmpty() ? DEFAULT_LIGHT_DELIMITER : tmp;
		} else {
			lightDelimiter = DEFAULT_LIGHT_DELIMITER;
		}
		
		// LINEAR ACCELERATION DELIMITER
		tmp = initParams.get("linear-acc-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			linearAccDelimiter = tmp.isEmpty() ? DEFAULT_LINEAR_ACC_DELIMITER : tmp;
		} else {
			linearAccDelimiter = DEFAULT_LINEAR_ACC_DELIMITER;
		}
		
		// MAGNETIC FIELD DELIMITER
		tmp = initParams.get("magnetic-field-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			magneticFieldDelimiter = tmp.isEmpty() ? DEFAULT_MAGNETIC_FIELD_DELIMITER : tmp;
		} else {
			magneticFieldDelimiter = DEFAULT_MAGNETIC_FIELD_DELIMITER;
		}
		
		// PRESSURE DELIMITER
		tmp = initParams.get("pressure-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			pressureDelimiter = tmp.isEmpty() ? DEFAULT_PRESSURE_DELIMITER : tmp;
		} else {
			pressureDelimiter = DEFAULT_PRESSURE_DELIMITER;
		}
		
		// PROXIMITY DELIMITER
		tmp = initParams.get("proximity-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			proximityDelimiter = tmp.isEmpty() ? DEFAULT_PROXIMITY_DELIMITER : tmp;
		} else {
			proximityDelimiter = DEFAULT_PROXIMITY_DELIMITER;
		}
		
		// HUMIDITY DELIMITER
		tmp = initParams.get("humidity-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			humidityDelimiter = tmp.isEmpty() ? DEFAULT_HUMIDITY_DELIMITER : tmp;
		} else {
			humidityDelimiter = DEFAULT_HUMIDITY_DELIMITER;
		}
		
		// ORIENTATION DELIMITER
		tmp = initParams.get("orientation-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			orientationDelimiter = tmp.isEmpty() ? DEFAULT_ORIENTATION_DELIMITER : tmp;
		} else {
			orientationDelimiter = DEFAULT_ORIENTATION_DELIMITER;
		}
		
		// BLUETOOTH DELIMITER
		tmp = initParams.get("bluetooth-delimiter");
		if(tmp != null) {
			tmp = tmp.trim();
			bluetoothDelimiter = tmp.isEmpty() ? DEFAULT_BLUETOOTH_DELIMITER : tmp;
		} else {
			bluetoothDelimiter = DEFAULT_BLUETOOTH_DELIMITER;
		}
		
		devicesIds = new HashSet<String>();
		pathToDevicesVS = new HashMap<String, List<String>>();
		devicesLastTimestamp = new HashMap<String, Long>();
		
		try {
			checkForExistingVS();
		} catch (Exception e) {
			logger.warn("Error occurred while checking for existing VSDs: " + e);
		} 
		
		return true;
	}

	@Override
	public void dispose() {
		if(timer != null) {
			timer.cancel();
		}
		if(deleteOnExit) {
			for (String id : devicesIds) {
				deleteFiles(pathToDevicesVS.get(id));
			}
		}
	}

	@Override
	public void dataAvailable(String inputStreamName, StreamElement streamElement) {
		String data = (String)streamElement.getData("DATA");
		if(data.isEmpty() || !data.contains(deviceIdDelimiter)) {
			logger.warn("Recieved data that is either empty or has no mobile id parameter.");
			return;
		}
		
		if(gatewayMode) {
			dataProduced(new StreamElement(outputStructure, new Serializable[] {"unknown(gateway mode)", data}));
			return;
		}
		
		String deviceId = data.split("!" + deviceIdDelimiter + "!")[1];
			
		if(onlyRegisteredDevices) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(dictionary));
				currentDeviceLabel = props.getProperty(deviceId);
				if(currentDeviceLabel == null) {
					logger.warn("Received data from unregistered device with id: " + deviceId
							+ ". Data will be discarded.");
					return;
				}
			} catch (FileNotFoundException e) {
				logger.error("Dictionary file was deleted since " + this.getClass().getSimpleName()
						+ " started. Without dictionary file this virtual sensor is unable to work.");
				return;
			} catch (IOException e) {
				logger.error("Error occurred while reading from dictionary file: " + dictionary.getAbsolutePath());
				logger.error(e.getMessage());
				return;
			}
		} else if(dictionary != null) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(dictionary));
				currentDeviceLabel = props.getProperty(deviceId);
			} catch (IOException e) {
				// do nothing, it doesn't matter
			}
		}
		
		if(!devicesIds.contains(deviceId)) {
			if(generateVirtualSensorDescriptionFiles(deviceId)) {
				devicesIds.add(deviceId);
			}
		}
		
		if(checkTimeout && devicesIds.contains(deviceId)) {
			devicesLastTimestamp.put(deviceId, System.currentTimeMillis());
			checkTimeout();
		}
			
		StringBuilder sb = new StringBuilder();
		for (String id : devicesIds) {
			sb.append(id).append(", ");
		}
		if(sb.length() > 0) {
			sb.delete(sb.length() - 2, sb.length());
		}
		
		dataProduced(new StreamElement(outputStructure, new Serializable[] {sb.toString(), data}));
	}
	
	private void deleteFiles(List<String> paths) {
		File tmp = null;
		for (String path : paths) {
			try {
				tmp = new File(path);
				tmp.delete();
			} catch (Exception e) {
				logger.error("Failed to delete file: " + path);
				logger.error(e);
			}
		}
	}
	
	private void checkForExistingVS() throws VirtualSensorException, ParserConfigurationException, SAXException, IOException {
		pathToDevicesVS = new HashMap<String, List<String>>();
		devicesIds = new HashSet<String>();
		devicesLastTimestamp = new HashMap<String, Long>();
		
		File[] files = new File(pathToVirtualSensorsFolder).listFiles();
		long now = System.currentTimeMillis();
		VirtualSensorDescriptionFile vsd = null;
		for (File file : files) {
			if(file.isFile() && file.getName().endsWith(".xml")) {
				vsd = new VirtualSensorDescriptionFile(file);
				if(vsd.getVSName().equals("mobile_service")) continue;

				String delimiter = vsd.getInitParam("device-id-delimiter");
				if(delimiter != null && delimiter.equals(deviceIdDelimiter)) {
					String deviceId = vsd.getInitParam("device-id");
					if(deviceId != null) {
						devicesIds.add(deviceId);
						devicesLastTimestamp.put(deviceId, now);
						List<String> paths = pathToDevicesVS.get(deviceId);
						if(paths == null) paths = new ArrayList<String>();
						paths.add(file.getAbsolutePath());
						pathToDevicesVS.put(deviceId, paths);
					}
				}
			}
		}
	}
	
	private void checkTimeout() {
		long now = System.currentTimeMillis();
		String id = null;
		long timestamp;
		Iterator<String> iterator = devicesIds.iterator();
		while (iterator.hasNext()) {
			id = iterator.next();
			timestamp = devicesLastTimestamp.get(id);
			if((double)(now - timestamp)/60000 > timeout) {
				deleteFiles(pathToDevicesVS.get(id));
				pathToDevicesVS.remove(id);
				devicesLastTimestamp.remove(id);
				iterator.remove();
			}
		}
	}
	
	private boolean generateVirtualSensorDescriptionFiles(String deviceId) {
		List<String> pathsToFiles = new ArrayList<String>();
		VirtualSensorDescriptionFile tmp = null;
		String fileName = null;
		
		String deviceLabel = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			deviceLabel = deviceId;
		} else {
			deviceLabel = currentDeviceLabel;
		}
		
		
		try {
			//battery
			if(generateBattery) {
				tmp = generateMobileBatteryVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + batteryFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//battery connected
			if(generateBatteryConnect) {
				tmp = generateMobileBatteryConnectedVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + batteryConnectFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//battery level
			if(generateBatteryLevel) {
				tmp = generateMobileBatteryLevelVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + batteryLevelFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//location
			if(generateLocation) {
				tmp = generateMobileLocationVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + locationFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//signal
			if(generateSignal) {
				tmp = generateMobileSignalVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + signalFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//wifi
			if(generateWifi) {
				tmp = generateMobileWiFiVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + wifiFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//network
			if(generateNetwork) {
				tmp = generateMobileNetworkVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + networkFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//all sensors
			if(generateSensors) {
				tmp = generateMobileSensorsVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + sensorsFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//orientation
			if(generateOrientation) {
				tmp = generateMobileOrientationVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + orientationFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//acceleration
			if(generateAcceleration) {
				tmp = generateMobileAccelerationVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + accelerationFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//gravity
			if(generateGravity) {
				tmp = generateMobileGravityVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + gravityFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//gyroscope
			if(generateGyroscope) {
				tmp = generateMobileGyroscopeVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + gyroscopeFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//linear acceleration
			if(generateLinearAcc) {
				tmp = generateMobileLinearAccVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + linearAccFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//weather
			if(generateWeather) {
				tmp = generateMobileWeatherVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + weatherFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//light proximity
			if(generateLightProximity) {
				tmp = generateMobileLightProximityVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + lightProximityFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//magnetic field
			if(generateMagnetic) {
				tmp = generateMobileMagneticFieldVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + magneticFieldFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
			
			//garmin
			if(generateBluetooth) {
				tmp = generateBluetoothGarminVS(deviceId);
				if(tmp == null) throw new Exception();
				fileName = pathToVirtualSensorsFolder + bluetoothGarminFileName + "_" + deviceLabel + ".xml";
				tmp.writeToFile(new File(fileName));
				pathsToFiles.add(fileName);
			}
		} catch (Exception e) {
			logger.error("Failed to generate all VSDs for device whose id is: " + deviceId 
					+ ", therefore won't write any of them. " + e);
			return false;
		}
		
		pathToDevicesVS.put(deviceId, pathsToFiles);
		return true;
	}
	
	private VirtualSensorDescriptionFile generateMobileBatteryVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilebattery_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileBattery");
			vs.addInitParam("delimiter", batteryDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("plugged", "int");
			vs.addOutputStructureField("status", "int");
			vs.addOutputStructureField("health", "int");
			vs.addOutputStructureField("temperature", "double");
			vs.addOutputStructureField("voltage", "double");
			vs.addOutputStructureField("level", "int");
			vs.setHistorySize("100");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile battery VSD: " + e);
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileBatteryConnectedVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilebatteryconn_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileBatteryConnected");
			vs.addInitParam("delimiter", batteryConnectDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("battery_connected", "int");
			vs.setHistorySize("100");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile battery connected VSD: " + e);
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileBatteryLevelVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilebatterylevel_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileBatteryLevel");
			vs.addInitParam("delimiter", batteryLevelDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("battery_level_ok", "int");
			vs.setHistorySize("100");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile battery level VSD: " + e);
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileLocationVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilelocation_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileLocation");
			vs.addInitParam("zoom", "8");
			vs.addInitParam("delimiter", locationDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("latitude_", "double");
			vs.addOutputStructureField("longitude_", "double");
			vs.addOutputStructureField("location", "binary:image/jpeg");
			vs.setHistorySize("40");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile location VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileSignalVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilesignal_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileSignal");
			vs.addInitParam("delimiter", signalDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("signal_strength", "int");
			vs.setHistorySize("100");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile signal VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileWiFiVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilewifi_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileWifiNetwork");
			vs.addInitParam("delimiter", networkDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("wifi_name", "VARCHAR(50)");
			vs.addOutputStructureField("ip_address", "VARCHAR(20)");
			vs.addOutputStructureField("mac_address", "VARCHAR(20)");
			vs.addOutputStructureField("link_speed", "VARCHAR(20)");
			vs.addOutputStructureField("signal_level", "int");
			vs.setHistorySize("100");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile wifi VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileNetworkVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilenetwork_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileNetwork");
			vs.addInitParam("delimiter", networkDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("network_type", "VARCHAR(50)");
			vs.addOutputStructureField("sim_country", "VARCHAR(50)");
			vs.addOutputStructureField("operator", "VARCHAR(50)");
			vs.setHistorySize("100");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile network VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileOrientationVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobileorientation_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileOrientation");
			vs.addInitParam("delimiter", orientationDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("orientation", "double");
			vs.setHistorySize("500");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile wifi VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileAccelerationVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobileacc_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileAccelerometer");
			vs.addInitParam("delimiter", accelerationDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("accelerometer_X", "double");
			vs.addOutputStructureField("accelerometer_Y", "double");
			vs.addOutputStructureField("accelerometer_Z", "double");
			vs.setHistorySize("500");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile acceleration VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileGravityVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilegravity_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileGravity");
			vs.addInitParam("delimiter", gravityDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("gravity_X", "double");
			vs.addOutputStructureField("gravity_Y", "double");
			vs.addOutputStructureField("gravity_Z", "double");
			vs.setHistorySize("500");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile gravity VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileGyroscopeVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilegyroscope_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileGyroscope");
			vs.addInitParam("delimiter", gyroscopeDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("gyroscope_X", "double");
			vs.addOutputStructureField("gyroscope_Y", "double");
			vs.addOutputStructureField("gyroscope_Z", "double");
			vs.setHistorySize("500");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile gyroscope VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileLinearAccVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilelinearacc_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileLinearAcceleration");
			vs.addInitParam("delimiter", linearAccDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("linearAcc_X", "double");
			vs.addOutputStructureField("linearAcc_Y", "double");
			vs.addOutputStructureField("linearAcc_Z", "double");
			vs.setHistorySize("500");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile linear acceleration VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileWeatherVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobileweather_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileWeather");
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addInitParam("temperature-delimiter", temperatureDelimiter);
			vs.addInitParam("pressure-delimiter", pressureDelimiter);
			vs.addInitParam("relative-humidity-delimiter", humidityDelimiter);
			vs.addOutputStructureField("pressure", "double");
			vs.addOutputStructureField("temperature", "double");
			vs.addOutputStructureField("relative_humidity", "double");
			vs.setHistorySize("500");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile linear acceleration VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileLightProximityVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilelightprox_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileLightProximity");
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addInitParam("light-delimiter", lightDelimiter);
			vs.addInitParam("proximity-delimiter", proximityDelimiter);
			vs.addOutputStructureField("light", "double");
			vs.addOutputStructureField("proximity", "int");
			vs.setHistorySize("500");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile linear acceleration VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileMagneticFieldVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilemagnetic_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileMagneticField");
			vs.addInitParam("delimiter", magneticFieldDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("magnetic_field_X", "double");
			vs.addOutputStructureField("magnetic_field_Y", "double");
			vs.addOutputStructureField("magnetic_field_Z", "double");
			vs.setHistorySize("500");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile acceleration VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateBluetoothGarminVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("garmin_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileBluetoothGarmin");
			vs.addInitParam("delimiter", bluetoothDelimiter);
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addOutputStructureField("latitude", "double");
			vs.addOutputStructureField("longitude", "double");
			vs.addOutputStructureField("latitude_", "double");
			vs.addOutputStructureField("longitude_", "double");
			vs.addOutputStructureField("time", "double");
			vs.addOutputStructureField("quality", "double");
			vs.addOutputStructureField("direction", "double");
			vs.addOutputStructureField("altitude", "double");
			vs.addOutputStructureField("speed", "double");
			vs.setHistorySize("500");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile acceleration VSD: " + e.getMessage());
			return null;
		}
	}
	
	private VirtualSensorDescriptionFile generateMobileSensorsVS(String deviceId) {
		String vsName = null;
		if(currentDeviceLabel == null || currentDeviceLabel.isEmpty()) {
			vsName = deviceId;
		} else {
			vsName = currentDeviceLabel;
		}
		
		try {
			VirtualSensorDescriptionFile vs = new VirtualSensorDescriptionFile();
			vs.setVSName("mobilesensors_" + vsName);
			vs.setProcessingClass("hr.fer.rasip.mobile.MobileSensors");
			vs.addInitParam("device-id", deviceId);
			vs.addInitParam("device-id-delimiter", deviceIdDelimiter);
			vs.addInitParam("accelerometer-delimiter", accelerationDelimiter);
			vs.addInitParam("temperature-delimiter", temperatureDelimiter);
			vs.addInitParam("gravity-delimiter", gravityDelimiter);
			vs.addInitParam("gyroscope-delimiter", gyroscopeDelimiter);
			vs.addInitParam("light-delimiter", lightDelimiter);
			vs.addInitParam("linear-acc-delimiter", linearAccDelimiter);
			vs.addInitParam("magnetic-field-delimiter", magneticFieldDelimiter);
			vs.addInitParam("pressure-delimiter", pressureDelimiter);
			vs.addInitParam("proximity-delimiter", proximityDelimiter);
			vs.addInitParam("relative-humidity-delimiter", humidityDelimiter);
			vs.addOutputStructureField("accelerometer_X", "double");
			vs.addOutputStructureField("accelerometer_Y", "double");
			vs.addOutputStructureField("accelerometer_Z", "double");
			vs.addOutputStructureField("temperature", "double");
			vs.addOutputStructureField("gravity_X", "double");
			vs.addOutputStructureField("gravity_Y", "double");
			vs.addOutputStructureField("gravity_Z", "double");
			vs.addOutputStructureField("gyroscope_X", "double");
			vs.addOutputStructureField("gyroscope_Y", "double");
			vs.addOutputStructureField("gyroscope_Z", "double");
			vs.addOutputStructureField("light", "double");
			vs.addOutputStructureField("linearAcc_X", "double");
			vs.addOutputStructureField("linearAcc_Y", "double");
			vs.addOutputStructureField("linearAcc_Z", "double");
			vs.addOutputStructureField("magnetic_field_X", "double");
			vs.addOutputStructureField("magnetic_field_Y", "double");
			vs.addOutputStructureField("magnetic_field_Z", "double");
			vs.addOutputStructureField("pressure", "double");
			vs.addOutputStructureField("proximity", "int");
			vs.addOutputStructureField("relative_humidity", "double");
			vs.setHistorySize("1000");
			vs.addStream("stream1");
			vs.addSource("stream1", "mobile");
			vs.setSamplingRate("stream1", "mobile", 1);
			vs.setStorageSize("stream1", "mobile", 1);
			vs.addAddress("stream1", "mobile", "local");
			vs.addAddressPredicate("stream1", "mobile", "local", "name", "mobile_service");
			return vs;
		} catch (Exception e) {
			logger.error("Error while generating mobile sensors VSD: " + e.getMessage());
			return null;
		}
	}
}