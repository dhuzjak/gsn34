package endymion.processor.filter;

import endymion.exception.EndymionException;
import endymion.sensor.GSNSensor;
import endymion.sensor.data.GSNSensorData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Nikola on 03.03.2015.
 */
public class GSNDataFilterSamplingRate extends GSNDataFilter {

    public GSNDataFilterSamplingRate () {
        super();
    }

    @Override
    public GSNSensor filterData(GSNSensor sensor) throws EndymionException {

        List<String> fields = sensor.getDataFields();

        double sampling_rate = Double.parseDouble(parameter);

        List<String> timestampsForRemoval = new ArrayList<String>();
        if (sensor.getDataFields().size() == 0) return sensor;

        GSNSensorData sensorData = sensor.getSensorData(sensor.getDataFields().get(0));

        HashMap<String, String> timeAndValue = sensorData.getTimeAndValue();
        Set<String> keyset = timeAndValue.keySet();

        for (String time : keyset) {
            if (Math.random() > sampling_rate) {
                timestampsForRemoval.add(time);
            }
        }

        for (String field : fields) {
            sensorData = sensor.getSensorData(field);
            timeAndValue = sensorData.getTimeAndValue();

            for (String time : timestampsForRemoval) {
                timeAndValue.remove(time);
            }

            sensorData.setTimeAndValue(timeAndValue);
        }

        return sensor;
    }

    @Override
    public String getParameterKey() {
        return "sampling-rate";
    }
}
