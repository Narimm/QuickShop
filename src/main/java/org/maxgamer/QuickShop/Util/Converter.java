package org.maxgamer.QuickShop.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Database.Database;
import org.maxgamer.QuickShop.Shop.ContainerShop;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopChunk;
import org.maxgamer.QuickShop.Shop.ShopManager;
import org.maxgamer.QuickShop.Shop.ShopType;

public class Converter {
    /**
     * Attempts to convert the quickshop database, if necessary.
     * 
     * @return -1 for failure, 0 for no changes, 1 for success converting.
     */
    public static int convert() {
        final Database database = QuickShop.instance.getDB();

        try {
            if (database.hasColumn("shops", "itemString")) {
                // Convert.
                try {
                    //Converter.convertDatabase_3_8();
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

        try {
            final Connection con = database.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM shops");
            final ResultSet rs = ps.executeQuery();

            final String colType = rs.getMetaData().getColumnTypeName(3);

            if (rs.next()) {
                ps.close();

                try {
                    rs.getString("item");
                    if (!colType.equalsIgnoreCase("BLOB")) {
                        System.out.println("Item column type: " + colType + ", converting to BLOB.");

                        // We're using the old format
                        try {
                            //Converter.convertDatabase_3_8();
                            return 1;
                        } catch (final Exception e) {
                            e.printStackTrace();
                            return -1;
                        }
                    }
                } catch (final SQLException e) {
                    // No item table column.
                    // No upgrade necessary.
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return -1;
        }

        try {
            if (database.hasColumn("shops", "item")) {
                //Converter.convertDatabase_3_8();
                return 1;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

//    public static void convertDatabase_3_8() throws Exception {
//        final Database database = QuickShop.instance.getDB();
//        final ShopManager shopManager = QuickShop.instance.getShopManager();
//
//        Connection con = database.getConnection();
//        System.out.println("Converting shops to 3.8 format...");
//        // Step 1: Load existing shops.
//        PreparedStatement ps = con.prepareStatement("SELECT * FROM shops");
//        final ResultSet rs = ps.executeQuery();
//        int shops = 0;
//        System.out.println("Loading shops...");
//        while (rs.next()) {
//            final int x = rs.getInt("x");
//            final int y = rs.getInt("y");
//            final int z = rs.getInt("z");
//            final String worldName = rs.getString("world");
//            try {
//                final World world = Bukkit.getWorld(worldName);
//
//                final ItemStack item = Util.getItemStack(rs.getBytes("item"));
//
//                final String owner = rs.getString("owner");
//                final double price = rs.getDouble("price");
//                final Location loc = new Location(world, x, y, z);
//
//                final int type = rs.getInt("type");
//                final Shop shop = new ContainerShop(loc, price, item, owner);
//                shop.setUnlimited(rs.getBoolean("unlimited"));
//                shop.setShopType(ShopType.fromID(type));
//
//                shopManager.loadShop(rs.getString("world"), shop);
//                shops++;
//            } catch (final Exception e) {
//                e.printStackTrace();
//                System.out.println("Error loading a shop! Coords: " + worldName + " (" + x + ", " + y + ", " + z
//                        + ") - Skipping it...");
//            }
//        }
//        ps.close();
//        rs.close();
//
//        System.out.println("Loading complete. Backing up and deleting shops table...");
//        // Step 2: Delete shops table.
//        final File existing = new File(QuickShop.instance.getDataFolder(), "shops.db");
//        final File backup = new File(existing.getAbsolutePath() + ".3.7.bak");
//
//        final InputStream in = new FileInputStream(existing);
//        final OutputStream out = new FileOutputStream(backup);
//
//        final byte[] buf = new byte[1024];
//        int len;
//        while ((len = in.read(buf)) > 0) {
//            out.write(buf, 0, len);
//        }
//        in.close();
//        out.close();
//
//        ps = con.prepareStatement("DELETE FROM shops");
//        ps.execute();
//        ps.close();
//        con.close();
//
//        con = database.getConnection();
//        ps = con.prepareStatement("DROP TABLE shops");
//        ps.execute();
//        ps.close();
//
//        // Step 3: Create shops table.
//        final Statement st = database.getConnection().createStatement();
//        final String createTable = "CREATE TABLE shops (" + "owner  TEXT(20) NOT NULL, "
//                + "price  double(32, 2) NOT NULL, " + "itemConfig  BLOB NOT NULL, " + "x  INTEGER(32) NOT NULL, "
//                + "y  INTEGER(32) NOT NULL, " + "z  INTEGER(32) NOT NULL, " + "world VARCHAR(32) NOT NULL, "
//                + "unlimited  boolean, " + "type  boolean, " + "PRIMARY KEY (x, y, z, world) " + ");";
//        st.execute(createTable);
//
//        // Step 4: Export the new data into the table
//        for (final Entry<String, HashMap<ShopChunk, HashMap<Location, Shop>>> worlds: shopManager.getShops().entrySet()) {
//            final String world = worlds.getKey();
//            for (final Entry<ShopChunk, HashMap<Location, Shop>> chunks: worlds.getValue().entrySet()) {
//                for (final Shop shop: chunks.getValue().values()) {
//                    ps = con.prepareStatement("INSERT INTO shops (owner, price, itemConfig, x, y, z, world, unlimited, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
//                    ps.setString(1, shop.getOwner());
//                    ps.setDouble(2, shop.getPrice());
//
//                    ps.setString(3, Util.serialize(shop.getItem()));
//
//                    ps.setInt(4, shop.getLocation().getBlockX());
//                    ps.setInt(5, shop.getLocation().getBlockY());
//                    ps.setInt(6, shop.getLocation().getBlockZ());
//                    ps.setString(7, world);
//                    ps.setInt(8, (shop.isUnlimited() ? 1 : 0));
//                    ps.setInt(9, ShopType.toID(shop.getShopType()));
//
//                    ps.execute();
//                    ps.close();
//
//                    shops--;
//                    if (shops % 10 == 0) {
//                        System.out.println("Remaining: " + shops + " shops.");
//                    }
//                }
//            }
//        }
//
//        System.out.println("Conversion complete.");
//    }
}