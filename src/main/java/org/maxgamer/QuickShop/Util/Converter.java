package org.maxgamer.QuickShop.Util;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Database.Database;
import com.google.common.io.Files;

public class Converter {
    /**
     * Attempts to convert the quickshop database, if necessary.
     * 
     * @return -1 for failure, 0 for no changes, 1 for success converting.
     */
    public static int convert() {
        final Database database = QuickShop.instance.getDB();

        try {
            if (database.hasColumn("shops", "owner")) {
                try {
                    Converter.convertDatabase_4_6();
                    return 1;
                } catch (final Exception e) {
                    e.printStackTrace();
                    return -1;
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }
    
    public static void convertDatabase_4_6() throws Exception {
        Database database = QuickShop.instance.getDB();
        
        System.out.println("Converting shops to 4.6 format...");
        System.out.println("Preparing UUID cache");
        
        HashMap<String, UUID> nameMap = new HashMap<>();
        for (OfflinePlayer player : Bukkit.getOfflinePlayers())
            nameMap.put(player.getName().toLowerCase(), player.getUniqueId());
        
        System.out.println("Making backup of the SQLite database");
        File existing = new File(QuickShop.instance.getDataFolder(), "shops.db");
        File backup = new File(existing.getAbsolutePath() + ".3.8.bak");
        Files.copy(existing, backup);
        
        // Start actual database manipulation
        Connection con = null;
        Statement statement = null;
        try {
            con = database.getConnection();
            statement = con.createStatement();
            
            // Step 1: Create new shop table
            String createTable = "CREATE TABLE shops2 (ownerId TEXT(36) NOT NULL, "
                    + "price  double(32, 2) NOT NULL, itemConfig  BLOB NOT NULL, x  INTEGER(32) NOT NULL, "
                    + "y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, "
                    + "unlimited  boolean, type  boolean, PRIMARY KEY (x, y, z, world) );";
            statement.executeUpdate(createTable);
            
            // Step 2: Load and insert
            System.out.println("Loading shops...");
            ResultSet rs = statement.executeQuery("SELECT * FROM shops");
            PreparedStatement insertShops = con.prepareStatement("INSERT INTO shops2 VALUES (?,?,?,?,?,?,?,?,?);");
            try {
                int failCount = 0;
                while (rs.next()) {
                    // Convert the name to UUID
                    String owner = rs.getString("owner");
                    UUID id = nameMap.get(owner.toLowerCase());
                    if (id == null)
                    {
                        System.out.println(String.format("Wont convert shop %d,%d,%d,%s. Cant resolve player %s", rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getString("world"), owner));
                        ++failCount;
                        continue;
                    }
                    
                    // Insert the new shop
                    insertShops.setString(1, id.toString());
                    insertShops.setDouble(2, rs.getDouble("price"));
                    insertShops.setString(3, rs.getString("itemConfig"));
                    insertShops.setInt(4, rs.getInt("x"));
                    insertShops.setInt(5, rs.getInt("y"));
                    insertShops.setInt(6, rs.getInt("z"));
                    insertShops.setString(7, rs.getString("world"));
                    insertShops.setInt(8, rs.getInt("unlimited"));
                    insertShops.setInt(9, rs.getInt("type"));
                    insertShops.addBatch();
                }
                
                System.out.println("Saving shops");
                insertShops.executeBatch();
                
                if (failCount != 0) {
                    System.out.println("Failed to convert " + failCount + " shops to use UUIDs. They have been skipped");
                }
            } finally {
                rs.close();
                insertShops.close();
            }
            
            System.out.println("Converting messages");
            // Step 3: Create new messages table
            statement.executeUpdate("CREATE TABLE messages2 (owner TEXT(36) NOT NULL, message TEXT(200) NOT NULL, time INTEGER(32) NOT NULL);");
            
            // Step 4: Load and insert messages
            PreparedStatement insertMessages = con.prepareStatement("INSERT INTO messages2 VALUES (?, ?, ?)");
            rs = statement.executeQuery("SELECT * FROM messages");
            try {
                int failCount = 0;
                while (rs.next()) {
                    // Convert the owner to UUID
                    String owner = rs.getString("owner");
                    UUID id = nameMap.get(owner.toLowerCase());
                    if (id == null) {
                        ++failCount;
                        continue;
                    }
                    
                    // Insert the new message
                    insertMessages.setString(1, id.toString());
                    insertMessages.setString(2, rs.getString("message"));
                    insertMessages.setLong(3, rs.getLong("time"));
                    insertMessages.addBatch();
                }
                
                System.out.println("Saving messages");
                insertMessages.executeBatch();
                
                if (failCount != 0) {
                    System.out.println("Failed to convert " + failCount + " messages to use UUIDs. They have been skipped");
                }
            } finally {
                insertMessages.close();
                rs.close();
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
            
            if (con != null) {
                con.close();
            }
        }
        
        // Create a new connection (clean lock state)
        try {
            con = database.getConnection();
            statement = con.createStatement();
            
            // Step 5: drop old tables
            statement.executeUpdate("DROP TABLE shops");
            statement.executeUpdate("DROP TABLE messages");
            
            // Step 6: rename tables
            statement.executeUpdate("ALTER TABLE shops2 RENAME TO shops");
            statement.executeUpdate("ALTER TABLE messages2 RENAME TO messages");
        } finally {
            if (statement != null) {
                statement.close();
            }
            
            if (con != null) {
                con.close();
            }
        }
        System.out.println("Conversion complete.");
    }
}