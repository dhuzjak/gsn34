package endymion.sensor.data;

import java.util.HashMap;

/**
 * Created by Nikola on 23.02.2015.
 */
public class GSNSensorData {

    protected HashMap<String, String> TimeAndValue;
    protected String dataField;
    protected String unit;

    public GSNSensorData () {
        TimeAndValue = new HashMap<String, String>();
    }

    public GSNSensorData (String dataField, String unit) {
        this.dataField = dataField;
        this.unit = unit;
        TimeAndValue = new HashMap<String, String>();
    }

    public String getDataField() {
        return dataField;
    }

    public void setDataField(String dataField) {
        this.dataField = dataField;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public HashMap<String, String> getTimeAndValue() {
        return TimeAndValue;
    }

    public void setTimeAndValue (HashMap<String, String> timeAndValue) {
        this.TimeAndValue = timeAndValue;
    }

    public void addTimeAndValue (String time, String value) {
        TimeAndValue.put(time, value);
    }
}
