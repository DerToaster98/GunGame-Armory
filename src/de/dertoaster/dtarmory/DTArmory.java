package de.dertoaster.dtarmory;

import javax.annotation.Nullable;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.dertoaster.dtarmory.ammunition.DTArmoryAmmoRegistry;
import de.dertoaster.dtarmory.guns.DTArmoryGunRegistry;
import de.dertoaster.dtlib.spigot.items.IPluginProvider;

public class DTArmory extends JavaPlugin implements IPluginProvider {
	
	private static DTArmory INSTANCE;
	
	public static DTArmoryAmmoRegistry AMMO_REGISTRY;
	public static DTArmoryGunRegistry GUN_REGISTRY;
	
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		INSTANCE = this;
		
		DTArmory.AMMO_REGISTRY = new DTArmoryAmmoRegistry(this);
		DTArmory.GUN_REGISTRY = new DTArmoryGunRegistry();
	}
	
	@Nullable
	public static DTArmory getInstance() {
		return INSTANCE;
	}

	@Override
	public JavaPlugin getPluginInstance() {
		return this;
	}

	@Override
	public PluginManager getPluginManager() {
		return this.getServer().getPluginManager();
	}

}
