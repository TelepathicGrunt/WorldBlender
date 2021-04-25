package com.telepathicgrunt.worldblender.mixin.dimensions;

import com.telepathicgrunt.worldblender.WBIdentifiers;
import com.telepathicgrunt.worldblender.dimension.EnderDragonFightModification;
import com.telepathicgrunt.worldblender.utils.ServerWorldAccess;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DragonFightManager.class)
public class EnderDragonFightMixin {

	@Shadow
	@Final
	private ServerWorld world;

	/*
	 * Reduce the laggy chunk load by letting the Altar class do a smaller chunk load
	 */
	@Inject(
			method = "isFightAreaLoaded()Z",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void loadSmallerChunks(CallbackInfoReturnable<Boolean> cir) {
		if(world.getDimensionKey().equals(WBIdentifiers.WB_WORLD_KEY)){
			if(((ServerWorldAccess)world).getAltar().isAltarMade()){
				cir.setReturnValue(true);
			}
			else{
				cir.setReturnValue(false);
			}
		}
	}

	/**
	 * Skip doing the laggy chunk checks. We will do a different check for portal in findExitPortal
	 */
	@Inject(
			method = "exitPortalExists()Z",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void exitPortalExists(CallbackInfoReturnable<Boolean> cir) {
		if(world.getDimensionKey().equals(WBIdentifiers.WB_WORLD_KEY)){
			cir.setReturnValue(false);
		}
	}


	/*
	 * Needed so that the portal check does not recognize a pattern in the
	 * Bedrock floor of World Blender's dimension as if it is an End Podium.
	 *
	 * This was the cause of End Podium and Altar not spawning in WB dimension randomly.
	 */
	@Inject(
			method = "findExitPortal()Lnet/minecraft/block/pattern/BlockPattern$PatternHelper;",
			at = @At(value = "RETURN", target = "Lnet/minecraft/util/math/vector/Vector3i;getY()I"),
			cancellable = true
	)
	private void findExitPortal(CallbackInfoReturnable<BlockPattern.PatternHelper> cir) {
		BlockPattern.PatternHelper result = EnderDragonFightModification.findEndPortal((DragonFightManager)(Object)this, cir.getReturnValue());
		if(cir.getReturnValue() != result) cir.setReturnValue(result);
	}
}
