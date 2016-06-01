package endymion.alarm.senders;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;

/**
 * Helper class - used for debugging only
 */
public class GSNAlarmSenderStdout extends GSNAlarmSender {

    String output = "";

    @Override
    public void setSendParameters(String... parameters) throws EndymionException {
        output = "Email to:\n";
        if (parameters.length == 1) {
            String[] sendToArray = parameters[0].split(";");

            for (String sendTo : sendToArray) {
                output += sendTo + "\n";
            }
        } else {
            throw new EndymionException("Wrong parameter number!", EndymionLoggerEnum.ERROR);
        }
    }

    @Override
    public void sendAlarm(String subjectLine, String alarmMessage) throws EndymionException {
        output += "\n";
        output += "Subject line: " + subjectLine + "\n";
        output += "Message: " + alarmMessage;

        System.out.println(output);
    }
}
