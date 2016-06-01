package gsn.endymion;

import gsn.beans.AddressBean;
import gsn.beans.DataField;
import gsn.beans.StreamElement;
import gsn.wrappers.AbstractWrapper;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;
/**
 * Created by Nikola on 17.03.2015.
 * This class acts as a wrapper to any sensor which gets data
 * from endymion wrapper and also acts as a listener to GSNStorageUnit
 * by implementing IEndymionListener interface
 */
public class EndymionVSensorWrapper extends AbstractWrapper implements IEndymionListener {

    protected String GSNId;
    protected String vSensorName;

    private AddressBean addressBean;

    protected final Logger logger = Logger.getLogger(this.getClass());
    /**
     * The "data" field will contain unstructured data
     * read from GSNStorageUnit and "timestamp"
     */
    protected DataField[] structure = new DataField[]
            {new DataField("data", "varchar(500)"),
                new DataField("timestamp", "varchar(100)")};

    private static int threadCounter = 0;

    @Override
    public DataField[] getOutputFormat() {
        return structure;
    }

    @Override
    public boolean initialize() {
        setName( getWrapperName() + ( ++threadCounter ) );
        addressBean = getActiveAddressBean();

        /**
         * The GSNId and vSensorName are read from the
         * vSensorConfiguration file (/virtual-sensors directory)
         */
        GSNId = addressBean.getPredicateValue("GSNId");
        vSensorName = addressBean.getPredicateValue("vSensorName");

        EndymionStorageUnit.getEndymionStorageUnit().attach(this);

        return true;
    }

    @Override
    public void dispose() {
        EndymionStorageUnit.getEndymionStorageUnit().detach(this);
        threadCounter--;
    }

    @Override
    public String getWrapperName() {
        return "EndymionVSensorWrapper";
    }

    /**
     * The update function is called by GSNStorageUnit when it receives
     * data from the Endymion.
     */
    @Override
    public void update() {

        /**
         * Fetching data with GSNId and vSensorName
         */
        HashMap<String, List<String>> storageElements =
                EndymionStorageUnit.getEndymionStorageUnit().getStorageElements(GSNId, vSensorName);

        /**
         * Iterating through sorted timestamps (chronological order)
         */
        SortedSet<String> timestamps = new TreeSet<String>(storageElements.keySet());
        for (String timestamp : timestamps) {

            List<String> data = storageElements.get(timestamp);
            Serializable[] value = new Serializable[2];

            /**
             * Extracting fields from a list and putting
             * it in a format so that the EndymionProcessingClass
             * can parse it
             */
            String dataStack = "";
            for (String field : data) {
                dataStack += field + EndymionWrapper.FIELD_SEPARATOR;
            }

            value[0] = dataStack;
            value[1] = timestamp;

            StreamElement streamElement = new StreamElement(structure, value);

            postStreamElement(streamElement);
        }
    }
}
