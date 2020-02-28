package net.telepathicgrunt.worldblender.blocks;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class WBPortalTileEntityRenderer extends TileEntityRenderer<WBPortalTileEntity>
{
	public static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
	public static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
	private static final Random RANDOM = new Random(31100L);
	private static final List<RenderType> field_228881_e_ = IntStream.range(0, 16).mapToObj((p_228882_0_) ->
	{
		return RenderType.getEndPortal(p_228882_0_ + 1);
	}).collect(ImmutableList.toImmutableList());


	public WBPortalTileEntityRenderer(TileEntityRendererDispatcher dispatcher)
	{
		super(dispatcher);
	}

	@Override
	public void render(WBPortalTileEntity tileEntity, float p_225616_2_, MatrixStack p_225616_3_, IRenderTypeBuffer renderBuffer, int p_225616_5_, int p_225616_6_)
	{
		RANDOM.setSeed(31100L);
		double distance = tileEntity.getPos().distanceSq(this.dispatcher.renderInfo.getProjectedView(), true);
		int passes = this.getPasses(distance);
		float offSet = this.getOffset();
		Matrix4f matrix4f = p_225616_3_.peek().getModel();
		this.func_228883_a_(tileEntity, offSet, 0.15F, matrix4f, renderBuffer.getBuffer(field_228881_e_.get(0)));

		for (int currentPass = 1; currentPass < passes; ++currentPass)
		{
			this.func_228883_a_(tileEntity, offSet, 2.0F / (float) (18 - currentPass), matrix4f, renderBuffer.getBuffer(field_228881_e_.get(currentPass)));
		}
	}


	private void func_228883_a_(WBPortalTileEntity tileEntity, float offSet, float p_228883_3_, Matrix4f matrix4f, IVertexBuilder vertexBuilder)
	{
		float f = (RANDOM.nextFloat() * 0.5F + 0.1F) * p_228883_3_;
		float f1 = (RANDOM.nextFloat() * 0.5F + 0.4F) * p_228883_3_;
		float f2 = (RANDOM.nextFloat() * 0.5F + 0.5F) * p_228883_3_;
		this.func_228884_a_(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, f, f1, f2, Direction.SOUTH);
		this.func_228884_a_(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f, f1, f2, Direction.NORTH);
		this.func_228884_a_(tileEntity, matrix4f, vertexBuilder, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, f, f1, f2, Direction.EAST);
		this.func_228884_a_(tileEntity, matrix4f, vertexBuilder, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, f, f1, f2, Direction.WEST);
		this.func_228884_a_(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f, f1, f2, Direction.DOWN);
		this.func_228884_a_(tileEntity, matrix4f, vertexBuilder, 0.0F, 1.0F, offSet, offSet, 1.0F, 1.0F, 0.0F, 0.0F, f, f1, f2, Direction.UP);
	}


	private void func_228884_a_(WBPortalTileEntity tileEntity, Matrix4f matrix4f, IVertexBuilder vertexBuilder, float p_228884_4_, float p_228884_5_, float p_228884_6_, float p_228884_7_, float p_228884_8_, float p_228884_9_, float p_228884_10_, float p_228884_11_, float p_228884_12_, float p_228884_13_, float p_228884_14_, Direction direction)
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
			return 3;
		}
		else if (distanceAway > 16384.0D)
		{
			return 5;
		}
		else if (distanceAway > 9216.0D)
		{
			return 7;
		}
		else if (distanceAway > 4096.0D)
		{
			return 9;
		}
		else if (distanceAway > 1024.0D)
		{
			return 11;
		}
		else if (distanceAway > 576.0D)
		{
			return 13;
		}
		else
		{
			return distanceAway > 256.0D ? 14 : 15;
		}
	}


	protected float getOffset()
	{
		return 0.75F;
	}

}