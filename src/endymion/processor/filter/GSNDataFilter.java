package endymion.processor.filter;

import endymion.exception.EndymionException;
import endymion.sensor.GSNSensor;

/**
 * Created by Nikola on 03.03.2015.
 */
public abstract class GSNDataFilter {

    protected String parameter;

    public GSNDataFilter () {

    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public abstract GSNSensor filterData (GSNSensor sensor) throws EndymionException;

    public abstract String getParameterKey ();
}
