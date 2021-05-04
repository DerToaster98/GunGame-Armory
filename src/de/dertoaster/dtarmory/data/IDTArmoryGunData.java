package de.dertoaster.dtarmory.data;

import de.dertoaster.dtlib.spigot.sound.DTLibComplexSound;

public interface IDTArmoryGunData extends IDTArmoryItemDataBase {
	
	public int getAmmoCapacity();
	
	//Timers
	public long getShotDelay();
	public long getReloadDuration();
	
	//Sounds
	public DTLibComplexSound getReloadingSound();
	public DTLibComplexSound getOutOfAmmoSound();

	public String getRequiredAmmo();
	
}
