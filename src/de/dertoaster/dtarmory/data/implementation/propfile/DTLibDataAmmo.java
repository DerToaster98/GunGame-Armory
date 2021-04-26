package de.dertoaster.dtarmory.data.implementation.propfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import de.dertoaster.dtarmory.data.IDTArmoryAmmoData;
import de.dertoaster.dtlib.properties.IConfigurable;
import de.dertoaster.dtlib.properties.datatypes.PropertyDataTypes;

public class DTLibDataAmmo implements IDTArmoryAmmoData, IConfigurable {

	private Map<String, PropertyEntry<?>> entries;

	private String itemID;
	private String displayName;
	
	private int customModelData;
	private int shotCount;
	
	private boolean itemUnbreakable;
	
	private Material itemMaterial;

	public DTLibDataAmmo() {
		this.entries = new HashMap<String, PropertyEntry<?>>();
		
		this.entries.put("id", new PropertyEntry<String>("itemident", PropertyDataTypes.DATA_TYPE_STRING, this::getItemIdent, this::setItemIdent));
		this.entries.put("displayname", new PropertyEntry<String>("displayname", PropertyDataTypes.DATA_TYPE_STRING, this::getItemIdent, this::setItemIdent));
		
		this.entries.put("modeldata", new PropertyEntry<Integer>("0", PropertyDataTypes.DATA_TYPE_INTEGER, this::getCustomModelData, this::setCustomModelData));
		this.entries.put("shotcount", new PropertyEntry<Integer>("0", PropertyDataTypes.DATA_TYPE_INTEGER, this::getShotCount, this::setShotCount));
		
		this.entries.put("itemunbreakable", new PropertyEntry<Boolean>("false", PropertyDataTypes.DATA_TYPE_BOOLEAN, this::isItemUnbreakable, this::setItemUnbreakable));
		
		this.entries.put("itemid", new PropertyEntry<String>("stick", PropertyDataTypes.DATA_TYPE_STRING, this::getMinecraftItemIDAsString, this::setMinecraftItemID));
	}

	@Override
	public int getCustomModelData() {
		return this.customModelData;
	}

	@Override
	public void setCustomModelData(int data) {
		this.customModelData = data;
	}

	@Override
	public String getItemIdent() {
		return this.itemID;
	}

	@Override
	public void setItemIdent(String data) {
		this.itemID = data;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public void setDisplayName(String data) {
		this.displayName = data;
	}

	@Override
	public List<String> getItemDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setItemDescription(List<String> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public Material getMinecraftItemID() {
		return this.itemMaterial;
	}
	
	private String getMinecraftItemIDAsString() {
		return this.getMinecraftItemID().toString();
	}

	private void setMinecraftItemID(String data) {
		try {
			if(Material.valueOf(data) != null) {
				this.itemMaterial = Material.valueOf(data);
			}
		} catch(Exception ex) {
			//Ignore
		}
	}
	
	@Override
	public boolean isItemUnbreakable() {
		return this.itemUnbreakable;
	}

	@Override
	public void setItemUnbreakable(boolean value) {
		this.itemUnbreakable = value;
	}

	@Override
	public Map<String, PropertyEntry<?>> getConfigurationEntries() {
		return this.entries;
	}

	@Override
	public int getShotCount() {
		return this.shotCount;
	}

	@Override
	public void setShotCount(int data) {
		this.shotCount = data;
	}

}
