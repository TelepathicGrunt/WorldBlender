package com.telepathicgrunt.world_blender.mixin;

import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.blocks.WBPortalBlockEntity;
import com.telepathicgrunt.world_blender.utils.ServerWorldAccess;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.tileentity.EndPortalTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DragonFightManager.class)
public class EnderDragonFightMixin {

	@Mutable
	@Final
	@Shadow
	private ServerWorld world;

	@Final
	@Shadow
	private BlockPattern portalPattern;

	@Mutable
	@Shadow
	private BlockPos exitPortalLocation;


	/**
	 * Skip doing the laggy chunk checks. We will do a different check for portal in findExitPortal
	 */
	@Inject(
			method = "exitPortalExists()Z",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void exitPortalExists(CallbackInfoReturnable<Boolean> cir) {
		if(this.world.getDimensionKey().equals(WBIdentifiers.WB_WORLD_KEY)){
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
		if(world.getDimensionKey().getLocation().equals(WBIdentifiers.MOD_DIMENSION_ID)){
			Chunk chunk = this.world.getChunk(0, 0);

			for(TileEntity tileentity : chunk.getTileEntityMap().values()) {
				if (tileentity instanceof WBPortalBlockEntity) {
					if(!((WBPortalBlockEntity) tileentity).isRemoveable()){
						BlockPattern.PatternHelper blockpattern$patternhelper = this.portalPattern.match(this.world, tileentity.getPos());
						if (blockpattern$patternhelper != null) {
							BlockPos blockpos = blockpattern$patternhelper.translateOffset(3, 7, 3).getPos();
							if (this.exitPortalLocation == null && blockpos.getX() == 0 && blockpos.getZ() == 0) {
								this.exitPortalLocation = blockpos;
							}

							cir.setReturnValue(blockpattern$patternhelper);
						}
					}
				}
			}

			cir.setReturnValue(null); // Skip checking the bedrock layer
		}
	}
}
