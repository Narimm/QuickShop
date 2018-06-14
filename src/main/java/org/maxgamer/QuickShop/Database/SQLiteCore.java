package org.maxgamer.QuickShop.Database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

public class SQLiteCore implements DatabaseCore {
    private Connection                           connection;
    private final File                           dbFile;
    private volatile Thread                      watcher;

    private volatile LinkedList<BufferStatement> queue = new LinkedList<>();

    public SQLiteCore(File dbFile) {
        this.dbFile = dbFile;
    }

    /**
     * Gets the database connection for
     * executing queries on.
     * 
     * @return The database connection
     */
    @Override
    public Connection getConnection() {
        try {
            // If we have a current connection, fetch it
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }

        if (dbFile.exists()) {
            // So we need a new connection
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
                if(connection.isValid(2)){
                    return connection;
                }else {
                    throw new SQLException("Connection is Not Valid");
                }
            } catch (final ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            // So we need a new file too.
            try {
                // Create the file
                dbFile.createNewFile();
                // Now we won't need a new file, just a connection.
                // This will return that new connection.
                return getConnection();
            } catch (final IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public void queue(BufferStatement bs) {
        synchronized (queue) {
            queue.add(bs);
        }

        if (watcher == null || !watcher.isAlive()) {
            startWatcher();
        }
    }

    @Override
    public void flush() {
        while (!queue.isEmpty()) {
            BufferStatement bs;
            synchronized (queue) {
                bs = queue.removeFirst();
            }

            synchronized (dbFile) {
                try {
                    final PreparedStatement ps = bs.prepareStatement(getConnection());
                    ps.execute();
                    ps.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void close() {
        flush();
    }

    private void startWatcher() {
        watcher = new Thread(() -> {
            try {
                Thread.sleep(30000);
            } catch (final InterruptedException ignored) {}

            flush();
        });
        watcher.start();
    }
}