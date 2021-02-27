package de.dertoaster.dtarmory.guns;

import java.util.Optional;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
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

import de.dertoaster.dtarmory.DTArmory;
import de.dertoaster.dtlib.spigot.items.CustomItemBase;
import de.dertoaster.dtlib.spigot.items.CustomItemRegistry;
import de.dertoaster.dtlib.spigot.items.ItemDamageUtil;
import de.dertoaster.dtlib.spigot.items.ItemDataUtil;

public abstract class AbstractGun extends CustomItemBase {

	public static final NamespacedKey MAGAZINE_DATA_KEY = new NamespacedKey(DTArmory.getInstance(), "dtarmory_magazine_key");
	public static final NamespacedKey COOLDOWN_DATA_KEY = new NamespacedKey(DTArmory.getInstance(), "dtarmory_cooldown_key");
	public static final NamespacedKey RELOADING_DATA_KEY = new NamespacedKey(DTArmory.getInstance(), "dtarmory_reloading_key");

	public AbstractGun(String itemID, CustomItemRegistry<AbstractGun> registry) {
		super(itemID, registry);
	}

	protected abstract Optional<String> getUsePermission();

	protected abstract void onShoot(ItemStack item, PlayerInteractEvent event);

	protected abstract void onSecondary(ItemStack item, PlayerInteractEvent event);

	protected abstract int getMaxAmmo();
	
	//Measured in milliseconds
	protected abstract long getReloadTime();
	protected abstract long getShotDelay();

	protected int getRemainingAmmo(ItemStack item) {
		Optional<Integer> itemData = ItemDataUtil.tryGetPersistentInteger(MAGAZINE_DATA_KEY, item);
		if(itemData.isPresent()) {
			return itemData.get();
		}
		
		return -1;
	}
	
	protected boolean hasFreeMagazineCapacity(ItemStack item) {
		return this.getRemainingAmmo(item) < this.getMaxAmmo();
	}
	
	/*
	 * Checks if the RELOADING_DATA_KEY has been set
	 */
	protected boolean isReloading(ItemStack item) {
		Optional<String> stringData =  ItemDataUtil.tryGetPersistentString(RELOADING_DATA_KEY, item);
		if(stringData.isPresent()) {
			return Boolean.valueOf(stringData.get());
		}
		return false;
	}
	/*
	 * Checks if the time stored in COOLDOWN_DATA_KEY is already in the past (or now)
	 */
	protected boolean isCurrentCooldownOver(ItemStack item) {
		Optional<Long> itemData = ItemDataUtil.tryGetPersistentLong(COOLDOWN_DATA_KEY, item);
		if(itemData.isPresent()) {
			//Saved data is the time at when it can shoot again. 
			return itemData.get() <= System.currentTimeMillis();
		}
		return true;
	}
	
	protected boolean hasPermission(Player player) {
		if (this.getUsePermission().isPresent() && !player.hasPermission(this.getUsePermission().get())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canItemBeUsed(Player player, ItemStack stack) {

		return false;
	}

	//TODO: Maybe merge onRightClick and onLeftClick somehow? They're literally the same .__.
	@Override
	public void onRightClick(ItemStack item, PlayerInteractEvent event) {
		if (hasPermission(event.getPlayer())) {
			if(this.isCurrentCooldownOver(item)) {
				this.onSecondary(item, event);
				//Since the cooldown is over, let's check if we were reloading
				if(this.isReloading(item)) {
					//We were reloading, let's remove the flag
					if(!ItemDataUtil.tryRemoveData(RELOADING_DATA_KEY, item)) {
						//Something went wrong => TODO: Log this shit :D
					}
				}
				//Let's add the shot cooldown to the item
				long future = System.currentTimeMillis() + this.getShotDelay();
				if(!ItemDataUtil.trySetPersistentLong(COOLDOWN_DATA_KEY, future, item)) {
					//Something went wrong => TODO: Log this shit
				}
			}
		}
		event.setCancelled(true);
	}

	@Override
	public void onLeftClick(ItemStack item, PlayerInteractEvent event) {
		if (hasPermission(event.getPlayer())) {
			if(this.isCurrentCooldownOver(item)) {
				if(!this.hasAmmo(item)) {
					//TODO: Play out of ammo sound
					
					
				}
				//TODO: Spawn additional projectiles when <shot delay> < <Current tick time>, also needs to adjust the "future"
				else {
					this.onShoot(item, event);
					//Since the cooldown is over, let's check if we were reloading
					if(this.isReloading(item)) {
						//We were reloading, let's remove the flag
						if(!ItemDataUtil.tryRemoveData(RELOADING_DATA_KEY, item)) {
							//Something went wrong => TODO: Log this shit :D
						}
					}
					this.updateItemMagazine(item);
					//Let's add the shot cooldown to the item
					long future = System.currentTimeMillis() + this.getShotDelay();
					if(!ItemDataUtil.trySetPersistentLong(COOLDOWN_DATA_KEY, future, item)) {
						//Something went wrong => TODO: Log this shit
					}
				}
			} else if(this.isReloading(item)) {
				this.removeReloadingMarkerOnItemMovement(item);
			}
		}
		event.setCancelled(true);
	}

	private void updateItemMagazine(ItemStack item) {
		int currentAmmo = this.getRemainingAmmo(item);
		currentAmmo--;
		double percentage = currentAmmo / this.getMaxAmmo();
		//Update magazine counter
		ItemDataUtil.trySetPersistentInteger(MAGAZINE_DATA_KEY, currentAmmo, item);
		//Update item damage (Ammo indicator)
		ItemDamageUtil.setDamagePercent(percentage, item);
	}

	@Override
	public void onInteractAtEntity(ItemStack item, Entity clicked, PlayerInteractAtEntityEvent event) {

	}

	@Override
	public void onDrop(ItemStack item, PlayerDropItemEvent event) {
		if (hasPermission(event.getPlayer())) {
			if(!this.isCurrentCooldownOver(item)) {
				if(this.isReloading(item)) {
					event.setCancelled(true);
					return;
				}
			}
			if(this.hasFreeMagazineCapacity(item)) {
				if(this.checkReload(item, event.getPlayer())) {
					ItemDataUtil.trySetPersistentLong(COOLDOWN_DATA_KEY, System.currentTimeMillis() + this.getReloadTime(), item);
					ItemDataUtil.trySetPersistentString(RELOADING_DATA_KEY, Boolean.TRUE.toString(), item);
				}
			}
			event.setCancelled(true);
			return;
		}
		event.setCancelled(false);
	}
	
	private boolean checkReload(ItemStack item, Player player) {
		if(player.getGameMode().equals(GameMode.CREATIVE)) {
			return true;
		}
		//TODO: check for the correct ammo being present
		//TODO: Remove ammo items WHEN RELOAD IS FINISHED
		//TODO: Add "used during reload" marker on ALL AMMO ITEMS of the needed ammo, that is to prevent the player removing the ammo before the reloading is over
		return true;
	}

	protected boolean hasAmmo(ItemStack item) {
		return this.getRemainingAmmo(item) > 0;
	}

	@Override
	public void onInventoryClick(ItemStack item, InventoryClickEvent event) {
		this.removeReloadingMarkerOnItemMovement(item);
	}

	@Override
	public void onInventoryDrag(ItemStack item, InventoryDragEvent event) {
		this.removeReloadingMarkerOnItemMovement(item);
	}

	@Override
	public void onDispense(ItemStack item, BlockDispenseEvent event) {

	}

	@Override
	public void onEquip(ItemStack item, PlayerItemHeldEvent event) {
		this.removeReloadingMarkerOnItemMovement(item);
	}

	@Override
	public void onUnequip(ItemStack item, PlayerItemHeldEvent event) {
		this.removeReloadingMarkerOnItemMovement(item);
	}

	@Override
	public void onSwapTo(EquipmentSlot newHand, boolean isDualWield, ItemStack item, PlayerSwapHandItemsEvent event) {
		this.removeReloadingMarkerOnItemMovement(item);
	}
	
	private void removeReloadingMarkerOnItemMovement(ItemStack item) {
		if(this.isReloading(item)) {
			if(!(ItemDataUtil.tryRemoveData(COOLDOWN_DATA_KEY, item) && ItemDataUtil.tryRemoveData(RELOADING_DATA_KEY, item))) {
				//Something went wrong => TODO: Log this shit
			}
		}
	}

}
