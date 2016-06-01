package endymion.configuration.reader;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Nikola on 26.02.2015.
 */
public class GSNConfigurationReaderXML extends GSNConfigurationReader {

    String configFilename;
    String dtdFilename;

    public GSNConfigurationReaderXML (String configFilename, String dtdFilename) {
        this.configFilename = configFilename;
        this.dtdFilename = dtdFilename;
    }

    @Override
    public List<String> readConfiguration() throws EndymionException {

        List<String> output = new ArrayList<String>();

        try {
            output.add(readFile(configFilename));
            output.add(readFile(dtdFilename));
        } catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.FATAL_ERROR);
        }

        return output;

    }

    protected String readFile (String filename) throws Exception {

        Reader reader = new BufferedReader(new FileReader(filename));

        String input = "";
        int c;

        while ((c = reader.read()) != -1) {
            input += (char) c;
        }

        return input;
    }


}
