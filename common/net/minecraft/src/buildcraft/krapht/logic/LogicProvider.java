/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.logic;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.logisticspipes.ExtractionMode;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.InventoryUtilFactory;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;

public class LogicProvider extends BaseRoutingLogic{

	private SimpleInventory dummyInventory = new SimpleInventory(9, "Items to provide (or empty for all)", 1);
	private boolean _filterIsExclude;
	private ExtractionMode _extractionMode = ExtractionMode.Normal;

	private final InventoryUtilFactory _invUtilFactory;
	private final InventoryUtil _dummyInvUtil;
	
	
	public LogicProvider(){
		this(new InventoryUtilFactory());
	}
	
	public LogicProvider (InventoryUtilFactory invUtilFactory){
		_invUtilFactory = invUtilFactory;
		_dummyInvUtil = _invUtilFactory.getInventoryUtil(dummyInventory);
	}

	@Override
	public void destroy() {}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		GuiProxy.openGuiProviderPipe(entityplayer.inventory, dummyInventory, this);	
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		dummyInventory.readFromNBT(nbttagcompound, "");
		_filterIsExclude = nbttagcompound.getBoolean("filterisexclude");
		_extractionMode = ExtractionMode.values()[nbttagcompound.getInteger("extractionMode")];
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	dummyInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setBoolean("filterisexclude", _filterIsExclude);
    	nbttagcompound.setInteger("extractionMode", _extractionMode.ordinal());
    }
	
	/** INTERFACE TO PIPE **/
	public boolean hasFilter(){
		return _dummyInvUtil.getItemsAndCount().size() > 0;
	}
	
	public boolean itemIsFiltered(ItemIdentifier item){
		return _dummyInvUtil.getItemsAndCount().containsKey(item);
	}
	
	public boolean isExcludeFilter(){
		return _filterIsExclude;
	}
	
	public void setFilterExcluded(boolean isExcluded){
		_filterIsExclude = isExcluded;
	}
	
	public ExtractionMode getExtractionMode(){
		return _extractionMode;
	}

	public void nextExtractionMode() {
		_extractionMode = _extractionMode.next();
	}
}
