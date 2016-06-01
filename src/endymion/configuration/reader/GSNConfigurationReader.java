package endymion.configuration.reader;

import endymion.exception.EndymionException;

import java.util.List;
/**
 * Created by Nikola on 24.02.2015.
 */
public abstract class GSNConfigurationReader {

    public abstract List<String> readConfiguration () throws EndymionException;
}
