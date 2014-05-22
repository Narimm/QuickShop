package org.maxgamer.QuickShop.Util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NMS {
    private static ArrayList<NMSDependent> dependents = new ArrayList<NMSDependent>();
    /** The ID for shop item names */
    private static int                     nextId     = 0;
    static {
        NMSDependent dep;

        /* ***********************
         * ** 1.6.1 ** *
         * **********************
         */
        dep = new NMSDependent("v1_7_R2") {
            @Override
            public void safeGuard(Item item) {
                ItemStack iStack = item.getItemStack();

                // Fetch the NMS item
                final net.minecraft.server.v1_7_R2.ItemStack nmsI = org.bukkit.craftbukkit.v1_7_R2.inventory.CraftItemStack
                        .asNMSCopy(iStack);
                // Force the count to 0, don't notify anything though.
                nmsI.count = 0;
                // Get the itemstack back as a bukkit stack
                iStack = org.bukkit.craftbukkit.v1_7_R2.inventory.CraftItemStack.asBukkitCopy(nmsI);

                // Set the display item to the stack.
                item.setItemStack(iStack);
            }

            @Override
            public byte[] getNBTBytes(ItemStack iStack) {
                final net.minecraft.server.v1_7_R2.ItemStack is = org.bukkit.craftbukkit.v1_7_R2.inventory.CraftItemStack
                        .asNMSCopy(iStack);
                // Save the NMS itemstack to a new NBT tag
                net.minecraft.server.v1_7_R2.NBTTagCompound itemCompound = new net.minecraft.server.v1_7_R2.NBTTagCompound();
                itemCompound = is.save(itemCompound);

                // Convert the NBT tag to a byte[]
                return net.minecraft.server.v1_7_R2.NBTCompressedStreamTools.a(itemCompound);
            }

            @Override
            public ItemStack getItemStack(byte[] bytes) {
                final net.minecraft.server.v1_7_R2.NBTTagCompound c = net.minecraft.server.v1_7_R2.NBTCompressedStreamTools
                        .a(bytes);
                final net.minecraft.server.v1_7_R2.ItemStack is = net.minecraft.server.v1_7_R2.ItemStack.createStack(c);
                return org.bukkit.craftbukkit.v1_7_R2.inventory.CraftItemStack.asBukkitCopy(is);
            }
        };
        NMS.dependents.add(dep);
    }

    /**
     * The known working NMSDependent. This will be null if we haven't found one
     * yet.
     */
    private static NMSDependent            nms;

    /**
     * Sets the given items stack size to 0,
     * as well as gives it a custom NBT tag
     * called "quickshop" in the root, to
     * prevent it from merging with other
     * items. This is all through NMS code.
     * 
     * @param item
     *            The given item
     * @throws ClassNotFoundException
     */
    public static void safeGuard(Item item) throws ClassNotFoundException {
        NMS.rename(item.getItemStack());
        NMS.protect(item);
        item.setPickupDelay(Integer.MAX_VALUE);
    }

    /**
     * Renames the given itemstack to ChatColor.RED + "QuickShop " +
     * Util.getName(iStack).
     * This prevents items stacking (Unless, ofcourse, the other item has a
     * jerky name too - Rare)
     * 
     * @param iStack
     *            the itemstack to rename.
     */
    private static void rename(ItemStack iStack) {
        // This stops it merging with other items. * Unless they're named
        // funnily... In which case, shit.
        final ItemMeta meta = iStack.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "QuickShop " + Util.getName(iStack) + " " + NMS.nextId++);
        iStack.setItemMeta(meta);
    }

    /**
     * Returns a byte array of the given ItemStack.
     * 
     * @param iStack
     *            The given itemstack
     * @return The compressed NBT tag version of the given stack.
     *         Will return null if used with an invalid Bukkit version.
     * @throws ClassNotFoundException
     *             if QS is not updated to this build of bukkit.
     */
    public static byte[] getNBTBytes(ItemStack iStack) throws ClassNotFoundException {
        NMS.validate();
        return NMS.nms.getNBTBytes(iStack);
    }

    /**
     * Converts the given bytes into an itemstack
     * 
     * @param bytes
     *            The bytes to convert to an itemstack
     * @return The itemstack
     * @throws ClassNotFoundException
     *             if QS is not updated to this build of bukkit.
     */
    public static ItemStack getItemStack(byte[] bytes) throws ClassNotFoundException {
        NMS.validate();
        return NMS.nms.getItemStack(bytes);
    }

    private static void protect(Item item) {
        try {
            final Field itemField = item.getClass().getDeclaredField("item");
            itemField.setAccessible(true);

            final Object nmsEntityItem = itemField.get(item);

            Method getItemStack;
            try {
                getItemStack = nmsEntityItem.getClass().getMethod("getItemStack");
            } catch (final NoSuchMethodException e) {
                getItemStack = nmsEntityItem.getClass().getMethod("d"); // Obfuscated
                                                                        // for
                                                                        // 'getItemStack'
            }

            final Object itemStack = getItemStack.invoke(nmsEntityItem);

            Field countField;

            try {
                countField = itemStack.getClass().getDeclaredField("count");
            } catch (final NoSuchFieldException e) {
                countField = itemStack.getClass().getDeclaredField("a"); // 'count'
                                                                         // is
                                                                         // called
                                                                         // 'a'
                                                                         // in
                                                                         // obfuscated
                                                                         // code
                                                                         // (...ForgeModLoader)
            }
            countField.setAccessible(true);
            countField.set(itemStack, 0);
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
            System.out.println("[QuickShop] Could not protect item from pickup properly! Dupes are now possible.");
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Finds the proper NMS version. If a version is already
     * selected, this method does nothing, even if the version
     * is invalid.
     * 
     * @throws ClassNotFoundException
     *             If there is no found working version.
     */
    private static void validate() throws ClassNotFoundException {
        if (NMS.nms != null) {
            return;
        }

        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        packageName = packageName.substring(packageName.lastIndexOf(".") + 1);
        System.out.println("Package: " + packageName);
        for (final NMSDependent dep: NMS.dependents) { // TODO I should put this
                                                       // in a map, it would be
                                                       // faster (But is it
                                                       // worth it?)
            // If this NMS version is the bukkit version, OR, both are for the
            // OLD way of doing things...
            if (dep.getVersion().equals(packageName)
                    || (dep.getVersion().isEmpty() && (packageName.equals("bukkit") || packageName
                            .equals("craftbukkit")))) { // We could be using
                                                        // either CB or Bukkit.
                NMS.nms = dep;
                return;
            }
        }
        throw new ClassNotFoundException("This version of QuickShop is incompatible."); // We
                                                                                        // haven't
                                                                                        // got
                                                                                        // code
                                                                                        // to
                                                                                        // support
                                                                                        // your
                                                                                        // version!
    }

    private static abstract class NMSDependent {
        private final String version;

        public String getVersion() {
            return version;
        }

        public NMSDependent(String version) {
            this.version = version;
        }

        public abstract void safeGuard(Item item);

        public abstract byte[] getNBTBytes(ItemStack iStack);

        public abstract ItemStack getItemStack(byte[] bytes);
    }
}