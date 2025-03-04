package logisticspipes.network.packets.gui;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.client.FMLClientHandler;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class OpenChatGui extends ModernPacket {

	public OpenChatGui(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {}

	@Override
    @SideOnly(Side.CLIENT)
	public void processPacket(EntityPlayer player) {
		FMLClientHandler.instance().getClient().displayGuiScreen(new GuiChat());
	}

	@Override
	public void writeData(LPDataOutput output) {}

	@Override
	public ModernPacket template() {
		return new OpenChatGui(getId());
	}
}
