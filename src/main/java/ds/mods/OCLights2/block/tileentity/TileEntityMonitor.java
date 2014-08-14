package ds.mods.OCLights2.block.tileentity;

import java.awt.Color;

import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import net.minecraft.tileentity.TileEntity;
import ds.mods.OCLights2.gpu.GPU;
import ds.mods.OCLights2.gpu.Monitor;

public class TileEntityMonitor extends TileEntity {
	public Monitor mon;

	public TileEntityMonitor()
	{
		mon = new Monitor(256,144,getMonitorObject());
		mon.tex.fill(Color.black);
	}

	public void connect(GPU g)
	{
		mon.addGPU(g);
	}

	public MonitorObject getMonitorObject()
	{
		return new MonitorObject();
	}

	public class MonitorObject
	{
		@Callback(direct=true)
		public Object[] getResolution(Context context, Arguments arguments) throws Exception {
				return new Object[]{mon.getWidth(),mon.getHeight()};
		}
	}
}
