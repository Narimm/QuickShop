package org.maxgamer.QuickShop.Listeners;

import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.Shop;

public class ChunkListener implements Listener {
    private final QuickShop plugin;

    public ChunkListener(QuickShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoad(ChunkLoadEvent e) {
        final Chunk c = e.getChunk();
        if (plugin.getShopManager().getShops() == null) {
            return;
        }

        final HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(c);

        if (inChunk == null) {
            return;
        }

        for (final Shop shop: inChunk.values()) {
            shop.onLoad();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent e) {
        final Chunk c = e.getChunk();

        final HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(c);

        if (inChunk == null) {
            return;
        }
        for (final Shop shop: inChunk.values()) {
            shop.onUnload();
        }
    }
}