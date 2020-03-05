package net.telepathicgrunt.worldblender.blocks;

import java.nio.FloatBuffer;
import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class WBPortalTileEntityRenderer extends TileEntityRenderer<WBPortalTileEntity>
{

	public WBPortalTileEntityRenderer()
	{
		super();
	}

	public static final ResourceLocation MAIN_TEXTURE = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	public static final ResourceLocation ADDITIVE_TEXTURE = new ResourceLocation("textures/misc/forcefield.png");
	private static final Random RANDOM = new Random(31100L);
	private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
	private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
	private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);


	@Override
	public void render(WBPortalTileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage)
	{
		RANDOM.setSeed(31100L);
		double d0 = x * x + y * y + z * z;
		int i = this.getPasses(d0);
		GlStateManager.disableLighting();
		RANDOM.setSeed(31100L);
		GlStateManager.getMatrix(2982, MODELVIEW);
		GlStateManager.getMatrix(2983, PROJECTION);

		// turns dark red when cooling down but lightens over time. And when finished cooling down, it pops to full brightness
		float coolDownEffect = tileEntity.isCoolingDown() ? 0.7f - tileEntity.getCoolDown() / 1200F : 0.85f;

		boolean flag = false;
		GameRenderer gamerenderer = Minecraft.getInstance().gameRenderer;

		for (int j = 0; j < i; ++j)
		{
			GlStateManager.pushMatrix();
			if (j == 0)
			{
				this.bindTexture(MAIN_TEXTURE);
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			}

			if (j >= 1)
			{
				this.bindTexture(ADDITIVE_TEXTURE);
				flag = true;
				gamerenderer.setupFogColor(true);
			}

			if (j == 1)
			{
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			}

			GlStateManager.texGenMode(GlStateManager.TexGen.S, 9216);
			GlStateManager.texGenMode(GlStateManager.TexGen.T, 9216);
			GlStateManager.texGenMode(GlStateManager.TexGen.R, 9216);
			GlStateManager.texGenParam(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
			GlStateManager.texGenParam(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
			GlStateManager.texGenParam(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
			GlStateManager.enableTexGen(GlStateManager.TexGen.S);
			GlStateManager.enableTexGen(GlStateManager.TexGen.T);
			GlStateManager.enableTexGen(GlStateManager.TexGen.R);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			GlStateManager.translatef(0.5F, 0.5F, 0.0F);
			GlStateManager.scalef(0.5F, 0.5F, 1.0F);
			float f2 = (float) (j + 1);
			GlStateManager.translatef(17.0F / f2, (2.0F + f2 / 1.5F) * ((float) (Util.milliTime() % 800000L) / 800000.0F), 0.0F);
			GlStateManager.rotatef((f2 * f2 * 4321.0F + f2 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.scalef(4.5F - f2 / 4.0F, 4.5F - f2 / 4.0F, 1.0F);
			GlStateManager.multMatrix(PROJECTION);
			GlStateManager.multMatrix(MODELVIEW);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
			float red = (RANDOM.nextFloat() * 3.85F) * 1.5F / (float) (18 - i) * coolDownEffect + tileEntity.getCoolDown() / 2800F;
			float green = (RANDOM.nextFloat() * 3.3F) * 1.5F / (float) (18 - i) * coolDownEffect;
			float blue = (RANDOM.nextFloat() * 2.95F) * 1.5F / (float) (18 - i) * coolDownEffect;
			if (tileEntity.shouldRenderFace(Direction.SOUTH))
			{
				bufferbuilder.pos(x, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
			}

			if (tileEntity.shouldRenderFace(Direction.NORTH))
			{
				bufferbuilder.pos(x, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y, z).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x, y, z).color(red, green, blue, 1.0F).endVertex();
			}

			if (tileEntity.shouldRenderFace(Direction.EAST))
			{
				bufferbuilder.pos(x + 1.0D, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y, z).color(red, green, blue, 1.0F).endVertex();
			}

			if (tileEntity.shouldRenderFace(Direction.WEST))
			{
				bufferbuilder.pos(x, y, z).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
			}

			if (tileEntity.shouldRenderFace(Direction.DOWN))
			{
				bufferbuilder.pos(x, y, z).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y, z).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
			}

			if (tileEntity.shouldRenderFace(Direction.UP))
			{
				bufferbuilder.pos(x, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x + 1.0D, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
				bufferbuilder.pos(x, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
			}

			tessellator.draw();
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
			this.bindTexture(MAIN_TEXTURE);
		}

		GlStateManager.disableBlend();
		GlStateManager.disableTexGen(GlStateManager.TexGen.S);
		GlStateManager.disableTexGen(GlStateManager.TexGen.T);
		GlStateManager.disableTexGen(GlStateManager.TexGen.R);
		GlStateManager.enableLighting();
		if (flag)
		{
			gamerenderer.setupFogColor(false);
		}
	}


	private FloatBuffer getBuffer(float p_147525_1_, float p_147525_2_, float p_147525_3_, float p_147525_4_)
	{
		this.buffer.clear();
		this.buffer.put(p_147525_1_).put(p_147525_2_).put(p_147525_3_).put(p_147525_4_);
		this.buffer.flip();
		return this.buffer;
	}


	protected int getPasses(double distanceAway)
	{
		if (distanceAway > 36864.0D)
		{
			return 1;
		}
		else if (distanceAway > 25600.0D)
		{
			return 2;
		}
		else if (distanceAway > 16384.0D)
		{
			return 3;
		}
		else if (distanceAway > 9216.0D)
		{
			return 4;
		}
		else if (distanceAway > 4096.0D)
		{
			return 5;
		}
		else if (distanceAway > 1024.0D)
		{
			return 6;
		}
		else if (distanceAway > 576.0D)
		{
			return 7;
		}
		else
		{
			return distanceAway > 256.0D ? 8 : 9;
		}
	}
}