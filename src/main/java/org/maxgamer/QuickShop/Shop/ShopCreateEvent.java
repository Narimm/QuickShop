package org.maxgamer.QuickShop.Shop;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Shop               shop;
    private boolean                  cancelled;

    private final Player             p;

    public ShopCreateEvent(Shop shop, Player p) {
        this.shop = shop;
        this.p = p;
    }

    /**
     * The shop to be created
     * 
     * @return The shop to be created
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * The player who is creating this shop
     * 
     * @return The player who is creating this shop
     */
    public Player getPlayer() {
        return p;
    }

    @Override
    public HandlerList getHandlers() {
        return ShopCreateEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return ShopCreateEvent.handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}