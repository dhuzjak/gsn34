package endymion.storage.handler;

import endymion.exception.EndymionException;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 31.05.2015.
 */
public abstract class GSNStorageHandler {

    public abstract void initializeStorage () throws EndymionException;

    public abstract boolean tableExists (String tableName) throws EndymionException;

    public abstract void createTable(String tableName, HashMap<String, String> columnsAndTypes) throws EndymionException;

    public abstract void insertData(String tableName, HashMap<String, String> values,
                                    HashMap<String, String> columnsAndTypes) throws EndymionException;

    public abstract HashMap<String, String> getFields (String tableName) throws EndymionException;

    public abstract void createField (String tableName, String fieldName, String fieldType) throws EndymionException;
}
