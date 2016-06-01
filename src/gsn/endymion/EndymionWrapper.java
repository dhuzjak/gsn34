package gsn.endymion;

import endymion.GSNDataCollectorMain;
import endymion.time.GSNTimeManager;
import endymion.processor.data.GSNStorageElement;
import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.wrappers.AbstractWrapper;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Nikola on 10.03.2015.
 * A wrapper for Endymion system
 */
public class EndymionWrapper extends AbstractWrapper{

    private static int threadCounter = 0;
    public static final String FIELD_SEPARATOR = ";";
    public static final String VAR_SEPARATOR = ",";

    private GSNDataCollectorMain gsnDataCollectorMain;

    /**
     * This output is purely used for debugging. All the data
     * is sent to the GSNStorageUnit instance
     */
    private static final DataField[] outputFormat = new DataField[] {
            new DataField("GSNId", "varchar(50)", "IpAddress:port of the GSN"),
            new DataField("vSensorName", "varchar(100)", "Name of the virtual sensor"),
            new DataField("data", "varchar(500)", "FieldName|value|unit")
    };



    @Override
    public DataField[] getOutputFormat() {
        return outputFormat;
    }

    @Override
    public boolean initialize() {
        setName( getWrapperName() + ( ++threadCounter ) );
        gsnDataCollectorMain = new GSNDataCollectorMain();
        return true;
    }

    @Override
    public void dispose() {
        threadCounter--;
    }

    @Override
    public String getWrapperName() {
        return "EndymionWrapper";
    }

    public void run () {

        int hist = 0;

        try {

            /**
             * Initializing the endymion system - reading endymion config file
             */
            gsnDataCollectorMain.initialize();

            while (isActive()) {

                /**
                 * The main Endymion "logic" - setting up the connections, fetching and processing
                 * data
                 */
                gsnDataCollectorMain.run();
                List<GSNStorageElement> storageElementList = gsnDataCollectorMain.getStorageElements();
                if (!storageElementList.isEmpty()) {

                    hist = 0;
                    String GSNId = null, vSensorName = null, data = null;
                    long timestamp = 0;

                    /**
                     * This piece of code is used for debugging only and should be gone in a
                     * final version :)
                     */
                    for (GSNStorageElement storageElement : gsnDataCollectorMain.getStorageElements()) {

                        if (storageElement.getTimed().trim().isEmpty())
                            continue;

                        if (storageElement.getUnit().toLowerCase().contains("binary")) {
                            continue;
                        }

                        if (GSNId == null && vSensorName == null && data == null) {
                            GSNId = storageElement.getGSNId();
                            vSensorName = storageElement.getvSensorName();
                            data = storageElement.getFieldName() + VAR_SEPARATOR + storageElement.getValue() +
                                    VAR_SEPARATOR + storageElement.getUnit();
                            timestamp = GSNTimeManager.dateFormat.parse(storageElement.getTimed()).getTime();
                        } else if (storageElement.getGSNId() == GSNId && storageElement.getvSensorName() == vSensorName
                                &&  GSNTimeManager.dateFormat.parse(storageElement.getTimed()).getTime() + hist == timestamp) {
                            data += FIELD_SEPARATOR + storageElement.getFieldName() + VAR_SEPARATOR + storageElement.getValue() +
                                    VAR_SEPARATOR + storageElement.getUnit();
                        } else {
                            StreamElement streamElement = new StreamElement(getOutputFormat(),
                                    new Serializable[] {GSNId, vSensorName, data}, timestamp);

                            postStreamElement(streamElement);

                            GSNId = storageElement.getGSNId();
                            vSensorName = storageElement.getvSensorName();
                            data = storageElement.getFieldName() + VAR_SEPARATOR + storageElement.getValue() +
                                    VAR_SEPARATOR + storageElement.getUnit();
                            if (timestamp ==  GSNTimeManager.dateFormat.parse(storageElement.getTimed()).getTime() + hist) {
                                hist++;
                            } else {
                                hist = 0;
                            }
                            timestamp =  GSNTimeManager.dateFormat.parse(storageElement.getTimed()).getTime() + hist;
                        }


                        if (storageElement == gsnDataCollectorMain.getStorageElements().
                                get(gsnDataCollectorMain.getStorageElements().size() - 1)) {
                            StreamElement streamElement = new StreamElement(getOutputFormat(),
                                    new Serializable[] {GSNId, vSensorName, data}, timestamp);

                            postStreamElement(streamElement);
                        }
                    }

                    /**
                     * Storing Endymion data
                     */
                    EndymionStorageUnit.getEndymionStorageUnit().addGSNData(gsnDataCollectorMain.getStorageElements());

                }
                System.out.println("Sleeping for 1 min.");
                sleep(60000);

                /**
                 * Clearing storage elements
                 */
                gsnDataCollectorMain.clearStorageElements();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
