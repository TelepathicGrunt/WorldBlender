package com.telepathicgrunt.world_blender.mixin;

import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.utils.ISeedReader;
import net.minecraft.block.entity.TileEntity;
import net.minecraft.block.entity.EndGatewayTileEntity;
import net.minecraft.block.entity.EndPortalTileEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonFight.class)
public class EnderDragonFightMixin {

	@Mutable
	@Final
	@Shadow
	private ServerWorld world;

	//Generate altar here only if enderdragon is on.
	//Otherwise spawning our altar in ServerWorld before dragon will not spawn dragon. Don't ask me why.
	//Cursed enderdragon code
	@Inject(
			method = "convertFromLegacy",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/EnderDragonFight;generateEndPortal(Z)V")
	)
	private void tickAltar(CallbackInfo ci) {
		if(world.getRegistryKey().getValue().equals(WBIdentifiers.MOD_DIMENSION_ID))
			((ISeedReader)world).getAltar().tick();
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
	@Overwrite(aliases = "worldContainsEndPortal")
	private boolean worldContainsEndPortal() {
		int radius = this.world.getRegistryKey().equals(WBIdentifiers.WB_WORLD_KEY) ? 1 : 8;
		for(int i = -radius; i <= radius; ++i) {
			for(int j = -radius; j <= radius; ++j) {
				WorldChunk worldChunk = this.world.getChunk(i, j);
				for (TileEntity blockEntity : worldChunk.getBlockEntities().values()) {

					// If in World Blender dimension, only check for End Port entity.
					// Vanilla checks for gateways too which err well, breaks portal
					// spawning as End Gateways can spawn close to spawn.
					if (blockEntity instanceof EndPortalBlockEntity && (
							!this.world.getRegistryKey().equals(WBIdentifiers.WB_WORLD_KEY) || !(blockEntity instanceof EndGatewayTileEntity)))
					{
						return true;
					}
				}
			}
		}

		return false;
	}
}
