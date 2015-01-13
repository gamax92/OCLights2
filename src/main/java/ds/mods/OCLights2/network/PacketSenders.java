package ds.mods.OCLights2.network;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.block.tileentity.TileEntityGPU;
import ds.mods.OCLights2.block.tileentity.TileEntityMonitor;
import ds.mods.OCLights2.block.tileentity.TileEntityTTrans;
import ds.mods.OCLights2.gpu.DrawCMD;
import ds.mods.OCLights2.gpu.Texture;
import ds.mods.OCLights2.network.PacketHandler.PacketMessage;
import ds.mods.OCLights2.serialize.Serialize;

public final class PacketSenders {

	public static void sendPacketsNow(Deque<DrawCMD> drawlist,
			TileEntityGPU tile) {
		if (tile == null) {
			throw new IllegalArgumentException(
					"GPU cannot send packet without Tile Entity!");
		}
		ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
		outputStream.writeByte(PacketHandlerIMPL.NET_GPUDRAWLIST);
		outputStream.writeInt(tile.xCoord);
		outputStream.writeInt(tile.yCoord);
		outputStream.writeInt(tile.zCoord);
		outputStream.writeInt(drawlist.size());
		while (!drawlist.isEmpty()) {
			DrawCMD c = drawlist.removeLast();
			outputStream.writeInt(c.cmd.ordinal());
			outputStream.writeInt(c.args.length);
			for (int g = 0; g < c.args.length; g++) {
				Object v = c.args[g];
				if (v != null && v.getClass().isArray())
				{
					Object[] arr = (Object[]) v;
					outputStream.writeByte(-1);
					outputStream.writeInt(arr.length);
					for (int i=0; i<arr.length; i++)
					{
						Serialize.serialize(outputStream, arr[i]);
					}
				}
				else
				{
					outputStream.writeByte(0);
					Serialize.serialize(outputStream, v);
				}
			}
		}
		try {
			PacketMessage[] packets = PacketChunker.instance.createPackets(
					"OCLights2", outputStream.toByteArray());

			for (int g = 0; g < packets.length; g++) {
				OCLights2.network.sendToAllAround(packets[g], new TargetPoint(tile.getWorldObj().provider.dimensionId, tile.xCoord, tile.yCoord, tile.zCoord, 4096.0D));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void GPUEvent(int par1, int par2, TileEntityMonitor tile,
			int wheel) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();

		out.writeByte(PacketHandlerIMPL.NET_GPUEVENT);
		out.writeInt(tile.xCoord);
		out.writeInt(tile.yCoord);
		out.writeInt(tile.zCoord);
		out.writeUTF("monitor_scroll");
		out.writeInt(3);

		out.writeInt(0);
		out.writeInt(par1);

		out.writeInt(0);
		out.writeInt(par2);

		out.writeInt(0);
		out.writeInt(wheel / 120);
		createPacketAndSend(out);
	}

	public synchronized static void sendPacketToPlayer(int x, int y, int z,
			TileEntityGPU tile, EntityPlayer player) {
		try {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeByte(PacketHandlerIMPL.NET_GPUINIT);
			out.writeInt(x);
			out.writeInt(y);
			out.writeInt(z);
			out.writeInt(tile.gpu.color.getRGB());
			double[] matrix = new double[6];
			tile.gpu.transform.getMatrix(matrix);
			writeMatrix(out, matrix);
			Iterator<AffineTransform> it = tile.gpu.transformStack.iterator();
			out.writeInt(tile.gpu.transformStack.size());
			while (it.hasNext()) {
				it.next().getMatrix(matrix);
				writeMatrix(out, matrix);
			}
			PacketMessage[] packets = PacketChunker.instance.createPackets(
					"OCLights2", out.toByteArray());
			for (int g = 0; g < packets.length; g++) {
				OCLights2.network.sendTo(packets[g], (EntityPlayerMP)player);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void sendTextures(EntityPlayer whom, Texture tex, int id, int x,
			int y, int z) {
		try {
			ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
			outputStream.writeByte(PacketHandlerIMPL.NET_GPUDOWNLOAD);
			outputStream.writeInt(x);
			outputStream.writeInt(y);
			outputStream.writeInt(z);
			outputStream.writeInt(id);
			outputStream.writeInt(tex.getWidth());
			outputStream.writeInt(tex.getHeight());
			int[] arr = new int[tex.getWidth() * tex.getHeight() * 4];
			tex.img.getRGB(0, 0, tex.getWidth(), tex.getHeight(), arr, 0,
					tex.getWidth());
			outputStream.writeInt(arr.length);
			for (int i = 0; i < arr.length; i++) {
				outputStream.writeInt(arr[i]);
			}
			PacketMessage[] packets = PacketChunker.instance.createPackets(
					"OCLights2", outputStream.toByteArray());
			for (int g = 0; g < packets.length; g++) {
				OCLights2.network.sendTo(packets[g], (EntityPlayerMP) whom);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void mouseEvent(int mx, int my, int par3,
			TileEntityMonitor tile) {
		ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
		outputStream.writeByte(PacketHandlerIMPL.NET_GPUMOUSE);
		outputStream.writeInt(tile.xCoord);
		outputStream.writeInt(tile.yCoord);
		outputStream.writeInt(tile.zCoord);
		outputStream.writeInt(0);
		outputStream.writeInt(par3);
		outputStream.writeInt(mx);
		outputStream.writeInt(my);
		createPacketAndSend(outputStream);
	}

	public static void mouseEventMove(int mx, int my, TileEntityMonitor tile) {
		ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
		outputStream.writeByte(PacketHandlerIMPL.NET_GPUMOUSE);
		outputStream.writeInt(tile.xCoord);
		outputStream.writeInt(tile.yCoord);
		outputStream.writeInt(tile.zCoord);
		outputStream.writeInt(1);
		outputStream.writeInt(mx);
		outputStream.writeInt(my);
		createPacketAndSend(outputStream);
	}

	public static void mouseEventUp(TileEntityMonitor tile) {
		ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
		outputStream.writeByte(PacketHandlerIMPL.NET_GPUMOUSE);
		outputStream.writeInt(tile.xCoord);
		outputStream.writeInt(tile.yCoord);
		outputStream.writeInt(tile.zCoord);
		outputStream.writeInt(2);
		createPacketAndSend(outputStream);
	}

	public static void screenshot(TileEntityTTrans tile, BufferedImage screenshot) {
		ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
		outputStream.writeByte(PacketHandlerIMPL.NET_SCREENSHOT);
		outputStream.writeInt(tile.xCoord);
		outputStream.writeInt(tile.yCoord);
		outputStream.writeInt(tile.zCoord);
		Image scaledshot = screenshot.getScaledInstance(tile.mon.getWidth(), tile.mon.getHeight(), 1);
		BufferedImage ScaledScreenshot = new BufferedImage(scaledshot.getWidth(null), scaledshot.getHeight(null), BufferedImage.TYPE_INT_RGB);
		// Draw the image on to the buffered image
		Graphics2D bGr = ScaledScreenshot.createGraphics();
		bGr.drawImage(scaledshot, 0, 0, null);
		bGr.dispose();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			//ImageIO.write(ScaledScreenshot, "jpg", baos);
			ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
			ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
			iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwparam.setCompressionQuality(0.5f);
			writer.setOutput(ImageIO.createImageOutputStream(baos));
			writer.write(null, new IIOImage(ScaledScreenshot, null, null), iwparam);
			byte[] screenshotArray = baos.toByteArray();
			outputStream.writeInt(screenshotArray.length);
			outputStream.write(screenshotArray);

			PacketMessage[] packets = PacketChunker.instance.createPackets("OCLights2", outputStream.toByteArray());
			for (int g = 0; g < packets.length; g++) {
				OCLights2.network.sendToServer(packets[g]);
			}
		} catch (IOException e1) {
			OCLights2.debug("failed to send screenshot packets");
		}
	}

	public static void sendKeyEvent(char par1, int par2, TileEntityMonitor tile) {
		ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
		outputStream.writeByte(PacketHandlerIMPL.NET_GPUEVENT);
		outputStream.writeInt(tile.xCoord);
		outputStream.writeInt(tile.yCoord);
		outputStream.writeInt(tile.zCoord);
		outputStream.writeUTF("key_down");
		outputStream.writeInt(2);
		
		outputStream.writeInt(0);
		outputStream.writeInt(par1);
		
		outputStream.writeInt(0);
		outputStream.writeInt(par2);
		
		createPacketAndSend(outputStream);
	}

	public static void sendKeyEventUp(char par1, int par2, TileEntityMonitor tile) {
		ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
		outputStream.writeByte(PacketHandlerIMPL.NET_GPUEVENT);
		outputStream.writeInt(tile.xCoord);
		outputStream.writeInt(tile.yCoord);
		outputStream.writeInt(tile.zCoord);
		outputStream.writeUTF("key_up");
		outputStream.writeInt(2);
		
		outputStream.writeInt(0);
		outputStream.writeInt(par1);
		
		outputStream.writeInt(0);
		outputStream.writeInt(par2);
		
		createPacketAndSend(outputStream);
	}
	
	public static void writeMatrix(ByteArrayDataOutput out, double[] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			out.writeDouble(matrix[i]);
		}
	}

	public synchronized static void ExternalMonitorUpdate(int xCoord,
			int yCoord, int zCoord, int dimId, int m_width, int m_height,
			int m_xIndex, int m_yIndex, int m_dir) {
		ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
		outputStream.writeByte(PacketHandlerIMPL.NET_GPUTILE);
		outputStream.writeInt(xCoord);
		outputStream.writeInt(yCoord);
		outputStream.writeInt(zCoord);
		outputStream.writeInt(m_width);
		outputStream.writeInt(m_height);
		outputStream.writeInt(m_xIndex);
		outputStream.writeInt(m_yIndex);
		outputStream.writeInt(m_dir);
		PacketMessage packet = new PacketMessage();
		packet.data = outputStream.toByteArray();
		OCLights2.network.sendToAllAround(packet, new TargetPoint(dimId,xCoord, yCoord, zCoord, 4096.0D));
	}

	public static void GPUDOWNLOAD(int xCoord, int yCoord, int zCoord) {
		ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
		outputStream.writeByte(PacketHandlerIMPL.NET_GPUDOWNLOAD);
		outputStream.writeInt(xCoord);
		outputStream.writeInt(yCoord);
		outputStream.writeInt(zCoord);
		createPacketAndSend(outputStream);
	}

	public static void createPacketAndSend(ByteArrayDataOutput mergeStream) {
		PacketMessage packet = new PacketMessage();
		packet.data = mergeStream.toByteArray();
		OCLights2.network.sendToServer(packet);
	}

	public static void SYNC(int monitorWidth, int monitorHeight,EntityPlayer player) {
		ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
		outputStream.writeByte(PacketHandlerIMPL.NET_SYNC);
		outputStream.writeShort(monitorWidth);
		outputStream.writeShort(monitorHeight);
		PacketMessage packet = new PacketMessage();
		packet.data = outputStream.toByteArray();
		OCLights2.network.sendTo(packet, (EntityPlayerMP)player);
	}

}
