package com.telepathicgrunt.worldblender.mixin.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.telepathicgrunt.worldblender.blocks.WBBlocks;
import com.telepathicgrunt.worldblender.blocks.WBPortalClientOverlay;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OverlayRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(OverlayRenderer.class)
public class OverlayRendererMixin {

	/**
	 * Need to mixin for my own block overlay rendering as my block doesn't cause suffocation which causes RenderBlockOverlayEvent to not run.
	 * This mixin will return a Pair<BlockState, BlockPos> which then will make the renderWBPortalBlockOverlay mixin fire
	 */
	@Inject(method = "getOverlayBlock(Lnet/minecraft/entity/player/PlayerEntity;)Lorg/apache/commons/lang3/tuple/Pair;",
			at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"),
			cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void isInWBPortalBlock(PlayerEntity playerIn, CallbackInfoReturnable<Pair<BlockState, BlockPos>> cir, BlockPos.Mutable blockpos$mutable,
										  int i, double d0, double d1, double d2, BlockState blockstate)
	{
		if(blockstate.getBlock() == WBBlocks.WORLD_BLENDER_PORTAL.get()){
			cir.setReturnValue(Pair.of(blockstate, blockpos$mutable.toImmutable()));
		}
	}

	/**
	 * Now render our overlay and set the pair to null. If we don't, the Forge event will fire and so will vanilla's block overlaying which
	 * will try to overlay the End Portal rod over our face which we do not want.
	 */
	@ModifyVariable(method = "renderOverlays(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/matrix/MatrixStack;)V",
			at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/OverlayRenderer;getOverlayBlock(Lnet/minecraft/entity/player/PlayerEntity;)Lorg/apache/commons/lang3/tuple/Pair;"))
	private static Pair<BlockState, BlockPos> renderWBPortalBlockOverlay(Pair<BlockState, BlockPos> foundBlock, Minecraft minecraftIn, MatrixStack matrixStackIn)
	{
		if(foundBlock != null && foundBlock.getLeft().getBlock() == WBBlocks.WORLD_BLENDER_PORTAL.get()){
			WBPortalClientOverlay.portalOverlay(minecraftIn.player, matrixStackIn);
			return null;
		}
		return foundBlock;
	}
}