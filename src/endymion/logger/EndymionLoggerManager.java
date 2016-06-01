package endymion.logger;

import endymion.exception.EndymionException;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikola on 31.03.2015.
 */
public class EndymionLoggerManager {

    private List<EndymionAbstractLogger> loggers;
    private static EndymionLoggerManager loggerManager;
    private static final String FILE_PATH = "endymion_configuration/logger/endymion_log.txt";

    private EndymionLoggerManager () {
        loggers = new ArrayList<EndymionAbstractLogger>();

        EndymionAbstractLogger fileLogger = new EndymionFileLogger(FILE_PATH);
        if (fileLogger.initialize()) {
            loggers.add(fileLogger);
        }

        loggers.add(new EndymionStdoutLogger());
    }

    public static EndymionLoggerManager getLoggerManager () {
        if (loggerManager == null) {
            loggerManager = new EndymionLoggerManager();
        }

        return loggerManager;
    }

    public void logMessage (String message) {
        logMessage(message, EndymionLoggerEnum.INFO);
    }

    public void logMessage (String message, EndymionLoggerEnum messageType) {
        for (EndymionAbstractLogger logger : loggers) {
            logger.setLogMessage(messageType, message);
        }
    }

    public void logMessage (EndymionException e) {
        String message = e.getMessage() + "\n";
        message += ExceptionUtils.getStackTrace(e);
        logMessage(message, e.getMessageType());
    }

}
