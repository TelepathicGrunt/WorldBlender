package com.telepathicgrunt.worldblender.mixin.dimensions;

import com.telepathicgrunt.worldblender.blocks.WBPortalBlockEntityRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	/**
	 * Need to call this outside the if statement which is why the Forge onDrawBlockHighlight won't work for me.
	 * This is to do optimized rendering of my block entity.
	 */
	@Inject(
			method = "updateCameraAndRender",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/Minecraft;objectMouseOver:Lnet/minecraft/util/math/RayTraceResult;",
					shift = At.Shift.AFTER,
					ordinal = 1
			)
	)
	private void drawWBPortalBlock(CallbackInfo ci) {
		WBPortalBlockEntityRenderer.drawBuffers();
	}
}