package endymion.configuration.processor;

import endymion.configuration.data.GSNConfiguration;
import endymion.exception.EndymionException;

import java.util.List;

/**
 * Created by Nikola on 24.02.2015.
 * Abstract configuration data processor. It is used for processing configuration
 * and creating configuration objects
 */
public abstract class GSNConfigurationDataProcessor {

    /**
     * Method which processes configuration in raw format and returns GSN configuration objects
     * @param configuration raw configuration files
     * @return configuration objects
     * @throws EndymionException
     */
    public abstract List<GSNConfiguration> processConfiguration (List<String> configuration) throws EndymionException;
}
