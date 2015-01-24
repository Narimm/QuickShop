package org.maxgamer.QuickShop.Util;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;

public class MsgUtil {
    private static QuickShop                           plugin;
    private static YamlConfiguration                   messages;
    private static HashMap<UUID, LinkedList<String>>   player_messages = new HashMap<UUID, LinkedList<String>>();

    static {
        MsgUtil.plugin = QuickShop.instance;
    }

    /**
     * Loads all the messages from messages.yml
     */
    public static void loadCfgMessages() {
        // Load messages.yml
        final File messageFile = new File(MsgUtil.plugin.getDataFolder(), "messages.yml");
        if (!messageFile.exists()) {
            MsgUtil.plugin.getLogger().info("Creating messages.yml");
            MsgUtil.plugin.saveResource("messages.yml", true);
        }

        // Store it
        MsgUtil.messages = YamlConfiguration.loadConfiguration(messageFile);
        MsgUtil.messages.options().copyDefaults(true);

        // Load default messages
        final InputStream defMessageStream = MsgUtil.plugin.getResource("messages.yml");
        final YamlConfiguration defMessages = YamlConfiguration.loadConfiguration(defMessageStream);
        MsgUtil.messages.setDefaults(defMessages);

        // Parse colour codes
        Util.parseColours(MsgUtil.messages);
    }

    /**
     * loads all player purchase messages from the database.
     */
    public static void loadTransactionMessages() {
        MsgUtil.player_messages.clear(); // Delete old messages
        try {
            final ResultSet rs = MsgUtil.plugin.getDB().getConnection().prepareStatement("SELECT * FROM messages")
                    .executeQuery();

            while (rs.next()) {
                final String owner = rs.getString("owner");
                final String message = rs.getString("message");
                
                UUID id;
                try {
                    id = UUID.fromString(owner);
                } catch (IllegalArgumentException e) {
                    // Just ignore the non converted ones
                    continue;
                }

                LinkedList<String> msgs = MsgUtil.player_messages.get(id);
                if (msgs == null) {
                    msgs = new LinkedList<String>();
                    MsgUtil.player_messages.put(id, msgs);
                }

                msgs.add(message);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
            System.out.println("Could not load transaction messages from database. Skipping.");
        }
    }

    /**
     * @param player
     *            The name of the player to message
     * @param message
     *            The message to send them
     *            Sends the given player a message if they're online.
     *            Else, if they're not online, queues it for them in the
     *            database.
     */
    @SuppressWarnings("deprecation")
    public static void send(OfflinePlayer player, String message) {
        if (!player.isOnline()) {
            LinkedList<String> msgs = MsgUtil.player_messages.get(player);
            if (msgs == null) {
                msgs = new LinkedList<String>();
                MsgUtil.player_messages.put(player.getUniqueId(), msgs);
            }
            msgs.add(message);

            final String q = "INSERT INTO messages (owner, message, time) VALUES (?, ?, ?)";
            MsgUtil.plugin.getDB().execute(q, player.getUniqueId(), message, System.currentTimeMillis());
        } else {
            player.getPlayer().sendMessage(message);
        }
    }

    /**
     * Deletes any messages that are older than a week in the database, to save
     * on space.
     */
    public static void clean() {
        System.out.println("Cleaning purchase messages from database that are over a week old...");

        // 604800,000 msec = 1 week.
        final long weekAgo = System.currentTimeMillis() - 604800000;

        MsgUtil.plugin.getDB().execute("DELETE FROM messages WHERE time < ?", weekAgo);
    }

    /**
     * Empties the queue of messages a player has and sends them to the player.
     * 
     * @param p
     *            The player to message
     * @return true if success, false if the player is offline or null
     */
    public static boolean flush(Player p) {
        if (p != null && p.isOnline()) {
            final LinkedList<String> msgs = MsgUtil.player_messages.get(p.getUniqueId());

            if (msgs != null) {
                for (final String msg: msgs) {
                    p.sendMessage(msg);
                }

                MsgUtil.plugin.getDB().execute("DELETE FROM messages WHERE owner = ?", p.getUniqueId());
                msgs.clear();
            }

            return true;
        }
        return false;
    }

    public static void sendShopInfo(Player p, Shop shop) {
        MsgUtil.sendShopInfo(p, shop, shop.getRemainingStock());
    }

    public static void sendShopInfo(Player p, Shop shop, int stock) {
        // Potentially faster with an array?
        final ItemStack items = shop.getItem();
        p.sendMessage("");
        p.sendMessage("");

        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.shop-information"));
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.owner", shop.getOwner().getName()));
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item", shop.getDataName()));

        if (Util.isTool(items.getType())) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| "
                    + MsgUtil.getMessage("menu.damage-percent-remaining", Util.getToolPercentage(items)));
        }

        if (shop.isSelling()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.stock", "" + stock));
        } else {
            final int space = shop.getRemainingSpace();
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.space", "" + space));
        }

        p.sendMessage(ChatColor.DARK_PURPLE + "| "
                + MsgUtil.getMessage("menu.price-per", shop.getDataName(), Util.format(shop.getPrice())));

        if (shop.isBuying()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.this-shop-is-buying"));
        } else {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.this-shop-is-selling"));
        }

        Map<Enchantment, Integer> enchs = items.getItemMeta().getEnchants();
        if (enchs != null && !enchs.isEmpty()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.enchants")
                    + "-----------------------+");
            for (final Entry<Enchantment, Integer> entries: enchs.entrySet()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " "
                        + entries.getValue());
            }
        }

        try {
            Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");

            if (items.getItemMeta() instanceof EnchantmentStorageMeta) {
                final EnchantmentStorageMeta stor = (EnchantmentStorageMeta) items.getItemMeta();
                stor.getStoredEnchants();

                enchs = stor.getStoredEnchants();
                if (enchs != null && !enchs.isEmpty()) {
                    p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------"
                            + MsgUtil.getMessage("menu.stored-enchants") + "--------------------+");
                    for (final Entry<Enchantment, Integer> entries: enchs.entrySet()) {
                        p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName()
                                + " " + entries.getValue());
                    }
                }
            }
        } catch (final ClassNotFoundException e) {
            // They don't have an up to date enough build of CB to do this.
            // TODO: Remove this when it becomes redundant
        }

        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
    }

    public static void sendPurchaseSuccess(Player p, Shop shop, int amount) {
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.successful-purchase"));

        p.sendMessage(ChatColor.DARK_PURPLE
                + "| "
                + MsgUtil.getMessage("menu.item-name-and-price", "" + amount, shop.getDataName(),
                        Util.format((amount * shop.getPrice()))));

        Map<Enchantment, Integer> enchs = shop.getItem().getItemMeta().getEnchants();
        if (enchs != null && !enchs.isEmpty()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.enchants")
                    + "-----------------------+");
            for (final Entry<Enchantment, Integer> entries: enchs.entrySet()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " "
                        + entries.getValue());
            }
        }

        enchs = shop.getItem().getItemMeta().getEnchants();
        if (enchs != null && !enchs.isEmpty()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------" + MsgUtil.getMessage("menu.stored-enchants")
                    + "--------------------+");
            for (final Entry<Enchantment, Integer> entries: enchs.entrySet()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " "
                        + entries.getValue());
            }
        }

        try {
            Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");

            if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
                final EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
                stor.getStoredEnchants();

                enchs = stor.getStoredEnchants();
                if (enchs != null && !enchs.isEmpty()) {
                    p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------"
                            + MsgUtil.getMessage("menu.stored-enchants") + "--------------------+");
                    for (final Entry<Enchantment, Integer> entries: enchs.entrySet()) {
                        p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName()
                                + " " + entries.getValue());
                    }
                }
            }
        } catch (final ClassNotFoundException e) {
            // They don't have an up to date enough build of CB to do this.
            // TODO: Remove this when it becomes redundant
        }

        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
    }

    public static void sendSellSuccess(Player p, Shop shop, int amount) {
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.successfully-sold"));
        p.sendMessage(ChatColor.DARK_PURPLE
                + "| "
                + MsgUtil.getMessage("menu.item-name-and-price", "" + amount, shop.getDataName(),
                        Util.format((amount * shop.getPrice()))));

        if (MsgUtil.plugin.getConfig().getBoolean("show-tax")) {
            final double tax = MsgUtil.plugin.getConfig().getDouble("tax");
            final double total = amount * shop.getPrice();
            if (tax != 0) {
                if (!p.equals(shop.getOwner().getPlayer())) {
                    p.sendMessage(ChatColor.DARK_PURPLE + "| "
                            + MsgUtil.getMessage("menu.sell-tax", "" + Util.format((tax * total))));
                } else {
                    p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.sell-tax-self"));
                }
            }
        }

        Map<Enchantment, Integer> enchs = shop.getItem().getItemMeta().getEnchants();
        if (enchs != null && !enchs.isEmpty()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.enchants")
                    + "-----------------------+");
            for (final Entry<Enchantment, Integer> entries: enchs.entrySet()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " "
                        + entries.getValue());
            }
        }

        try {
            Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");

            if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
                final EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
                stor.getStoredEnchants();

                enchs = stor.getStoredEnchants();
                if (enchs != null && !enchs.isEmpty()) {
                    p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------"
                            + MsgUtil.getMessage("menu.stored-enchants") + "-----------------------+");
                    for (final Entry<Enchantment, Integer> entries: enchs.entrySet()) {
                        p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName()
                                + " " + entries.getValue());
                    }
                }
            }
        } catch (final ClassNotFoundException e) {
            // They don't have an up to date enough build of CB to do this.
            // TODO: Remove this when it becomes redundant
        }

        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
    }

    public static String getMessage(String loc, String... args) {
        String raw = MsgUtil.messages.getString(loc);

        if (raw == null || raw.isEmpty()) {
            return "Invalid message: " + loc;
        }
        if (args == null) {
            return raw;
        }

        for (int i = 0; i < args.length; i++) {
            raw = raw.replace("{" + i + "}", args[i]);
        }
        return raw;
    }
}