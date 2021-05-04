package de.dertoaster.dtarmory.ammunition;

import java.util.Optional;

import org.bukkit.NamespacedKey;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.dertoaster.dtarmory.DTArmory;
import de.dertoaster.dtarmory.data.IDTArmoryAmmoData;
import de.dertoaster.dtlib.spigot.items.CustomItemBase;
import de.dertoaster.dtlib.spigot.items.ItemDamageUtil;
import de.dertoaster.dtlib.spigot.items.ItemDataUtil;

public class ItemAmmunition extends CustomItemBase {
	
	protected final IDTArmoryAmmoData ammoData;
	
	public static final NamespacedKey SHOT_COUNT_DATA_KEY = new NamespacedKey(DTArmory.getInstance(), "dtarmory_ammo_shot_key");
	public static final NamespacedKey MARKED_AS_RELOAD_AMMO_DATA_KEY = new NamespacedKey(DTArmory.getInstance(), "dtarmory_ammo_marked_for_reload_key");

	public ItemAmmunition(final IDTArmoryAmmoData ammoData) {
		super(ammoData.getItemIdent(), DTArmory.AMMO_REGISTRY);
		
		this.ammoData = ammoData;
	}

	@Override
	public boolean canItemBeUsed(Player player, ItemStack stack) {
		return true;
	}

	@Override
	protected ItemStack getDefault() {
		ItemStack stack = new ItemStack(this.ammoData.getMinecraftItemID());
		ItemMeta meta = stack.getItemMeta();
		
		meta.setUnbreakable(this.ammoData.isItemUnbreakable());
		meta.setCustomModelData(this.ammoData.getCustomModelData());
		meta.setDisplayName(this.ammoData.getDisplayName());
		meta.setLore(this.ammoData.getItemDescription());
		
		stack.setItemMeta(meta);
		
		return stack;
	}

	@Override
	public void onRightClick(ItemStack item, PlayerInteractEvent event) {
		if(event.getClickedBlock().getState() instanceof Openable) {
		} else {
			event.setCancelled(true);
		}
	}

	@Override
	public void onLeftClick(ItemStack item, PlayerInteractEvent event) {

	}

	@Override
	public void onInteractAtEntity(ItemStack item, Entity clicked, PlayerInteractAtEntityEvent event) {

	}

	@Override
	public void onDrop(ItemStack item, PlayerDropItemEvent event) {
		if(hasRequiredForReloadMarking(item)) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onInventoryClick(ItemStack item, InventoryClickEvent event) {
		if(hasRequiredForReloadMarking(item)) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onInventoryDrag(ItemStack item, InventoryDragEvent event) {
		if(hasRequiredForReloadMarking(item)) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onDispense(ItemStack item, BlockDispenseEvent event) {
		if(hasRequiredForReloadMarking(item)) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onEquip(ItemStack item, PlayerItemHeldEvent event) {

	}

	@Override
	public void onUnequip(ItemStack item, PlayerItemHeldEvent event) {

	}

	@Override
	public void onSwapTo(EquipmentSlot newHand, boolean isDualWield, ItemStack item, PlayerSwapHandItemsEvent event) {
		
	}
	
	public static int getShotsInAmmo(ItemStack item) {
		Optional<Integer> itemData = ItemDataUtil.tryGetPersistentInteger(SHOT_COUNT_DATA_KEY, item);
		if(itemData.isPresent()) {
			return itemData.get();
		}
		return -1;
	}
	
	public ItemStack[] generateItemStackWithShotCount(int shotCount) {
		int stackCount = (int) Math.ceil(shotCount / this.ammoData.getShotCount());
		ItemStack[] result = new ItemStack[stackCount];
		int shotsToLoad = shotCount;
		for(int i = 0; i < stackCount; i++) {
			int shots = shotsToLoad - this.ammoData.getShotCount() > 0 ? this.ammoData.getShotCount() : shotsToLoad;
			shotsToLoad -= this.ammoData.getShotCount();
			if(shots <= 0) {
				result[i] = null;
				break;
			}
			result[i] = this.getDefault();
			ItemDataUtil.trySetPersistentInteger(SHOT_COUNT_DATA_KEY, shots, result[i]);
			ItemDamageUtil.setDamagePercent(shots / this.ammoData.getShotCount(), result[i]);
		}
		return result;
	}
	
	public static boolean markStackAsRequiredForReload(ItemStack stack) {
		return ItemDataUtil.trySetPersistentBoolean(MARKED_AS_RELOAD_AMMO_DATA_KEY, true, stack);
	}
	
	public static boolean removeRequiredForReloadMarking(ItemStack stack) {
		return ItemDataUtil.tryRemoveData(MARKED_AS_RELOAD_AMMO_DATA_KEY, stack);
	}
	
	public static boolean hasRequiredForReloadMarking(ItemStack stack) {
		Optional<Boolean> val = ItemDataUtil.tryGetPersistentBoolean(MARKED_AS_RELOAD_AMMO_DATA_KEY, stack);
		if(val.isPresent()) {
			return val.get();
		}
		return false;
	}

}
