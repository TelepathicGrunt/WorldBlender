package com.telepathicgrunt.worldblender.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.telepathicgrunt.worldblender.blocks.WBRenderTexturingState.WBPortalTexturingState;
import com.telepathicgrunt.worldblender.mixin.blocks.RenderPhaseAccessor;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

import java.util.Random;
import java.util.stream.IntStream;

// CLIENT-SIDED
public class WBPortalBlockEntityRenderer extends TileEntityRenderer<WBPortalBlockEntity>
{
	public WBPortalBlockEntityRenderer(TileEntityRendererDispatcher dispatcher)
	{
		super(dispatcher);
	}

	// Culling optimization by Comp500
	// https://github.com/comp500/PolyDungeons/blob/master/src/main/java/polydungeons/block/entity/DecorativeEndBlockEntity.java
	private static boolean wasRendered = false;
	public static void drawBuffers() {
		if (wasRendered) {
			// Should only be run if render has been called at least once
			wasRendered = false;
			for (int i = 0; i < 9; i++) {
				RenderType layer = WB_RENDER_TYPE[i];
				BufferBuilder buf = BUFFER_BUILDERS[i];
				layer.finish(buf, 0, 0, 0);
				// Set up the buffer builder to be ready to accept vertices again
				buf.begin(layer.getDrawMode(), layer.getVertexFormat());
			}
		}
	}

	@Override
	public void render(WBPortalBlockEntity tileEntity, float partialTicks, MatrixStack modelMatrix, IRenderTypeBuffer renderBuffer, int combinedLightIn, int combinedOverlayIn)
	{
		wasRendered = true;
		RANDOM.setSeed(31100L);
		double distance = tileEntity.getPos().distanceSq(this.renderDispatcher.renderInfo.getProjectedView(), true);
		int passes = this.getPasses(distance);
		Matrix4f matrix4f = modelMatrix.getLast().getMatrix();
		this.drawColor(tileEntity, 0.1F, matrix4f, BUFFER_BUILDERS[0]);

		for (int currentPass = 1; currentPass < passes; ++currentPass)
		{
			this.drawColor(tileEntity, 2.0F / (20 - currentPass), matrix4f, BUFFER_BUILDERS[currentPass]);
		}
	}


	private void drawColor(WBPortalBlockEntity tileEntity, float modifier, Matrix4f matrix4f, IVertexBuilder vertexBuilder)
	{
		// turns dark red when cooling down but lightens over time. And when finished cooling down, it pops to full brightness
		float coolDownEffect = tileEntity.isCoolingDown() ? 0.7f - tileEntity.getCoolDown()/1200F : 0.85f ;

		float red = (RANDOM.nextFloat() * 3.95F) * modifier * coolDownEffect + tileEntity.getCoolDown()/2800F;
		float green = (RANDOM.nextFloat() * 2.95F) * modifier * coolDownEffect;
		float blue = (RANDOM.nextFloat() * 3.0F) * modifier * coolDownEffect;
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, red, green, blue, Direction.SOUTH);
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, red, green, blue, Direction.NORTH);
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, red, green, blue, Direction.EAST);
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, red, green, blue, Direction.WEST);
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, red, green, blue, Direction.DOWN);
		this.setVertexColor(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, red, green, blue, Direction.UP);
	}


	private void setVertexColor(WBPortalBlockEntity tileEntity, Matrix4f matrix4f, IVertexBuilder vertexBuilder, float pos1, float pos2, float pos3, float pos4, float pos5, float pos6, float pos7, float pos8, float red, float green, float blue, Direction direction)
	{
		if (tileEntity.shouldRenderFace(direction))
		{
			vertexBuilder.pos(matrix4f, pos1, pos3, pos5).color(red, green, blue, 1.0F).endVertex();
			vertexBuilder.pos(matrix4f, pos2, pos3, pos6).color(red, green, blue, 1.0F).endVertex();
			vertexBuilder.pos(matrix4f, pos2, pos4, pos7).color(red, green, blue, 1.0F).endVertex();
			vertexBuilder.pos(matrix4f, pos1, pos4, pos8).color(red, green, blue, 1.0F).endVertex();
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
			return 8;
		}
	}


	//////////////////////////////////RENDER STATE STUFF//////////////////////////////////////////

	public static final ResourceLocation MAIN_TEXTURE = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	public static final ResourceLocation ADDITIVE_TEXTURE = new ResourceLocation("textures/misc/forcefield.png");
	private static final Random RANDOM = new Random(31100L);
	private static final BufferBuilder[] BUFFER_BUILDERS = new BufferBuilder[9];
	private static final RenderType[] WB_RENDER_TYPE = IntStream.range(0, 9).mapToObj((index) ->
			getWBPortal(index + 1)).toArray(RenderType[]::new);

	public static RenderType getWBPortal(int layer)
	{
		RenderState.TransparencyState renderstate$transparencystate;
		RenderState.TextureState renderstate$texturestate;
		if (layer <= 1)
		{
			renderstate$transparencystate = RenderPhaseAccessor.wb_getTRANSLUCENT_TRANSPARENCY();
			renderstate$texturestate = new RenderState.TextureState(MAIN_TEXTURE, false, false);
		}
		else if (layer <= 3)
		{
			renderstate$transparencystate = RenderPhaseAccessor.wb_getADDITIVE_TRANSPARENCY();
			renderstate$texturestate = new RenderState.TextureState(ADDITIVE_TEXTURE, true, false);
		}
		else
		{
			renderstate$transparencystate = RenderPhaseAccessor.wb_getADDITIVE_TRANSPARENCY();
			renderstate$texturestate = new RenderState.TextureState(ADDITIVE_TEXTURE, false, false);
		}

		RenderType renderLayer = RenderType.makeType(
				"world_blender_portal",
				DefaultVertexFormats.POSITION_COLOR,
				7,
				90,
				false,
				true,
				RenderType.State.getBuilder()
						.transparency(renderstate$transparencystate)
						.texture(renderstate$texturestate)
						.texturing(new WBPortalTexturingState(layer))
						.fog(RenderPhaseAccessor.wb_getBLACK_FOG())
						.build(false));

		BufferBuilder builder = new BufferBuilder(renderLayer.getBufferSize());
		builder.begin(renderLayer.getDrawMode(), renderLayer.getVertexFormat());
		BUFFER_BUILDERS[layer - 1] = builder;
		return renderLayer;
	}
}