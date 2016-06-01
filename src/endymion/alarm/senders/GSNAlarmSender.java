package endymion.alarm.senders;

import endymion.exception.EndymionException;

/**
 * Created by Nikola on 14.04.2015.
 * The abstract class which represents alarm sender
 */
public abstract class GSNAlarmSender {

    /**
     * This method is used for setting the parameters needed for completing the send operation
     * @param parameters - send parameters
     * @throws EndymionException - unexpected parameters given
     */
    public abstract void setSendParameters (String... parameters) throws EndymionException;

    /**
     * This method is used for send operation
     * @param subjectLine - general message (subject line in email)
     * @param alarmMessage - message regarding the concrete alarm
     * @throws EndymionException - send operation failed
     */
    public abstract void sendAlarm (String subjectLine, String alarmMessage) throws EndymionException;
}
