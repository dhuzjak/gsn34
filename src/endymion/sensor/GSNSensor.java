package endymion.sensor;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.sensor.data.GSNSensorData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikola on 23.02.2015.
 */
public class GSNSensor {

    protected List<GSNSensorData> sensorData;
    protected String vSensor;
    protected String GSNId;

    public GSNSensor() {
        sensorData = new ArrayList<GSNSensorData>();
    }

    public GSNSensor(String vSensor) {
        this.vSensor = vSensor;
        sensorData = new ArrayList<GSNSensorData>();
    }

    public String getvSensor() {
        return vSensor;
    }

    public void setvSensor(String vSensor) {
        this.vSensor = vSensor;
    }

    public List<String> getDataFields() {
        List<String> dataFieldsS = new ArrayList<String>();

        for (GSNSensorData data : sensorData) {
            dataFieldsS.add(data.getDataField());
        }

        return dataFieldsS;
    }

    public void addDataField(String dataField, String unit) throws EndymionException {

        if (!getDataFields().contains(dataField)) {
            GSNSensorData data = new GSNSensorData();
            data.setDataField(dataField);
            data.setUnit(unit);

            sensorData.add(data);
        } else {
            throw new EndymionException("Field with name: " + dataField + " already exists in sensor " + vSensor,
                    EndymionLoggerEnum.WARNING);
        }
    }

    public GSNSensorData getSensorData(String field) throws EndymionException {

        for (GSNSensorData data : sensorData) {
            if (data.getDataField().equalsIgnoreCase(field)) {
                return data;
            }
        }

        throw new EndymionException("Field " + field + " doesn't exist in sensor " + vSensor);
    }

    public void setSensorData(String field, String timed, String value) throws EndymionException {
        GSNSensorData data = getSensorData(field);
        if (timed == null) {
            throw new EndymionException("Timed is null!", EndymionLoggerEnum.WARNING);
        }

        if (value == null) {
            throw new EndymionException("Value is null", EndymionLoggerEnum.WARNING);
        }

        data.addTimeAndValue(timed, value);

    }

    public String getGSNId() {
        return GSNId;
    }

    public void setGSNId(String GSNId) {
        this.GSNId = GSNId;
    }
}
