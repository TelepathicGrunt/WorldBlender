package com.telepathicgrunt.world_blender.mixin;

import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.utils.ServerWorldAccess;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.tileentity.EndPortalTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.end.DragonFightManager;
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

	//Generate altar here only if enderdragon is on.
	//Otherwise spawning our altar in ServerWorld before dragon will not spawn dragon. Don't ask me why.
	//Cursed enderdragon code
	@Inject(
			method = "scanForLegacyFight",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/end/DragonFightManager;generatePortal(Z)V")
	)
	private void tickAltar(CallbackInfo ci) {
		if(world.getDimensionKey().func_240901_a_().equals(WBIdentifiers.MOD_DIMENSION_ID))
			((ServerWorldAccess)world).getAltar().tick();
	}


// Can't get this working.
//	@ModifyConstant(method = "worldContainsEndPortal",
//					constant = @Constant(classValue = Integer.class))
//	private int worldContainsEndPortalRadius(int radii) {
//		if(world.getRegistryKey().getValue().equals(WBResourceLocations.MOD_DIMENSION_ID) && radii != 1) {
//			radii = (int)Math.signum(radii);
//		}
//		return radii;
//	}

	/**
	 * Check only the center 9 chunks to prevent server hang when generating portal and EnderDragon.
	 * But only does the speed up in World Blender's world. Otherwise, does the 64 chunk check by default.
	 * @author TelepathicGrunt
	 * @reason spawn end podium and altar faster in world blender dimension
	 */
	@Overwrite(aliases = "exitPortalExists")
	private boolean exitPortalExists() {
		int radius = this.world.getDimensionKey().equals(WBIdentifiers.WB_WORLD_KEY) ? 1 : 8;
		for(int i = -radius; i <= radius; ++i) {
			for(int j = -radius; j <= radius; ++j) {
				Chunk worldChunk = this.world.getChunk(i, j);
				for (TileEntity blockEntity : worldChunk.getTileEntityMap().values()) {

					// If in World Blender dimension, only check for End Port entity.
					// Vanilla checks for gateways too which err well, breaks portal
					// spawning as End Gateways can spawn close to spawn.
					if (blockEntity instanceof EndPortalTileEntity && (
							!this.world.getDimensionKey().equals(WBIdentifiers.WB_WORLD_KEY) || !(blockEntity instanceof EndGatewayTileEntity)))
					{
						return true;
					}
				}
			}
		}

		return false;
	}


	/*
	 * Needed so that the portal check does not recognize a pattern in the
	 * Bedrock floor of World Blender's dimensiion as if it is an End Podium.
	 *
	 * This was the cause of End Podium and Altar not spawning in WB dimension randomly.
	 */
	@Inject(
			method = "findExitPortal()Lnet/minecraft/block/pattern/BlockPattern$PatternHelper;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/block/pattern/BlockPattern;match(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/pattern/BlockPattern$PatternHelper;"),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void doNotCheckWBBedrockFloor(CallbackInfoReturnable<BlockPattern.PatternHelper> cir, int k, int l) {
		if(l < 5 && world.getDimensionKey().func_240901_a_().equals(WBIdentifiers.MOD_DIMENSION_ID))
			cir.setReturnValue(null); // Skip checking the bedrock layer
	}
}
