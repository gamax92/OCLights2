package ds.mods.OCLights2.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import ds.mods.OCLights2.network.PacketHandler.PacketMessage;

public class PacketHandler implements IMessageHandler<PacketMessage, IMessage> {

	PacketHandlerIMPL impl = new PacketHandlerIMPL();
	
	@Override
	public IMessage onMessage(PacketMessage message, MessageContext ctx) {
		EntityPlayer player = ctx.side == Side.SERVER ? ctx.getServerHandler().playerEntity : null;
		impl.onPacketData(ctx.netHandler, message, player);
		return null;
	}
	
	public static class PacketMessage implements IMessage {
		byte[] data;

		@Override
		public void fromBytes(ByteBuf buf) {
			int len = buf.readInt();
			data = new byte[len];
			buf.readBytes(data);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(data.length);
			buf.writeBytes(data);
		}
		
	}
}
