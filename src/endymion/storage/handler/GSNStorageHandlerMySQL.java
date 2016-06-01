package endymion.storage.handler;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import endymion.exception.EndymionException;
import endymion.logger.EndymionLoggerEnum;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikola on 01.06.2015.
 */
public class GSNStorageHandlerMySQL extends GSNStorageHandler {

    Connection connection;
    private String serverName;
    private String username;
    private String password;

    public GSNStorageHandlerMySQL (String serverName, String username, String password) {
        this.serverName = serverName;
        this.username = username;
        this.password = password;
    }

    @Override
    public void initializeStorage() throws EndymionException {

        try {
            // Load the Connector/J driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            // Establish connection to MySQL
            connection = DriverManager.getConnection(serverName, username, password);
        } catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.FATAL_ERROR);
        }
    }

    @Override
    public boolean tableExists(String tableName) throws EndymionException {
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SHOW TABLES LIKE '" + tableName + "'");
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.ERROR);
        }

    }

    @Override
    public void createTable(String tableName, HashMap<String, String> columnsAndTypes) throws EndymionException {
        String columns = "(";

        columns += "PK bigint AUTO_INCREMENT PRIMARY KEY,";

        for (String column : columnsAndTypes.keySet()) {
            String type = columnsAndTypes.get(column);
            if (type.contains("binary")) {
                type = "LONGBLOB";
            }
            columns += column + " " + type + ",";
        }

        columns = columns.substring(0, columns.length() - 1); // remove last ,
        columns += ");";

        String query = "CREATE TABLE " + tableName + " " + columns;

        try {
            connection.createStatement().executeUpdate(query);

        } catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.ERROR);
        }

    }

    @Override
    public void insertData(String tableName, HashMap<String, String> values, HashMap<String, String> columnsAndTypes)
            throws EndymionException {

        List<InputStream> streams = new ArrayList<InputStream>();
        String query = "INSERT INTO " + tableName + " ";
        String columnsList = "(";
        String valuesList = "(";


        for (String valueKey : values.keySet()) {
            columnsList += valueKey + ",";
            if (columnsAndTypes.get(valueKey).contains("varchar")) {
                valuesList += "'" + values.get(valueKey) + "',";
            }else if (columnsAndTypes.get(valueKey).contains("binary")) {
                streams.add(imageToInputStream(values.get(valueKey)));
                valuesList += "?,";
            } else {
                valuesList += values.get(valueKey) + ",";
            }
        }

        columnsList = columnsList.substring(0, columnsList.length()-1);
        valuesList = valuesList.substring(0, valuesList.length()-1);

        columnsList += ")";
        valuesList += ")";

        query = query + columnsList + " VALUES " + valuesList;

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 1; i <= streams.size(); ++i) {
                statement.setBlob(i, streams.get(i - 1));
            }
            statement.executeUpdate();

        } catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.ERROR);
        }

    }

    public HashMap<String, String> getFields (String tableName) throws EndymionException {
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = resultSet.getMetaData();
            HashMap<String, String> columns = new HashMap<String, String>();
            for (int i = 0; i < metaData.getColumnCount(); ++i) {
                columns.put(metaData.getColumnName(i+1), metaData.getColumnTypeName(i+1));
            }
            return columns;
        } catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.ERROR);
        }
    }

    public void createField (String tableName, String columnName, String columnType) throws EndymionException{

        String query = "ALTER TABLE " + tableName + " ADD " + columnName + " " + columnType;

        try {
            connection.createStatement().executeUpdate(query);

        } catch (Exception e) {
            throw new EndymionException(e.getMessage(), EndymionLoggerEnum.ERROR);
        }
    }

    private InputStream imageToInputStream (String image) {
        byte[] bytes = Base64.decode(image);

        return new ByteArrayInputStream(bytes);
    }


}
