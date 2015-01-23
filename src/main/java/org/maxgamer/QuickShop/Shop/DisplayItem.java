package org.maxgamer.QuickShop.Shop;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.maxgamer.QuickShop.QuickShop;

/**
 * @author Netherfoam
 *         A display item, that spawns a block above the chest and cannot be
 *         interacted with.
 */
public class DisplayItem {
    private final Shop      shop;
    private final ItemStack iStack;
    private Item            item;

    // private Location displayLoc;

    /**
     * Creates a new display item.
     * 
     * @param shop
     *            The shop (See Shop)
     * @param iStack
     *            The item stack to clone properties of the display item from.
     */
    public DisplayItem(Shop shop, ItemStack iStack) {
        this.shop = shop;
        this.iStack = iStack.clone();
        // this.displayLoc = shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    /**
     * Spawns the dummy item on top of the shop.
     */
    public void spawn() {
        if (shop.getLocation().getWorld() == null) {
            return;
        }

        final Location dispLoc = getDisplayLocation();

        item = shop.getLocation().getWorld().dropItem(dispLoc, iStack);
        item.setVelocity(new Vector(0, 0.1, 0));

        if (QuickShop.instance.debug) {
            System.out.println("Spawned item. Safeguarding.");
        }
        
        ItemMeta meta = iStack.getItemMeta();
        meta.setDisplayName("quickshop");
        meta.setLore(Arrays.asList(UUID.randomUUID().toString()));
        iStack.setItemMeta(meta);
    }

    /**
     * Spawns the new display item. Does not remove duplicate items.
     */
    public void respawn() {
        remove();
        spawn();
    }

    /**
     * Removes all items floating ontop of the chest
     * that aren't the display item.
     */
    public boolean removeDupe() {
        if (shop.getLocation().getWorld() == null) {
            return false;
        }
        final QuickShop qs = (QuickShop) Bukkit.getPluginManager().getPlugin("QuickShop");
        final Location displayLoc = shop.getLocation().getBlock().getRelative(0, 1, 0).getLocation();

        boolean removed = false;

        final Chunk c = displayLoc.getChunk();
        for (final Entity e: c.getEntities()) {
            if (!(e instanceof Item)) {
                continue;
            }
            if (item != null && e.getEntityId() == item.getEntityId()) {
                continue;
            }
            final Location eLoc = e.getLocation().getBlock().getLocation();

            if (eLoc.equals(displayLoc) || eLoc.equals(shop.getLocation())) {
                final ItemStack near = ((Item) e).getItemStack();
                // if its the same its a dupe
                if (shop.matches(near)) {
                    e.remove();
                    removed = true;
                    if (qs.debug) {
                        System.out.println("Removed rogue item: " + near.getType());
                    }
                }
            }
        }
        return removed;
    }

    /**
     * Removes the display item.
     */
    public void remove() {
        if (item == null) {
            return;
        }
        item.remove();
    }

    /**
     * @return Returns the exact location of the display item. (1 above shop
     *         block, in the center)
     */
    public Location getDisplayLocation() {
        return shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    /**
     * Returns the reference to this shops item. Do not modify.
     */
    public Item getItem() {
        return item;
    }
}