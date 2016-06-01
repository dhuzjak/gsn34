package endymion.logger;

/**
 * Created by Nikola on 31.03.2015.
 */
public abstract class EndymionAbstractLogger {

    public abstract boolean initialize ();
    public abstract void setLogMessage (EndymionLoggerEnum type, String message);
}
