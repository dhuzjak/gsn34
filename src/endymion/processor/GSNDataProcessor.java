package endymion.processor;

import endymion.exception.EndymionException;
import endymion.processor.data.GSNStorageElement;
import endymion.processor.filter.GSNDataFilter;
import endymion.processor.filter.GSNDataFilterSamplingRate;
import endymion.sensor.GSNSensor;

import java.util.*;

/**
 * Created by Nikola on 23.02.2015.
 */
public class GSNDataProcessor {
    protected List<GSNDataFilter> filters;

    public GSNDataProcessor () {
        filters = new ArrayList<GSNDataFilter>();
        //filters.add(new GSNDataFilterOnlyLastValue());
        filters.add(new GSNDataFilterSamplingRate());
    }

    public void setFilterParameters (HashMap<String, String> parameters) {
        for (GSNDataFilter filter : filters) {
            filter.setParameter(parameters.get(filter.getParameterKey()));
        }
    }

    public List<GSNStorageElement> processData (GSNSensor sensor) throws EndymionException {

        for (GSNDataFilter filter : filters) {
            sensor = filter.filterData(sensor);
        }

        return createStorageElements(sensor);
    }

    protected List<GSNStorageElement> createStorageElements (GSNSensor sensor) throws EndymionException {
        List<GSNStorageElement> storageElements = new ArrayList<GSNStorageElement>();

        for (String field : sensor.getDataFields()) {

            HashMap<String, String> timeAndValue = sensor.getSensorData(field).getTimeAndValue();
            SortedSet<String> orderedTimestamps = new TreeSet<String>(timeAndValue.keySet());
            for (String time : orderedTimestamps) {
                storageElements.add(new GSNStorageElement(sensor.getGSNId(), sensor.getvSensor(), field,
                        time, timeAndValue.get(time), sensor.getSensorData(field).getUnit()));
            }
        }

        return storageElements;
    }
}
