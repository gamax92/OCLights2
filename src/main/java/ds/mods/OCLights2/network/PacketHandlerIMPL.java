package ds.mods.OCLights2.network;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import li.cil.oc.api.machine.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.Level;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import ds.mods.OCLights2.ClientDrawThread;
import ds.mods.OCLights2.Config;
import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.block.tileentity.TileEntityAdvancedlight;
import ds.mods.OCLights2.block.tileentity.TileEntityExternalMonitor;
import ds.mods.OCLights2.block.tileentity.TileEntityGPU;
import ds.mods.OCLights2.block.tileentity.TileEntityMonitor;
import ds.mods.OCLights2.block.tileentity.TileEntityTTrans;
import ds.mods.OCLights2.client.ClientProxy;
import ds.mods.OCLights2.gpu.DrawCMD;
import ds.mods.OCLights2.gpu.GPU;
import ds.mods.OCLights2.gpu.Texture;
import ds.mods.OCLights2.network.PacketHandler.PacketMessage;
import ds.mods.OCLights2.serialize.Serialize;

public class PacketHandlerIMPL {
	static final byte NET_GPUDRAWLIST = 0;
	static final byte NET_GPUEVENT = 1;
	static final byte NET_GPUDOWNLOAD = 2;
	static final byte NET_GPUMOUSE = 3;
	static final byte NET_GPUKEY = 4;
	static final byte NET_GPUTILE = 5;
	static final byte NET_GPUINIT = 6;
	static final byte NET_LIGHT = 7;
	static final byte NET_SPLITPACKET = 8;
	static final byte NET_SYNC = 9;
	static final byte NET_SCREENSHOT = 10;
	static boolean doThreadding = true;
	static ClientDrawThread thread;
	{
		if (doThreadding) {
			thread = new ClientDrawThread();
			thread.setName("OCLights2 Draw Thread");
			thread.start();
		}
	}

	public void onPacketData(INetHandler manager, PacketMessage packet, EntityPlayer player) {
		ByteArrayDataInput maindat = ByteStreams.newDataInput(packet.data);
		byte typ = maindat.readByte();
		if (typ == NET_SPLITPACKET) {
			try {
				byte[] data = PacketChunker.instance.getBytes(packet);
				if (data != null) { // data is now the full, combined data

					// im crap at gzip ;_; but hey! it works!
					ByteArrayInputStream input = new ByteArrayInputStream(data);
					GZIPInputStream zipStream = new GZIPInputStream(input);
					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					while (zipStream.available() > 0) {
						bo.write(zipStream.read());
					}
					ByteArrayDataInput dat = ByteStreams.newDataInput(bo.toByteArray());
					zipStream.close();
					input.close();
					typ = dat.readByte();
					if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
						ServerSide(typ, dat, player);
					} else if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
						ClientSide(typ, dat);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
				ServerSide(typ, maindat, player);
			} else if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				ClientSide(typ, maindat);
			}
		}
	}

	public static void ServerSide(byte typ, ByteArrayDataInput PacketData, EntityPlayer player) {
		switch (typ) {
		case (NET_GPUMOUSE): {
			int x = PacketData.readInt();
			int y = PacketData.readInt();
			int z = PacketData.readInt();
			TileEntityMonitor mtile = (TileEntityMonitor) player.worldObj.getTileEntity(x, y, z);
			if (mtile != null) {
				int cmd = PacketData.readInt();
				switch (cmd) {
				case 0: {
					// MouseStart//
					int button = PacketData.readInt();
					int mx = PacketData.readInt();
					int my = PacketData.readInt();
					for (GPU g : mtile.mon.gpu) {
						TileEntityGPU tile = g.tile;
						if (tile != null)
							tile.startClick(player, button, mx, my);
					}
					break;
				}
				case 1: {
					// MouseMove//
					int mx = PacketData.readInt();
					int my = PacketData.readInt();
					for (GPU g : mtile.mon.gpu) {
						TileEntityGPU tile = g.tile;
						if (tile != null)
							tile.moveClick(player, mx, my);
					}
					break;
				}
				case 2: {
					// MouseEnd//
					for (GPU g : mtile.mon.gpu) {
						TileEntityGPU tile = g.tile;
						if (tile != null)
							tile.endClick(player);
					}
				}
				}
			}
			break;
		}
		case (NET_GPUEVENT): {
			int x = PacketData.readInt();
			int y = PacketData.readInt();
			int z = PacketData.readInt();
			TileEntityMonitor mtile = (TileEntityMonitor) player.worldObj.getTileEntity(x, y, z);
			if (mtile != null) {
				String event = PacketData.readUTF();
				int len = PacketData.readInt();
				Object[] args = new Object[len + 1];
				for (int i1 = 1; i1 <= len; i1++) {
					int type = PacketData.readInt();
					switch (type) {
					case 0: {
						args[i1] = PacketData.readInt();
						break;
					}
					case 1: {
						args[i1] = PacketData.readUTF();
						break;
					}
					case 2: {
						args[i1] = String.valueOf(PacketData.readChar());
						break;
					}
					}
				}
				for (GPU g : mtile.mon.gpu) {
					TileEntityGPU tile = g.tile;
					args[0] = tile.node().address();
					if (tile != null) {
						for (Context c : tile.comp)
							if (c != null) {
								c.signal(event, args);
							}
					}
				}
			}
			break;
		}
		case (NET_GPUDOWNLOAD): {
			int x = PacketData.readInt();
			int y = PacketData.readInt();
			int z = PacketData.readInt();
			TileEntityGPU tile = (TileEntityGPU) player.worldObj.getTileEntity(x, y, z);
			if (tile != null) {
				PacketSenders.sendPacketToPlayer(x, y, z, tile, player);
				for (int i1 = 0; i1 < tile.gpu.textures.length; i1++) {
					if (tile.gpu.textures[i1] != null) {
						PacketSenders.sendTextures(player, tile.gpu.textures[i1], i1, x, y, z);
					}
				}
			}
			break;
		}
		case (NET_SCREENSHOT): {
			int x = PacketData.readInt();
			int y = PacketData.readInt();
			int z = PacketData.readInt();
			int len = PacketData.readInt();
			byte[] arr = new byte[len];
			PacketData.readFully(arr);
			TileEntityTTrans tile = (TileEntityTTrans) player.worldObj.getTileEntity(x, y, z);
			if (tile != null) {
				HashMap<Double, Double> table = new HashMap<Double, Double>();
				ByteArrayInputStream in = new ByteArrayInputStream(arr);
				Double at = 1D;
				int r;
				while ((r = in.read()) != -1) {
					table.put(at++, (double) r);
				}

				for (GPU g : tile.mon.gpu) {
					TileEntityGPU gtile = g.tile;
					if (gtile != null) {
						for (Context c : gtile.comp)
							if (c != null) {
								c.signal("tablet_image", new Object[] { table });
							}
					}
				}
			}
		}
		}
	}

	public static void ClientSide(byte typ, ByteArrayDataInput PacketData) {
		switch (typ) {
		case (NET_GPUDRAWLIST): {
			int x = PacketData.readInt();
			int y = PacketData.readInt();
			int z = PacketData.readInt();
			TileEntityGPU tile = (TileEntityGPU) ClientProxy.getClientWorld().getTileEntity(x, y, z);
			if (tile != null) {
				int len = PacketData.readInt();
				GPU gpu = tile.gpu;
				for (int i = 0; i < len; i++) {
					DrawCMD cmd = new DrawCMD();
					cmd.cmd = TileEntityGPU.EnumCache[PacketData.readInt()];
					int lent = PacketData.readInt();
					cmd.args = new Object[lent];
					for (int g = 0; g < lent; g++) {
						if (PacketData.readByte() == -1) {
							int count = PacketData.readInt();
							cmd.args[g] = new Object[count];
							Object[] arr = (Object[]) cmd.args[g];
							for (int e = 0; e < count; e++) {
								arr[e] = Serialize.unserialize(PacketData);
							}
						} else {
							PacketData.skipBytes(-1);
							cmd.args[g] = Serialize.unserialize(PacketData);
						}
					}
					if (!doThreadding)
						try {
							tile.gpu.processCommand(cmd);
						} catch (Exception e) {
							OCLights2.debug("failed to process command in clientsidedrawlist");
						}
					else {
						if (!thread.isAlive()) {
							OCLights2.logger.log(Level.FATAL, "The client draw thread died, restarting");
							thread = new ClientDrawThread();
							thread.setName("OCLights2 Draw Thread");
							thread.start();
						}
						if (thread.draws.get(tile.gpu) == null) {
							thread.draws.put(tile.gpu, new ArrayDeque<DrawCMD>());
						}
						thread.draws.get(tile.gpu).addLast(cmd);
					}
				}
			}
			break;
		}
		case (NET_GPUINIT): {
			int x = PacketData.readInt();
			int y = PacketData.readInt();
			int z = PacketData.readInt();
			TileEntityGPU tile = (TileEntityGPU) ClientProxy.getClientWorld().getTileEntity(x, y, z);
			if (tile == null)
				return;
			if (tile.gpu == null)
				return;
			tile.gpu.color = new Color(PacketData.readInt(), true);
			double[] matrix = new double[6];
			readMatrix(PacketData, matrix);
			tile.gpu.transform = new AffineTransform(matrix);
			tile.gpu.transformStack.clear();
			for (int i = 0; i < PacketData.readInt(); i++) {
				readMatrix(PacketData, matrix);
				tile.gpu.transformStack.push(new AffineTransform(matrix));
			}
			break;
		}
		case (NET_GPUDOWNLOAD): {
			int x = PacketData.readInt();
			int y = PacketData.readInt();
			int z = PacketData.readInt();
			TileEntityGPU tile = (TileEntityGPU) ClientProxy.getClientWorld().getTileEntity(x, y, z);
			if (tile == null || tile.gpu == null)
				return;
			recTexture(PacketData, tile);
			break;
		}
		case (NET_GPUTILE): {
			int x = PacketData.readInt();
			int y = PacketData.readInt();
			int z = PacketData.readInt();
			TileEntityExternalMonitor tile = (TileEntityExternalMonitor) ClientProxy.getClientWorld().getTileEntity(x, y, z);
			if (tile != null) {
				tile.handleUpdatePacket(PacketData);
			}
			break;
		}
		case (NET_LIGHT): {
			int x = PacketData.readInt();
			int y = PacketData.readInt();
			int z = PacketData.readInt();
			TileEntityAdvancedlight tile = (TileEntityAdvancedlight) ClientProxy.getClientWorld().getTileEntity(x, y, z);
			if (tile != null) {
				TileEntityAdvancedlight ntile = tile;
				ntile.r = PacketData.readFloat();
				ntile.g = PacketData.readFloat();
				ntile.b = PacketData.readFloat();
			}
			break;
		}
		case (NET_SYNC): {
			Config.widthMon = PacketData.readInt();
			Config.heightMon = PacketData.readInt();
			Config.widthTab = PacketData.readInt();
			Config.heightTab = PacketData.readInt();
			Config.widthExt = PacketData.readInt();
			Config.heightExt = PacketData.readInt();
			Config.resExt = PacketData.readInt();
		}
		}
	}

	public static void recTexture(ByteArrayDataInput dat, TileEntityGPU tile) {
		GPU gpu = tile.gpu;
		int id = dat.readInt();
		int w = dat.readInt();
		int h = dat.readInt();
		if (gpu.textures[id] == null) {
			gpu.textures[id] = new Texture(w, h);
		}
		Texture tex = gpu.textures[id];
		if (tex.getWidth() != w || tex.getHeight() != h) {
			gpu.textures[id] = new Texture(w, h);
			tex = gpu.textures[id];
		}
		int[] arr = new int[dat.readInt()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = dat.readInt();
		}
		tex.img.setRGB(0, 0, w, h, arr, 0, w);
	}

	public static void readMatrix(ByteArrayDataInput dat, double[] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = dat.readDouble();
		}
	}

	public void playerLoggedIn(EntityPlayer player, INetHandler netHandler, NetworkManager manager) {
		if (MinecraftServer.getServer().isDedicatedServer()) {
			PacketSenders.SYNC(Config.widthMon, Config.heightMon,
					Config.widthTab, Config.heightTab,
					Config.widthExt, Config.heightExt, Config.resExt, player);
		}
	}

	public void clientLoggedIn(INetHandler clientHandler, NetworkManager manager) {
		if (Minecraft.getMinecraft().isSingleplayer()) {
			OCLights2.debug("Singleplayer detected, sync not needed");
		} else {
			OCLights2.debug("PREP'd for SYNC");
		}
	}
	
}