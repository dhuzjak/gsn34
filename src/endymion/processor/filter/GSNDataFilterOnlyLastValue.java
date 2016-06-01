package endymion.processor.filter;

import endymion.time.GSNTimeManager;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.sensor.GSNSensor;
import endymion.sensor.data.GSNSensorData;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 03.03.2015.
 */
public class GSNDataFilterOnlyLastValue extends GSNDataFilter {

    public GSNDataFilterOnlyLastValue () {
        super();
    }

    @Override
    public GSNSensor filterData(GSNSensor sensor) throws EndymionException {
        if (!parameter.equalsIgnoreCase("true")) return sensor;
        List<String> fields = sensor.getDataFields();

        DateFormat format = GSNTimeManager.dateFormat;

        for (String field : fields) {
            GSNSensorData sensorData = sensor.getSensorData(field);
            HashMap<String, String> timeAndValue = sensorData.getTimeAndValue();

            if (timeAndValue.isEmpty()) continue;

            String maxDate = "";
            String value = "";
            try {
                for (String date : timeAndValue.keySet()) {
                    if (maxDate.isEmpty()) {
                        maxDate = date;
                        value = timeAndValue.get(maxDate);
                    } else if (format.parse(maxDate).before(format.parse(date))) {
                        maxDate = date;
                        value = timeAndValue.get(maxDate);
                    }
                }
            } catch (Exception e) {
                throw new EndymionException(e.getMessage(), EndymionLoggerEnum.WARNING);
            }

            timeAndValue.clear();
            timeAndValue.put(maxDate, value);
            sensorData.setTimeAndValue(timeAndValue);

        }

        return sensor;
    }

    @Override
    public String getParameterKey() {
        return "only-last-value";
    }
}
