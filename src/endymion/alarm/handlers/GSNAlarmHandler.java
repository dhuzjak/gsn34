package endymion.alarm.handlers;

import endymion.alarm.senders.GSNAlarmSender;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.logger.EndymionLoggerManager;
import endymion.time.GSNTimeManager;
import org.apache.commons.lang.StringUtils;

import endymion.alarm.senders.GSNAlarmSenderMqtt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONObject;

/**
 * Created by Nikola on 14.04.2015.
 * This abstract class represents an alarm handler class.
 * It's responsibility is to check if the alarm should be raised (based on condition)
 * and send alarm using GSNAlarmSender
 */
public abstract class GSNAlarmHandler {

    /**
     * Time period that has to pass before the alarm is raised - from configuration
     * Format: (0-9)+(h|m)
     * Examples: 30m - 30 minutes, 12h - 12 hours
     */
    protected String timePeriod;

    /**
     * The time when the last alarm was raised.
     * It is used for continuous alarming (if the repeat is true)
     */
    protected String lastSentTimestamp;

    /**
     * The flag that tells if the alarm should be repeated - from configuration
     */
    protected boolean repeat;

    /**
     * The list of recipients of the alarm (emails)
     */
    protected List<String> sendToList;

    /**
     * Alarm sender object that is responsible for sending alarm
     */
    protected GSNAlarmSender alarmSender;

    /**
     * The name of the alarm
     */
    protected String alarmName;

    /**
     * The alarm Id for client application
     */
    protected Integer alarmId;

    /**
     * Was alarm activated before
     * Set true for first run message
     */
    protected boolean alarmActivated;


    /**
     * Constructor
     * @param alarmName - the name of the alarm
     */
    public GSNAlarmHandler (String alarmName) {
        sendToList = new ArrayList<String>();
        lastSentTimestamp = null;
        this.alarmName = alarmName;
        alarmActivated = true;
        alarmId = 9999;
    }

    /**
     * timePeriod setter
     * @param timePeriod
     */
    public void setTimePeriod (String timePeriod) {
        this.timePeriod = timePeriod;
    }

    /**
     * repeat setter
     * @param repeat
     */
    public void setRepeat (boolean repeat) {
        this.repeat = repeat;
    }

    /**
     * send-to list adder
     * @param sendTo
     */
    public void addSendTo (String sendTo) {
        this.sendToList.add(sendTo);
    }

    /**
     * This method is used for checking if the alarm should be send
     * @return - true if the alarm should be sent, false otherwise
     */
    public abstract boolean checkTimestamp ();

    /**
     * This method uses AlarmSender object to send alarm
     * @throws EndymionException - error occurred when sending alarm
     */
    public void raiseAlarm () throws EndymionException {

        /* changed for mqtt implementation
        //initializeSender();
        */
        
        alarmActivated = true;

        if(alarmSender instanceof GSNAlarmSenderMqtt){
            alarmSender.sendAlarm(null, generateJSON("alarm"));
        }
        else{
            alarmSender.sendAlarm("GSN Endymion Alarm", composeMessage());
        }
        
        setLastSentTimestamp(GSNTimeManager.dateFormat.format(new Date()));
    }

    /**
     * Setter for AlarmSender object
     * @param alarmSender
     */
    public void setAlarmSender (GSNAlarmSender alarmSender)  {
        this.alarmSender = alarmSender;

    }

    // added for mqtt implementation
    /**
     * This method checks timestamp of last data and if timestamp is after last alarm
     * sends ok message
     *
     * @return - true if its ok to send "Ok" message, else false
     */
    public abstract boolean checkOkSend();

    // added for mqtt implementation
    /**
     * If sender is type GSNAlarmSenderMqtt and if alarm was set before
     * Then send Status "Ok" message that says GSN or VS recovered
     * from alarm
     * 
     * Only one "Ok" message is generated so after message we reset
     * alarmActivated status to false
     */
    
    public void okMessage() throws EndymionException{

        
        if(alarmSender instanceof GSNAlarmSenderMqtt){
            
            if(alarmActivated){

                alarmSender.sendAlarm(null, generateJSON("Ok"));
                alarmActivated = false;
                // dont set timestamp for OK message !!!
                //setLastSentTimestamp(GSNTimeManager.dateFormat.format(new Date()));
                
            }
        }
    }


    /**
     * Initialization of the sender includes passing the recipient list
     * as a string with ';' separated elements
     * @throws EndymionException
     */
    public void initializeSender () throws EndymionException {
   
        alarmSender.setSendParameters(StringUtils.join(this.sendToList.iterator(), ";"));

    }

    /**
     * This method is used for generating alarm message
     * @return - alarm message
     */
    protected abstract String composeMessage ();

    // added for mqtt implementation
    /**
     * This method is used for generating JSON alarm message
     * @return - JSON alarm message
     */
    protected abstract String generateJSON (String alarmType);

    /**
     * Getter for alarm name
     * @return - alarm name
     */
    public String getAlarmName () {
        return alarmName;
    }

    /**
     * Getter for GSNId
     * @return - GSN ID
     */
    public abstract String getGSNId ();

    /**
     * Getter for GSNId
     * @return - GSN ID
     */
    public abstract String getVSensor ();

    /**
     * Setter for lastSentTimestamp
     * @param lastSentTimestamp
     */
    protected void setLastSentTimestamp (String lastSentTimestamp) {
        this.lastSentTimestamp = lastSentTimestamp;
    }

    // added for mqtt implementation
    /**
     * alarmId setter
     * @param alarmId
     */
    public void setAlarmId (String alarmId) throws EndymionException {

        try
        {
          this.alarmId = Integer.parseInt(alarmId.trim());

        }
        catch (Exception e) {
            throw new EndymionException("In GSN: " + getGSNId() + ", alarm: " + getAlarmName() +
                    ". Parameter alarm-id must be a integer >= 0.", EndymionLoggerEnum.ERROR);
        }
    }
    
}
