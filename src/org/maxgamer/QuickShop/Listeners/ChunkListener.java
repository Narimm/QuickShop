package org.maxgamer.QuickShop.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.maxgamer.QuickShop.QuickShop;
import org.maxgamer.QuickShop.Shop.DisplayItem;
import org.maxgamer.QuickShop.Shop.Shop;
import org.maxgamer.QuickShop.Shop.ShopChunk;


public class ChunkListener implements Listener{
	private QuickShop plugin;
	//public HashMap<Chunk, List<Shop>> chunkMap = new HashMap<Chunk, List<Shop>>(10);
	
	public ChunkListener(QuickShop plugin){
		this.plugin = plugin;
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void onChunkLoad(ChunkLoadEvent e){
		//Testing
		Chunk c = e.getChunk();
		if(plugin.getShops() == null) return;
		
		HashMap<Location, Shop> inChunk = plugin.getShopsInChunk(c);
		
		if(inChunk == null) return;
		
		for(Shop shop : inChunk.values()){
			DisplayItem disItem = shop.getDisplayItem();
			disItem.removeDupe();
			disItem.remove();
			disItem.spawn();
			
			plugin.debug("Chunk loading spawning item: " + disItem.getItem().getItemStack().getType());
		}
		
		
		/*
		List<Shop> shops = new ArrayList<Shop>(5);
		
		for(Shop shop : plugin.getShops().values()){
			Location loc = shop.getLocation();
			if(		loc.getWorld() != null &&
					loc.getChunk().getX() == c.getX() &&
					loc.getChunk().getZ() == c.getZ()){
				shops.add(shop);
				
				DisplayItem disItem = shop.getDisplayItem();
				disItem.removeDupe();
				disItem.remove();
				disItem.spawn();
				plugin.debug("Chunk loading spawning item: " + disItem.getItem().getItemStack().getType());
			}
		}
		this.chunkMap.put(c, shops); */
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onChunkUnload(ChunkUnloadEvent e){
		Chunk c = e.getChunk();
		
		//ShopChunk shopChunk = new ShopChunk(c.getWorld(), c.getX(), c.getZ());
		
		HashMap<Location, Shop> inChunk = plugin.getShopsInChunk(c);
		
		if(inChunk == null) return;
		
		for(Shop shop : inChunk.values()){
			DisplayItem disItem = shop.getDisplayItem();
			disItem.removeDupe();
			disItem.remove();
			
			plugin.debug("Chunk unloading unspawning item: " + disItem.getItem().getItemStack().getType());
		}
		
		/*
		List<Shop> shops = this.chunkMap.get(c);
		if(shops == null) return;
		for(Shop shop : shops){
			DisplayItem disItem = shop.getDisplayItem();
			disItem.removeDupe();
			disItem.remove();
			plugin.debug("Chunk loading spawning item: " + disItem.getItem().getItemStack().getType());
		}
		this.chunkMap.remove(c);*/
		/*
		if(plugin.getShops() == null) return;
		for(Shop shop : plugin.getShops().values()){
			if(shop.getLocation().getChunk().equals(c)){
				DisplayItem disItem = shop.getDisplayItem();
				disItem.removeDupe();
				disItem.remove();
				disItem.spawn();
				
			}
		}*/
	}
}