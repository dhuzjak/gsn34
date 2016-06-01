package endymion.logger;

import endymion.time.GSNTimeManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by Nikola on 31.03.2015.
 */
public class EndymionFileLogger extends EndymionAbstractLogger {

    private String filename;
    public EndymionFileLogger (String filename) {
        this.filename = filename;
    }

    @Override
    public boolean initialize() {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            String timestamp = GSNTimeManager.multidataDateFormat.format(new Date());
            out.println("Endymion started: [" + timestamp + "]");
            out.println();
            out.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public void setLogMessage(EndymionLoggerEnum type, String message) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            String timestamp = GSNTimeManager.multidataDateFormat.format(new Date());
            out.println("[" + timestamp + "]");
            switch (type) {
                case INFO:
                    out.println("[INFO]: " + message);
                    break;
                case WARNING:
                    out.println("[WARNING]: " + message);
                    break;
                case ERROR:
                    out.println("[ERROR]: " + message);
                    break;
                case FATAL_ERROR:
                    out.println("[FATAL ERROR]: " + message);
                    break;
                default:
                    out.println("[UNKNOWN]: " + message);
                    break;
            }
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
