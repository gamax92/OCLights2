package ds.mods.OCLights2.block.tileentity;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.AbstractValue;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import org.apache.logging.log4j.Level;

import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.gpu.GPU;
import ds.mods.OCLights2.gpu.Monitor;

public class TileEntityMonitor extends TileEntity {
	public Monitor mon;

	public TileEntityMonitor() {
		mon = new Monitor(256, 144, getMonitorObject());
		mon.tex.fill(Color.black);
	}

	public void connect(GPU g) {
		mon.addGPU(g);
	}

	public MonitorObject getMonitorObject() {
		return new MonitorObject();
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, worldObj.provider.dimensionId, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.func_148857_g());
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(mon.tex.img, "png", output);
			byte[] data = output.toByteArray();
			nbt.setByteArray("texture", data);
		} catch (IOException e) {
			OCLights2.logger.log(Level.WARN, "Failed to save monitor texture");
			OCLights2.logger.log(Level.WARN, e.getLocalizedMessage());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (nbt.hasKey("texture")) {
			byte[] texture = nbt.getByteArray("texture");
			BufferedImage img = null;
			try {
				img = ImageIO.read(new ByteArrayInputStream(texture));
			} catch (IOException e) {
			}
			if (img == null) {
				OCLights2.logger.log(Level.WARN, "Failed to load monitor texture");
			} else {
				mon.tex.graphics.drawImage(img, 0, 0, null);
			}
		}
	}

	public class MonitorObject extends AbstractValue {
		@Callback(direct = true)
		public Object[] getResolution(Context context, Arguments arguments) throws Exception {
			return new Object[] { mon.getWidth(), mon.getHeight() };
		}
	}
}
