package org.maxgamer.QuickShop.Util;


import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sign;

import org.bukkit.potion.PotionEffect;
import org.maxgamer.QuickShop.QuickShop;

import com.google.common.collect.Maps;

import au.com.addstar.monolith.StringTranslator;

public class Util {
    private static HashSet<Material> tools       = new HashSet<Material>();
    private static HashSet<Material> blacklist   = new HashSet<Material>();
    private static HashSet<Material> shoppables  = new HashSet<Material>();
    private static HashSet<Material> transparent = new HashSet<Material>();

    private static QuickShop         plugin;

    private final static String      charset     = "ISO-8859-1";

    static {
        Util.plugin = QuickShop.instance;

        for (final String s: Util.plugin.getConfig().getStringList("shop-blocks")) {
            Material mat = Material.getMaterial(s.toUpperCase());
            if (mat == null) {
                Util.plugin.getLogger().info("Invalid shop-block: " + s);
            } else {
                Util.shoppables.add(mat);
            }
        }

        Util.tools.add(Material.BOW);
        Util.tools.add(Material.SHEARS);
        Util.tools.add(Material.FISHING_ROD);
        Util.tools.add(Material.FLINT_AND_STEEL);

        Util.tools.add(Material.CHAINMAIL_BOOTS);
        Util.tools.add(Material.CHAINMAIL_CHESTPLATE);
        Util.tools.add(Material.CHAINMAIL_HELMET);
        Util.tools.add(Material.CHAINMAIL_LEGGINGS);

        Util.tools.add(Material.WOOD_AXE);
        Util.tools.add(Material.WOOD_HOE);
        Util.tools.add(Material.WOOD_PICKAXE);
        Util.tools.add(Material.WOOD_SPADE);
        Util.tools.add(Material.WOOD_SWORD);

        Util.tools.add(Material.LEATHER_BOOTS);
        Util.tools.add(Material.LEATHER_CHESTPLATE);
        Util.tools.add(Material.LEATHER_HELMET);
        Util.tools.add(Material.LEATHER_LEGGINGS);

        Util.tools.add(Material.DIAMOND_AXE);
        Util.tools.add(Material.DIAMOND_HOE);
        Util.tools.add(Material.DIAMOND_PICKAXE);
        Util.tools.add(Material.DIAMOND_SPADE);
        Util.tools.add(Material.DIAMOND_SWORD);

        Util.tools.add(Material.DIAMOND_BOOTS);
        Util.tools.add(Material.DIAMOND_CHESTPLATE);
        Util.tools.add(Material.DIAMOND_HELMET);
        Util.tools.add(Material.DIAMOND_LEGGINGS);
        Util.tools.add(Material.STONE_AXE);
        Util.tools.add(Material.STONE_HOE);
        Util.tools.add(Material.STONE_PICKAXE);
        Util.tools.add(Material.STONE_SPADE);
        Util.tools.add(Material.STONE_SWORD);

        Util.tools.add(Material.GOLD_AXE);
        Util.tools.add(Material.GOLD_HOE);
        Util.tools.add(Material.GOLD_PICKAXE);
        Util.tools.add(Material.GOLD_SPADE);
        Util.tools.add(Material.GOLD_SWORD);

        Util.tools.add(Material.GOLD_BOOTS);
        Util.tools.add(Material.GOLD_CHESTPLATE);
        Util.tools.add(Material.GOLD_HELMET);
        Util.tools.add(Material.GOLD_LEGGINGS);
        Util.tools.add(Material.IRON_AXE);
        Util.tools.add(Material.IRON_HOE);
        Util.tools.add(Material.IRON_PICKAXE);
        Util.tools.add(Material.IRON_SPADE);
        Util.tools.add(Material.IRON_SWORD);

        Util.tools.add(Material.IRON_BOOTS);
        Util.tools.add(Material.IRON_CHESTPLATE);
        Util.tools.add(Material.IRON_HELMET);
        Util.tools.add(Material.IRON_LEGGINGS);

        final List<String> configBlacklist = Util.plugin.getConfig().getStringList("blacklist");

        for (final String s: configBlacklist) {
            Material mat = Material.getMaterial(s.toUpperCase());
            if (mat == null) {
                mat = Material.getMaterial(Integer.parseInt(s));
                if (mat == null) {
                    Util.plugin.getLogger().info(s + " is not a valid material.  Check your spelling or ID");
                    continue;
                }
            }
            Util.blacklist.add(mat);
        }

        Util.transparent.clear();
        // ToDo: add extras to config file
        Util.addTransparentBlock(Material.AIR);
        /* Misc */
        Util.addTransparentBlock(Material.CAKE_BLOCK);

        /* Redstone Material */
        Util.addTransparentBlock(Material.REDSTONE_WIRE);

        /* Redstone Torches */
        Util.addTransparentBlock(Material.REDSTONE_TORCH_OFF);
        Util.addTransparentBlock(Material.REDSTONE_TORCH_ON);

        /* Diodes (Repeaters) */
        Util.addTransparentBlock(Material.DIODE_BLOCK_OFF);
        Util.addTransparentBlock(Material.DIODE_BLOCK_ON);

        /* Power Sources */
        Util.addTransparentBlock(Material.DETECTOR_RAIL);
        Util.addTransparentBlock(Material.LEVER);
        Util.addTransparentBlock(Material.STONE_BUTTON);
        Util.addTransparentBlock(Material.WOOD_BUTTON);
        Util.addTransparentBlock(Material.STONE_PLATE);
        Util.addTransparentBlock(Material.WOOD_PLATE);

        /* Nature Material */
        Util.addTransparentBlock(Material.RED_MUSHROOM);
        Util.addTransparentBlock(Material.BROWN_MUSHROOM);

        Util.addTransparentBlock(Material.RED_ROSE);
        Util.addTransparentBlock(Material.YELLOW_FLOWER);

        Util.addTransparentBlock(Material.FLOWER_POT);

        /* Greens */
        Util.addTransparentBlock(Material.LONG_GRASS);
        Util.addTransparentBlock(Material.VINE);
        Util.addTransparentBlock(Material.WATER_LILY);

        /* Seedy things */
        Util.addTransparentBlock(Material.MELON_STEM);
        Util.addTransparentBlock(Material.PUMPKIN_STEM);
        Util.addTransparentBlock(Material.CROPS);
        Util.addTransparentBlock(Material.NETHER_WARTS);

        /* Semi-nature */
        Util.addTransparentBlock(Material.SNOW);
        Util.addTransparentBlock(Material.FIRE);
        Util.addTransparentBlock(Material.WEB);
        Util.addTransparentBlock(Material.TRIPWIRE);
        Util.addTransparentBlock(Material.TRIPWIRE_HOOK);

        /* Stairs */
        Util.addTransparentBlock(Material.COBBLESTONE_STAIRS);
        Util.addTransparentBlock(Material.BRICK_STAIRS);
        Util.addTransparentBlock(Material.SANDSTONE_STAIRS);
        Util.addTransparentBlock(Material.NETHER_BRICK_STAIRS);
        Util.addTransparentBlock(Material.SMOOTH_STAIRS);

        /* Wood Stairs */
        Util.addTransparentBlock(Material.BIRCH_WOOD_STAIRS);
        Util.addTransparentBlock(Material.WOOD_STAIRS);
        Util.addTransparentBlock(Material.JUNGLE_WOOD_STAIRS);
        Util.addTransparentBlock(Material.SPRUCE_WOOD_STAIRS);

        /* Lava & Water */
        Util.addTransparentBlock(Material.LAVA);
        Util.addTransparentBlock(Material.STATIONARY_LAVA);
        Util.addTransparentBlock(Material.WATER);
        Util.addTransparentBlock(Material.STATIONARY_WATER);

        /* Saplings and bushes */
        Util.addTransparentBlock(Material.SAPLING);
        Util.addTransparentBlock(Material.DEAD_BUSH);

        /* Construction Material */
        /* Fences */
        Util.addTransparentBlock(Material.FENCE);
        Util.addTransparentBlock(Material.FENCE_GATE);
        Util.addTransparentBlock(Material.IRON_FENCE);
        Util.addTransparentBlock(Material.NETHER_FENCE);

        /* Ladders, Signs */
        Util.addTransparentBlock(Material.LADDER);
        Util.addTransparentBlock(Material.SIGN_POST);
        Util.addTransparentBlock(Material.WALL_SIGN);

        /* Bed */
        Util.addTransparentBlock(Material.BED_BLOCK);

        /* Pistons */
        Util.addTransparentBlock(Material.PISTON_EXTENSION);
        Util.addTransparentBlock(Material.PISTON_MOVING_PIECE);
        Util.addTransparentBlock(Material.RAILS);

        /* Torch & Trapdoor */
        Util.addTransparentBlock(Material.TORCH);
        Util.addTransparentBlock(Material.TRAP_DOOR);

        /* New */
        Util.addTransparentBlock(Material.BREWING_STAND);
        Util.addTransparentBlock(Material.WOODEN_DOOR);
        Util.addTransparentBlock(Material.WOOD_STEP);
    }

    public static boolean isTransparent(Material m) {
        final boolean trans = Util.transparent.contains(m);
        return trans;
    }

    public static void addTransparentBlock(Material m) {
        if (Util.transparent.add(m) == false) {
            System.out.println("Already added as transparent: " + m.toString());
        }
        if (!m.isBlock()) {
            System.out.println(m + " is not a block!");
        }
    }

    public static void parseColours(YamlConfiguration config) {
        final Set<String> keys = config.getKeys(true);

        for (final String key: keys) {
            String filtered = config.getString(key);
            if (filtered.startsWith("MemorySection")) {
                continue;
            }
            filtered = ChatColor.translateAlternateColorCodes('&', filtered);
            config.set(key, filtered);
        }
    }

    /**
     * Returns true if the given block could be used to make a shop out of.
     * 
     * @param b
     *            The block to check. Possibly a chest, dispenser, etc.
     * @return True if it can be made into a shop, otherwise false.
     */
    public static boolean canBeShop(Block b) {
        final BlockState bs = b.getState();
        if (bs instanceof InventoryHolder == false) {
            return false;
        }
        return Util.shoppables.contains(bs.getType());
    }

    /**
     * Gets the percentage (Without trailing %) damage on a tool.
     * 
     * @param item
     *            The ItemStack of tools to check
     * @return The percentage 'health' the tool has. (Opposite of total damage)
     */
    public static String getToolPercentage(ItemStack item) {
        final double dura = item.getDurability();
        final double max = item.getType().getMaxDurability();

        final DecimalFormat formatter = new DecimalFormat("0");
        return formatter.format((1 - dura / max) * 100.0);
    }

    /**
     * Returns the chest attached to the given chest. The given block must be a
     * chest.
     * 
     * @param b
     *            The chest to check.
     * @return the block which is also a chest and connected to b.
     */
    public static Block getSecondHalf(Block b) {
        if (b.getType().toString().contains("CHEST") == false) {
            return null;
        }

        final Block[] blocks = new Block[4];
        blocks[0] = b.getRelative(1, 0, 0);
        blocks[1] = b.getRelative(-1, 0, 0);
        blocks[2] = b.getRelative(0, 0, 1);
        blocks[3] = b.getRelative(0, 0, -1);

        for (final Block c: blocks) {
            if (c.getType() == b.getType()) {
                return c;
            }
        }

        return null;
    }

    /**
     * Converts a string into an item from the database.
     * 
     * @param itemString
     *            The database string. Is the result of makeString(ItemStack
     *            item).
     * @return A new itemstack, with the properties given in the string
     */
    public static ItemStack makeItem(String itemString) {
        final String[] itemInfo = itemString.split(":");

        final ItemStack item = new ItemStack(Material.getMaterial(itemInfo[0]));
        final MaterialData data = new MaterialData(Integer.parseInt(itemInfo[1]));
        item.setData(data);
        item.setDurability(Short.parseShort(itemInfo[2]));
        item.setAmount(Integer.parseInt(itemInfo[3]));

        for (int i = 4; i < itemInfo.length; i = i + 2) {
            int level = Integer.parseInt(itemInfo[i + 1]);

            final Enchantment ench = Enchantment.getByName(itemInfo[i]);
            if (ench == null) {
                continue; // Invalid
            }
            if (ench.canEnchantItem(item)) {
                if (level <= 0) {
                    continue;
                }
                level = Math.min(ench.getMaxLevel(), level);

                item.addEnchantment(ench, level);
            }

        }
        return item;
    }

    public static String serialize(ItemStack iStack) {
        final YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("item", iStack);
        return cfg.saveToString();
    }

    public static ItemStack deserialize(String config) throws InvalidConfigurationException {
        final YamlConfiguration cfg = new YamlConfiguration();
        cfg.loadFromString(config);
        final ItemStack stack = cfg.getItemStack("item");
        return stack;
    }

    /**
     * Fetches an ItemStack's name - For example, converting INK_SAC:11 to
     * Dandellion Yellow, or WOOL:14 to Red Wool
     * 
     * @param i
     *            The itemstack to fetch the name of
     * @return The human readable item name.
     */
    public static String getName(ItemStack i) {
        if (i.getType() == Material.POTION) {
            ItemMeta meta = i.getItemMeta();
            String name = null;
            if(meta instanceof PotionMeta){
            name = getPotionName(i);}
            if (name != null) {
                return name;
            }
        } else if (i.getType().isRecord()) {
            return getRecordName(i.getType());
        } else if (i.getType() == Material.MAP) {
            return StringTranslator.getName(i) + " #" + i.getDurability();
        }
        
        return StringTranslator.getName(i);
    }
    
    public static Map<String, Object> getData(ItemStack i) {
        
        Map<String, Object> result = Maps.newHashMap();
        switch (i.getType()) {
        case MAP: {
            MapView map = Bukkit.getMap(i.getDurability());
            result.put("Location", String.format("%d,%d in %s", map.getCenterX(), map.getCenterZ(), map.getWorld().getName()));
            result.put("Scale", map.getScale());
            
            break;
        }
        default:
            break;
        }
        
        return result;
    }

    /**
     * Converts a name like IRON_INGOT into Iron Ingot to improve readability
     * 
     * @param ugly
     *            The string such as IRON_INGOT
     * @return A nicer version, such as Iron Ingot
     * 
     *         Credits to mikenon on GitHub!
     */
    public static String prettifyText(String ugly) {
        if (!ugly.contains("_") && (!ugly.equals(ugly.toUpperCase()))) {
            return ugly;
        }
        String fin = "";
        ugly = ugly.toLowerCase();
        if (ugly.contains("_")) {
            final String[] splt = ugly.split("_");
            int i = 0;
            for (final String s: splt) {
                i += 1;
                if (s.isEmpty())
                    continue;

                if (s.length() == 1)
                    fin += Character.toUpperCase(s.charAt(0));
                else
                    fin += Character.toUpperCase(s.charAt(0)) + s.substring(1);

                if (i < splt.length) {
                    fin += " ";
                }
            }
        } else {
            fin += Character.toUpperCase(ugly.charAt(0)) + ugly.substring(1);
        }
        return fin;
    }

    public static String toRomain(Integer value) {
        return Util.toRoman(value.intValue());
    }

    private static final String[] ROMAN   = {"X", "IX", "V", "IV", "I"};
    private static final int[]    DECIMAL = {10, 9, 5, 4, 1};

    /**
     * Converts the given number to roman numerals. If the number is less than or equal to 40
     * or greater than or equal 0, it will just return the number as a string.
     * 
     * @param n
     *            The number to convert
     * @return The roman numeral representation of this number, or the number in
     *         decimal form as a string if {@code n <= 0 || n >= 40}.
     */
    public static String toRoman(int n) {
        if (n <= 0 || n >= 40) {
            return "" + n;
        }
        String roman = "";

        for (int i = 0; i < Util.ROMAN.length; i++) {
            while (n >= Util.DECIMAL[i]) {
                n -= Util.DECIMAL[i];
                roman += Util.ROMAN[i];
            }
        }

        return roman;
    }
    
    private static String getRecordName(Material record) {
        switch(record) {
        case GOLD_RECORD:
            return "Record - 13";
        case GREEN_RECORD:
            return "Record - cat";
        case RECORD_3:
            return "Record - blocks";
        case RECORD_4:
            return "Record - chirp";
        case RECORD_5:
            return "Record - far";
        case RECORD_6:
            return "Record - mall";
        case RECORD_7:
            return "Record - mellohi";
        case RECORD_8:
            return "Record - stal";
        case RECORD_9:
            return "Record - strad";
        case RECORD_10:
            return "Record - ward";
        case RECORD_11:
            return "Record - 11";
        case RECORD_12:
            return "Record - wait";
        default:
            throw new AssertionError("Unknown record " + record);
        }
    }

    private static String getPotionName(ItemStack item) {
        PotionMeta meta = null;
        String prefix = "MAIN-";
        if (item.getType() == Material.POTION) {
            if (item.getItemMeta() instanceof PotionMeta) {
                meta = (PotionMeta) item.getItemMeta();
                prefix += "Potion";
            } else {
                return "Water Bottle";
            }
        }
        if (item.getType() == Material.SPLASH_POTION) {
            if (item.getItemMeta() instanceof PotionMeta) {
                meta = (PotionMeta) item.getItemMeta();
                prefix += "Splash Potion";
            } else {
                return "Splasn Water Bottle";
            }
        }
        if (item.getType() == Material.LINGERING_POTION) {
            if (item.getItemMeta() instanceof PotionMeta) {
                meta = (PotionMeta) item.getItemMeta();
                prefix += "Lingering Potion";
            } else {
                return "Lingering Water Bottle";
            }
        }
        if(meta == null){
            return prefix+"NO Potion Data ";
        }
        if(meta.getBasePotionData().isExtended()){
            prefix += "Extended Duration ";
        }
        if(meta.getBasePotionData().isUpgraded()){
            prefix += "Amplified Effect ";
        }
        prefix +=  meta.getDisplayName();
        boolean noEffects = false;
        List<PotionEffect> potionEffects = meta.getCustomEffects();
        noEffects = potionEffects.isEmpty();
        if (!noEffects)  {
            PotionEffect maineffect = potionEffects.get(0);
            prefix += maineffect.getType().getName() + ",Duration="+maineffect.getDuration()+ "Amplifier=" + maineffect.getAmplifier();
            String effects = "Full Effect List -- ";
            for (final PotionEffect effect: potionEffects) {
                effects += " Effect:"+effect.getType().getName()+" Amp:" +effect.getAmplifier() + " Dur:"+effect.getDuration();
            }
            return prefix + effects;
        }
        return null;
    }

    /**
     * @param mat
     *            The material to check
     * @return Returns true if the item is a tool (Has durability) or false if
     *         it doesn't.
     */
    public static boolean isTool(Material mat) {
        return Util.tools.contains(mat);
    }

    /**
     * Compares two items to each other. Returns true if they match.
     * 
     * @param stack1
     *            The first item stack
     * @param stack2
     *            The second item stack
     * @return true if the itemstacks match. (Material, durability, enchants)
     */
    public static boolean matches(ItemStack stack1, ItemStack stack2) {
        if (stack1 == stack2) {
            return true; // Referring to the same thing, or both are null.
        }
        if (stack1 == null || stack2 == null) {
            return false; // One of them is null (Can't be both, see above)
        }

        if (stack1.getType() != stack2.getType()) {
            return false; // Not the same material
        }
        if (stack1.getDurability() != stack2.getDurability()) {
            return false; // Not the same durability
        }
        if (!stack1.getEnchantments().equals(stack2.getEnchantments())) {
            return false; // They have the same enchants
        }

        try {
            Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
            final boolean book1 = stack1.getItemMeta() instanceof EnchantmentStorageMeta;
            final boolean book2 = stack2.getItemMeta() instanceof EnchantmentStorageMeta;
            if (book1 != book2) {
                return false;// One has enchantment meta, the other does not.
            }
            if (book1 == true) { // They are the same here (both true or both
                                 // false). So if one is true, the other is
                                 // true.
                final Map<Enchantment, Integer> ench1 = ((EnchantmentStorageMeta) stack1.getItemMeta())
                        .getStoredEnchants();
                final Map<Enchantment, Integer> ench2 = ((EnchantmentStorageMeta) stack2.getItemMeta())
                        .getStoredEnchants();
                if (!ench1.equals(ench2)) {
                    return false; // Enchants aren't the same.
                }
            }
        } catch (final ClassNotFoundException e) {
            // Nothing. They dont have a build high enough to support this.
        }

        return true;
    }

    /**
     * Formats the given number according to how vault would like it.
     * E.g. $50 or 5 dollars.
     * @param n a double representing the number to be formatted
     * @return The formatted string.
     */
    public static String format(double n) {
        try {
            return Util.plugin.getEcon().format(n);
        } catch (final NumberFormatException e) {
            return "$" + n;
        }
    }

    /**
     * @param m The material to check if it is blacklisted
     * @return true if the material is black listed. False if not.
     */
    public static boolean isBlacklisted(Material m) {
        return Util.blacklist.contains(m);
    }

    /**
     * Fetches the block which the given sign is attached to
     * 
     * @param b The Block the sign is attached
     * @return The block the sign is attached to
     */
    public static Block getAttached(Block b) {
        try {
            final Sign sign = (Sign) b.getState().getData(); // Throws a NPE
                                                             // sometimes??
            final BlockFace attached = sign.getAttachedFace();

            if (attached == null) {
                return null;
            }
            return b.getRelative(attached);
        } catch (final NullPointerException e) {
            return null; // /Not sure what causes this.
        }
    }

    /**
     * Counts the number of items in the given inventory where
     * Util.matches(inventory item, item) is true.
     * 
     * @param inv
     *            The inventory to search
     * @param item
     *            The ItemStack to search for
     * @return The number of items that match in this inventory.
     */
    public static int countItems(Inventory inv, ItemStack item) {
        int items = 0;
        for (final ItemStack iStack: inv.getContents()) {
            if (iStack == null) {
                continue;
            }
            if (Util.matches(item, iStack)) {
                items += iStack.getAmount();
            }
        }
        return items;
    }

    /**
     * Returns the number of items that can be given to the inventory safely.
     * 
     * @param inv
     *            The inventory to count
     * @param item
     *            The item prototype. Material, durabiltiy and enchants must
     *            match for 'stackability' to occur.
     * @return The number of items that can be given to the inventory safely.
     */
    public static int countSpace(Inventory inv, ItemStack item) {
        int space = 0;
        for (final ItemStack iStack: inv.getContents()) {
            if (iStack == null || iStack.getType() == Material.AIR) {
                space += item.getMaxStackSize();
            } else if (Util.matches(item, iStack)) {
                space += item.getMaxStackSize() - iStack.getAmount();
            }
        }
        return space;
    }

    /**
     * Returns true if the given location is loaded or not.
     * 
     * @param loc
     *            The location
     * @return true if the given location is loaded or not.
     */
    public static boolean isLoaded(Location loc) {
        // System.out.println("Checking isLoaded(Location loc)");
        if (loc.getWorld() == null) {
            // System.out.println("Is not loaded. (No world)");
            return false;
        }
        // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
        // location rounded to the nearest 16.
        final int x = (int) Math.floor((loc.getBlockX()) / 16.0);
        final int z = (int) Math.floor((loc.getBlockZ()) / 16.0);

        // System.out.println("Chunk is loaded " + x + ", " + z);
// System.out.println("Chunk is NOT loaded " + x + ", " + z);
        return loc.getWorld().isChunkLoaded(x, z);
    }
}
