package ds.mods.OCLights2.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ds.mods.OCLights2.CommonProxy;
import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.block.tileentity.TileEntityExternalMonitor;
import ds.mods.OCLights2.gpu.GPU;

public class BlockExternalMonitor extends Block {
	public BlockExternalMonitor(Material par2Material) {
		super(par2Material);
		this.setBlockName("monitor.big");
		this.setCreativeTab(OCLights2.ocltab);
		this.setHardness(0.6F).setStepSound(Block.soundTypeStone);
	}
	
	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4, Block par5, int par6) {
		TileEntityExternalMonitor tile = (TileEntityExternalMonitor) par1World.getTileEntity(par2, par3, par4);
		tile.destroy();
		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}

	@Override
	public boolean onBlockActivated(World world, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float vecX,
			float vecY, float vecZ) {
		TileEntityExternalMonitor tile = (TileEntityExternalMonitor) world.getTileEntity(par2,par3,par4);
		float x = 0f;
		float y = 0f;
		switch (tile.m_dir)
		{
			case 0:
			{
				if (vecZ == 0.0f)
				{
					x = 1F-vecX;
					y = vecY;
				}
				else
				{
					return false;
				}
				break;
			}
			case 1:
			{
				if (vecX == 1.0f)
				{
					x = vecY;
					y = vecZ;
				}
				else
				{
					return false;
				}
				break;
			}
			case 2:
			{
				if (vecZ == 1.0f)
				{
					x = vecX;
					y = vecY;
				}
				else
				{
					return false;
				}
				break;
			}
			case 3:
			{
				if (vecX == 0.0f)
				{
					x = vecY;
					y = vecZ;
				}
				else
				{
					return false;
				}
				break;
			}
		}
		int px = (int) Math.floor(x*32F);
		int py = (int) Math.floor((1F-y)*32F);
		px+=(tile.m_width-tile.m_xIndex-1)*32;
		py+=(tile.m_height-tile.m_yIndex-1)*32;
		if (!world.isRemote)
		{
			//Send it to the tileentity!
			if (tile.mon != null && tile.mon.gpu != null)
			{
				for (GPU g : tile.mon.gpu)
				{
					g.tile.startClick((EntityPlayer) par5EntityPlayer, 0, px, py);
					g.tile.endClick((EntityPlayer) par5EntityPlayer);
				}
			}
		}
		return true;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1iBlockAccess, int par2, int par3, int par4) {
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean hasTileEntity(int meta)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World w, int meta)
	{
		return new TileEntityExternalMonitor();
	}
	
	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack item)
	{
		if(!world.isRemote){
		int l = MathHelper.floor_double(entityliving.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		TileEntityExternalMonitor tile = (TileEntityExternalMonitor) world.getTileEntity(i, j, k);
		tile.setDir(l);
		tile.contractNeighbours();
        tile.contract();
        tile.expand();
		}
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return CommonProxy.modelID;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	 @Override
	  public void registerBlockIcons(IIconRegister iconRegister) {
	      blockIcon = iconRegister.registerIcon("oclights:monitorsides");
	  }
}
