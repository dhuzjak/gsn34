package gsn.endymion;

import endymion.processor.data.GSNStorageElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 17.03.2015.
 * The Singleton class which receives data from EndymionWrapper and
 * acts as a subject to observers (IEndymionListener)
 */
public class EndymionStorageUnit {

    /**
     * Wrappers which implement IEndymionListener
     */
    List<IEndymionListener> listeners;

    /**
     * A list of storage elements recieved from endymion
     */
    List<GSNStorageElement> storageElements;

    /**
     * Singleton object
     */
    protected static EndymionStorageUnit endymionStorageUnit;

    /**
     * Wrapper subscribe to GSNStorage by using function
     * @param listener - A wrapper which implemnts IEndymionListener
     */
    public void attach (IEndymionListener listener) {
        listeners.add(listener);
    }

    /**
     * Wrappers unsubscribe from GSNStorage by calling this function
     * @param listener - A wrapper which implements IEndymionListener
     */
    public void detach (IEndymionListener listener) {
        listeners.remove(listener);
    }

    /**
     * This function is called each time new data arrives
     */
    public void notifyListeners () {

        for (IEndymionListener listener : listeners) {
            listener.update();
        }
    }

    /**
     * Private constructor
     */
    private EndymionStorageUnit () {
        listeners = new ArrayList<IEndymionListener>();
        storageElements = new ArrayList<GSNStorageElement>();
    }

    /**
     * A static function used for getting GSNStorageUnit instance
     * @return - GSNStorageUnit instance (Singleton)
     */
    public static EndymionStorageUnit getEndymionStorageUnit () {

        if (endymionStorageUnit == null) {
            endymionStorageUnit = new EndymionStorageUnit();
        }

        return endymionStorageUnit;
    }

    /**
     * This function is called by EndymionWrapper which handles
     * the Endymion subsystem. It is used to store data received by
     * the Endymion
     * @param storageElements - Sensor data received by Endymion
     */
    public void addGSNData (List<GSNStorageElement> storageElements) {
        this.storageElements = storageElements;
        notifyListeners();

        /**
         * Clearing data
         */
        if (this.storageElements != null) {
            storageElements.clear();
        }
    }

    /**
     * This function is called by wrappers to fetch data from GSNStorageUnit
     * @param GSNId is the ID of the GSN
     * @param vSensorName is the name of the virtual sensor
     * @return HashMap with timestamps as a key and the list of data field values
     */
    public HashMap<String, List<String>>  getStorageElements (String GSNId, String vSensorName) {
        HashMap<String, List<String>> returnData = new HashMap<String, List<String>>();

        for (GSNStorageElement storageElement : this.storageElements) {

            /**
             * Extract data whose GSNId and vSensorName matches the input
             */
            if (storageElement.getGSNId().equals(GSNId)
                    && storageElement.getvSensorName().equalsIgnoreCase(vSensorName)) {

                if (!returnData.containsKey(storageElement.getTimed())) {
                    returnData.put(storageElement.getTimed(), new ArrayList<String>());
                }

                /**
                 *  Formating output
                 */
                String data = storageElement.getFieldName() + EndymionWrapper.VAR_SEPARATOR
                        + storageElement.getValue() + EndymionWrapper.VAR_SEPARATOR + storageElement.getUnit();

                returnData.get(storageElement.getTimed()).add(data);
            }
        }

        return returnData;
    }
}
