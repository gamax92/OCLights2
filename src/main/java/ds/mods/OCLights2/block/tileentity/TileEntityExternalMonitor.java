package ds.mods.OCLights2.block.tileentity;

import java.awt.Color;

import ds.mods.OCLights2.Config;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLLog;
import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.gpu.Monitor;
import ds.mods.OCLights2.gpu.Texture;
import ds.mods.OCLights2.network.PacketSenders;

public class TileEntityExternalMonitor extends TileEntityMonitor {
	private static final int WIDTH = Config.widthExt;
	private static final int HEIGHT = Config.heightExt;
	private static final int RES = Config.resExt;
	public static final int TICKS_TIL_SYNC = 20 * 600;
	public boolean dirty = false;
	public boolean m_destroyed = false;
	public boolean m_ignoreMe = false;
	public int m_connections = 0;
	public int m_totalConnections = 0;
	public int m_width = 1;
	public int m_height = 1;
	public int m_xIndex = 0;
	public int m_yIndex = 0;
	public int m_dir = 2;
	public int m_tts = TICKS_TIL_SYNC;
	public Monitor m_originMonitor;

	public TileEntityExternalMonitor() {
		mon = new Monitor(RES, RES, getMonitorObject());
		mon.tex.fill(Color.black);
	}

	@Override
	public MonitorObject getMonitorObject() {
		return new ExternalMonitorObject();
	}
	
	@Override
	public Packet getDescriptionPacket() {
		dirty = true;
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
		nbt.setInteger("xIndex", this.m_xIndex);
		nbt.setInteger("yIndex", this.m_yIndex);
		nbt.setInteger("width", this.m_width);
		nbt.setInteger("height", this.m_height);
		nbt.setInteger("dir", this.m_dir);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.m_xIndex = nbt.getInteger("xIndex");
		this.m_yIndex = nbt.getInteger("yIndex");
		this.m_width = nbt.getInteger("width");
		this.m_height = nbt.getInteger("height");
		this.m_dir = nbt.getInteger("dir");
		dirty = true;
	}
	
	public void destroy() {
		if (!this.m_destroyed) {
			this.m_destroyed = true;
			if (!this.worldObj.isRemote) {
				contractNeighbours();
			}
		}
	}

	public boolean isDestroyed() {
		return this.m_destroyed;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if ((getXIndex() != 0) || (getYIndex() != 0)) {
			return OCLights2.monitorBig.getCollisionBoundingBoxFromPool(
					this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		}

		TileEntityExternalMonitor monitor = getNeighbour(this.m_width - 1,
				this.m_height - 1);

		if (monitor != null) {
			int minX = Math.min(this.xCoord, monitor.xCoord);
			int minY = Math.min(this.yCoord, monitor.yCoord);
			int minZ = Math.min(this.zCoord, monitor.zCoord);

			int maxX = (minX == monitor.xCoord ? this.xCoord : monitor.xCoord) + 1;
			int maxY = (minY == monitor.yCoord ? this.yCoord : monitor.yCoord) + 1;
			int maxZ = (minZ == monitor.zCoord ? this.zCoord : monitor.zCoord) + 1;
			return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
		}

		return OCLights2.monitorBig.getCollisionBoundingBoxFromPool(
				this.worldObj, this.xCoord, this.yCoord, this.zCoord);
	}

	public void rebuildTerminal(Monitor copyFrom) {
		int termWidth = this.m_width * RES;
		int termHeight = this.m_height * RES;
		this.mon.resize(termWidth, termHeight);
		this.mon.removeAllGPUs();
		propogateTerminal();
	}

	public void propogateTerminal() {
		if (origin() == null) return;
		Monitor originTerminal = origin().mon;
		Texture old = new Texture(originTerminal.tex.getWidth(), originTerminal.tex.getHeight());
		old.drawTexture(originTerminal.tex, 0, 0, Color.white);
		originTerminal.removeAllGPUs();
		if (originTerminal.getWidth() != m_width * RES || originTerminal.getHeight() != m_height * RES) {
			originTerminal = new Monitor(m_width * RES, m_height * RES, getMonitorObject());
		} else {
			originTerminal.tex.drawTexture(old, 0, 0, Color.white);
			originTerminal.tex.texUpdate();	
		}
		origin().mon = originTerminal;
		for (int y = 0; y < this.m_height; y++) {
			for (int x = 0; x < this.m_width; x++) {
				TileEntityExternalMonitor monitor = getNeighbour(x, y);
				if (monitor != null) {
					{
						if ((x != 0) || (y != 0)) {
							monitor.mon.removeAllGPUs();
							monitor.mon = originTerminal;
						}
					}
				}
			}
		}
	}

	public int getDir() {
		return this.m_dir;
	}

	public void setDir(int _dir) {
		this.m_dir = _dir;
	}

	public int getRight() {
		int dir = getDir();
		switch (dir) {
		case 0:
			return 5;
		case 1:
			return 2;
		case 2:
			return 4;
		case 3:
			return 3;
		default:
			FMLLog.info("Dir: "+dir);
		}
		return dir;
	}

	public int getWidth() {
		return this.m_width;
	}

	public int getHeight() {
		return this.m_height;
	}

	public int getXIndex() {
		return this.m_xIndex;
	}

	public int getYIndex() {
		return this.m_yIndex;
	}

	public TileEntityExternalMonitor getSimilarMonitorAt(int x, int y, int z) {
		if ((y >= 0) && (y < this.worldObj.getHeight())) {
			if (this.worldObj.getChunkProvider().chunkExists(x >> 4, z >> 4)) {
				TileEntity tile = this.worldObj.getTileEntity(x, y, z);
				if ((tile != null) && ((tile instanceof TileEntityExternalMonitor))) {
					TileEntityExternalMonitor monitor = (TileEntityExternalMonitor) tile;
					if ((monitor.getDir() == getDir())
							&& (!monitor.m_destroyed) && (!monitor.m_ignoreMe)) {
						return monitor;
					}
				}
			}
		}

		return null;
	}

	public TileEntityExternalMonitor getNeighbour(int x, int y) {
		int right = getRight();
		int xOffset = -this.m_xIndex + x;
		return getSimilarMonitorAt(this.xCoord
						+ net.minecraft.util.Facing.offsetsXForSide[right] * xOffset,
				this.yCoord - this.m_yIndex + y, this.zCoord
						+ net.minecraft.util.Facing.offsetsZForSide[right]
						* xOffset);
	}

	public TileEntityExternalMonitor origin() {
		return getNeighbour(0, 0);
	}

	public void resize(int width, int height) {
		resize(width, height, false);
	}

	public void resize(int width, int height, boolean ignoreTerminals) {
		int right = getRight();
		int rightX = net.minecraft.util.Facing.offsetsXForSide[right];
		int rightZ = net.minecraft.util.Facing.offsetsZForSide[right];

		int totalConnections = 0;
		int maxConnections = 0;
		Monitor existingTerminal = null;
		int existingScale = 2;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				TileEntityExternalMonitor monitor = getSimilarMonitorAt(this.xCoord
						+ rightX * x, this.yCoord + y, this.zCoord + rightZ * x);
				if (monitor != null) {
					totalConnections += monitor.m_connections;
					if ((!ignoreTerminals)
							&& (monitor.m_connections > maxConnections)) {
						// synchronized (monitor.mon)
						{
							existingTerminal = monitor.mon;
						}
						maxConnections = monitor.m_connections;
					}

					monitor.m_totalConnections = 0;
					monitor.m_xIndex = x;
					monitor.m_yIndex = y;
					monitor.m_width = width;
					monitor.m_height = height;
					monitor.dirty = true;
				}
			}
		}

		this.m_totalConnections = totalConnections;
		rebuildTerminal(existingTerminal);

		this.worldObj.markBlockRangeForRenderUpdate(this.xCoord, this.yCoord,
				this.zCoord, this.xCoord + rightX * width,
				this.yCoord + height, this.zCoord + rightZ * width);
	}

	public boolean mergeLeft() {
		TileEntityExternalMonitor left = getNeighbour(-1, 0);
		if ((left != null) && (left.m_yIndex == 0)
				&& (left.m_height == this.m_height)) {
			int width = left.m_width + this.m_width;
			if (width <= WIDTH) {
				left.origin().resize(width, this.m_height);
				left.expand();
				return true;
			}
		}
		return false;
	}

	public boolean mergeRight() {
		TileEntityExternalMonitor right = getNeighbour(this.m_width, 0);
		if ((right != null) && (right.m_yIndex == 0)
				&& (right.m_height == this.m_height)) {
			int width = this.m_width + right.m_width;
			if (width <= WIDTH) {
				origin().resize(width, this.m_height);
				expand();
				return true;
			}
		}
		return false;
	}

	public boolean mergeUp() {
		TileEntityExternalMonitor above = getNeighbour(0, this.m_height);
		if ((above != null) && (above.m_xIndex == 0)
				&& (above.m_width == this.m_width)) {
			int height = above.m_height + this.m_height;
			if (height <= HEIGHT) {
				origin().resize(this.m_width, height);
				expand();
				return true;
			}
		}
		return false;
	}

	public boolean mergeDown() {
		TileEntityExternalMonitor below = getNeighbour(0, -1);
		if ((below != null) && (below.m_xIndex == 0)
				&& (below.m_width == this.m_width)) {
			int height = this.m_height + below.m_height;
			if (height <= HEIGHT) {
				below.origin().resize(this.m_width, height);
				below.expand();
				return true;
			}
		}
		return false;
	}

	public void expand() {
		dirty = true;
		while ((mergeLeft()) || (mergeRight()) || (mergeUp()) || (mergeDown())) {
		}
		;
	}

	public void contractNeighbours() {
		this.mon.removeAllGPUs();
		this.m_ignoreMe = true;
		if (this.m_xIndex > 0) {
			TileEntityExternalMonitor left = getNeighbour(this.m_xIndex - 1,
					this.m_yIndex);
			if (left != null) {
				left.contract();
			}
		}
		if (this.m_xIndex + 1 < this.m_width) {
			TileEntityExternalMonitor right = getNeighbour(this.m_xIndex + 1,
					this.m_yIndex);
			if (right != null) {
				right.contract();
			}
		}
		if (this.m_yIndex > 0) {
			TileEntityExternalMonitor below = getNeighbour(this.m_xIndex,
					this.m_yIndex - 1);
			if (below != null) {
				below.contract();
			}
		}
		if (this.m_yIndex + 1 < this.m_height) {
			TileEntityExternalMonitor above = getNeighbour(this.m_xIndex,
					this.m_yIndex + 1);
			if (above != null) {
				above.contract();
			}
		}
		this.m_ignoreMe = false;
	}

	public void contract() {
		dirty = true;
		int height = this.m_height;
		int width = this.m_width;

		TileEntityExternalMonitor origin = origin();
		if (origin == null) {
			TileEntityExternalMonitor right = null;
			TileEntityExternalMonitor below = null;
			if (width > 1) {
				right = getNeighbour(1, 0);
			}
			if (height > 1) {
				below = getNeighbour(0, 1);
			}

			Monitor claimedTerminal = null;
			if (right != null) {
				right.resize(width - 1, 1);
				claimedTerminal = right.mon;
			}
			if (below != null) {
				below.resize(width, height - 1, claimedTerminal != null);
			}
			if (right != null) {
				right.expand();
			}
			if (below != null) {
				below.expand();
			}
			mon.removeAllGPUs();
			mon = new Monitor(RES, RES, getMonitorObject());
			return;
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				TileEntityExternalMonitor monitor = origin.getNeighbour(x, y);
				if (monitor == null) {
					TileEntityExternalMonitor above = null;
					TileEntityExternalMonitor left = null;
					TileEntityExternalMonitor right = null;
					TileEntityExternalMonitor below = null;

					Monitor claimedTerminal = null;
					if (y > 0) {
						above = origin;
						above.resize(width, y);
						claimedTerminal = above.mon;
					}
					if (x > 0) {
						left = origin.getNeighbour(0, y);
						if (left != null) {
							left.resize(x, 1, claimedTerminal != null);
							if (claimedTerminal == null) {
								claimedTerminal = left.mon;
							}
						}
					}
					if (x + 1 < width) {
						right = origin.getNeighbour(x + 1, y);
						if (right != null) {
							right.resize(width - (x + 1), 1,
									claimedTerminal != null);
							if (claimedTerminal == null) {
								claimedTerminal = right.mon;
							}
						}
					}
					if (claimedTerminal != null)
						claimedTerminal.removeAllGPUs();
					if (y + 1 < height) {
						below = origin.getNeighbour(0, y + 1);
						if (below != null) {
							below.resize(width, height - (y + 1),
									claimedTerminal != null);
						}
					}

					if (above != null) {
						above.expand();
					}
					if (left != null) {
						left.expand();
					}
					if (right != null) {
						right.expand();
					}
					if (below != null) {
						below.expand();
					}
					return;
				}
			}
		}
	}

	@Override
	public void updateEntity() {
		if (!worldObj.isRemote && (dirty || m_tts-- < 0)) {
			// Send update packet
			PacketSenders.ExternalMonitorUpdate(xCoord, yCoord, zCoord, worldObj.provider.dimensionId,m_width, m_height,m_xIndex,m_yIndex,m_dir);
			dirty = false;
			m_tts = TICKS_TIL_SYNC;
		}
	}

	public void handleUpdatePacket(ByteArrayDataInput dat) {
		m_width = dat.readInt();
		m_height = dat.readInt();
		m_xIndex = dat.readInt();
		m_yIndex = dat.readInt();
		m_dir = dat.readInt();
		propogateTerminal();
	}

	public class ExternalMonitorObject extends MonitorObject {
		@Callback(direct=true)
		public Object[] getDPM(Context context, Arguments arguments) {
			return new Object[]{RES};
		}
		
		@Callback(direct=true)
		public Object[] getBlockResolution(Context context, Arguments arguments) {
			return new Object[]{m_width,m_height};
		}
	}
}
