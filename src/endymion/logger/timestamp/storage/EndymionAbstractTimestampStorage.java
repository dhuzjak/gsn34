package endymion.logger.timestamp.storage;

import endymion.exception.EndymionException;

/**
 * Created by Nikola on 25.04.2015.
 */
public abstract class EndymionAbstractTimestampStorage {

    public abstract boolean initialize ();

    public abstract String getTimestamp (String... keys) throws EndymionException;

    public abstract void setTimestamp (String timestamp, String... keys) throws EndymionException;

}
