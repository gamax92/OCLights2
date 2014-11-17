package ds.mods.OCLights2.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.block.tileentity.TileEntityColorLight;

public class BlockColorLight extends BlockContainer {

	public BlockColorLight(Material par2Material) {
		super(par2Material);
		this.setBlockName("Light");
		this.setLightLevel(1.0F);
		this.setHardness(0.6F).setStepSound(Block.soundTypeStone);
		this.setCreativeTab(OCLights2.ocltab);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return this.blockIcon;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
       par1IconRegister.registerIcon("OCLights:light");
	}
	@Override
	public int quantityDropped(Random random)
    {
        return 1;
    }

    @Override
	public TileEntity createNewTileEntity(World world, int metadata)
    {
        return new TileEntityColorLight();
    }
}
