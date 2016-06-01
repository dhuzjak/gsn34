package endymion.storage.data;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;
import endymion.processor.data.GSNStorageElement;
import endymion.time.GSNTimeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 31.05.2015.
 */
public class GSNStorageRow {

    protected String tableName;
    protected String timestamp;
    protected List<GSNStorageElement> storageElements = new ArrayList<GSNStorageElement>();


    public void addStorageElement (GSNStorageElement element) throws EndymionException {

        if (tableName == null) {
            tableName = element.getGSNId() + "_" + element.getvSensorName();
        } else if (!tableName.equalsIgnoreCase(element.getGSNId() + "_" + element.getvSensorName())) {
            throw new EndymionException("Wrong storage element added!", EndymionLoggerEnum.ERROR);
        }

        if (timestamp == null) {
            timestamp = element.getTimed();
        } else if (!timestamp.equalsIgnoreCase(element.getTimed())) {
            throw new EndymionException("Wrong storage element added!", EndymionLoggerEnum.ERROR);
        }

        storageElements.add(element);
    }

    public HashMap<String, String> getFieldsAndTypes () {
        HashMap<String, String> fieldsAndTypes = new HashMap<String, String>();

        for (GSNStorageElement element : storageElements) {
            fieldsAndTypes.put(element.getFieldName(), element.getUnit());
        }

        fieldsAndTypes.put("timed", "bigint");

        return fieldsAndTypes;
    }

    public HashMap<String, String> getFieldsAndValues () {
        HashMap<String, String> fieldsAndValues = new HashMap<String, String>();

        for (GSNStorageElement element : storageElements) {
            fieldsAndValues.put(element.getFieldName(), element.getValue());
        }

        long timestampL = 0L;

        try {
            timestampL = GSNTimeManager.dateFormat.parse(timestamp).getTime();
        } catch (Exception e) {

        }

        fieldsAndValues.put("timed", String.valueOf(timestampL));

        return fieldsAndValues;
    }

    public String getTableName() {
        return tableName.replaceAll("\\.", "_").replaceAll(":", "_");
    }

    public String getTimestamp() {
        return timestamp;
    }
}
