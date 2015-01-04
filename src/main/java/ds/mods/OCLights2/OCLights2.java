package ds.mods.OCLights2;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import ds.mods.OCLights2.network.PacketHandler;
import ds.mods.OCLights2.network.PacketHandler.PacketMessage;

@Mod(modid = "OCLights2", name = "OCLights2", version = "0.4.1-75",dependencies="required-after:OpenComputers",acceptedMinecraftVersions = "1.7.10")
public class OCLights2 {
	@Mod.Instance("OCLights2")
	public static OCLights2 instance;
	
	@SidedProxy(serverSide = "ds.mods.OCLights2.CommonProxy", clientSide = "ds.mods.OCLights2.client.ClientProxy")
	public static CommonProxy proxy;
	
	public static Block gpu,monitor,monitorBig,light,advancedlight,ttrans;
	public static Item ram,tablet;
	public static Logger logger;
	
	public static SimpleNetworkWrapper network = new SimpleNetworkWrapper("OCLights2");
	
	public static CreativeTabs ocltab = new CreativeTabs("OCLights2") {
		@Override
		public ItemStack getIconItemStack() {
			this.getTranslatedTabLabel();
			return new ItemStack(tablet, 1, 0);
		}

		@Override
		public Item getTabIconItem() {
			return tablet;
		}
	};

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.loadConfig(new Configuration(event.getSuggestedConfigurationFile()));
		logger = event.getModLog();
		
		proxy.registerBlocks();
        
		logger.log(Level.INFO, "STANDING BY");
	}

	@Mod.EventHandler
	public void load(FMLPostInitializationEvent event) {
		proxy.registerRenderInfo();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        network.registerMessage(PacketHandler.class, PacketMessage.class, 0, Side.CLIENT);
        network.registerMessage(PacketHandler.class, PacketMessage.class, 1, Side.SERVER);
	}

	public static void debug(String debugmsg) {
		if (Config.DEBUGS) {
			logger.log(Level.INFO, debugmsg);
		}
	}
}
