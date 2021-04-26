package de.dertoaster.dtarmory.data;

public interface IDTArmoryGunData extends IDTArmoryItemDataBase {
	
	public int getAmmoCapacity();
	
	public long getShotDelay();
	public long getReloadDuration();
	
}
