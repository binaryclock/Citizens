package com.fullwall.Citizens.Traders;

import java.util.HashMap;

import net.minecraft.server.InventoryPlayer;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.fullwall.Citizens.Citizens;
import com.fullwall.Citizens.Economy.EconomyHandler;
import com.fullwall.Citizens.Traders.TraderInterface.Mode;
import com.fullwall.Citizens.Utils.MessageUtils;
import com.fullwall.Citizens.Utils.StopWatch;
import com.fullwall.Citizens.Utils.StringUtils;
import com.fullwall.resources.redecouverte.NPClib.HumanNPC;

public class TraderTask implements Runnable {
	private HumanNPC npc;
	private CraftPlayer player;
	private int taskID;
	private Citizens plugin;
	private PlayerInventory previousNPCInv;
	private PlayerInventory previousPlayerInv;
	private Mode mode;

	private boolean debug = true;
	
	public TraderTask(HumanNPC NPC, Player player, Citizens plugin, Mode mode) {
		this.npc = NPC;
		this.player = (CraftPlayer) player;
		this.plugin = plugin;
		
		// Create the inventory objects
		previousNPCInv = new CraftInventoryPlayer( new InventoryPlayer( null ) );
		previousPlayerInv = new CraftInventoryPlayer( new InventoryPlayer( null ) );
		
		// clone the items to the newly created inventory objects
		clonePlayerInventory( npc.getBukkitEntity().getInventory(), this.previousNPCInv );
		clonePlayerInventory( player.getInventory(), this.previousPlayerInv );
		
		this.mode = mode;
		sendJoinMessage();
	}
	private void log( String msg )
	{
		if ( debug == true )
			System.out.println( "Citizens: TraderTask." + msg );
	}
	/*
	 * cloneItemStack
	 * - Creates a new copy of the ItemStack from the source.
	 * 
	 * @ source - the source to be cloned
	 * 
	 * returns: the cloned ItemStack
	 */
	private ItemStack cloneItemStack( ItemStack source )
	{
		if ( source == null ) // sanity check
			return null;
		
		/* might have to add different ways to create ItemStacks depending on which item is being cloned,
		 * but this should take care of almost all items.
		 */
		ItemStack clone = new ItemStack( source.getType(),
							   source.getAmount(),
				               source.getDurability(),
							   ( source.getData() != null ? source.getData().getData() : null ) );
		return clone;
	}
	/*
	 * clonePlayerInventory
	 * - Clones all ItemStack objects inside the source PlayerInventory object to the target PlayerInventory
	 * object.
	 * 
	 * @ source - the source to be cloned
	 * @ target - the target ItemStack
	 * 
	 * returns: nothing
	 */
	private void clonePlayerInventory( PlayerInventory source, PlayerInventory target )
	{
		log( "clonePlayerInventory() cloning PlayerInventory." );
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();
		
		ItemStack[] contents = new ItemStack[ source.getContents().length ];
		System.arraycopy( source.getContents(), 0, contents, 0, contents.length );
		target.setContents( contents );
		
		target.setHelmet( cloneItemStack( source.getHelmet() ) );
		target.setChestplate( cloneItemStack( source.getChestplate() ) );
		target.setLeggings( cloneItemStack( source.getLeggings() ) );
		target.setBoots( cloneItemStack( source.getBoots() ) );
		target.setItemInHand( cloneItemStack( source.getItemInHand() ) );
		
		stopwatch.stop();
		log( "clonePlayerInventory() finished cloning PlayerInventory - took: " + stopwatch.getElapsedTime() );
	}

	public void addID(int ID) {
		this.taskID = ID;
	}

	public void kill() {
		npc.setFree(true);
		plugin.getServer().getScheduler().cancelTask(taskID);
		sendLeaveMessage();
		int index = TraderInterface.tasks.indexOf(taskID);
		if (index != -1)
			TraderInterface.tasks.remove(TraderInterface.tasks.indexOf(taskID));
	}

	@Override
	public void run() {
		if (npc == null
				|| player == null
				|| player.getHandle().activeContainer == player.getHandle().defaultContainer) {
			kill();
			return;
		}
		// If the player cursor is empty (no itemstack in it).
		if (player.getHandle().inventory.j() == null)
			return;
		if (mode == Mode.STOCK)
			return;
		int count = 0;
		boolean found = false;
		for (ItemStack i : npc.getBukkitEntity().getInventory().getContents()) {
			if (!(previousNPCInv.getItem(count).equals(i))) {
				found = true;
				handleNPCItemClicked(count);
				break;
			}
			count += 1;
		}
		count = 0;
		if (!found) {
			for (ItemStack i : player.getInventory().getContents()) {
				if (!(previousPlayerInv.getItem(count).equals(i))) {
					handlePlayerItemClicked(count);
					break;
				}
				count += 1;
			}
		}
		
		clonePlayerInventory( npc.getBukkitEntity().getInventory(), this.previousNPCInv );
		clonePlayerInventory( player.getInventory(), this.previousPlayerInv );

		// Set the itemstack in the player's cursor to null.
		player.getHandle().inventory.b((net.minecraft.server.ItemStack) null);
	}

	private void sendJoinMessage() {
		switch (mode) {
		case INFINITE:
		case NORMAL:
			player.sendMessage(ChatColor.GREEN + "Transaction log");
			player.sendMessage(ChatColor.GOLD
					+ "-------------------------------");
			break;
		case STOCK:
			player.sendMessage(ChatColor.GOLD
					+ "Stocking of "
					+ StringUtils.yellowify(npc.getSpacedName(), ChatColor.GOLD)
					+ " started.");
			break;
		}
	}

	private void sendLeaveMessage() {
		switch (mode) {
		case INFINITE:
		case NORMAL:
			player.sendMessage(ChatColor.GOLD
					+ "-------------------------------");
			break;
		case STOCK:
			player.sendMessage(ChatColor.GOLD
					+ "Stocking of "
					+ StringUtils.yellowify(npc.getSpacedName(), ChatColor.GOLD)
					+ " finished.");
			break;
		}
	}

	private void handleNPCItemClicked(int slot) {
		npc.getBukkitEntity().getInventory()
				.setItem(slot, previousNPCInv.getItem(slot));
		ItemStack i = npc.getBukkitEntity().getInventory().getItem(slot);
		if (!(npc.getTraderNPC().isBuyable(i.getTypeId()))) {
			player.sendMessage(StringUtils.yellowify(i.getType().name(),
					ChatColor.RED) + " isn't being sold here.");
			return;
		}
		Buyable buyable = npc.getTraderNPC().getBuyable(i.getTypeId());
		int amount = npc.getBukkitEntity().getInventory().getItem(slot)
				.getAmount();
		if (amount - buyable.getBuying().getAmount() <= 0) {
			player.sendMessage(ChatColor.RED
					+ "Need at least "
					+ StringUtils.yellowify(buyable.getBuying().getAmount()
							+ " " + buyable.getBuying().getType().name(),
							ChatColor.RED) + "(s) on the clicked stack.");
			return;
		}
		if (!EconomyHandler.canBuy(buyable.getPrice(), player)) {
			player.sendMessage(ChatColor.RED
					+ "You don't have enough money to buy "
					+ StringUtils.yellowify(buyable.getBuying().getAmount()
							+ " " + buyable.getBuying().getType().name(),
							ChatColor.RED) + "(s).");
			return;
		}
		ItemStack transfer = buyable.getBuying();
		HashMap<Integer, ItemStack> unbought = player.getInventory().addItem(
				transfer);
		if (unbought.size() >= 1) {
			player.getInventory().setContents(previousPlayerInv.getContents());
			player.sendMessage(ChatColor.RED
					+ "You don't have enough room in your inventory to add "
					+ StringUtils.yellowify(buyable.getBuying().getAmount()
							+ " " + buyable.getBuying().getType().name(),
							ChatColor.RED) + "(s).");
			return;
		}
		EconomyHandler.pay(buyable.getPrice(), player);
		player.sendMessage(ChatColor.GREEN
				+ "Bought "
				+ StringUtils
						.yellowify(buyable.getBuying().getAmount() + " "
								+ buyable.getBuying().getType().name(),
								ChatColor.GREEN)
				+ "(s) at "
				+ StringUtils.yellowify(
						MessageUtils.getPriceMessage(buyable.getPrice()),
						ChatColor.GREEN) + " per stack.");
		npc.getBukkitEntity()
				.getInventory()
				.setContents(
						sortInventory(npc.getBukkitEntity().getInventory()));
	}

	private void handlePlayerItemClicked(int slot) {
		player.getInventory().setItem(slot, previousPlayerInv.getItem(slot));
		ItemStack i = player.getInventory().getItem(slot);
		if (!npc.getTraderNPC().isSellable(i.getTypeId())) {
			player.sendMessage(StringUtils.yellowify(i.getType().name(),
					ChatColor.RED) + " isn't available for purchase.");
			return;
		}
		Sellable sellable = npc.getTraderNPC().getSellable(i.getTypeId());
		int amount = player.getInventory().getItem(slot).getAmount();
		if (amount - sellable.getSelling().getAmount() <= 0) {
			player.sendMessage(ChatColor.RED
					+ "You need to click on at a stack of at least "
					+ StringUtils.yellowify(sellable.getSelling().getAmount()
							+ " " + sellable.getSelling().getType().name(),
							ChatColor.RED) + "(s).");
			return;
		}
		if (!EconomyHandler.canBuy(sellable.getPrice(), npc)) {
			player.sendMessage(ChatColor.RED
					+ "Not enough money available to buy "
					+ StringUtils.yellowify(sellable.getSelling().getAmount()
							+ " " + sellable.getSelling().getType().name(),
							ChatColor.RED) + "(s).");
			return;
		}
		ItemStack transfer = sellable.getSelling();
		HashMap<Integer, ItemStack> unsold = npc.getBukkitEntity()
				.getInventory().addItem(transfer);
		if (unsold.size() >= 1) {
			npc.getBukkitEntity().getInventory()
					.setContents(previousNPCInv.getContents());
			player.sendMessage(ChatColor.RED
					+ "Not enough room available to add "
					+ StringUtils.yellowify(sellable.getSelling().getAmount()
							+ " " + sellable.getSelling().getType().name(),
							ChatColor.RED) + "(s) to the current stock.");
			return;
		}
		EconomyHandler.pay(sellable.getPrice(), npc);
		player.sendMessage(ChatColor.GREEN
				+ "Sold "
				+ StringUtils.yellowify(sellable.getSelling().getAmount() + " "
						+ sellable.getSelling().getType().name(),
						ChatColor.GREEN)
				+ "(s) at "
				+ StringUtils.yellowify(
						MessageUtils.getPriceMessage(sellable.getPrice()),
						ChatColor.GREEN) + " per stack.");
		npc.getBukkitEntity()
				.getInventory()
				.setContents(
						sortInventory(npc.getBukkitEntity().getInventory()));
	}

	public ItemStack[] sortInventory(PlayerInventory inventory) {
		return InventorySorter.sortInventory(inventory.getContents());
	}
}
