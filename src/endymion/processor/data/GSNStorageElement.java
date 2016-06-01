package endymion.processor.data;

import endymion.time.GSNTimeManager;

import java.text.DateFormat;

/**
 * Created by Nikola on 03.03.2015.
 */
public class GSNStorageElement implements Comparable<GSNStorageElement>{

    protected String GSNId;
    protected String vSensorName;
    protected String fieldName;
    protected String timed;
    protected String value;
    protected String unit;

    public GSNStorageElement (String GSNId, String vSensorName, String fieldName,
                              String timed, String value, String unit) {
        this.GSNId = GSNId;
        this.vSensorName = vSensorName;
        this.fieldName = fieldName;
        this.timed = timed;
        this.value = value;
        this.unit = unit;
    }

    public String getvSensorName() {
        return vSensorName;
    }

    public void setvSensorName(String vSensorName) {
        this.vSensorName = vSensorName;
    }

    public String getGSNId() {
        return GSNId;
    }

    public void setGSNId(String GSNId) {
        this.GSNId = GSNId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getTimed() {
        return timed;
    }

    public void setTimed(String timed) {
        this.timed = timed;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public int compareTo(GSNStorageElement o) {
        DateFormat format = GSNTimeManager.dateFormat;
        try {
            long myTime = format.parse(getTimed()).getTime();
            long hisTime = format.parse(o.getTimed()).getTime();
            if (myTime != hisTime) {
                return (int) (myTime - hisTime);
            } else {
                return getvSensorName().compareTo(o.getvSensorName());
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
