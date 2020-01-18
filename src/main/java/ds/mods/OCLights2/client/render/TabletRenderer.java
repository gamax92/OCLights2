package ds.mods.OCLights2.client.render;

import java.awt.Color;
import java.util.UUID;

import cpw.mods.fml.common.Loader;
import ds.mods.OCLights2.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import ds.mods.OCLights2.OCLights2;
import ds.mods.OCLights2.block.tileentity.TileEntityTTrans;
import ds.mods.OCLights2.gpu.Texture;
import ds.mods.OCLights2.item.ItemTablet;
import ds.mods.OCLights2.utils.TabMesg;

public class TabletRenderer implements IItemRenderer {

	private static final int WIDTH = Config.widthTab;
	private static final int HEIGHT = Config.heightTab;
	
	ModelTablet model = new ModelTablet();
	TextureManager re;
	ResourceLocation texture = new ResourceLocation("oclights", "textures/items/Tablet.png");
	public static Texture defaultTexture = new Texture(WIDTH, HEIGHT);
	public static Texture errorTexture = new Texture(WIDTH, HEIGHT);
	public static DynamicTexture dyntex = new DynamicTexture(WIDTH,HEIGHT);
	public static int[] dyntex_data;
	
	public TabletRenderer()
	{
		dyntex_data = dyntex.getTextureData();
		
		defaultTexture.rgbCache = new int[WIDTH*HEIGHT];
		defaultTexture.fill(Color.blue);
		defaultTexture.drawText("Hello, World!", 0, 0, Color.white);
		defaultTexture.drawText("Please configure the tablet with a Tablet Transmitter.", 0, 9, Color.white);
		defaultTexture.drawText("You can do this by right clicking it with your tablet.", 0, 18, Color.white);
		defaultTexture.texUpdate();
		
		errorTexture.rgbCache = new int[WIDTH*HEIGHT];
		errorTexture.fill(Color.red);
		errorTexture.drawText("Out of range.", 0, 0, Color.white);
		errorTexture.texUpdate();
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		if (re == null)
			re = Minecraft.getMinecraft().renderEngine;
		re.bindTexture(texture);
		GL11.glPushMatrix();
		switch (type)
		{
		case ENTITY:
			if(RenderItem.renderInFrame){
			   GL11.glRotatef(90, 0F, 0F, 1F);
			   GL11.glTranslatef(0, -0.5F, 0.10F);
			}else{
			   GL11.glRotatef(180, 0F, 0F, 1F);
			   GL11.glTranslatef(0F, -0.25F, 0F);}
			break;
		case EQUIPPED:
			int i;
			for (i = 0; i<4; i++) {GL11.glPopMatrix();};
			for (i = 0; i<4; i++) {GL11.glPushMatrix();};
			GL11.glScalef(.5F, .5F, .5F);
			Entity entity = (Entity) data[1];
			if (entity instanceof EntityZombie)
				GL11.glTranslatef(0F, 0F, -0.5F);
			else
				GL11.glTranslatef(0F, 0.75F, -0.5F);
			GL11.glRotatef(90F+60F, 1F, 0F, 0F);
			GL11.glRotatef(180F, 0F, 0F, 1F);
			break;
		case EQUIPPED_FIRST_PERSON:
			GL11.glRotatef(-45F, 0F, 1F, 0F);
			GL11.glRotatef(-90F-60F, 1F, 0F, 0F);
			GL11.glTranslatef(-0.75F, .25F, 2.75F);
			GL11.glScalef(4F, 1F, 4F);
			break;
		case FIRST_PERSON_MAP:
			break;
		case INVENTORY:
			GL11.glRotatef(180, 0F, 0F, 1F);
			GL11.glRotatef(180, 0F, 1F, 0F);
			GL11.glTranslatef(0F, -0.25F, 0F);
			GL11.glScalef(1F, 1F, 1F);
			break;
		default:
			break;
		}
		{
			model.draw();
			NBTTagCompound nbt = ((ItemTablet)OCLights2.tablet).getNBT(item, Minecraft.getMinecraft().theWorld);
			if (nbt == null)
			{
				GL11.glPopMatrix();
				return;
			}
			//Well, we need to get the screen :P
			Texture tex = defaultTexture;
			if (nbt.getBoolean("canDisplay"))
			{
				String uuistr = nbt.getString("trans");
				if (uuistr != null)
				{
					UUID trans = UUID.fromString(uuistr);
					if (!(trans == null || TabMesg.getTabVar(trans, "x") == null))
					{
						if (Minecraft.getMinecraft().theWorld == null) {GL11.glPopMatrix(); return;}
						TileEntity noncast = Minecraft.getMinecraft().theWorld
								.getTileEntity(
										(Integer)TabMesg.getTabVar(trans, "x"),
										(Integer)TabMesg.getTabVar(trans, "y"),
										(Integer)TabMesg.getTabVar(trans, "z"));
						if (!(noncast == null || !(noncast instanceof TileEntityTTrans)))
						{
							TileEntityTTrans tile = (TileEntityTTrans) noncast;
							
							if (tile.mon.tex == null) {nbt.setBoolean("canDisplay", false); return;}
							else if (isInOfRange(trans)){
								tex = tile.mon.tex;
							}
							else{
								//tablet is out of range,  fak shit up :D
								tex = errorTexture;
							}
						}
						else
							nbt.setBoolean("canDisplay", false);
					}
					else
						nbt.setBoolean("canDisplay", false);
				}
				else
					nbt.setBoolean("canDisplay", false);
			}
			GL11.glTranslatef(0F, -0.0001F, 0F);
			TextureUtil.uploadTexture(dyntex.getGlTextureId(), tex.rgbCache, WIDTH, HEIGHT);
			Tessellator tess = Tessellator.instance;
			GL11.glDisable(GL11.GL_LIGHTING);
			tess.startDrawingQuads();
			if (!Loader.isModLoaded("coloredlightscore"))
				tess.setBrightness(0x00F000F0);
			else
				tess.setBrightness(0xFFFFF);
			tess.addVertexWithUV(-8/16D, 0.5D-(2/16D), -(6/16D),0.0D,1.0D);
			tess.addVertexWithUV(0.5D, 0.5D-(2/16D), -(6/16D),1.0D,1.0D);
			tess.addVertexWithUV(0.5D, 0.5D-(2/16D), (3/16D),1.0D,0.0D);
			tess.addVertexWithUV(-8/16D, 0.5D-(2/16D), (3/16D),0.0D,0.0D);
			tess.draw();
			GL11.glEnable(GL11.GL_LIGHTING);
		}
		GL11.glPopMatrix();
	}
	
	public static boolean isInOfRange(UUID trans){
		int xDifference = (int) Math.abs(Minecraft.getMinecraft().thePlayer.posX - (Integer)TabMesg.getTabVar(trans, "x"));
		int yDiference = (int) Math.abs(Minecraft.getMinecraft().thePlayer.posY - (Integer)TabMesg.getTabVar(trans, "y"));
		int zDifference = (int) Math.abs(Minecraft.getMinecraft().thePlayer.posZ - (Integer)TabMesg.getTabVar(trans, "z"));
		int tabletRange= 10;
		if (xDifference < tabletRange && yDiference < tabletRange && zDifference < tabletRange){
			return true;
		}
		return false;
		
	}

}
