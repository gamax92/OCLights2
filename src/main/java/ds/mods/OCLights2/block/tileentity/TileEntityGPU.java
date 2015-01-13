package ds.mods.OCLights2.block.tileentity;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import li.cil.oc.api.FileSystem;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import ds.mods.OCLights2.CommandEnum;
import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.converter.ConvertInteger;
import ds.mods.OCLights2.gpu.DrawCMD;
import ds.mods.OCLights2.gpu.GPU;
import ds.mods.OCLights2.gpu.Monitor;
import ds.mods.OCLights2.gpu.Texture;
import ds.mods.OCLights2.network.PacketSenders;
import ds.mods.OCLights2.utils.Convert;

public class TileEntityGPU extends TileEntity implements Environment {
	public GPU gpu;
	private ArrayList<DrawCMD> newarr = new ArrayList<DrawCMD>();
	public ArrayList<Context> comp = new ArrayList<Context>();
	private TreeMap<String, Integer> playerToClickMap = new TreeMap<String, Integer>();
	private TreeMap<Integer, int[]> clickToDataMap = new TreeMap<Integer, int[]>();
	public int[] addedType = new int[1025];
	private boolean frame = false;
	private byte ticks = 0;
	private boolean sentOnce = false;
	public static final CommandEnum[] EnumCache = CommandEnum.values();
	private ManagedEnvironment fileSystem;
	protected Node node;
	protected boolean addedToNetwork = false;

	public TileEntityGPU() {
		gpu = new GPU(1024 * 8);
		gpu.tile = this;
		fileSystem = FileSystem.asManagedEnvironment(FileSystem.fromClass(OCLights2.class, "oclights", "lua"), "ocl_gpu");
		node = Network.newNode(this, Visibility.Network).withComponent("ocl_gpu").create();
	}

	public void startClick(EntityPlayer player, int button, int x, int y) {
		int id = new Random().nextInt();
		while (playerToClickMap.containsValue(id)) {
			id = new Random().nextInt();
		}
		playerToClickMap.put(player.getDisplayName(), id);
		clickToDataMap.put(id, new int[] { button, x, y });

		String event = "monitor_down";
		Object[] args = new Object[] { node.address(), x, y, button, id };
		for (Context c : comp) {
			c.signal(event, args);
		}
	}

	public void moveClick(EntityPlayer player, int nx, int ny) {
		int id = playerToClickMap.get(player.getDisplayName());
		int[] data = clickToDataMap.get(id);
		int button = data[0];
		data[1] = nx;
		data[2] = ny;

		String event = "monitor_move";
		Object[] args = new Object[] { node.address(), nx, ny, button, id };
		for (Context c : comp) {
			c.signal(event, args);
		}
	}

	public void endClick(EntityPlayer player) {
		int id = playerToClickMap.get(player.getDisplayName());
		int[] data = clickToDataMap.get(id);
		int button = data[0];
		int x = data[1];
		int y = data[2];

		String event = "monitor_up";
		Object[] args = new Object[] { node.address(), x, y, button, id };
		for (Context c : comp) {
			c.signal(event, args);
		}
		playerToClickMap.remove(player.getDisplayName());
		clickToDataMap.remove(id);
	}

	@Callback(direct=true)
	public Object[] fill(Context context, Arguments args) throws Exception {
		//fill
		DrawCMD cmd = new DrawCMD();
		cmd.cmd = CommandEnum.Fill;
		gpu.processCommand(cmd);
		gpu.drawlist.push(cmd);
		return null;
	}

	@Callback(direct=true)
	public Object[] createTexture(Context context, Arguments args) throws Exception {
		//createTexture
		if (args.count() > 1) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0), args.checkInteger(1) };
			cmd.cmd = CommandEnum.CreateTexture;
			cmd.args = nargs;
			Object[] ret = gpu.processCommand(cmd);
			int id = (Integer) ret[0];
			if (id == -1) {
				throw new Exception("createTexture: Not enough memory");
			} else if (id == -2) {
				throw new Exception("createTexture: Not enough texture slots");
			} else {
				gpu.drawlist.push(cmd);
				return ret;
			}
		} else {
			throw new Exception("createTexture: Argument Error: width, height expected");
		}
	}

	@Callback(direct=true)
	public Object[] getFreeMemory(Context context, Arguments args) {
		//getFreeMemory
		return new Object[] { gpu.getFreeMemory() };
	}

	@Callback(direct=true)
	public Object[] getTotalMemory(Context context, Arguments args) {
		//getTotalMemory
		return new Object[] { gpu.maxmem };
	}

	@Callback(direct=true)
	public Object[] getUsedMemory(Context context, Arguments args) {
		//getUsedMemory
		return new Object[] { gpu.getUsedMemory() };
	}

	@Callback(direct=true)
	public Object[] bindTexture(Context context, Arguments args) throws Exception {
		//bindTexture
		if (args.count() > 0) {
			if (gpu.textures[args.checkInteger(0)] == null)
				throw new Exception("bindTexture: Texture does not exist");
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0) };
			cmd.cmd = CommandEnum.BindTexture;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("bindTexture: Argument Error: textureid expected");
		}
		return null;
	}

	@Callback(direct=true)
	public Object[] plot(Context context, Arguments args) throws Exception {
		//was plot and setColorRGB is now plot
		if (args.count() >= 2) {
			int x = args.checkInteger(0);
			int y = args.checkInteger(1);
			Point2D point = gpu.transform.transform(new Point2D.Double(x, y), null);
			double tx = point.getX();
			double ty = point.getY();
			int w = gpu.bindedTexture.getWidth();
			int h = gpu.bindedTexture.getHeight();
			if (tx < 0 || ty < 0 || tx > w || ty > h) //Don't draw if out of bounds!
				return null;
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { x, y };
			cmd.cmd = CommandEnum.Plot;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("plot: Argument Error: x, y expected");
		}
		return null;
	}

	@Callback(direct=true)
	public Object[] drawTexture(Context context, Arguments args) throws Exception {
		//drawTexture
		if (args.count() == 3) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { 0, args.checkInteger(0), args.checkInteger(1), args.checkInteger(2) };
			cmd.cmd = CommandEnum.DrawTexture;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else if (args.count() > 6) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { 1, args.checkInteger(0), args.checkInteger(1), args.checkInteger(2), args.checkInteger(3), args.checkInteger(4), args.checkInteger(5), args.checkInteger(6) };
			cmd.cmd = CommandEnum.DrawTexture;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("drawTexture: Argument Error: textureid, x, y expected");
		}
		return null;
	}

	@Callback(direct=true)
	public Object[] freeTexture(Context context, Arguments args) throws Exception {
		//freeTexture
		if (args.count() == 1) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0) };
			cmd.cmd = CommandEnum.FreeTexture;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("freeTexture: Argument Error: textureid expected");
		}
		return null;
	}

	@Callback(direct=true)
	public Object[] line(Context context, Arguments args) throws Exception {
		//line
		if (args.count() > 3) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0), args.checkInteger(1), args.checkInteger(2), args.checkInteger(3) };
			cmd.cmd = CommandEnum.Line;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("line: Argument Error: x1, y1, x2, y2 expected");
		}
		return null;
	}

	@Callback(direct=true)
	public Object[] getSize(Context context, Arguments args) throws Exception {
		//getSize
		int tex = gpu.bindedSlot;
		if (args.count() >= 1) {
			tex = args.checkInteger(0);
		}
		if (gpu.textures[tex] == null)
			throw new Exception("getMonitorSize: texture does not exist");
		Texture texture = gpu.textures[tex];
		return new Object[] { texture.getWidth(), texture.getHeight() };
	}

	@Callback(direct=true)
	public Object[] getPixelColor(Context context, Arguments args) throws Exception {
		//getPixelColor
		if (args.count() > 1) {
			int x = args.checkInteger(0);
			int y = args.checkInteger(1);
			int[] dat = gpu.bindedTexture.getRGB(x, y);
			return new Object[] { dat[0] & 0xFF, dat[1] & 0xFF, dat[2] & 0xFF, dat[3] & 0xFF };
		} else {
			throw new Exception("getPixelColor: Argument Error: x, y expected");
		}
	}

	@Callback(direct=true)
	public Object[] rectangle(Context context, Arguments args) throws Exception {
		//rectangle
		if (args.count() > 3) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0), args.checkInteger(1), args.checkInteger(2), args.checkInteger(3) };
			cmd.cmd = CommandEnum.Rectangle;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("rectangle: Argument Error: x, y, width, height expected");
		}
		return null;
	}

	@Callback(direct=true)
	public Object[] filledRectangle(Context context, Arguments args) throws Exception {
		//filledrectangle
		if (args.count() > 3) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0), args.checkInteger(1), args.checkInteger(2), args.checkInteger(3) };
			cmd.cmd = CommandEnum.FilledRectangle;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("filledRectangle: Argument Error: x, y, width, height expected");
		}
		return null;
	}
	
	@Callback(direct=true)
	public Object[] triangle(Context context, Arguments args) throws Exception {
		//triangle
		if (args.count() > 5) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0), args.checkInteger(1), args.checkInteger(2), args.checkInteger(3), args.checkInteger(4), args.checkInteger(5) };
			cmd.cmd = CommandEnum.Triangle;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("rectangle: Argument Error: x1, y1, x2, y2, x3, y3 expected");
		}
		return null;
	}

	@Callback(direct=true)
	public Object[] filledTriangle(Context context, Arguments args) throws Exception {
		//filledtriangle
		if (args.count() > 5) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0), args.checkInteger(1), args.checkInteger(2), args.checkInteger(3), args.checkInteger(4), args.checkInteger(5) };
			cmd.cmd = CommandEnum.FilledTriangle;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("filledRectangle: Argument Error: x1, y1, x2, y2, x3, y3 expected");
		}
		return null;
	}
	
	@Callback(direct=true)
	public Object[] oval(Context context, Arguments args) throws Exception {
		//oval
		if (args.count() > 3) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0), args.checkInteger(1), args.checkInteger(2), args.checkInteger(3) };
			cmd.cmd = CommandEnum.Oval;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("rectangle: Argument Error: x, y, width, height expected");
		}
		return null;
	}

	@Callback(direct=true)
	public Object[] filledOval(Context context, Arguments args) throws Exception {
		//filledoval
		if (args.count() > 3) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0), args.checkInteger(1), args.checkInteger(2), args.checkInteger(3) };
			cmd.cmd = CommandEnum.FilledOval;
			cmd.args = nargs;
			gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
		} else {
			throw new Exception("filledRectangle: Argument Error: x, y, width, height expected");
		}
		return null;
	}

	@Callback(direct=true)
	public Object[] getBindedTexture(Context context, Arguments args) {
		//getBindedTexture
		return new Object[] { gpu.bindedSlot };
	}

	@Callback(direct=true)
	public Object[] setPixels(Context context, Arguments args) throws Exception {
		//setPixels
		if (args.count() < 4) {
			throw new Exception("setPixelsRaw: Argument Error: w, h, x, y, {[r,g,b,a]}... expected");
		} else {
			int w = args.checkInteger(0);
			int h = args.checkInteger(1);
			// We send the arguments straight to the GPU!
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[(w * h * 4) + 4 + 1];
			nargs[0] = 0;
			nargs[1] = w;
			nargs[2] = h;
			nargs[3] = args.checkInteger(2);
			nargs[4] = args.checkInteger(3);
			Map m = args.checkTable(4);
			for (int i = 1; i <= (w * h * 4); i++) {
				nargs[i + 4] = ConvertInteger.convert(m.get((double) i)).intValue();
			}
			cmd.cmd = CommandEnum.SetPixels;
			cmd.args = nargs;
			Object[] ret = gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
			return ret;
		}
	}

	@Callback(direct=true)
	public Object[] flipVertically(Context context, Arguments args) throws Exception {
		//flipTextureV
		if (args.count() > 0) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0) };
			cmd.cmd = CommandEnum.FlipVertically;
			cmd.args = nargs;
			Object[] ret = gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
			return ret;
		} else {
			throw new Exception("Number expected.");
		}
	}

	@Callback(value="import",direct=true)
	public Object[] importData(Context context, Arguments args) throws Exception {
		//import
		double a = System.currentTimeMillis();
		Byte[] data;
		if (args.count() == 1 && args.isTable(0)) {
			//One of the things I hate is that ComputerCraft uses Doubles for all their values
			//Double the fun! -alekso56
			Map m = args.checkTable(0);
			data = new Byte[m.size()];
			for (double i = 0; i < data.length; i++) {
				data[(int) i] = ((Double) m.get(i + 1D)).byteValue();
			}
		} else if (args.count() == 1 && args.isString(0)) {
			data = Convert.toByte(args.checkByteArray(0));
		} else if (args.count() == 2 && args.isString(0) && args.isString(1)) {
			String address = args.checkString(0);
			Node testNode = node.network().node(address);
			if (testNode == null)
				throw new Exception("No such component");
			else if (!testNode.canBeReachedFrom(node))
				throw new Exception("Cannot reach component");
			String path = args.checkString(1);
			File cleanPath = new File("/",path);
			File f = new File(OCLights2.proxy.getWorldDir(worldObj), "opencomputers" + File.separatorChar + address + File.separatorChar + cleanPath.getCanonicalPath());
			if (!f.exists())
				throw new Exception("No such file");
			else if (f.isDirectory())
				throw new Exception("Cannot import a directory");
			data = Convert.toByte(FileUtils.readFileToByteArray(f));
		} else {
			throw new Exception("import: Argument Error: (filedata or address, path) expected");
		}
		DrawCMD cmd = new DrawCMD();
		Object[] nargs = new Object[] { data };
		cmd.cmd = CommandEnum.Import;
		cmd.args = nargs;
		int id = (Integer) gpu.processCommand(cmd)[0];
		Texture tex = gpu.textures[id];
		Object[] ret = { id, tex.getWidth(), tex.getHeight() };
		gpu.drawlist.push(cmd);
		double b = System.currentTimeMillis();
		OCLights2.debug("Import time: " + (b - a) + "ms");
		return ret;
	}

	@Callback(value="export",direct=true)
	public Object[] exportData(Context context, Arguments args) throws Exception {
		//export
		if (args.count() > 1) {
			int texid = args.checkInteger(0);
			String format = args.checkString(1);
			if (texid < 0 || texid > gpu.textures.length || gpu.textures[texid] == null) {
				throw new Exception("Texture does not exist.");
			}
			Texture tex = gpu.textures[texid];
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(tex.img, format, output);
			byte[] data = output.toByteArray();
			HashMap<Double, Double> out = new HashMap<Double, Double>();
			for (int i = 0; i < data.length; i++) {
				out.put((double) (i + 1), (double) data[i]);
			}
			return new Object[] { out };
		} else {
			throw new Exception("export: Argument Error: textureid, format expected");
		}
	}

	@Callback(direct=true)
	public Object[] drawText(Context context, Arguments args) throws Exception {
		//Drawtext
		if (args.count() > 2) {
			String str = args.checkString(0);
			int x = args.checkInteger(1);
			int y = args.checkInteger(2);
			Point2D point = gpu.transform.transform(new Point2D.Double(x, y), null);
			double tx = point.getX();
			double ty = point.getY();
			int w = gpu.bindedTexture.getWidth();
			int h = gpu.bindedTexture.getHeight();
			double tw = Texture.getStringWidth(str);
			double th = 8;
			if ((tx < 0 && tx + tw < 0) || (ty < 0 && ty + th < 0) || (tx > w) || (ty > h)) //Don't draw if out of bounds!
			{
				return null;
			}
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[2 + str.length()];
			nargs[0] = x;
			nargs[1] = y;
			for (int i = 0; i < str.length(); i++) {
				nargs[2 + i] = str.charAt(i);
			}
			cmd.cmd = CommandEnum.DrawText;
			cmd.args = nargs;
			Object[] ret = gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
			return ret;
		} else {
			throw new Exception("drawText: Argument Error: text, x, y expected");
		}
	}

	@Callback(direct=true)
	public Object[] getTextWidth(Context context, Arguments args) throws Exception {
		//getTextWidth
		if (args.count() > 0) {
			String str = args.checkString(0);
			return new Object[] { Texture.getStringWidth(str) };
		} else {
			throw new Exception("getTextWidth: Argument Error: text expected");
		}
	}

	@Callback(direct=true)
	public Object[] setColor(Context context, Arguments args) throws Exception {
		//setColor
		if (args.count() > 2) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[4];
			for (int i = 0; i < 4; i++) {
				nargs[i] = args.count() > i ? args.checkInteger(i) : 255;
			}
			if (gpu.color.getRed() == (Integer) nargs[0] && gpu.color.getBlue() == (Integer) nargs[1] && gpu.color.getGreen() == (Integer) nargs[2] && gpu.color.getAlpha() == (Integer) nargs[3]) {
				return null;
			}
			cmd.cmd = CommandEnum.SetColor;
			cmd.args = nargs;
			Object[] ret = gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
			return null;
		} else {
			throw new Exception("setColor: Argument Error: int, int, int[, int] expected");
		}
	}

	@Callback(direct=true)
	public Object[] getColor(Context context, Arguments args) {
		//getColor
		return new Object[] { gpu.color.getRed(), gpu.color.getGreen(), gpu.color.getBlue(), gpu.color.getAlpha() };
	}

	@Callback(direct=true)
	public Object[] translate(Context context, Arguments args) throws Exception {
		//translate
		double x = args.checkDouble(0);
		double y = args.checkDouble(1);
		DrawCMD cmd = new DrawCMD();
		Object[] nargs = new Object[2];
		nargs[0] = x;
		nargs[1] = y;
		cmd.cmd = CommandEnum.Transelate;
		cmd.args = nargs;
		Object[] ret = gpu.processCommand(cmd);
		gpu.drawlist.push(cmd);
		return null;
	}

	@Callback(direct=true)
	public Object[] rotate(Context context, Arguments args) throws Exception {
		//rotate
		double r = args.checkDouble(0);
		DrawCMD cmd = new DrawCMD();
		Object[] nargs = new Object[1];
		nargs[0] = r;
		cmd.cmd = CommandEnum.Rotate;
		cmd.args = nargs;
		Object[] ret = gpu.processCommand(cmd);
		gpu.drawlist.push(cmd);
		return null;
	}

	@Callback(direct=true)
	public Object[] rotateAround(Context context, Arguments args) throws Exception {
		//rotateAround
		double r = args.checkDouble(0);
		double x = args.checkDouble(1);
		double y = args.checkDouble(2);
		DrawCMD cmd = new DrawCMD();
		Object[] nargs = new Object[3];
		nargs[0] = r;
		nargs[1] = x;
		nargs[2] = y;
		cmd.cmd = CommandEnum.RotateAround;
		cmd.args = nargs;
		Object[] ret = gpu.processCommand(cmd);
		gpu.drawlist.push(cmd);
		return null;
	}

	@Callback(direct=true)
	public Object[] scale(Context context, Arguments args) throws Exception {
		//scale
		double x = args.checkDouble(0);
		double y = args.checkDouble(1);
		DrawCMD cmd = new DrawCMD();
		Object[] nargs = new Object[2];
		nargs[0] = x;
		nargs[1] = y;
		cmd.cmd = CommandEnum.Scale;
		cmd.args = nargs;
		Object[] ret = gpu.processCommand(cmd);
		gpu.drawlist.push(cmd);
		return null;
	}

	@Callback(direct=true)
	public Object[] push(Context context, Arguments args) throws Exception {
		//push
		DrawCMD cmd = new DrawCMD();
		cmd.cmd = CommandEnum.Push;
		Object[] ret = gpu.processCommand(cmd);
		gpu.drawlist.push(cmd);
		return null;
	}

	@Callback(direct=true)
	public Object[] pop(Context context, Arguments args) throws Exception {
		//pop
		DrawCMD cmd = new DrawCMD();
		cmd.cmd = CommandEnum.Pop;
		Object[] ret = gpu.processCommand(cmd);
		gpu.drawlist.push(cmd);
		return null;
	}

	@Callback(direct=true)
	public Object[] getMonitor(Context context, Arguments args) {
		//getMonitor
		return new Object[] { gpu.currentMonitor.obj };
	}

	@Callback(direct=true)
	public Object[] blur(Context context, Arguments args) throws Exception {
		//blur
		if (args.count() > 0) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0) };
			cmd.cmd = CommandEnum.Blur;
			cmd.args = nargs;
			Object[] ret = gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
			return ret;
		} else {
			throw new Exception("blur: Argument Error: textureid expected");
		}
	}

	@Callback(direct=true)
	public Object[] startFrame(Context context, Arguments args) {
		//startFrame
		frame = true;
		return null;
	}

	@Callback(direct=true)
	public Object[] endFrame(Context context, Arguments args) {
		//endFrame
		frame = false;
		return null;
	}

	@Callback(direct=true)
	public Object[] clearRectangle(Context context, Arguments args) throws Exception {
		//clearRect
		if (args.count() >= 4) {
			DrawCMD cmd = new DrawCMD();
			Object[] nargs = new Object[] { args.checkInteger(0), args.checkInteger(1), args.checkInteger(2), args.checkInteger(3) };
			cmd.cmd = CommandEnum.ClearRectangle;
			cmd.args = nargs;
			Object[] ret = gpu.processCommand(cmd);
			gpu.drawlist.push(cmd);
			return ret;
		} else {
			throw new Exception("clearRect: Argument Error: x, y, width, height expected");
		}
	}

	@Callback(direct=true)
	public Object[] origin(Context context, Arguments args) throws Exception {
		//origin
		DrawCMD cmd = new DrawCMD();
		Object[] nargs = new Object[] {};
		cmd.cmd = CommandEnum.Origin;
		cmd.args = nargs;
		Object[] ret = gpu.processCommand(cmd);
		gpu.drawlist.push(cmd);
		return ret;
	}

	@Override
	public void onConnect(Node node) {
		if (node.host() instanceof Context) {
			comp.add((Context) node.host());
			node.connect(fileSystem.node());
		}
	}

	@Override
	public void onDisconnect(Node node) {
		if (node.host() instanceof Context) {
			comp.remove((Context) node.host());
			node.disconnect(fileSystem.node());
		} else if (node == this.node) {
			fileSystem.node().remove();
		}
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
		if (node != null && node.host() == this) {
			final NBTTagCompound nodeNbt = new NBTTagCompound();
			node.save(nodeNbt);
			nbt.setTag("oc:node", nodeNbt);
		}
		if (fileSystem.node() != null) {
			final NBTTagCompound nodeNbt = new NBTTagCompound();
			fileSystem.node().save(nodeNbt);
			nbt.setTag("oc:fsnode", nodeNbt);
		}
		nbt.setIntArray("addedTypes", addedType);
		nbt.setInteger("vram", gpu.maxmem);
		NBTTagCompound textures = new NBTTagCompound();
		for (int texid = 1; texid < gpu.textures.length; texid++) {
			if (gpu.textures[texid] != null) {
				try {
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					ImageIO.write(gpu.textures[texid].img, "png", output);
					byte[] data = output.toByteArray();
					textures.setByteArray(String.valueOf(texid), data);
				} catch (IOException e) {
					OCLights2.logger.log(Level.WARN, "Failed to save texture " + texid);
					OCLights2.logger.log(Level.WARN, e.getLocalizedMessage());
				}
			}
		}
		nbt.setTag("textures", textures);
		nbt.setInteger("bindedSlot", gpu.bindedSlot);
		nbt.setInteger("color",gpu.color.getRGB());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (node != null && node.host() == this) {
			node.load(nbt.getCompoundTag("oc:node"));
		}
		if (fileSystem.node() != null) {
			fileSystem.node().load(nbt.getCompoundTag("oc:fsnode"));
		}
		addedType = nbt.getIntArray("addedTypes");
		if (addedType == null) {
			addedType = new int[1025];
		} else if (addedType.length != 1025) {
			addedType = new int[1025];
		}
		int init = gpu.maxmem;
		gpu.maxmem = nbt.getInteger("vram");
		if (init > gpu.maxmem) {
			gpu.maxmem = init;
		}
		NBTTagCompound textures = nbt.getCompoundTag("textures");
		Iterator tagIterator = textures.func_150296_c().iterator();
		while (tagIterator.hasNext()) {
			String idString = (String) tagIterator.next();
			byte[] texture = textures.getByteArray(idString);
			int texid = Integer.parseInt(idString);
			
			BufferedImage img = null;
			try {
				img = ImageIO.read(new ByteArrayInputStream(texture));
			} catch (IOException e) {
			}
			if (img == null) {
				OCLights2.logger.log(Level.WARN, "Failed to load texture " + idString);
			} else {
				gpu.textures[texid] = new Texture(img.getWidth(),img.getHeight());
				gpu.textures[texid].graphics.drawImage(img, 0, 0, null);
			}
		}
		if (nbt.hasKey("bindedSlot") && nbt.getInteger("bindedSlot") != 0) {
			try {
				gpu.bindTexture(nbt.getInteger("bindedSlot"));
			} catch (Exception e) {
				OCLights2.logger.log(Level.WARN, "Failed to restore binded texture state");
			}
		}
		if (nbt.hasKey("color")) {
			gpu.color = new Color(nbt.getInteger("color"),true);
		}
	}

	public void connectToMonitor() {
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
			TileEntity ftile = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
			if (ftile != null) {
				if (ftile instanceof TileEntityMonitor) {
					TileEntityMonitor tile = (TileEntityMonitor) worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
					if (tile != null) {
						boolean found = false;
						for (Monitor m : gpu.monitors) {
							if (m == tile.mon) {
								found = true;
								break;
							}
						}
						if (found)
							break;
						tile.connect(this.gpu);
						//tile.mon.tex.fill(Color.black);
						//tile.mon.tex.drawText("Monitor connected", 0, 0, Color.white);
						tile.mon.tex.texUpdate();
						gpu.setMonitor(tile.mon);
						return;
					}
				}
			}
		}
	}

	@Override
	public synchronized void updateEntity() {
		super.updateEntity();
		if (!addedToNetwork) {
			addedToNetwork = true;
			Network.joinOrCreateNetwork(this);
		}
		synchronized (this) {
			if (!frame) {
				gpu.processSendList();
			}
		}
		connectToMonitor();
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && ticks++ % 20 == 0 && !sentOnce) {
			PacketSenders.GPUDOWNLOAD(xCoord, yCoord, zCoord);
			sentOnce = true;
		}
	}

	@Override
	public Node node() {
		return node;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (node != null)
			node.remove();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (node != null)
			node.remove();
	}

	@Override
	public void onMessage(Message message) {
	}

	/* @Override
	public boolean equals(SimpleComponent other) {
		if(other.getComponentName() == getComponentName()){return true;}
		else return false;
	} */
}
