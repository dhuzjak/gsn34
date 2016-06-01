package endymion.storage.handler;

import endymion.exception.EndymionException;

import java.util.HashMap;

/**
 * Created by Nikola on 04.06.2015.
 */
public class GSNStorageHandlerCache extends GSNStorageHandler {

    private GSNStorageHandler storageHandler;
    private HashMap<String, HashMap<String, String>> cachedTables;

    public GSNStorageHandlerCache (GSNStorageHandler storageHandler) {
        this.storageHandler = storageHandler;
        cachedTables = new HashMap<String, HashMap<String, String>>();
    }
    @Override
    public void initializeStorage() throws EndymionException {
        cachedTables.clear();
        storageHandler.initializeStorage();
    }

    @Override
    public boolean tableExists(String tableName) throws EndymionException {

        if (!cachedTables.containsKey(tableName)) {
            boolean exists = storageHandler.tableExists(tableName);

            if (exists) {
                cachedTables.put(tableName, null);
            }

            return exists;
        } else {
            return true;
        }

    }

    @Override
    public void createTable(String tableName, HashMap<String, String> columnsAndTypes) throws EndymionException {
        storageHandler.createTable(tableName, columnsAndTypes);

        cachedTables.put(tableName, columnsAndTypes);

    }

    @Override
    public void insertData(String tableName, HashMap<String, String> values, HashMap<String, String> columnsAndTypes) throws EndymionException {
        storageHandler.insertData(tableName, values, columnsAndTypes);
    }

    @Override
    public HashMap<String, String> getFields(String tableName) throws EndymionException {
        if (!cachedTables.containsKey(tableName) || cachedTables.get(tableName) == null) {
            HashMap<String, String> fields = storageHandler.getFields(tableName);
            cachedTables.put(tableName, fields);
        }

        return cachedTables.get(tableName);
    }

    @Override
    public void createField(String tableName, String fieldName, String fieldType) throws EndymionException {
        storageHandler.createField(tableName, fieldName, fieldType);

        cachedTables.get(tableName).put(fieldName, fieldType);
    }
}
