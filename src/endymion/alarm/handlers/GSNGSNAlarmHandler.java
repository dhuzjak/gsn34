package endymion.alarm.handlers;

import endymion.logger.timestamp.EndymionTimestampManager;
import endymion.time.GSNTimeManager;

import java.util.Date;

import org.json.simple.JSONObject;
/**
 * Created by Nikola on 14.04.2015.
 * This class extends GSNAlarmHandler and it's used for alarms on
 * GSN level
 */
public class GSNGSNAlarmHandler extends GSNAlarmHandler {

    /**
     * GSN ID
     */
    protected String GSNId;

    /**
     * Timestamp that stores the last timestamp received from GSN
     */
    protected String gsnTimestamp;

    /**
     * Empty constructor
     */
    public GSNGSNAlarmHandler() {
        super(null);
    }

    /**
     * Constructor
     *
     * @param GSNId
     * @param alarmName
     */
    public GSNGSNAlarmHandler(String GSNId, String alarmName) {
        super(alarmName);
        this.GSNId = GSNId;
        lastSentTimestamp = EndymionTimestampManager.getTimestampManager().getTimestampAlarm(this.GSNId, alarmName);
    }

    /**
     * Checks the last timestamp received from GSN. If the GSN hasn't responded
     * since system start, the system start timestamp is used.
     *
     * @return - If the last timestamp received occurred before the timePeriod or the last sent timestamp
     * occurred before the time period and the timestamp hasn't been received, and the repeat is true this method
     * returns true, else false.
     */
    @Override
    public boolean checkTimestamp() {

        Date gsnDate;
        Date lastSentDate;

        gsnTimestamp = getLastTimestamp();
        if (gsnTimestamp == null) {
            gsnTimestamp = GSNTimeManager.getTimeManager().getSystemStartTimestamp();
        }

        if (lastSentTimestamp != null) {

            try {
                gsnDate = GSNTimeManager.dateFormat.parse(gsnTimestamp);
                lastSentDate = GSNTimeManager.dateFormat.parse(lastSentTimestamp);


                if (gsnDate.after(lastSentDate)) {
                    //do nothing
                    
                    /*
                    // changed for mqtt implementation
                    //lastSentTimestamp = null;
                    */

                } else {
                    return repeat && (GSNTimeManager.getTimeManager().compareDateTime(lastSentTimestamp, timePeriod));
                }


            } catch (Exception e) {
                return false;
            }
        }
        

        try {
            if (GSNTimeManager.getTimeManager().compareDateTime(gsnTimestamp, timePeriod)) {
                return true;
            }
        } catch (Exception e) {

        }

        return false;
    }

    // added for mqtt implementation
    /**
     * This method checks timestamp of last data and if timestamp is after last alarm
     * sends ok message
     *
     * @return - true if its ok to send "Ok" message, else false
     */
    @Override
    public boolean checkOkSend(){


        Date gsnDate;
        Date lastSentDate;

        gsnTimestamp = getLastTimestamp();
        if (gsnTimestamp == null) {
            gsnTimestamp = GSNTimeManager.getTimeManager().getSystemStartTimestamp();
        }

        // check timestamp of new data, if timestamp is after last alarm then it ok to send "ok" message
        if (lastSentTimestamp != null) {
            try {

                gsnDate = GSNTimeManager.dateFormat.parse(gsnTimestamp);
                lastSentDate = GSNTimeManager.dateFormat.parse(lastSentTimestamp);

                if (gsnDate.after(lastSentDate)) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return false;

    }

    /**
     * This method generates an alarm message that will be send
     *
     * @return - composed message
     */
    @Override
    protected String composeMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("The alarm was raised in GSN " + GSNId + ".\n");
        builder.append("The cause of the alarm is expiration of the waiting time period of " + timePeriod + ".\n");

        if (getLastTimestamp() != null)
            builder.append("Last timestamp value received from GSN is " + getLastTimestamp() + ".\n");
        else
            builder.append("Timestamp value was not received from GSN since Endymion started at "
                    + GSNTimeManager.getTimeManager().getSystemStartTimestamp() + "\n");

        if (lastSentTimestamp != null) {
            builder.append("Last alarm was raised at " + lastSentTimestamp + "\n");
        }

        if (repeat) {
            builder.append("If the GSN doesn't respond in " + timePeriod + " time period, this message will be sent again.");
        } else {
            builder.append("This message will not be repeated!");
        }

        return builder.toString();
    }

    // added for mqtt implementation
    /**
     * This method is used for generating JSON alarm message
     * @return - JSON alarm message
     */
    @Override
    protected String generateJSON(String alarmType) {

        JSONObject jsonOutput = new JSONObject();

        jsonOutput.put("alarmType", alarmType);
        jsonOutput.put("alarmId", alarmId);
        jsonOutput.put("GSNId", GSNId);
        jsonOutput.put("alarmName", alarmName);
        jsonOutput.put("expiratonPeriod", timePeriod);
        if (getLastTimestamp() != null)
            jsonOutput.put("lastTimestamp", getLastTimestamp());
        else
            jsonOutput.put("lastTimestamp", GSNTimeManager.getTimeManager().getSystemStartTimestamp());

        jsonOutput.put("lastAlarmTimestamp", lastSentTimestamp);
        jsonOutput.put("repeatPeriod", timePeriod);
        jsonOutput.put("repeat", repeat);
        
        return jsonOutput.toJSONString();
        
    }

    @Override
    public String getGSNId() {
        return GSNId;
    }

    @Override
    public String getVSensor() {
        return null;
    }

    /**
     * This method returns the last timestamp received from GSN
     *
     * @return - timestamp
     */
    protected String getLastTimestamp() {
        return GSNTimeManager.getTimeManager().getLastTimestamp(GSNId);
    }

    /**
     * Setter for lastSentTimestamp - timestamp is stored
     * @param lastSentTimestamp
     */
    protected void setLastSentTimestamp (String lastSentTimestamp) {
        this.lastSentTimestamp = lastSentTimestamp;
        EndymionTimestampManager.getTimestampManager().setTimestampAlarm(GSNId, alarmName, this.lastSentTimestamp);
    }
}