package de.dertoaster.dtarmory.data;

import java.util.List;

import org.bukkit.Material;

public interface IDTArmoryItemDataBase {

	public int getCustomModelData();

	public void setCustomModelData(int data);

	public String getItemIdent();

	public void setItemIdent(String data);

	public String getDisplayName();

	public void setDisplayName(String data);

	public List<String> getItemDescription();

	public void setItemDescription(List<String> data);

	/* must return true in "isItem()" */
	public Material getMinecraftItemID();
	public boolean isItemUnbreakable();
	public void setItemUnbreakable(boolean value);

}
