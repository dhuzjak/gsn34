package endymion.time;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.logger.EndymionLoggerManager;
import endymion.logger.timestamp.EndymionTimestampManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 07.03.2015.
 * This class keeps all the timestamp and time-sampling data
 * as well as date formats that GSN uses
 */
public class GSNTimeManager {

    /**
     * The map which contains a GSNId and a map which in term contains
     * sensor names (of that GSN) and timestamps of the most recently
     * received data
     */
    HashMap<String, HashMap<String, String>> timestamps;

    /**
     * The map which contains a GSNId and a map which in term contains
     * sensor names (of that GSN) and time-samling for those sensors
     */
    HashMap<String, HashMap<String, String>> TimeSampling;

    /**
     * The map which contains GSNId and its time-sampling
     */
    HashMap<String, String> gsnTimeSampling;

    /**
     * The date format of GSNs timestamps
     */
    public static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");

    /**
     * The date format which GSN expects when performing a multidata request
     */
    public static final DateFormat multidataDateFormat = new SimpleDateFormat("dd/MM/yyyy+HH:mm:ss");

    public static final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    protected  List<DateFormat> supportedDateFormats;

    private static GSNTimeManager timeManager;

    private String systemStartTimestamp;

    EndymionTimestampManager timestampManager;

    /**
     * Constructor
     */
    private GSNTimeManager () {
        timestamps = new HashMap<String, HashMap<String, String>>();
        TimeSampling = new HashMap<String, HashMap<String, String>>();
        gsnTimeSampling = new HashMap<String, String>();
        systemStartTimestamp = dateFormat.format(new Date());
        timestampManager = EndymionTimestampManager.getTimestampManager();
        initializeSupportedDateFormats ();
    }

    public static GSNTimeManager getTimeManager() {
        if (timeManager == null) {
            timeManager = new GSNTimeManager();
        }

        return timeManager;
    }

    /**
     * The function which sets the timestamp of the most recently received data
     * @param GSNId - ID of the GSN which data was received
     * @param vSensorName - The name of the sensor which data was received
     * @param timestamp - received timestamp
     */
    public void setLastTimestamp(String GSNId, String vSensorName, String timestamp) {

        if (!timestamps.containsKey(GSNId)) {
            timestamps.put(GSNId, new HashMap<String, String>());
        }

        try {
            timestamp = getUsedTimeFormat(timestamp);
        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
        }

        HashMap<String, String> vSensorAndTime = timestamps.get(GSNId);
        vSensorAndTime.put(vSensorName, timestamp);

        timestampManager.setTimestampVSensor(GSNId, vSensorName, timestamp);

    }

    /**
     * The function which returns last timestamp
     * @param GSNId - GSN ID
     * @param vSensorName - Sensor name
     * @return - The last timestamp
     */
    public String getLastTimestamp (String GSNId, String vSensorName) {
        try {
            String timestamp = timestamps.get(GSNId).get(vSensorName);
            if (timestamp != null && !timestamp.trim().isEmpty()) {
                return timestamp;
            }
        } catch (Exception e) {

        }

        String timestamp = timestampManager.getTimestampVSensor(GSNId, vSensorName);
        if (timestamp != null) {
            setLastTimestamp(GSNId, vSensorName, timestamp);
        }
        return timestamp;
    }

    public String getLastTimestamp (String GSNId) {
        String timestamp = null, tempTimestamp = null;

        if (this.timestamps.get(GSNId) == null) {
            return null;
        }

        for (String vSensorTimestamp : this.timestamps.get(GSNId).keySet()) {
            tempTimestamp = getLastTimestamp(GSNId, vSensorTimestamp);
            try {
                if (timestamp == null || dateFormat.parse(timestamp).before(dateFormat.parse(tempTimestamp))) {
                    timestamp = tempTimestamp;
                }
            } catch (Exception e) {

            }
        }

        return timestamp;

    }

    /**
     * Function which is used for setting time-sampling (how often will data be fetched)
     * @param GSNId - GSN ID
     * @param vSensorName - Sensor name
     * @param timeSampling - The time-sampling which contains a number (int)
     *                     and a time mark (m for minutes, h for hours)
     */
    public void setTimeSampling (String GSNId, String vSensorName, String timeSampling) {
        if (!TimeSampling.containsKey(GSNId)) {
            TimeSampling.put(GSNId, new HashMap<String, String>());
        }

        HashMap<String, String> vSensorAndTime = TimeSampling.get(GSNId);
        vSensorAndTime.put(vSensorName, timeSampling);
    }

    public String getTimeSampling (String GSNId, String vSensorName) {
        try {
            return TimeSampling.get(GSNId).get(vSensorName);
        } catch (Exception e) {
            return null;
        }
    }

    public void setGsnTimeSampling (String GSNId, String timeSampling) {
        gsnTimeSampling.put(GSNId, timeSampling);
    }

    public String getGsnTimeSampling (String GSNId) {
        return gsnTimeSampling.get(GSNId);
    }

    public boolean isSensorReady (String GSNId, String vSensorName) {

        String timestamp = getLastTimestamp(GSNId, vSensorName);
        if (timestamp == null) return true;

        String timeSampling = getTimeSampling(GSNId, vSensorName);
        if (timeSampling == null || timeSampling.trim().isEmpty()) {
            timeSampling = getGsnTimeSampling(GSNId);
        }

        try {
            return compareDateTime(timestamp, timeSampling);
        } catch (Exception e) {
            return false;
        }

    }

    public boolean compareDateTime (String lastTimestamp, String timeSampling) throws Exception {


        long sampling = 0;

       /* System.out.println("Last timestamp: " + lastTimestamp);
        System.out.println("Time sampling: " + timeSampling);
        System.out.println("Now is: " + dateFormat.format(new Date())); */

        if (timeSampling.endsWith("m")) {
            timeSampling = timeSampling.substring(0, timeSampling.length()-1);
            sampling = Integer.parseInt(timeSampling) * 60 * 1000;
        } else if (timeSampling.endsWith("h")) {
            timeSampling = timeSampling.substring(0, timeSampling.length()-1);
            sampling = Integer.parseInt(timeSampling) * 3600 * 1000;
        }

        long timestamp = dateFormat.parse(lastTimestamp).getTime();

       // System.out.println("Caluculated in ms: " + (long)(timestamp + sampling));

        return (new Date().getTime() > timestamp + sampling);

    }

    public String getSystemStartTimestamp() {
        return systemStartTimestamp;
    }

    protected void initializeSupportedDateFormats () {
        supportedDateFormats = new ArrayList<DateFormat>();
        supportedDateFormats.add(isoDateFormat);
        supportedDateFormats.add(dateFormat);
        supportedDateFormats.add(multidataDateFormat);
        supportedDateFormats.add(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS zzzz"));
    }

    protected List<DateFormat> getSupportedDateFormats () {
        return supportedDateFormats;
    }

    public String getUsedTimeFormat (String timestamp) throws EndymionException {
        for (DateFormat dateFormat : getSupportedDateFormats()) {
            try {
                return GSNTimeManager.dateFormat.format(dateFormat.parse(timestamp));
            } catch (Exception e) {

            }
        }

        try {
            long timestampL = Long.parseLong(timestamp);
            return GSNTimeManager.dateFormat.format(new Date(timestampL));
        } catch (Exception e) {

        }

        throw new EndymionException("Unsupported date format: " + timestamp, EndymionLoggerEnum.ERROR);
    }


}
