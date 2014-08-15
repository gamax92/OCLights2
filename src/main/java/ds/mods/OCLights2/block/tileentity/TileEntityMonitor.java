package ds.mods.OCLights2.block.tileentity;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.prefab.AbstractValue;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
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
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
		this.readFromNBT(pkt.data);
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
			OCLights2.logger.log(Level.WARNING, "Failed to save monitor texture");
			OCLights2.logger.log(Level.WARNING, e.getLocalizedMessage());
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
				OCLights2.logger.log(Level.WARNING, "Failed to load monitor texture");
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
