package gsn.endymion;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import gsn.beans.DataField;
import gsn.beans.StreamElement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Nikola on 05.05.2015.
 */
public class EndymionVSensorImageWrapper extends EndymionVSensorWrapper {


    protected DataField[] imageStructure = new DataField[]
            {new DataField("image", "binary"),
                    new DataField("data", "varchar(500)"),
                    new DataField("timestamp", "varchar(100)")};
    protected static final String imageToken = "IMAGE_TOKEN";

    @Override
    public DataField[] getOutputFormat() {
        return imageStructure;
    }

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
            Serializable[] value = new Serializable[3];

            /**
             * Extracting fields from a list and putting
             * it in a format so that the EndymionProcessingClass
             * can parse it
             */
            String dataStack = "";
            byte[] imageStack = null;
            for (String field : data) {
                if (isImage(field)) {
                    imageStack = extractImageFromField (field);
                    field = replaceImageWithToken (field);
                }
                dataStack += field + EndymionWrapper.FIELD_SEPARATOR;
            }

            value[0] = imageStack;
            value[1] = dataStack;
            value[2] = timestamp;

            StreamElement streamElement = new StreamElement(imageStructure, value);

            postStreamElement(streamElement);
        }
    }

    protected boolean isImage (String field) {

        String type = field.substring(field.lastIndexOf(EndymionWrapper.VAR_SEPARATOR) + 1, field.length());
        if (type.toLowerCase().contains("binary")) {
            return true;
        } else {
            return false;
        }
    }

    protected byte[] extractImageFromField (String field) {
        String image = field.substring(field.indexOf(EndymionWrapper.VAR_SEPARATOR) + 1, field.lastIndexOf(EndymionWrapper.VAR_SEPARATOR));
        return Base64.decode(image);
    }

    protected String replaceImageWithToken (String field) {
        String fieldName = field.substring(0, field.indexOf(EndymionWrapper.VAR_SEPARATOR) + 1);
        String type = field.substring(field.lastIndexOf(EndymionWrapper.VAR_SEPARATOR), field.length());

        return fieldName + this.imageToken + type;
    }
}
