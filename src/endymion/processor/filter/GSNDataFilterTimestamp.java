package endymion.processor.filter;

import endymion.time.GSNTimeManager;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.sensor.GSNSensor;
import endymion.sensor.data.GSNSensorData;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Nikola on 07.03.2015.
 */
public class GSNDataFilterTimestamp extends GSNDataFilter {
    @Override
    public GSNSensor filterData(GSNSensor sensor) throws EndymionException {

        List<String> fields = sensor.getDataFields();

        String timestamp = parameter;

        if (parameter == null || parameter.trim().isEmpty()) return sensor;

        DateFormat format = GSNTimeManager.dateFormat;

        for (String field : fields) {
            GSNSensorData sensorData = sensor.getSensorData(field);
            HashMap<String, String> timeAndValue = sensorData.getTimeAndValue();
            Set<String> keyset = timeAndValue.keySet();
            try {
                for (String time : keyset) {
                    if (format.parse(time).before(format.parse(timestamp))
                            || format.parse(time).equals(format.parse(timestamp))) {
                        timeAndValue.remove(time);
                    }
                }
            } catch (Exception e) {
                throw new EndymionException(e.getMessage(), EndymionLoggerEnum.WARNING);
            }

            sensorData.setTimeAndValue(timeAndValue);
        }

        return sensor;
    }

    @Override
    public String getParameterKey() {
        return "timestamp";
    }
}
