package ds.mods.OCLights2.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.block.tileentity.TileEntityTTrans;

public class BlockTabletTransceiver extends Block {
	IIcon sides = null;
	
	public BlockTabletTransceiver(Material par2Material) {
		super(par2Material);
		this.setBlockName("monitor.tablet");
		this.setCreativeTab(OCLights2.ocltab);
		this.setHardness(0.6F).setStepSound(Block.soundTypeStone);
	}
	
	@Override
	public void onBlockPlacedBy(World world, int par2, int par3, int par4,
			EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
		if(!world.isRemote){
		int l = MathHelper.floor_double(par5EntityLivingBase.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int i1 = world.getBlockMetadata(par2, par3, par4) >> 2;
		++l;
		l %= 4;
		if (l == 0) {
			world.setBlockMetadataWithNotify(par2, par3, par4, 4 | i1 << 2,2);
		}

		if (l == 1) {
			world.setBlockMetadataWithNotify(par2, par3, par4, 2 | i1 << 2,2);
		}

		if (l == 2) {
			world.setBlockMetadataWithNotify(par2, par3, par4, 5 | i1 << 2,2);
		}

		if (l == 3) {
			world.setBlockMetadataWithNotify(par2, par3, par4, 3 | i1 << 2,2);
		}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
			if(meta == side || side == 4 && meta == 0) {
			return this.blockIcon;
			} else {
			return sides;
			}
	}

	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon("oclights:tabletTfront");
	    sides = par1IconRegister.registerIcon("oclights:tabletTsides");
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityTTrans();
	}

}
