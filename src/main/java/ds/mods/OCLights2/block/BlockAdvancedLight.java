package ds.mods.OCLights2.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.CommonProxy;
import ds.mods.OCLights2.block.tileentity.TileEntityAdvancedlight;

public class BlockAdvancedLight extends BlockContainer
{
    public BlockAdvancedLight(int i, Material j)
    {
        super(i, j);
        this.setLightValue(1.0F);
        this.setUnlocalizedName("Advanced.Light");
		this.setCreativeTab(OCLights2.ocltab);
		this.setHardness(0.6F).setStepSound(soundStoneFootstep);
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
	public TileEntity createNewTileEntity(World var1)
    {
        return new TileEntityAdvancedlight();
    }
    
}
