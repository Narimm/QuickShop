package org.maxgamer.QuickShop.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class MySQLCore implements DatabaseCore {
    private final String                 url;
    private static final int             MAX_CONNECTIONS = 8;
    private Properties info;
    private static ArrayList<Connection> pool            = new ArrayList<>();

    public MySQLCore(String host, String database, String port, Properties info) {
        info.put("autoReconnect", "true");
        info.put("useUnicode", "true");
        info.put("characterEncoding", "utf8");
        info.putIfAbsent("useSSL","false");
        this.info = info;
        url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        for (int i = 0; i < MySQLCore.MAX_CONNECTIONS; i++) {
            MySQLCore.pool.add(null);
        }
    }

    /**
     * Gets the database connection for
     * executing queries on.
     * 
     * @return The database connection
     */
    @Override
    public Connection getConnection() {
        for (int i = 0; i < MySQLCore.MAX_CONNECTIONS; i++) {
            Connection connection = MySQLCore.pool.get(i);
            try {
                // If we have a current connection, fetch it
                if (connection != null && !connection.isClosed()) {
                    if (connection.isValid(10)) {
                        return connection;
                    }
                    // Else, it is invalid, so we return another connection.
                }
                connection = DriverManager.getConnection(url, info);

                MySQLCore.pool.set(i, connection);

                return connection;
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void queue(BufferStatement bs) {
        try {
            Connection con = getConnection();
            while (con == null) {
                try {
                    Thread.sleep(15);
                } catch (final InterruptedException ignored) {}
                // Try again
                con = getConnection();
            }
            final PreparedStatement ps = bs.prepareStatement(con);
            ps.execute();
            ps.close();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        // Nothing, because queries are executed immediately for MySQL
    }

    @Override
    public void flush() {
        // Nothing, because queries are executed immediately for MySQL
    }
}