package ds.mods.OCLights2;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import ds.mods.OCLights2.network.PacketHandler;

@Mod(modid = "OCLights2", name = "OCLights2", version = "0.4.1-75",dependencies="required-after:OpenComputers",acceptedMinecraftVersions = "1.6.4")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, channels = { "OCLights2" },packetHandler = PacketHandler.class,connectionHandler = PacketHandler.class)
public class OCLights2 {
	@Mod.Instance("OCLights2")
	public static OCLights2 instance;
	
	@SidedProxy(serverSide = "ds.mods.OCLights2.CommonProxy", clientSide = "ds.mods.OCLights2.client.ClientProxy")
	public static CommonProxy proxy;
	
	public static Block gpu,monitor,monitorBig,light,advancedlight,ttrans;
	public static Item ram,tablet;
	public static Logger logger;
	
	public static CreativeTabs ocltab = new CreativeTabs("OCLights2") {
		@Override
		public ItemStack getIconItemStack() {
			this.getTranslatedTabLabel();
			return new ItemStack(tablet.itemID, 1, 0);
		}
	};

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.loadConfig(new Configuration(event.getSuggestedConfigurationFile()));
		logger = event.getModLog();
		logger.setParent(FMLLog.getLogger());
		
		proxy.registerBlocks();
        
		logger.log(Level.INFO, "STANDING BY");
	}

	@Mod.EventHandler
	public void load(FMLPostInitializationEvent event) {
		proxy.registerRenderInfo();
        NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
	}

	public static void debug(String debugmsg) {
		if (Config.DEBUGS) {
			logger.log(Level.INFO, debugmsg);
		}
	}
}
