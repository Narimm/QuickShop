package org.maxgamer.QuickShop.Shop;

public class ShopChunk {
    private final String world;
    private final int    x;
    private final int    z;
    private int          hash;

    public ShopChunk(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
        hash = this.x * this.z; // We don't need to use the world's hash, as
                                // these are seperated by world in memory
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        } else {
            final ShopChunk shopChunk = (ShopChunk) obj;
            return (getWorld().equals(shopChunk.getWorld()) && getX() == shopChunk.getX() && getZ() == shopChunk.getZ());
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }

}
