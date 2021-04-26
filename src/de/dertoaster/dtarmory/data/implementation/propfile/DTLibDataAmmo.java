package de.dertoaster.dtarmory.data.implementation.propfile;

import java.util.Map;

import de.dertoaster.dtarmory.data.IDTArmoryAmmoData;
import de.dertoaster.dtlib.properties.datatypes.PropertyDataTypes;

public class DTLibDataAmmo extends DTLibDataCommon implements IDTArmoryAmmoData {

	private int shotCount;
	
	@Override
	public int getShotCount() {
		return this.shotCount;
	}

	@Override
	public void setShotCount(int data) {
		this.shotCount = data;
	}

	@Override
	protected void registerAdditional(Map<String, PropertyEntry<?>> entries) {
		entries.put("shotcount", new PropertyEntry<Integer>("0", PropertyDataTypes.DATA_TYPE_INTEGER, this::getShotCount, this::setShotCount));
	}
	

}
