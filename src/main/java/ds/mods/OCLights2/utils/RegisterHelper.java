package ds.mods.OCLights2.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import ds.mods.OCLights2.OCLights2;

public class RegisterHelper {
	
	public static boolean canRegisterItem(int itemid, String name) {
		if (itemid > 0) {
			if (Item.itemsList[itemid] == null) {
				return true;
			}
		}
		OCLights2.debug("sawwy, itemid " + itemid + " was disabld, name was " + name);
		return false;
	}

	public static boolean canRegisterBlock(int blockId, String name) {
		if (blockId > 0) {
			if (Block.blocksList[blockId] == null) {
				return true;
			}
		}
		OCLights2.debug("sawwy, blockid " + blockId + " was disabld, name was " + name);
		return false;
	}
}
