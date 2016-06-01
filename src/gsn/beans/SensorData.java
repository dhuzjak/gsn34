package gsn.beans;

import java.util.HashMap;

public class SensorData{
	
	//Sensor ID
	private final String ID;
	
	//Sensor Data
	private HashMap <String, Double> data = new HashMap <String, Double>();
	
	//Timestamp for received data
	private final long timestamp;
	
	//Geo Coordinates
	private String latitude;
	private String longitude;
	
	public SensorData(String inID, long inTimestamp) throws Exception {
		if(inID.trim().equalsIgnoreCase("")) throw new Exception("Sensor ID must be set.");
		this.ID = inID;
		this.timestamp = inTimestamp;
		
		latitude = null;
		longitude = null;
	}
	
	public void put(String inKey, Double inValue){
		this.data.put(inKey, inValue);
	}
	
	public String[] getKeys(){
		return this.data.keySet().toArray(new String[0]);
	}
	
	public Double get(String inKey){
		return this.data.get(inKey);
	}
	
	public void remove(String inKey){
		this.data.remove(inKey);
	}
	
	public long getTimestamp(){
		return this.timestamp;
	}
	
	public String getID(){
		return this.ID;
	}
	
	public int getNumberOfElements(){
		return this.data.size();
	}
	
	public boolean isExpired(long inTimestamp){
		if(this.timestamp + inTimestamp < System.currentTimeMillis())
			return true;
		return false;
	}
	
	public void setLatitude(String inLatitude){
		if(inLatitude.trim().equalsIgnoreCase("null") || inLatitude.trim().equalsIgnoreCase("") || inLatitude == null)
			return;
			
		try{
			if(Double.parseDouble(inLatitude) < -90 || Double.parseDouble(inLatitude) > 90 ) return;
		}catch(Exception e){		
		}
		
		this.latitude = inLatitude;
	}
	
	public void setLongitude(String inLongitude){
		if(inLongitude.trim().equalsIgnoreCase("null") || inLongitude.trim().equalsIgnoreCase("") || inLongitude == null)
			return;
		
		try{
			if(Double.parseDouble(inLongitude) < -180 || Double.parseDouble(inLongitude) > 180 ) return;
		}catch(Exception e){		
		}

		this.longitude = inLongitude;
	}
	
	public String getCoordinates(){
		return this.latitude + "," + this.longitude;
	}
	
	public String getLatitude(){
		return this.latitude;
	}
	
	public String getLongitude(){
		return this.longitude;
	}
	
	public boolean areCoordinatesSet(){
		if(this.latitude == null || this.longitude == null)
			return false;
		else
			return true;
	}
}
