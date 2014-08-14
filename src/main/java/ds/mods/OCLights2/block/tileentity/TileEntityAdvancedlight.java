package ds.mods.OCLights2.block.tileentity;

import java.io.IOException;

import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.PacketDispatcher;
import ds.mods.OCLights2.network.PacketChunker;

public class TileEntityAdvancedlight extends TileEntity implements SimpleComponent {
	    public float r = 255;
	    public float g = 255;
	    public float b = 255;
		private int ticksSinceSync;

	    @Override
		public String getComponentName()
	    {
	        return "light_adv";
	    }
        
        @Callback
        public Object[] setColorRGB(Context context, Arguments arguments) throws Exception {
        	int r = arguments.checkInteger(0);
    		int g = arguments.checkInteger(1);
    		int b = arguments.checkInteger(2);
    		if (r > 255 || r < 0 || g > 255 || g < 0 || b > 255 || b < 0)
            {
            	throw new Exception("Invalid RGB!");
            }
        	this.r = r;
            this.g = g;
            this.b = b;
            colorChange();
            return null;
        }
        
        @Callback
        public Object[] getColorRGB(Context context, Arguments arguments) {
        	return (new Object[]{this.r,this.g,this.b});
        }
        
	    public void colorChange()
	    {
	    	ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
	    	try {
	    		outputStream.writeInt(xCoord);
	    		outputStream.writeInt(yCoord);
	    		outputStream.writeInt(zCoord);
	    		outputStream.writeFloat(this.r);
	    		outputStream.writeFloat(this.g);
	    		outputStream.writeFloat(this.b);
	    	Packet[] packets = PacketChunker.instance.createPackets("OCLights2", outputStream.toByteArray());
	    	PacketDispatcher.sendPacketToAllPlayers(packets[0]);
	    	} catch (IOException ex) {
	    		ex.printStackTrace();
	    	}
	    }
	    @Override
	    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	    {
	    	super.readFromNBT(par1NBTTagCompound);
	        this.r = par1NBTTagCompound.getFloat("r");
	        this.g = par1NBTTagCompound.getFloat("g");
	        this.b = par1NBTTagCompound.getFloat("b");
	        colorChange();
	    }
	    @Override
	    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	    {
	    	super.writeToNBT(par1NBTTagCompound);
	    	par1NBTTagCompound.setFloat("r",this.r);
	    	par1NBTTagCompound.setFloat("g",this.g);
	    	par1NBTTagCompound.setFloat("b",this.b);
	    }
	    @Override
	    public void updateEntity() {
	    	if ((++ticksSinceSync % 20) == 0) {
	    		colorChange();
	    	}
	    }

	    /* @Override
		public boolean equals(SimpleComponent other) {
			if(other.getComponentName() == getComponentName()){return true;}
			else return false;
		} */
}
