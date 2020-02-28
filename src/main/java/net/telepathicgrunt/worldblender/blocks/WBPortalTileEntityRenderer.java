package net.telepathicgrunt.worldblender.blocks;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class WBPortalTileEntityRenderer extends TileEntityRenderer<WBPortalTileEntity>
{

	public WBPortalTileEntityRenderer(TileEntityRendererDispatcher dispatcher)
	{
		super(dispatcher);
	}


	@Override
	public void render(WBPortalTileEntity tileEntity, float p_225616_2_, MatrixStack modelMatrix, IRenderTypeBuffer renderBuffer, int p_225616_5_, int p_225616_6_)
	{
		RANDOM.setSeed(31100L);
		double distance = tileEntity.getPos().distanceSq(this.dispatcher.renderInfo.getProjectedView(), true);
		int passes = this.getPasses(distance);
		Matrix4f matrix4f = modelMatrix.peek().getModel();
		this.drawColor(tileEntity, 0.15F, matrix4f, renderBuffer.getBuffer(WB_RENDER_TYPE.get(0)));

		for (int currentPass = 1; currentPass < passes; ++currentPass)
		{
			this.drawColor(tileEntity, 1.5F / (float) (18 - currentPass), matrix4f, renderBuffer.getBuffer(WB_RENDER_TYPE.get(currentPass)));
		}
	}


	private void drawColor(WBPortalTileEntity tileEntity, float modifier, Matrix4f matrix4f, IVertexBuilder vertexBuilder)
	{
		// turns dark red when cooling down but lightens over time. And when finished cooling down, it pops to full brightness
		float coolDownEffect = tileEntity.isCoolingDown() ? 0.85f - tileEntity.getCoolDown()/1200F : 1.0f ; 

		float red = (RANDOM.nextFloat() * 4.2F) * modifier * coolDownEffect + tileEntity.getCoolDown()/2800F;
		float green = (RANDOM.nextFloat() * 3.0F) * modifier * coolDownEffect;
		float blue = (RANDOM.nextFloat() * 2.6F) * modifier * coolDownEffect;
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, red, green, blue, Direction.SOUTH);
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, red, green, blue, Direction.NORTH);
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, red, green, blue, Direction.EAST);
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, red, green, blue, Direction.WEST);
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, red, green, blue, Direction.DOWN);
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, red, green, blue, Direction.UP);
	}


	private void setVertexColor(WBPortalTileEntity tileEntity, Matrix4f matrix4f, IVertexBuilder vertexBuilder, float p_228884_4_, float p_228884_5_, float p_228884_6_, float p_228884_7_, float p_228884_8_, float p_228884_9_, float p_228884_10_, float p_228884_11_, float p_228884_12_, float p_228884_13_, float p_228884_14_, Direction direction)
	{
		if (tileEntity.shouldRenderFace(direction))
		{
			vertexBuilder.vertex(matrix4f, p_228884_4_, p_228884_6_, p_228884_8_).color(p_228884_12_, p_228884_13_, p_228884_14_, 1.0F).endVertex();
			vertexBuilder.vertex(matrix4f, p_228884_5_, p_228884_6_, p_228884_9_).color(p_228884_12_, p_228884_13_, p_228884_14_, 1.0F).endVertex();
			vertexBuilder.vertex(matrix4f, p_228884_5_, p_228884_7_, p_228884_10_).color(p_228884_12_, p_228884_13_, p_228884_14_, 1.0F).endVertex();
			vertexBuilder.vertex(matrix4f, p_228884_4_, p_228884_7_, p_228884_11_).color(p_228884_12_, p_228884_13_, p_228884_14_, 1.0F).endVertex();
		}
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

	
	//////////////////////////////////RENDER STATE STUFF//////////////////////////////////////////

	public static final ResourceLocation MAIN_TEXTURE = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	public static final ResourceLocation ADDITIVE_TEXTURE = new ResourceLocation("textures/misc/forcefield.png");
	private static final Random RANDOM = new Random(31100L);
	private static final List<RenderType> WB_RENDER_TYPE = IntStream.range(0, 16).mapToObj((index) ->
	{
		return getWBPortal(index + 1);
	}).collect(ImmutableList.toImmutableList());

	
	public static RenderType getWBPortal(int layer)
	{
		RenderState.TransparencyState renderstate$transparencystate;
		RenderState.TextureState renderstate$texturestate;
		if (layer <= 1)
		{
			renderstate$transparencystate = RenderState.TRANSLUCENT_TRANSPARENCY;
			renderstate$texturestate = new RenderState.TextureState(MAIN_TEXTURE, false, false);
		}
		else
		{
			renderstate$transparencystate = RenderState.ADDITIVE_TRANSPARENCY;
			renderstate$texturestate = new RenderState.TextureState(ADDITIVE_TEXTURE, false, false);
		}

		return RenderType.of("end_portal", DefaultVertexFormats.POSITION_COLOR, 7, 256, false, true, RenderType.State.builder().transparency(renderstate$transparencystate).texture(renderstate$texturestate).texturing(new RenderState.PortalTexturingState(layer)).fog(RenderState.BLACK_FOG).build(false));
	}
}