/**
 * Filename: DBConnection.java Description: Establishing connection to Database
 *
 * Created: May 5, 2008 Creator: PULUMBARITDS Modified: Aug 4, 2009 Modifier:
 * PULUMBARITDS Remarks: Added getResultObject() method
 */
package com.bsp.connection;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DB2Connection {

    private boolean dbstatus;  // Connection status
    private Connection conn;      // Connection
    private PreparedStatement pstmt;     // Statement
    private ResultSet rs;        // Result Set
    private ResultSetMetaData rsmd;      // Result Set Meta Data
    private ResultSet generatedKeys;
    private final static Logger LOGGER = Logger.getLogger(DB2Connection.class.getName());
    private String databaseName;
    private Object[][] resObj;
    

    /* Once instantiated, the connection will be located */
    /* If cannot locate the DB or an error occur, this   */
    /* will throw an Exception */
    public DB2Connection(Properties configProp) throws Exception {
        if (conn == null) {
            Properties props = new Properties();
            props.load(new FileInputStream(new File(configProp.getProperty("config"))));

            Class.forName("com.ibm.db2.jcc.DB2Driver");

            conn = DriverManager.getConnection(
                    props.getProperty("db2.connectionString"),
                    props.getProperty("db2.username"),
                    props.getProperty("db2.password"));
            databaseName = props.getProperty("db2.db.name").isEmpty() ? "" : props.getProperty("db2.db.name");
            LOGGER.info(databaseName);
        } else {
            if (conn.isClosed()) {

            Properties props = new Properties();
            props.load(new FileInputStream(new File(configProp.getProperty("config"))));
            
            Class.forName("com.ibm.db2.jcc.DB2Driver");
                
            conn = DriverManager.getConnection(
                    props.getProperty("db2.connectionString"),
                    props.getProperty("db2.username"),
                    props.getProperty("db2.password"));
            databaseName = props.getProperty("db2.db.name").isEmpty() ? "" : props.getProperty("db2.db.name");
            LOGGER.info(databaseName);
        }
        }
    }

    public void prepareQuery(String query) throws SQLException {
        LOGGER.finer(query);
        pstmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    public void prepareInsertQuery(String tablename, String[] fields, Object[] arg0) throws SQLException {
        String query = "";
        query += " INSERT INTO " + tablename;
        query += " (" + QueryAssembler.parse(fields) + ")";
        query += " VALUES";
        query += " (";

        for (int i = 0; i < arg0.length; i++) {
            query += "?";

            if (i < (arg0.length - 1)) {
                query += ", ";
            }
        }

        query += ")";

        LOGGER.finer(query);
        pstmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        for (int i = 0; i < arg0.length; i++) {
            setStatementType(i + 1, arg0[i]);
        }
    }

    public void prepareUpdateQuery(String tablename, String[] wherekeys, Object[] wherevalues, String[] ufields, Object[] uvalues) throws SQLException {
        String query = "";

        query += " UPDATE " + tablename;

        if (ufields != null && uvalues != null) {
            if (ufields.length > 0) {
                if (!ufields[0].trim().equals("")) {
                    query += " SET " + QueryAssembler.toUpdateString(ufields);
                }
            }
        }

        if (wherekeys != null && wherevalues != null) {
            if (wherekeys.length > 0) {
                if (!wherekeys[0].trim().equals("")) {
                    query += " WHERE " + QueryAssembler.toWhereString(wherekeys);
                }
            }
        }

        LOGGER.finer(query);
        pstmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        for (int i = 0; i < uvalues.length; i++) {
            setStatementType(i + 1, uvalues[i]);
        }

        for (int i = 0; i < wherevalues.length; i++) {
            setStatementType((uvalues.length + i + 1), wherevalues[i]);
        }
    }

    public void prepareDeleteQuery(String tablename, String[] searchfield, Object[] searchvalue) throws SQLException {
        String query = "";

        query += " DELETE FROM " + tablename;
        query += " WHERE " + QueryAssembler.toWhereString(searchfield);

        LOGGER.finer(query);
        pstmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        for (int i = 0; i < searchvalue.length; i++) {
            setStatementType(i + 1, searchvalue[i]);
        }
    }

    public void prepareSelectQuery(String tablename, String[] fields, String[] searchfield,
            Object[] searchvalue, String[] orderby) throws SQLException {

        String query = "";

        query += " SELECT " + QueryAssembler.parse(fields);
        query += " FROM " + tablename;

        if (searchfield != null && searchvalue != null) {
            if (searchfield.length > 0) {
                if (!searchfield[0].trim().equals("")) {
                    query += " WHERE " + QueryAssembler.toWhereString(searchfield);
                }
            }
        }

        if (orderby != null) {
            if (orderby.length > 0) {
                if (!orderby[0].trim().equals("")) {
                    query += " ORDER BY " + QueryAssembler.parse(orderby);
                }
            }
        }

        LOGGER.finer(query);
        pstmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        if (searchvalue != null) {
            for (int i = 0; i < searchvalue.length; i++) {
                setStatementType(i + 1, searchvalue[i]);
            }
        }
    }

    public void prepareSelectQuery(String tablename, String[] fields, String[] searchfield,
            Object[] searchvalue, String[] orderby, boolean distinct, boolean like) throws SQLException {

        String query = "";

        query += " SELECT ";

        if (distinct) {
            query += "DISTINCT ";
        }

        query += QueryAssembler.parse(fields);

        query += " FROM " + tablename;

        if (searchfield != null && searchvalue != null) {
            if (searchfield.length > 0) {
                if (!searchfield[0].trim().equals("")) {
                    query += " WHERE " + QueryAssembler.toWhereString(searchfield, like);
                }
            }
        }

        if (orderby != null) {
            if (orderby.length > 0) {
                if (!orderby[0].trim().equals("")) {
                    query += " ORDER BY " + QueryAssembler.parse(orderby);
                }
            }
        }

        LOGGER.finer(query);
        pstmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        if (searchvalue != null) {
            for (int i = 0; i < searchvalue.length; i++) {
                setStatementType(i + 1, searchvalue[i]);
            }
        }
    }

    private void setStatementType(int i, Object value) throws SQLException {
//        System.out.println(i + ": " + value.toString());
        LOGGER.log(Level.FINER, "{0}: {1}", new Object[]{i, value.toString()});
        if (value instanceof Integer) {
            pstmt.setInt(i, Integer.parseInt(value.toString()));
        } else if (value instanceof Double) {
            pstmt.setDouble(i, Double.parseDouble(value.toString()));
        } else if (value instanceof Float) {
            pstmt.setFloat(i, Float.parseFloat(value.toString()));
        } else if (value instanceof Long) {
            pstmt.setLong(i, Long.parseLong(value.toString()));
        } else if (value instanceof Boolean) {
            pstmt.setBoolean(i, Boolean.parseBoolean(value.toString()));
        } else if (value instanceof Short) {
            pstmt.setShort(i, Short.parseShort(value.toString()));
        } else if (value instanceof BigDecimal) {
            pstmt.setBigDecimal(i, (BigDecimal) value);
        } else if (value instanceof Date) {
            pstmt.setDate(i, (Date) value);
        } else {
            pstmt.setString(i, value.toString());
        }
    }

    /* Pass the query to be excuted */
    /* If a result is expected, use the getResult() */
    /* or getResultObject() method */
    public void executeQuery() throws Exception {
        if (pstmt.execute()) {

            rs = pstmt.getResultSet();
            rsmd = rs.getMetaData();

            getResultSet();
        }
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        generatedKeys = pstmt.getGeneratedKeys();
        return generatedKeys;
    }

    /* Get all the result in an Object format        */
    /* This can be helpful to check the valid format */
    private void getResultSet() throws SQLException {
        try {
            int rowCount = 0;

            // Count number or rows
            while (rs.next()) {            // Pass through all the row
                rowCount = rs.getRow();   // to get the last row
            }

            // Initialize the size of the result variable
            resObj = new Object[rowCount][rsmd.getColumnCount()];

            // Move the pointer before the first row
            rs.beforeFirst();

            int row = 0;

            // Pass through all the data and save this to array
            while (rs.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    resObj[row][i - 1] = rs.getObject(i);
                    //result[row][i-1] = rs.getObject(i).toString();
                }
                row++;
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Object[][] getResultObject() throws SQLException {
        return resObj;
    }

    /* Get all the field names in the table that is being queried */
    public String getFieldName(int column) throws SQLException {
        return rsmd.getColumnName(column);
    }

    /* Get all the field names in the table that is being queried */
    public String[] getFieldNames() throws SQLException {
        String[] fields = new String[rsmd.getColumnCount()];

        for (int i = 0; i < fields.length; i++) {
            fields[i] = rsmd.getColumnName(i + 1);
        }

        return fields;
    }

    /* Closing of DBConnection */
    public void close() throws Exception {
        // close now
        if (pstmt != null) {
            pstmt.close();
        }
        if (conn != null) {
            conn.close();
        }

        dbstatus = false;
        pstmt = null;
        conn = null;
    }

    /* Check if the connection is open */
    /* Return type is boolean          */
    public boolean isOpen() {
        return dbstatus;
    }

    public Connection getConnection() {
        return conn;
    }

    public ResultSet getResult() throws SQLException {
        rs.beforeFirst();
        return rs;
    }

    /**
     * @return the dataSource
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
