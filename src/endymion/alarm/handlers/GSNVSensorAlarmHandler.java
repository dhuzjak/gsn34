package endymion.alarm.handlers;

import endymion.logger.timestamp.EndymionTimestampManager;
import endymion.time.GSNTimeManager;

import org.json.simple.JSONObject;

/**
 * Created by Nikola on 14.04.2015.
 * This class extends GSNGSNAlarmHandler and is used for handling
 * alarms on vSensor level
 */
public class GSNVSensorAlarmHandler extends GSNGSNAlarmHandler {

    /**
     * vSensor name
     */
    String vSensor;

    /**
     * Constructor
     * @param GSNId
     * @param vSensor
     * @param alarmName
     */
    public GSNVSensorAlarmHandler (String GSNId, String vSensor, String alarmName) {
        super(GSNId, alarmName);
        this.vSensor = vSensor;
        lastSentTimestamp = EndymionTimestampManager.getTimestampManager().
                getTimestampAlarm(this.GSNId, this.vSensor, this.alarmName);
    }

    /**
     * This method generates an alarm message that will be send
     * @return - composed message
     */
    @Override
    protected String composeMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("The alarm was raised in GSN " + GSNId + " for sensor " + vSensor + ".\n");
        builder.append("The cause of the alarm is expiration of the waiting time period of " + timePeriod + ".\n");

        if (getLastTimestamp() != null)
            builder.append("Last timestamp value received from GSN sensor " + vSensor + " is " + getLastTimestamp() + ".\n");
        else
            builder.append("Timestamp value was not received from" + vSensor + " since Endymion started at "
                    + GSNTimeManager.getTimeManager().getSystemStartTimestamp() + "\n");

        if (lastSentTimestamp != null) {
            builder.append("Last alarm was raised at " + lastSentTimestamp + "\n");
        }
        if (repeat) {
            builder.append("If the sensor doesn't respond in " + timePeriod + " time period, this message will be sent again.");
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
        jsonOutput.put("vSensor", vSensor);
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

    /**
     * This method overrides getLastTimestamp from GSN alarm handler.
     * @return - the last timestamp recevide from vSensor
     */
    protected String getLastTimestamp () {
        return GSNTimeManager.getTimeManager().getLastTimestamp(GSNId, vSensor);
    }

    /**
     * vSensor getter
     * @return
     */
    public String getVSensor () {
        return vSensor;
    }

    protected void setLastSentTimestamp (String lastSentTimestamp) {
        this.lastSentTimestamp = lastSentTimestamp;
        EndymionTimestampManager.getTimestampManager().setTimestampAlarm(GSNId, vSensor ,alarmName, this.lastSentTimestamp);
    }
}
