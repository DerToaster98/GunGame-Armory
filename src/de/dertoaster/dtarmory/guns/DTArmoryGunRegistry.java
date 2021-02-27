package de.dertoaster.dtarmory.guns;

import de.dertoaster.dtarmory.DTArmory;
import de.dertoaster.dtlib.spigot.items.CustomItemRegistry;

public class DTArmoryGunRegistry extends CustomItemRegistry<AbstractGun> {
	
	public DTArmoryGunRegistry() {
		super(DTArmory.getInstance());
	}
	
	//TODO: Listen to "death event" and when that is active, remove all markers from all GG items

}
