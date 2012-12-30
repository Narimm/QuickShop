package org.maxgamer.QuickShop;

import java.io.File;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.maxgamer.QuickShop.Shop.Shop;

public class MsgUtil{
	private static QuickShop plugin;
	private static YamlConfiguration messages;
	
	static{
		plugin = QuickShop.instance;
	}
	
	public static void loadMessages(){
		//Load messages.yml
		File messageFile = new File(plugin.getDataFolder(), "messages.yml");
		if(!messageFile.exists()){
			plugin.getLogger().info("Creating messages.yml");
			plugin.saveResource("messages.yml", true);
		}
		
		//Store it
		messages = YamlConfiguration.loadConfiguration(messageFile);
		messages.options().copyDefaults(true);
		
		//Load default messages
		InputStream defMessageStream = plugin.getResource("messages.yml");
		YamlConfiguration defMessages = YamlConfiguration.loadConfiguration(defMessageStream);
		messages.setDefaults(defMessages);
		
		//Parse colour codes
		Util.parseColours(messages);
	}

	/**
	 * @param player The name of the player to message
	 * @param message The message to send them
	 * Sends the given player a message if they're online.  
	 * Else, if they're not online, queues it for them in the database.
	*/
	public static void send(String player, String message){
		Player p = Bukkit.getPlayerExact(player);
		if(p == null){
			player = player.toLowerCase();		
			String q = "INSERT INTO messages (owner, message, time) VALUES (?, ?, ?)";
			plugin.getDB().execute(q, player, message, System.currentTimeMillis());
		}
		else{
			p.sendMessage(message);
		}
	}
	
	/**
	 * Empties the queue of messages a player has and sends them to the player.
	 * Loads the messages from the database in a seperate thread. 
	 * @param p The player to message
	 * @param delay The number of ticks to wait before sending the messages
	 * @return true if success, false if the player is offline or null
	 */
	public static boolean flush(Player p, int delay){
		if(p != null && p.isOnline()){
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Messenger(p), delay);
			
			return true;
		}
		return false;
	}
	
	/**
	 * @param player The player whose messages to remove from the database.
	 * Deletes all messages for a player in the database.
	*/
	public static void deleteMessages(String player){
		plugin.getDB().execute("DELETE FROM messages WHERE owner = ?", player.toLowerCase());
	}
	
	/**
	 * @param player The player whose messages you wish to fetch
	 * @return An arraylist of messages in order of time that should be sent to the player.
	 * 
	*/
	public static List<String> getMessages(String player){
		player = player.toLowerCase();
		
		List<String> messages = new ArrayList<String>(5);
		
		String q = "SELECT * FROM messages WHERE owner = '"+plugin.getDB().escape(player)+"' ORDER BY time ASC";
		
		try{
			PreparedStatement ps = plugin.getDB().getConnection().prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				messages.add(rs.getString("message"));
			}
		}
		catch(SQLException e){
			e.printStackTrace();
			plugin.getLogger().info("Could not load messages for " + player);
		}
		return messages;
	}
	
	public static void sendShopInfo(Player p, Shop shop){
		sendShopInfo(p, shop, shop.getRemainingStock());
	}
	
	public static void sendShopInfo(Player p, Shop shop, int stock){
		//Potentially faster with an array?
		ItemStack items = shop.getItem();
		p.sendMessage("");
		p.sendMessage("");
		
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.shop-information"));
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.owner", shop.getOwner()));
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item", shop.getDataName()));
		
		if(Util.isTool(items.getType())){
			p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.damage-percent-remaining", Util.getToolPercentage(items)));
		}
		
		if(shop.isSelling()){
			p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.stock", ""+stock));
		}
		else{
			int space = shop.getRemainingSpace();
			p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.space", ""+space));
		}
		
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.price-per", shop.getDataName(), Util.format(shop.getPrice())));
		
		if(shop.isBuying()){
			p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.this-shop-is-buying"));
		}
		else{
			p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.this-shop-is-selling"));
		}
		
		Map<Enchantment, Integer> enchs = items.getItemMeta().getEnchants();
		if(enchs != null && !enchs.isEmpty()){
			p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------"+MsgUtil.getMessage("menu.enchants")+"-----------------------+");
			for(Entry<Enchantment, Integer> entries : enchs.entrySet()){
				p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey() .getName() + " " + entries.getValue() );
			}
		}
		
		try{
			Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
			
			if(items.getItemMeta() instanceof EnchantmentStorageMeta){
				EnchantmentStorageMeta stor = (EnchantmentStorageMeta) items.getItemMeta();
				stor.getStoredEnchants();
				
				enchs = stor.getStoredEnchants();
				if(enchs != null && !enchs.isEmpty()){
					p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------"+MsgUtil.getMessage("menu.stored-enchants")+"--------------------+");
					for(Entry<Enchantment, Integer> entries : enchs.entrySet()){
						p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey() .getName() + " " + entries.getValue() );
					}
				}
			}
		}
		catch(ClassNotFoundException e){
			//They don't have an up to date enough build of CB to do this.
			//TODO: Remove this when it becomes redundant
		}
		
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
	}
	
	public static void sendPurchaseSuccess(Player p, Shop shop, int amount){
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.successful-purchase"));
		
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item-name-and-price", ""+amount, shop.getDataName(), Util.format((amount * shop.getPrice()))));
		

		Map<Enchantment, Integer> enchs = shop.getItem().getItemMeta().getEnchants();
		if(enchs != null && !enchs.isEmpty()){
			p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------"+MsgUtil.getMessage("menu.enchants")+"-----------------------+");
			for(Entry<Enchantment, Integer> entries : enchs.entrySet()){
				p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey() .getName() + " " + entries.getValue() );
			}
		}
		
		enchs = shop.getItem().getItemMeta().getEnchants();
		if(enchs != null && !enchs.isEmpty()){
			p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------"+MsgUtil.getMessage("menu.stored-enchants")+"--------------------+");
			for(Entry<Enchantment, Integer> entries : enchs.entrySet()){
				p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey() .getName() + " " + entries.getValue() );
			}
		}
		
		try{
			Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
			
			if(shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta){
				EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
				stor.getStoredEnchants();
				
				enchs = stor.getStoredEnchants();
				if(enchs != null && !enchs.isEmpty()){
					p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------"+MsgUtil.getMessage("menu.stored-enchants")+"--------------------+");
					for(Entry<Enchantment, Integer> entries : enchs.entrySet()){
						p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey() .getName() + " " + entries.getValue() );
					}
				}
			}
		}
		catch(ClassNotFoundException e){
			//They don't have an up to date enough build of CB to do this.
			//TODO: Remove this when it becomes redundant
		}
		
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
	}
	
	public static void sendSellSuccess(Player p, Shop shop, int amount){
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.successfully-sold"));
		p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item-name-and-price", ""+amount, shop.getDataName(), Util.format((amount * shop.getPrice()))));
		
		Map<Enchantment, Integer> enchs = shop.getItem().getItemMeta().getEnchants();
		if(enchs != null && !enchs.isEmpty()){
			p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------"+MsgUtil.getMessage("menu.enchants")+"-----------------------+");
			for(Entry<Enchantment, Integer> entries : enchs.entrySet()){
				p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey() .getName() + " " + entries.getValue() );
			}
		}
		
		try{
			Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
			
			if(shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta){
				EnchantmentStorageMeta stor = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
				stor.getStoredEnchants();
				
				enchs = stor.getStoredEnchants();
				if(enchs != null && !enchs.isEmpty()){
					p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------"+MsgUtil.getMessage("menu.stored-enchants")+"-----------------------+");
					for(Entry<Enchantment, Integer> entries : enchs.entrySet()){
						p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey() .getName() + " " + entries.getValue() );
					}
				}
			}
		}
		catch(ClassNotFoundException e){
			//They don't have an up to date enough build of CB to do this.
			//TODO: Remove this when it becomes redundant
		}
		
		p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
	}
	
	public static String getMessage(String loc, String... args){
		String raw = messages.getString(loc);
		
		if(raw == null || raw.isEmpty()){
			return "Invalid message: " + loc;
		}
		if(args == null){
			return raw;
		}
		
		for(int i = 0; i < args.length; i++){
			raw = raw.replace("{"+i+"}", args[i]);
		}
		return raw;
	}
}