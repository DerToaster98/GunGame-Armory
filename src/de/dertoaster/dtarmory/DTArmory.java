package de.dertoaster.dtarmory;

import javax.annotation.Nullable;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.dertoaster.dtlib.spigot.items.IPluginProvider;

public class DTArmory extends JavaPlugin implements IPluginProvider {
	
	private static DTArmory INSTANCE;
	
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		INSTANCE = this;
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
