package javasrc.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;

/**
 * VARS Query class
 *
 * @author Kevin Barnard
 * @since 6/7/2018
 */
public class VARSQuery {

    private static final String
            SERVER = "perseus.shore.mbari.org",
            DATABASE_NAME = "VARS",
            USER = "everyone",
            PASSWORD = "guest";

    private PreparedStatement statement;

    /**
     * Explicit constructor
     *
     * @param sql VARSQuery to be executed
     */
    public VARSQuery(String sql) {
        this.setStatement(sql);
    }

    /**
     * Default constructor
     */
    public VARSQuery() {
    }

    /**
     * Generate statement to query VARS database
     *
     * @return PreparedStatement statement to be executed
     * @throws SQLException
     */
    private PreparedStatement generateStatement(String sql) {

        Connection conn = null;
        String url = "jdbc:sqlserver://" + SERVER + ";databaseName=" + DATABASE_NAME + ";";

        try {
            conn = DriverManager.getConnection(url, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error connecting to server at " + url);
            e.printStackTrace();
            return null;
        }

        try {
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            System.err.println("Error preparing statement. Check SQL:\n\t" + sql);
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Read an SQL statement in from an input stream.
     *
     * @param stream Stream to read from
     * @return SQL statement String.
     */
    public static String readFromStream(InputStream stream) {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder statement = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) statement.append(line.trim()).append(" ");

            return statement.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Public use setter, generates and sets object prepared statement
     *
     * @param sql String script to be run
     */
    public void setStatement(String sql) {
        this.statement = generateStatement(sql);
    }

    /**
     * Execute the query's prepared statement
     *
     * @return ResultSet result of execution
     */
    public ResultSet executeStatement() {
        try {
            return statement.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error executing statement: " + statement.toString());
            e.printStackTrace();
            return null;
        }
    }

}