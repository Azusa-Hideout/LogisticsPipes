package logisticspipes.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;

import logisticspipes.LPBlocks;
import logisticspipes.LPConstants;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.RequestRotationPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.proxy.computers.wrapper.CCObjectWrapper;
import logisticspipes.proxy.opencomputers.IOCTile;
import logisticspipes.proxy.opencomputers.asm.BaseWrapperClass;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.world.DoubleCoordinates;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

@Optional.InterfaceList({
    @Optional.Interface(modid = LPConstants.openComputersModID, iface = "li.cil.oc.api.network.ManagedPeripheral"),
    @Optional.Interface(modid = LPConstants.openComputersModID, iface = "li.cil.oc.api.network.Environment"),
    @Optional.Interface(modid = LPConstants.openComputersModID, iface = "li.cil.oc.api.network.SidedEnvironment")
})
@CCType(name = "LogisticsSolidBlock")
public class LogisticsSolidTileEntity extends TileEntity implements ITickable, ILPCCTypeHolder, IRotationProvider, ManagedPeripheral, Environment, SidedEnvironment, IOCTile {

	private final Object[] ccTypeHolder = new Object[1];
	private boolean addedToNetwork = false;
	private boolean init = false;
	public int rotation = 0;

	private boolean doneBackwardsCompatCheck = false;

	public Object node; // Node class

	public LogisticsSolidTileEntity() {
		SimpleServiceLocator.openComputersProxy.initLogisticsSolidTileEntity(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		rotation = nbt.getInteger("rotation");
		SimpleServiceLocator.openComputersProxy.handleReadFromNBT(this, nbt);
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		nbt.setInteger("rotation", rotation);
		SimpleServiceLocator.openComputersProxy.handleWriteToNBT(this, nbt);
		return nbt;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		SimpleServiceLocator.openComputersProxy.handleChunkUnload(this);
	}

	@Override
	public void update() {
		tryUpdateBlockFormat();

		if (!addedToNetwork) {
			addedToNetwork = true;
			SimpleServiceLocator.openComputersProxy.addToNetwork(this);
		}
		if (MainProxy.isClient(getWorld())) {
			if (!init) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestRotationPacket.class).setBlockPos(pos));
				init = true;
			}
        }
	}

	// backwards compat; TODO remove in 1.13
	protected void tryUpdateBlockFormat() {
		if (getWorld().isRemote) return;
		if (doneBackwardsCompatCheck) return;

		if (getBlockType() == LPBlocks.dummy) {
			getWorld().setBlockState(getPos(), BlockDummy.updateBlockMap.get(getWorld().getBlockState(getPos()).getValue(BlockDummy.PROP_BLOCKTYPE)).getDefaultState());
		}

		doneBackwardsCompatCheck = true;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, @Nonnull IBlockState newSate) {
		// backwards compat; TODO remove in 1.13
		if (oldState.getBlock() == LPBlocks.dummy && newSate.getBlock() instanceof LogisticsSolidBlock) return false;

		return super.shouldRefresh(world, pos, oldState, newSate);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		SimpleServiceLocator.openComputersProxy.handleInvalidate(this);
	}

	public void onBlockBreak() {
	}

	@Override
	@CCCommand(description = "Returns the LP rotation value for this block")
	public int getRotation() {
		return rotation;
	}

	public boolean isActive() {
		return false;
	}

	@Override
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public void notifyOfBlockChange() {
	}

	@Override
	@Optional.Method(modid = LPConstants.openComputersModID)
	public Node node() {
		return (Node) node;
	}

	@Override
	@Optional.Method(modid = LPConstants.openComputersModID)
	public void onConnect(Node node1) {
	}

	@Override
	@Optional.Method(modid = LPConstants.openComputersModID)
	public void onDisconnect(Node node1) {
	}

	@Override
	@Optional.Method(modid = LPConstants.openComputersModID)
	public void onMessage(Message message) {
	}

	@Override
	@Optional.Method(modid = LPConstants.openComputersModID)
	public Object[] invoke(String s, Context context, Arguments arguments) {
		BaseWrapperClass object = (BaseWrapperClass) CCObjectWrapper.getWrappedObject(this, BaseWrapperClass.WRAPPER);
		object.isDirectCall = true;
		return CCObjectWrapper.createArray(object);
	}

	@Override
	@Optional.Method(modid = LPConstants.openComputersModID)
	public String[] methods() {
		return new String[] { "getBlock" };
	}

	@Override
	@Optional.Method(modid = LPConstants.openComputersModID)
	public Node sidedNode(EnumFacing side) {
		final NeighborTileEntity<TileEntity> neighbor = new WorldCoordinatesWrapper(this).getNeighbor(side);
		if (neighbor == null || neighbor.isLogisticsPipe() || neighbor.getTileEntity() instanceof LogisticsSolidTileEntity) {
			return null;
		} else {
			return node();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	@Optional.Method(modid = LPConstants.openComputersModID)
	public boolean canConnect(EnumFacing side) {
		final NeighborTileEntity<TileEntity> neighbor = new WorldCoordinatesWrapper(this).getNeighbor(side);
		return neighbor != null && !neighbor.isLogisticsPipe() && !(neighbor.getTileEntity() instanceof LogisticsSolidTileEntity);
	}

	@Override
	public Object getOCNode() {
		return node();
	}

	public DoubleCoordinates getLPPosition() {
		return new DoubleCoordinates(this);
	}

	public World getWorldForHUD() {
		return getWorld();
	}

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}

}
