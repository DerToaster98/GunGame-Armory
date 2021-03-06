package de.dertoaster.dtarmory.guns;

import java.util.Optional;

import javax.annotation.Nonnull;

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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.dertoaster.dtarmory.DTArmory;
import de.dertoaster.dtarmory.ammunition.ItemAmmunition;
import de.dertoaster.dtarmory.data.IDTArmoryGunData;
import de.dertoaster.dtlib.spigot.items.CustomItemBase;
import de.dertoaster.dtlib.spigot.items.CustomItemRegistry;
import de.dertoaster.dtlib.spigot.items.ItemDamageUtil;
import de.dertoaster.dtlib.spigot.items.ItemDataUtil;

public abstract class AbstractGun extends CustomItemBase {

	public static final NamespacedKey MAGAZINE_DATA_KEY = new NamespacedKey(DTArmory.getInstance(), "dtarmory_magazine_key");
	public static final NamespacedKey COOLDOWN_DATA_KEY = new NamespacedKey(DTArmory.getInstance(), "dtarmory_cooldown_key");
	public static final NamespacedKey RELOADING_DATA_KEY = new NamespacedKey(DTArmory.getInstance(), "dtarmory_reloading_key");

	protected final IDTArmoryGunData gunData;
	protected final ItemAmmunition requiredAmmo;
	
	AbstractGun(String itemID, IDTArmoryGunData data) {
		super(itemID, DTArmory.GUN_REGISTRY);
		this.gunData = data;
		this.requiredAmmo = DTArmory.AMMO_REGISTRY.getEntry(this.gunData.getRequiredAmmo()).get();
	}

	protected Optional<String> getUsePermission() {
		return this.gunData.getPermission();
	}

	protected abstract void onShoot(ItemStack item, PlayerInteractEvent event);

	protected abstract void onSecondary(ItemStack item, PlayerInteractEvent event);

	public int getMaxAmmo() {
		return this.gunData.getAmmoCapacity();
	}
	
	//Measured in milliseconds
	protected long getReloadTime() {
		return this.gunData.getReloadDuration();
	}
	protected long getShotDelay() {
		return this.gunData.getShotDelay();
	}

	/*
	 * Return the amount of shots that are loaded into the weapon, if it is not loaded, it will return -1
	 */
	public static int getRemainingAmmo(ItemStack item) {
		Optional<Integer> itemData = ItemDataUtil.tryGetPersistentInteger(MAGAZINE_DATA_KEY, item);
		if(itemData.isPresent()) {
			return itemData.get();
		}
		
		return -1;
	}
	
	protected boolean hasFreeMagazineCapacity(ItemStack item) {
		return AbstractGun.getRemainingAmmo(item) < this.getMaxAmmo();
	}
	
	/*
	 * Checks if the RELOADING_DATA_KEY has been set to true, will return false otherwise
	 */
	public static boolean isReloading(ItemStack item) {
		Optional<String> stringData =  ItemDataUtil.tryGetPersistentString(RELOADING_DATA_KEY, item);
		if(stringData.isPresent()) {
			return Boolean.valueOf(stringData.get());
		}
		return false;
	}
	/*
	 * Checks if the time stored in COOLDOWN_DATA_KEY is already in the past (or now)
	 */
	public static boolean isCurrentCooldownOver(ItemStack item) {
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
			if(AbstractGun.isCurrentCooldownOver(item)) {
				this.onSecondary(item, event);
				//No, we don't need ammo here
				//Since the cooldown is over, let's check if we were reloading
				/*if(AbstractGun.isReloading(item)) {
					//We were reloading, let's remove the flag
					if(!ItemDataUtil.tryRemoveData(RELOADING_DATA_KEY, item)) {
						//Something went wrong => TODO: Log this shit :D
					}
				}*/
				//Well, we don't really need a cooldown for our secondary action, if at all we need a separate one...
				//Let's add the shot cooldown to the item
				/*long future = System.currentTimeMillis() + this.getShotDelay();
				if(!ItemDataUtil.trySetPersistentLong(COOLDOWN_DATA_KEY, future, item)) {
					//Something went wrong => TODO: Log this shit
				}*/
			}
		}
		event.setCancelled(true);
	}

	@Override
	public void onLeftClick(ItemStack item, PlayerInteractEvent event) {
		if (hasPermission(event.getPlayer())) {
			if(AbstractGun.isCurrentCooldownOver(item)) {
				if(!this.hasAmmo(item)) {
					//DONE: Play out of ammo sound
					this.gunData.getOutOfAmmoSound().play(event.getPlayer().getWorld(), event.getPlayer().getLocation());
					
				}
				//TODO: Spawn additional projectiles when <shot delay> < <Current tick time>, also needs to adjust the "future"
				else {
					//Since the cooldown is over, let's check if we were reloading
					if(AbstractGun.isReloading(item)) {
						//Remove ammo items
						int currentAmmo = AbstractGun.getRemainingAmmo(item);
						int ammoToRemove = this.getMaxAmmo() - currentAmmo;
						
						//We were reloading, let's remove the flag
						cancelReload(item, event.getPlayer().getInventory());
					} 
					this.onShoot(item, event);
					this.updateItemMagazine(item);
					//Let's add the shot cooldown to the item
					long future = System.currentTimeMillis() + this.getShotDelay();
					if(!ItemDataUtil.trySetPersistentLong(COOLDOWN_DATA_KEY, future, item)) {
						//Something went wrong => TODO: Log this shit
					}
				}
			} else if(AbstractGun.isReloading(item)) {
				this.cancelReload(item, event.getPlayer().getInventory());
			}
		}
		event.setCancelled(true);
	}

	private void updateItemMagazine(ItemStack item) {
		int currentAmmo = AbstractGun.getRemainingAmmo(item);
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
			if(!AbstractGun.isCurrentCooldownOver(item)) {
				if(AbstractGun.isReloading(item)) {
					event.setCancelled(true);
					return;
				}
			}
			if(this.hasFreeMagazineCapacity(item)) {
				if(this.checkReload(item, event.getPlayer())) {
					ItemDataUtil.trySetPersistentLong(COOLDOWN_DATA_KEY, System.currentTimeMillis() + this.getReloadTime(), item);
					ItemDataUtil.trySetPersistentString(RELOADING_DATA_KEY, Boolean.TRUE.toString(), item);
					
					this.gunData.getReloadingSound().play(event.getPlayer().getWorld(), event.getPlayer().getLocation());
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
		//DONE: check for the correct ammo being present
		if(!containsValidAmmo(player.getInventory())) {
			return false;
		}
		//TODO: Remove ammo items WHEN RELOAD IS FINISHED
		//DONE: Add "used during reload" marker on ALL AMMO ITEMS of the needed ammo, that is to prevent the player removing the ammo before the reloading is over
		for(ItemStack stack : player.getInventory().getContents()) {
			Optional<ItemAmmunition> itemAmmo = DTArmory.AMMO_REGISTRY.getEntry(stack);
			if(itemAmmo.isPresent() && this.isAmmoValid(itemAmmo.get())) {
				ItemAmmunition.markStackAsRequiredForReload(stack);
			}
		}
		return true;
	}

	public ItemAmmunition getRequiredAmmo() {
		return this.requiredAmmo;
	}

	protected boolean hasAmmo(ItemStack item) {
		return AbstractGun.getRemainingAmmo(item) > 0;
	}

	@Override
	public void onInventoryClick(ItemStack item, InventoryClickEvent event) {
		this.cancelReload(item, event.getInventory());
	}

	@Override
	public void onInventoryDrag(ItemStack item, InventoryDragEvent event) {
		this.cancelReload(item, event.getInventory());
	}

	@Override
	public void onDispense(ItemStack item, BlockDispenseEvent event) {

	}

	@Override
	public void onEquip(ItemStack item, PlayerItemHeldEvent event) {
		this.cancelReload(item, event.getPlayer().getInventory());
	}

	@Override
	public void onUnequip(ItemStack item, PlayerItemHeldEvent event) {
		this.cancelReload(item, event.getPlayer().getInventory());
	}

	@Override
	public void onSwapTo(EquipmentSlot newHand, boolean isDualWield, ItemStack item, PlayerSwapHandItemsEvent event) {
		this.cancelReload(item, event.getPlayer().getInventory());
	}
	
	/*
	 * Removes the reload and cooldown marker and removes reload markings from the ammo items in the inventory
	 */
	private void cancelReload(ItemStack item, Inventory inventory) {
		if(AbstractGun.isReloading(item)) {
			if(!(ItemDataUtil.tryRemoveData(COOLDOWN_DATA_KEY, item) && ItemDataUtil.tryRemoveData(RELOADING_DATA_KEY, item))) {
				//Something went wrong => TODO: Log this shit
			}
			for(ItemStack ammoStack : inventory.getContents()) {
				try {
					ItemAmmunition.removeRequiredForReloadMarking(ammoStack);
				} catch(Exception ex) {
					//Seems like something went wrong, well i don't care
				}
			}
		}
	}
	
	//Ammo handling
	protected boolean isAmmoValid(@Nonnull ItemAmmunition ammo) {
		return this.requiredAmmo == ammo;
	}
	
	public boolean containsValidAmmo(@Nonnull Inventory inventory) {
		CustomItemRegistry<ItemAmmunition> ammoReg = DTArmory.AMMO_REGISTRY;
		for(ItemStack stack : inventory.getStorageContents()) {
			Optional<ItemAmmunition> ammoOptional = ammoReg.getEntry(stack);
			if(ammoOptional.isPresent()) {
				return this.isAmmoValid(ammoOptional.get());
			}
		}
		return false;
	}
	
	
	
}
