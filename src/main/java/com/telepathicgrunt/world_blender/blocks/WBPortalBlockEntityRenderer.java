package com.telepathicgrunt.world_blender.blocks;

import com.google.common.collect.ImmutableList;
import com.telepathicgrunt.world_blender.blocks.WBRenderTexturingState.WBPortalTexturingState;
import com.telepathicgrunt.world_blender.mixin.RenderPhaseAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;


@Environment(EnvType.CLIENT)
public class WBPortalBlockEntityRenderer extends BlockEntityRenderer<WBPortalBlockEntity>
{
	public WBPortalBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher)
	{
		super(dispatcher);
	}

	@Override
	public void render(WBPortalBlockEntity tileEntity, float partialTicks, MatrixStack modelMatrix, VertexConsumerProvider renderBuffer, int combinedLightIn, int combinedOverlayIn)
	{
		RANDOM.setSeed(31100L);
		double distance = tileEntity.getPos().getSquaredDistance(this.dispatcher.camera.getPos(), true);
		int passes = this.getPasses(distance);
		Matrix4f matrix4f = modelMatrix.peek().getModel();
		this.drawColor(tileEntity, 0.1F, matrix4f, renderBuffer.getBuffer(WB_RENDER_TYPE.get(0)));

		for (int currentPass = 1; currentPass < passes; ++currentPass)
		{
			this.drawColor(tileEntity, 2.0F / (20 - currentPass), matrix4f, renderBuffer.getBuffer(WB_RENDER_TYPE.get(currentPass)));
		}
	}


	private void drawColor(WBPortalBlockEntity tileEntity, float modifier, Matrix4f matrix4f, VertexConsumer vertexBuilder)
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


	private void setVertexColor(WBPortalBlockEntity tileEntity, Matrix4f matrix4f, VertexConsumer vertexBuilder, float pos1, float pos2, float pos3, float pos4, float pos5, float pos6, float pos7, float pos8, float red, float green, float blue, Direction direction)
	{
		if (tileEntity.shouldRenderFace(direction))
		{
			vertexBuilder.vertex(matrix4f, pos1, pos3, pos5).color(red, green, blue, 1.0F).next();
			vertexBuilder.vertex(matrix4f, pos2, pos3, pos6).color(red, green, blue, 1.0F).next();
			vertexBuilder.vertex(matrix4f, pos2, pos4, pos7).color(red, green, blue, 1.0F).next();
			vertexBuilder.vertex(matrix4f, pos1, pos4, pos8).color(red, green, blue, 1.0F).next();
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

	public static final Identifier MAIN_TEXTURE =     new Identifier("textures/misc/enchanted_item_glint.png");
	public static final Identifier ADDITIVE_TEXTURE = new Identifier("textures/misc/forcefield.png");
	private static final Random RANDOM = new Random(31100L);
	private static final List<RenderLayer> WB_RENDER_TYPE = IntStream.range(0, 9).mapToObj((index) ->
			getWBPortal(index + 1)).collect(ImmutableList.toImmutableList());

	
	public static RenderLayer getWBPortal(int layer)
	{
		RenderPhase.Transparency renderstate$transparencystate;
		RenderPhase.Texture renderstate$texturestate;
		if (layer <= 1)
		{
			renderstate$transparencystate = RenderPhaseAccessor.getTRANSLUCENT_TRANSPARENCY();
			renderstate$texturestate = new RenderPhase.Texture(MAIN_TEXTURE, false, false);
		}
		else if (layer <= 3)
		{
			renderstate$transparencystate = RenderPhaseAccessor.getADDITIVE_TRANSPARENCY();
			renderstate$texturestate = new RenderPhase.Texture(ADDITIVE_TEXTURE, true, false);
		}
		else
		{
			renderstate$transparencystate = RenderPhaseAccessor.getADDITIVE_TRANSPARENCY();
			renderstate$texturestate = new RenderPhase.Texture(ADDITIVE_TEXTURE, false, false);
		}

		return RenderLayer.of(
				"world_blender_portal",
				VertexFormats.POSITION_COLOR,
				7,
				90,
				false,
				true,
				RenderLayer.MultiPhaseParameters.builder()
						.transparency(renderstate$transparencystate)
						.texture(renderstate$texturestate)
						.texturing(new WBPortalTexturingState(layer))
						.fog(RenderPhaseAccessor.getBLACK_FOG())
						.build(false));
	}
}