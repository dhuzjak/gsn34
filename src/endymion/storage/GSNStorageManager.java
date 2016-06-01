package endymion.storage;

import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerManager;
import endymion.processor.data.GSNStorageElement;
import endymion.storage.data.GSNStorageRow;
import endymion.storage.handler.GSNStorageHandler;
import endymion.storage.handler.GSNStorageHandlerCache;
import endymion.storage.handler.GSNStorageHandlerMySQL;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 31.05.2015.
 */
public class GSNStorageManager {

    GSNStorageHandler storageHandler;

    public GSNStorageManager (String serverAddress, String username, String password) {
        storageHandler = new GSNStorageHandlerCache(new GSNStorageHandlerMySQL(serverAddress, username, password));
    }

    public void initializeStorage () throws EndymionException {
        storageHandler.initializeStorage();
    }

    public void storeDataElements (List<GSNStorageElement> storageElements) {

        try {
            initializeStorage();
        } catch (EndymionException e) {
            EndymionLoggerManager.getLoggerManager().logMessage(e);
            return;
        }

        HashMap<String, GSNStorageRow> storageRows = new HashMap<String, GSNStorageRow>();
        for (GSNStorageElement storageElement : storageElements) {

            String key = storageElement.getGSNId() + "_" + storageElement.getvSensorName()
                    + "_" + storageElement.getTimed();

            if (!storageRows.containsKey(key)) {
                storageRows.put(key, new GSNStorageRow());
            }
            try {
                storageRows.get(key).addStorageElement(storageElement);
            } catch (EndymionException e) {
                EndymionLoggerManager.getLoggerManager().logMessage(e);
            }
        }

        for (String key : storageRows.keySet()) {

            GSNStorageRow storageRow = storageRows.get(key);

            //System.out.println(storageRow.getTableName() + " : " + storageRow.getTimestamp());
            try {
                if (!storageHandler.tableExists(storageRow.getTableName())) {
                    storageHandler.createTable(storageRow.getTableName(), storageRow.getFieldsAndTypes());
                }

                createMissingColumns(storageRow.getTableName(), storageRow.getFieldsAndTypes());

                storageHandler.insertData(storageRow.getTableName(), storageRow.getFieldsAndValues(),
                            storageRow.getFieldsAndTypes());
            } catch (EndymionException e) {
                EndymionLoggerManager.getLoggerManager().logMessage(e);
            }
        }
    }

    private void createMissingColumns (String tableName, HashMap<String, String> fieldsAndTypes) throws EndymionException {

        HashMap<String, String> columnsAndTypes = storageHandler.getFields(tableName);

        for (String field : fieldsAndTypes.keySet()) {
            if (!columnsAndTypes.containsKey(field)) {
                storageHandler.createField(tableName, field, fieldsAndTypes.get(field));
            }
        }
    }
}
