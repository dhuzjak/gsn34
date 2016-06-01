package hr.fer.rasip.mobile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.vsensor.AbstractVirtualSensor;

public class MobileLocation extends AbstractVirtualSensor {

	private final transient Logger logger = Logger.getLogger(this.getClass());
	
	private static final String DEFAULT_DEVICE_ID_DELIMITER = "android_id";
	private static final String DEFAULT_DELIMITER = "LOCATION";
	private static final int DEFAULT_ZOOM = 8;
	
	private String deviceId;
	private String deviceIdDelimiter;
	private String delimiter;
	private int zoom;
	private DataField[] outputStructure;
	
	@Override
	public boolean initialize() {
		outputStructure = getVirtualSensorConfiguration().getOutputStructure();
		Map<String, String> params = getVirtualSensorConfiguration().getMainClassInitialParams();
		delimiter = params.get("delimiter");
		if(delimiter == null || delimiter.isEmpty()) {
			delimiter = DEFAULT_DELIMITER;
			logger.warn("Default value " + DEFAULT_DELIMITER + " used for delimiter");
		} else {
			delimiter = "!" + delimiter + "!";
		}
		
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
		
		try {
			zoom = Integer.parseInt(params.get("zoom"));
		} catch (NumberFormatException e) {
			logger.warn("Value entered for \"zoom\" parameter is not valid integer. Default value will be used.");
			zoom = DEFAULT_ZOOM;
		}
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
		
		String url = "https://maps.googleapis.com/maps/api/staticmap?size=300x300&markers=" + pieces[1] + "," + pieces[0] + "&zoom=" + zoom;
		
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		try {
			BufferedImage img = ImageIO.read(new URL(url));
			ImageIO.write(img, "png", byteArray);
		} catch (Exception e) {
			logger.error("Mobile location: Virtual sensor is unable to retrieve google map");
			return;
		}
		dataProduced(new StreamElement(outputStructure, new Serializable[]{ Double.parseDouble(pieces[1]), Double.parseDouble(pieces[0]), byteArray.toByteArray() }, streamElement.getTimeStamp()));
	}

}
