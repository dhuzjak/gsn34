package endymion.logger;

/**
 * Created by Nikola on 31.03.2015.
 */
public class EndymionStdoutLogger extends EndymionAbstractLogger {
    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public void setLogMessage(EndymionLoggerEnum type, String message) {
        System.out.println(message);
    }
}
