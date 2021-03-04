package com.telepathicgrunt.worldblender.mixin.dimensions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.lighting.WorldLightManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldLightManager.class)
public class WorldLightManagerMixin {

	/**
	 * Prevents lighting crashes that is next to impossible to debug. Special thanks to shartte for
	 * figuring out the cause and allowing me to use his mixin workaround!
	 *
	 * @author shartte - https://github.com/AppliedEnergistics/Applied-Energistics-2/pull/4935/files
	 * @reason Required to make sure structures that replaces light blocks does not crash servers
	 */
	@Inject(method = "onBlockEmissionIncrease", at = @At("HEAD"), cancellable = true)
	public void onBlockEmissionIncrease(BlockPos blockPos, int lightLevel, CallbackInfo ci) {
		if (lightLevel == 0) {
			ci.cancel();
		}
	}
}
