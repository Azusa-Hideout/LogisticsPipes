package logisticspipes.pipes.basic.ltgpmodcompat;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;

import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.DuctUnit;
import cofh.thermaldynamics.duct.tiles.IDuctHolder;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import logisticspipes.LPConstants;
import logisticspipes.proxy.td.subproxies.ITDPart;

import net.minecraftforge.fml.common.Optional;

@Optional.Interface(modid = LPConstants.thermalDynamicsModID, iface = "cofh.thermaldynamics.duct.tiles.IDuctHolder")
public abstract class LPDuctHolderTileEntity extends LPMicroblockTileEntity implements IDuctHolder {

	public ITDPart tdPart;

	@Nullable
	@Override
	@Optional.Method(modid = LPConstants.thermalDynamicsModID)
	public <T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> T getDuct(DuctToken<T, G, C> ductToken) {
		return ((IDuctHolder) tdPart.getInternalDuct()).getDuct(ductToken);
	}

	@Override
	@Optional.Method(modid = LPConstants.thermalDynamicsModID)
	public boolean isSideBlocked(int i) {
		return tdPart.isLPSideBlocked(i);
	}

	@Override
	@Optional.Method(modid = LPConstants.thermalDynamicsModID)
	public void setPos(BlockPos pos) {
		super.setPos(pos);
		tdPart.setPos(pos);
	}
}
