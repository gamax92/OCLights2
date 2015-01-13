package ds.mods.OCLights2.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ds.mods.OCLights2.CommonProxy;
import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.block.tileentity.TileEntityAdvancedlight;

public class BlockAdvancedLight extends BlockContainer
{
    public BlockAdvancedLight(Material j)
    {
        super(j);
        this.setLightLevel(1.0F);
        this.setBlockName("Advanced.Light");
		this.setCreativeTab(OCLights2.ocltab);
		this.setHardness(0.6F).setStepSound(Block.soundTypeStone);
    }
    
    @Override
	public int quantityDropped(Random random)
    {
        return 1;
    }
    
    @Override
	public boolean isOpaqueCube() {
	  return false;
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
	public TileEntity createNewTileEntity(World world, int metadata)
    {
        return new TileEntityAdvancedlight();
    }
    
}
