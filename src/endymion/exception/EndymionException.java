package endymion.exception;

import endymion.logger.EndymionLoggerEnum;

/**
 * Created by Nikola on 27.03.2015.
 */
public class EndymionException extends Exception {

    private EndymionLoggerEnum messageType;

    public EndymionException(String message) {
        super(message);
        this.messageType = EndymionLoggerEnum.INFO;
    }

    public EndymionException(String message, EndymionLoggerEnum messageType) {
        super(message);
        this.messageType = messageType;
    }

    public EndymionLoggerEnum getMessageType () {
        return messageType;
    }





}
