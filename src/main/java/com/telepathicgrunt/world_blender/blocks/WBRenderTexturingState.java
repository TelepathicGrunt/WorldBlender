package com.telepathicgrunt.world_blender.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class WBRenderTexturingState
{
	@OnlyIn(Dist.CLIENT)
	public static final class WBPortalTexturingState extends RenderState.TexturingState
	{
		private final int iteration;


		public WBPortalTexturingState(int layerIndex)
		{
			super("portal_texturing", () ->
			{
				RenderSystem.matrixMode(5890);
				RenderSystem.pushMatrix();
				RenderSystem.loadIdentity();
				RenderSystem.translatef(0.5F, 0.5F, 0.0F);
				RenderSystem.scalef(0.5F, 0.5F, 1.0F);
				RenderSystem.translatef(17.0F / layerIndex, (2.0F + layerIndex / 1.5F) * (Util.milliTime() % 1000000000000000000L / 100000.0F), 0.0F);
				RenderSystem.rotatef((layerIndex * layerIndex * 4321.0F + layerIndex * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
				RenderSystem.scalef(4.5F - layerIndex / 4.0F, 4.5F - layerIndex / 4.0F, 1.0F);
				RenderSystem.mulTextureByProjModelView();
				RenderSystem.matrixMode(5888);
				RenderSystem.setupEndPortalTexGen();
			}, () ->
			{
				RenderSystem.matrixMode(5890);
				RenderSystem.popMatrix();
				RenderSystem.matrixMode(5888);
				RenderSystem.clearTexGen();
			});
			this.iteration = layerIndex;
		}


		@Override
		public int hashCode()
		{
			return Integer.hashCode(this.iteration);
		}
	}
}
