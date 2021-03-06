package org.maxgamer.QuickShop.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public class BufferStatement {
    private final Object[]  values;
    private final String    query;
    private final Exception stacktrace;

    /**
     * Represents a PreparedStatement in a state before preparing it (E.g. No
     * file I/O Required)
     * 
     * @param query
     *            The query to execute. E.g. INSERT INTO accounts (user, passwd)
     *            VALUES (?, ?)
     * @param values
     *            The values to replace ? with in
     *            query. These are in order.
     */
    public BufferStatement(String query, Object... values) {
        this.query = query;
        this.values = values;
        stacktrace = new Exception(); // For error handling
        stacktrace.fillInStackTrace(); // We can declare where this statement
                                       // came from.
    }

    /**
     * Returns a prepared statement using the given connection.
     * Will try to return an empty statement if something went wrong.
     * If that fails, returns null.
     * 
     * This method escapes everything automatically.
     * 
     * @param con
     *            The connection to prepare this on using
     *            con.prepareStatement(..)
     * @return The prepared statement, ready for execution.
     * @throws SQLException
     *             If the connection is invalid.
     */
    public PreparedStatement prepareStatement(Connection con) throws SQLException {
        PreparedStatement ps;
        ps = con.prepareStatement(query);
        for (int i = 1; i <= values.length; i++) {
            ps.setObject(i, values[i - 1]);
        }
        return ps;
    }

    /**
     * Used for debugging. This stacktrace is recorded when the statement
     * is created, so printing it to the screen will provide useful debugging
     * information about where the query came from, if something went wrong
     * while executing it.
     * 
     * @return The stacktrace elements.
     */
    public StackTraceElement[] getStackTrace() {
        return stacktrace.getStackTrace();
    }

    /**
     * @return A string representation of this statement. Returns
     *         "Query: " + query + ", values: " +
     *         Arrays.toString(values).
     */
    @Override
    public String toString() {
        return "Query: " + query + ", values: " + Arrays.toString(values);
    }
}