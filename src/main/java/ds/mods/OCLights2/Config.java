package ds.mods.OCLights2;

import net.minecraftforge.common.config.Configuration;

public class Config {
	public static boolean DEBUGS;
	public static boolean Vanilla,IC2;
	public static boolean scaleGui;
	public static int Tablet,light,advlight,Monitor,MonitorBig,Gpu,Ram,TTrans;
	public static int widthMon, heightMon;
	public static int widthTab, heightTab;
	public static int widthExt, heightExt, resExt;
	static void loadConfig(Configuration config) {
	    config.load();
	    Monitor= config.get("Blocks and item ids", "Monitor", 543).getInt(543);
	    Gpu = config.get("Blocks and item ids", "Gpu", 542).getInt(542);
	    TTrans = config.get("Blocks and item ids", "TTrans", 544).getInt(544);
	    MonitorBig = config.get("Blocks and item ids", "Big Monitor", 545).getInt(545);
	    Ram = config.get("Blocks and item ids", "Ram", 4097-256).getInt(4097 - 256);
	    Tablet = config.get("Blocks and item ids", "Tablet", 4098-256).getInt(4098 - 256);
	    light = config.get("Blocks and item ids", "Light", 546).getInt(546);
	    advlight = config.get("Blocks and item ids", "Advanced Light", 547).getInt(547);
	    DEBUGS = config.get("Misc", "DEBUG", false).getBoolean(false);
	    Vanilla = config.get("Misc", "CompatVanilla", true).getBoolean(true);
	    IC2 = config.get("Misc", "CompatIC2", false).getBoolean(false);
		scaleGui = config.get("Misc", "Autoscale GUI", true).getBoolean(true);

		widthMon = config.getInt("Width", "Monitor", 256, 1, 1024, "");
		heightMon = config.getInt("Height", "Monitor", 144, 1, 1024, "");

		widthTab = config.getInt("Width", "Tablet", 256, 1, 1024, "");
		heightTab = config.getInt("Height", "Tablet", 144, 1, 1024, "");

		widthExt = config.getInt("Max Width", "External Monitor", 16, 1, 32, "");
		heightExt = config.getInt("Max Height", "External Monitor", 9, 1, 32, "");
		resExt = config.getInt("Resolution", "External Monitor", 32, 1, 128, "");

	    config.save();
	}
}
