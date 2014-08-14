package ds.mods.OCLights2.block.tileentity;

import java.util.Arrays;
import java.util.List;

import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityColorLight  extends TileEntity implements SimpleComponent {
    int color;
    public static final List<String> colors = Arrays.asList( "black", "red", "green", "brown", "blue", "purple", "cyan", "silver", "gray", "pink", "lime", "yellow", "lightBlue", "magenta", "orange", "white");
	@Override
	public String getComponentName() {
		return "light";
	}
	
	@Callback
	public Object[] setColor(Context context, Arguments arguments) throws Exception {
		String colorString = arguments.checkString(0);
		try {
			color = Integer.parseInt(colorString);
		} catch (NumberFormatException ex) {
			if (colors.contains(colorString.toLowerCase())) {
				color = colors.indexOf(colorString.toLowerCase());
			}
			else{throw new Exception("Invalid COLOR!");}
		}
		if (color > 16 || color < 0) {
			throw new Exception("Invalid COLOR!");
		}
        //colorChange();
        return null;
	}
	
	@Callback
	public Object[] getColor(Context context, Arguments arguments) {
		return (new Object[]{this.color});
	}
	
	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
		this.color = par1NBTTagCompound.getInteger("color");
		//colorChange();
	}
	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setInteger("color",this.color);
	}

	/* @Override
	public boolean equals(SimpleComponent other) {
		if(other.getComponentName() == getComponentName()){return true;}
		else return false;
	} */
}
