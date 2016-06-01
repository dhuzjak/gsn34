package endymion.logger.timestamp;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerManager;
import endymion.logger.timestamp.storage.EndymionAbstractTimestampStorage;
import endymion.logger.timestamp.storage.EndymionPropertiesTimestampStorage;

/**
 * Created by Nikola on 25.04.2015.
 */
public class EndymionTimestampManager {

    private EndymionAbstractTimestampStorage timestampStorage;

    private static EndymionTimestampManager timestampManager;

    public static EndymionTimestampManager getTimestampManager() {
        if (timestampManager == null) {
            timestampManager = new EndymionTimestampManager();
        }

        return timestampManager;
    }

    private EndymionTimestampManager () {
        timestampStorage = new EndymionPropertiesTimestampStorage();
        timestampStorage.initialize();
    }

    public String getTimestampVSensor (String GSNId, String vSensor) {
        try {
            return timestampStorage.getTimestamp(GSNId, vSensor);
        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
            return null;
        }
    }

    public void setTimestampVSensor (String GSNId, String vSensor, String timestamp) {
        try {
            timestampStorage.setTimestamp(timestamp, GSNId, vSensor);
        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
        }
    }

    public void setTimestampAlarm (String GSNId, String vSensor, String alarmName, String timestamp) {
        try {
            timestampStorage.setTimestamp(timestamp, "alarm", GSNId, vSensor, alarmName);
        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
        }
    }

    public void setTimestampAlarm (String GSNId, String alarmName, String timestamp) {
        try {
            timestampStorage.setTimestamp(timestamp, "alarm", GSNId, alarmName);
        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
        }
    }

    public String getTimestampAlarm (String GSNId, String vSensor, String alarmName) {
        try {
           return timestampStorage.getTimestamp("alarm", GSNId, vSensor, alarmName);
        } catch (EndymionException e) {
            return null;
        }

    }

    public String getTimestampAlarm (String GSNId, String alarmName) {
        try {
            return timestampStorage.getTimestamp("alarm", GSNId, alarmName);
        } catch (EndymionException e) {
            return null;
        }
    }


}
