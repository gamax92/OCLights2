package ds.mods.OCLights2.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.OCLights2.OCLights2;

public class ItemRAM extends Item {
	public ItemRAM() {
		super();
		this.hasSubtypes = true;
		this.setUnlocalizedName("ram");
		this.setCreativeTab(OCLights2.ocltab);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, @SuppressWarnings("rawtypes") List par3List, boolean par4)
	{
		int ramammt = (par1ItemStack.getItemDamage()+1);
		par3List.add(ramammt+"K");
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, @SuppressWarnings("rawtypes") List par3List)
    {
		super.getSubItems(par1, par2CreativeTabs, par3List);
		for (int i = 1; i<8; i++)
		{
			par3List.add(new ItemStack(par1, 1, i));
		}
    }
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon("oclights:ram");
    }

}
