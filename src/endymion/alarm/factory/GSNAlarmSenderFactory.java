package endymion.alarm.factory;

import endymion.alarm.senders.GSNAlarmEmailSender;
import endymion.alarm.senders.GSNAlarmSender;
import endymion.alarm.senders.GSNAlarmSenderStdout;
import endymion.alarm.senders.GSNAlarmSenderMqtt;
import endymion.exception.EndymionException;

/**
 * Created by Nikola on 17.04.2015.
 * This class provides a method for fetching GSNAlarmSender object based on type.
 * Current senders are Email and Stdout
 */
public class GSNAlarmSenderFactory {

    /**
     * This static method returns GSNAlarmSender based on senderType
     * @param senderType - string representing sender type (email, stdout, mqtt)
     * @return - GSNSenderAlarm object
     * @throws EndymionException - wrong type given
     */
    public static GSNAlarmSender getAlarmSender (String senderType) throws EndymionException {
        if (senderType.equalsIgnoreCase("email")) {
            return new GSNAlarmEmailSender();
        } else if (senderType.equalsIgnoreCase("stdout")) {
            return new GSNAlarmSenderStdout();
        } else if (senderType.equalsIgnoreCase("mqtt")) {
            return new GSNAlarmSenderMqtt();
        } else {
            throw new EndymionException("Wrong alarm sender type: " + senderType);
        }
    }
}
