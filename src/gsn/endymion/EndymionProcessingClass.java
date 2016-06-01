package gsn.endymion;

import endymion.time.GSNTimeManager;
import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.vsensor.AbstractVirtualSensor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nikola on 13.03.2015.
 * This is the processing class for EndymionVSensorWrapper
 */
public class EndymionProcessingClass extends AbstractVirtualSensor {
    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public void dispose() {

    }

    /**
     *
     * @param inputStreamName is the name of the input stream as specified in the
     * configuration file of the virtual sensor. @param inputDataFromInputStream
     * is actually the real data which is produced by the input stream and should
     * @param streamElement is the table row containing String data, String timestamp and sometimes image
     */
    @Override
    public void dataAvailable(String inputStreamName, StreamElement streamElement) {

        String [] fieldNames = streamElement.getFieldNames();
        DataField[] dataFields = null;
        Serializable[] values = null;
        long timestampL = 0;

        for (int j = 0; j < fieldNames.length; ++j) {

            /**
             *  If the field is "data" - unstructured data received from
             *  the Endymion. It holds field name, value and type for
             *  a single read (in a single moment)
             */
            if (fieldNames[j].toLowerCase().equalsIgnoreCase("data")) {
                String data = (String)streamElement.getData(fieldNames[j]);

                String[] fields = data.split(EndymionWrapper.FIELD_SEPARATOR);

                dataFields = new DataField[fields.length];
                values = new Serializable[fields.length];

                for (int i = 0; i < fields.length; ++i) {
                    //System.out.println("Field: " + fields[i]);
                    String[] field = fields[i].split(EndymionWrapper.VAR_SEPARATOR);

                    if (field.length != 3) {
                        System.out.println("Wrong field format - " + fields[i]);
                        return;
                    }

                    if (field[2].toLowerCase().equalsIgnoreCase("int") || field[2].toLowerCase().equalsIgnoreCase("integer")) {
                        values[i] = Integer.parseInt(field[1]);
                    } else if (field[2].equalsIgnoreCase("double")) {
                        values[i] = Double.parseDouble(field[1]);
                    } else if (field[2].startsWith("varchar")) {
                        values[i] = field[1];
                    } else if (field[2].toLowerCase().contains("binary")) {
                        values[i] = streamElement.getData("image");;
                    }

                    dataFields[i] = new DataField(field[0], field[2]);

                }
            /**
             *  If the field is "timestamp" - the timestamp format matches the
             *  GSNTimeManager.dateFormat
             */
            } else if (fieldNames[j].toLowerCase().equalsIgnoreCase("timestamp")) {
                String timestamp = (String)streamElement.getData(fieldNames[j]);
                try {
                    timestampL = GSNTimeManager.dateFormat.parse(timestamp).getTime();
                } catch (Exception e) {
                    e.printStackTrace();
                    /**
                     * If the timestamp is empty or can't be read, use a
                     * current time
                     */
                    timestampL = new Date().getTime();
                }
            }

        }

        StreamElement out = new StreamElement(dataFields, values, timestampL);

        dataProduced(out);

    }
}
