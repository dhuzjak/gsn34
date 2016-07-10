package endymion.alarm.manager;

import endymion.alarm.factory.GSNAlarmSenderFactory;
import endymion.alarm.handlers.GSNAlarmHandler;
import endymion.alarm.handlers.GSNGSNAlarmHandler;
import endymion.alarm.handlers.GSNVSensorAlarmHandler;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.logger.EndymionLoggerManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikola on 14.04.2015.
 * This class is used as a main interface for managing alarms.
 */
public class GSNAlarmManager {

    /**
     * List of alarm handlers
     */
    List<GSNAlarmHandler> alarmHandlers;

    /**
     * Constructor
     */
    public GSNAlarmManager () {
        alarmHandlers = new ArrayList<GSNAlarmHandler>();
    }

    /**
     * Method which adds GSN alarmHandler to list
     * @param gsnId - GSN ID
     * @param alarmName - alarm name
     * @param alarmSenderType - sender type
     * @throws EndymionException - wrong alarmSenderType or alarm already exists
     */
    public void addAlarmHandler (String gsnId, String alarmName, String alarmSenderType) throws EndymionException {

        try {
            getAlarmById(gsnId, alarmName);
            throw new EndymionException("Alarm with GSNId " + gsnId + " and alarm name " + alarmName +
                    " already exists!", EndymionLoggerEnum.ERROR);
        } catch (EndymionException e) {

        }

        GSNAlarmHandler alarmHandler = new GSNGSNAlarmHandler(gsnId, alarmName);
        alarmHandler.setAlarmSender(GSNAlarmSenderFactory.getAlarmSender(alarmSenderType));
        alarmHandlers.add(alarmHandler);
    }

    /**
     * Method which adds vSensor alarmHandler to list
     * @param gsnId - GSN ID
     * @param vSensorName - name of the vSensor
     * @param alarmName - alarm name
     * @param alarmSenderType - sender type
     * @throws EndymionException - wrong sender type or alarm already exists
     */
    public void addAlarmHandler (String gsnId, String vSensorName, String alarmName, String alarmSenderType)
            throws EndymionException {

        try {
            getAlarmById(gsnId, vSensorName, alarmName);
            throw new EndymionException("Alarm with GSNId " + gsnId + "and vSensorName " + vSensorName +
                    " and alarm name " + alarmName + " already exists!", EndymionLoggerEnum.ERROR);
        } catch (EndymionException e) {

        }

        GSNAlarmHandler alarmHandler = new GSNVSensorAlarmHandler(gsnId, vSensorName, alarmName);
        alarmHandler.setAlarmSender(GSNAlarmSenderFactory.getAlarmSender(alarmSenderType));
        alarmHandlers.add(alarmHandler);
    }

    /**
     * Setter for time period - from configuration
     * @param gsnId - GSN ID
     * @param alarmName - alarm name
     * @param timePeriod - a time after which the alarm is sent
     * @throws EndymionException - alarm doesn't exist
     */
    public void setTimePeriod (String gsnId, String alarmName, String timePeriod) throws EndymionException {
        setTimePeriod(gsnId, null, alarmName, timePeriod);
    }

    /**
     * Setter for time period - from configuration
     * @param gsnId - GSN ID
     * @param vSensorName - vSensor name
     * @param alarmName - alarm name
     * @param timePeriod - a time after which the alarm is sent
     * @throws EndymionException - alarm doesn't exist
     */
    public void setTimePeriod (String gsnId, String vSensorName, String alarmName, String timePeriod) throws EndymionException {
        GSNAlarmHandler alarmHandler = getAlarmById(gsnId, vSensorName, alarmName);
        alarmHandler.setTimePeriod(timePeriod);
    }

    /**
     * Setter for repeat - from configuration
     * @param gsnId - GSN ID
     * @param alarmName - alarm name
     * @param repeat - should the alarm be repeated
     * @throws EndymionException - alarm doesn't exist
     */
    public void setRepeat(String gsnId, String alarmName, boolean repeat) throws EndymionException {
        setRepeat(gsnId, null, alarmName, repeat);
    }

    /**
     * Setter for repeat - from configuration
     * @param gsnId - GSN ID
     * @param vSensorName - vSensor name
     * @param alarmName - alarm name
     * @param repeat - should the alarm be repeated
     * @throws EndymionException - alarm doesn't exist
     */
    public void setRepeat (String gsnId, String vSensorName, String alarmName, boolean repeat) throws EndymionException {
        GSNAlarmHandler alarmHandler = getAlarmById(gsnId, vSensorName, alarmName);
        alarmHandler.setRepeat(repeat);
    }

    /**
     * Method that adds alarm recipients
     * @param gsnId - GSN ID
     * @param alarmName - alarm name
     * @param sendTo - alarm recipient (email)
     * @throws EndymionException - alarm doesn't exist
     */
    public void addSendTo (String gsnId, String alarmName, String sendTo) throws EndymionException {
        addSendTo(gsnId, null, alarmName, sendTo);
    }

    /**
     * Method that adds alarm recipients
     * @param gsnId - GSN ID
     * @param vSensorName - vSensor name
     * @param alarmName - alarm name
     * @param sendTo - alarm recipient (email)
     * @throws EndymionException - alarm doesn't exist
     */
    public void addSendTo (String gsnId, String vSensorName, String alarmName, String sendTo) throws EndymionException {
        GSNAlarmHandler alarmHandler = getAlarmById(gsnId, vSensorName, alarmName);
        alarmHandler.addSendTo(sendTo);
    }

    /**
     * Method that gets GSNAlarmHandler object
     * @param gsnId
     * @param alarmName
     * @return GSNAlarmHandler object
     * @throws EndymionException - alarm doesn't exist
     */
    protected GSNAlarmHandler getAlarmById (String gsnId, String alarmName) throws EndymionException {

        return getAlarmById(gsnId, null, alarmName);
    }

    /**
     * Method that gets GSNAlarmHandler object
     * @param gsnId
     * @param vSensor
     * @param alarmName
     * @return GSNAlarmHandler object
     * @throws EndymionException - alarm doesn't exist
     */
    protected GSNAlarmHandler getAlarmById (String gsnId, String vSensor, String alarmName) throws EndymionException {

        for (GSNAlarmHandler alarmHandler : alarmHandlers) {
            if (alarmHandler.getAlarmName().equalsIgnoreCase(alarmName)
                    && alarmHandler.getGSNId().equalsIgnoreCase(gsnId)) {
                if (vSensor == null) {
                    return alarmHandler;
                } else if (alarmHandler.getVSensor() != null && alarmHandler.getVSensor().equalsIgnoreCase(vSensor)) {
                    return alarmHandler;
                }
            }
        }

        throw new EndymionException("Alarm with paramters: " + gsnId + ", " + vSensor + ", " + alarmName,
                EndymionLoggerEnum.WARNING);
    }

    // redesigned for mqtt implementation
    /**
     * Method that checks all alarms, and raises them if
     * they return true
     * Then it checks if alarms recovered and if true sends "ok"
     * message
     */
    public void checkAlarms () {
        for (GSNAlarmHandler alarmHandler : alarmHandlers) {
            if (alarmHandler.checkTimestamp()) {
                try {
                    alarmHandler.raiseAlarm();
                } catch (EndymionException e) {
                    EndymionLoggerManager.getLoggerManager().logMessage(e);
                }
            }
            else if(alarmHandler.checkOkSend()){
                try {
                    alarmHandler.okMessage();
                } catch (EndymionException e) {
                    EndymionLoggerManager.getLoggerManager().logMessage(e);
                }

            }
        }
    }

    /*
        Used for development, not used in final stage
    // added for mqtt implementation
    /**
     * Method that sends "Ok" message on GSN level
     * @param gsnId - the Id of GSN
     * @param alarmName - the name of the alarm
     * @throws EndymionException
     *//*
    public void okMessage (String gsnId,  String alarmName) throws EndymionException{

        okMessage(gsnId, null, alarmName);

    }

    // added for mqtt implementation
    /**
     * Method that sends "Ok" message on vSensor level
     * @param gsnId - the Id of GSN
     * @param vSensorName - the name of vSensor
     * @param alarmName - the name of the alarm
     * @throws EndymionException
     *//*
    public void okMessage (String gsnId, String vSensor, String alarmName) throws EndymionException {
        for (GSNAlarmHandler alarmHandler : alarmHandlers) {
             if (alarmHandler.getAlarmName().equalsIgnoreCase(alarmName)
                    && alarmHandler.getGSNId().equalsIgnoreCase(gsnId)) {
                if (vSensor == null) {
                    alarmHandler.okMessage();
                } else if (alarmHandler.getVSensor() != null && alarmHandler.getVSensor().equalsIgnoreCase(vSensor)) {
                    alarmHandler.okMessage();
                }
            }
 
        }
    }

    */

    // added for mqtt implementation
    /**
     * Method that sets alarm Id on GSN level
     * @param gsnId - the Id of GSN
     * @param alarmName - the name of the alarm
     * @param alarmId - the Id of the alarm
     * @throws EndymionException
     */
    public void setAlarmId (String gsnId, String alarmName, String alarmId) throws EndymionException {
        setAlarmId(gsnId, null, alarmName, alarmId);
    }

    // added for mqtt implementation
    /**
     * Method that sets alarm Id on vSensor level
     * @param gsnId - the Id of GSN
     * @param vSensorName - the name of vSensor
     * @param alarmName - the name of the alarm
     * @param alarmId - the Id of the alarm
     * @throws EndymionException
     */
    public void setAlarmId (String gsnId, String vSensorName, String alarmName, String alarmId) throws EndymionException {
        GSNAlarmHandler alarmHandler = getAlarmById(gsnId, vSensorName, alarmName);
        alarmHandler.setAlarmId(alarmId);
    }

    // added for mqtt implementation
    /**
     * Initialization of the sender on GSN level
     * @param gsnId - the Id of GSN
     * @param alarmName - the name of the alarm
     * @throws EndymionException
     */
    public void initializeSender (String gsnId, String alarmName) throws EndymionException {
        initializeSender(gsnId, null, alarmName);
    }

    // added for mqtt implementation
    /**
     * Initialization of the sender on vSensor level
     * @param gsnId - the Id of GSN
     * @param vSensorName - the name of vSensor
     * @param alarmName - the name of the alarm
     * @throws EndymionException
     */
    public void initializeSender (String gsnId, String vSensorName, String alarmName) throws EndymionException {
        GSNAlarmHandler alarmHandler = getAlarmById(gsnId, vSensorName, alarmName);
        alarmHandler.initializeSender();
    }


}
