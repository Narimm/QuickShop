package org.maxgamer.QuickShop.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseHelper {
    public static void setup(Database db, Logger log) throws SQLException {
        if (!db.hasTable("shops")) {
            DatabaseHelper.createShopsTable(db);
        }
        if (!db.hasTable("messages")) {
            DatabaseHelper.createMessagesTable(db);
        }
        DatabaseHelper.checkColumns(db, log);
    }

    /**
     * Verifies that all required columns exist.
     * @param db  the database
     */
    private static void checkColumns(Database db) {
        PreparedStatement ps = null;
        try {
            // V3.4.2
            ps = db.getConnection().prepareStatement(
                    "ALTER TABLE shops MODIFY COLUMN price double(32,2) NOT NULL AFTER ownerId");
            ps.execute();
            ps.close();
        } catch (final SQLException ignored) {}
        try {
            // V3.4.3
            ps = db.getConnection().prepareStatement(
                    "ALTER TABLE messages MODIFY COLUMN time BIGINT(32) NOT NULL AFTER message");
            ps.execute();
            ps.close();
        } catch (final SQLException ignored) {}

    }

    public static void checkColumns(Database db, Logger log){
        checkColumns(db);
        try {
            log.info(db.getConnection().getSchema());
        }catch (final SQLException e){e.printStackTrace();}

    }


    /**
     * Creates the database table 'shops'.
     * @param db the database
     * 
     * @throws SQLException
     *             If the connection is invalid.
     */
    public static void createShopsTable(Database db) throws SQLException {
        final Statement st = db.getConnection().createStatement();
        final String createTable = "CREATE TABLE shops (" + "ownerId  TEXT(36) NOT NULL, "
                + "price  double(32, 2) NOT NULL, " + "itemConfig TEXT CHARSET utf8 NOT NULL, "
                + "x  INTEGER(32) NOT NULL, " + "y  INTEGER(32) NOT NULL, " + "z  INTEGER(32) NOT NULL, "
                + "world VARCHAR(32) NOT NULL, " + "unlimited  boolean, " + "type  boolean, "
                + "PRIMARY KEY (x, y, z, world) " + ");";
        st.execute(createTable);
    }

    /**
     * Creates the database table 'messages'
     * @param db the database

     * @throws SQLException
     *             If the connection is invalid
     */
    public static void createMessagesTable(Database db) throws SQLException {
        final Statement st = db.getConnection().createStatement();
        final String createTable = "CREATE TABLE messages (" + "owner  TEXT(20) NOT NULL, "
                + "message  TEXT(200) NOT NULL, " + "time  BIGINT(32) NOT NULL " + ");";
        st.execute(createTable);
    }
}