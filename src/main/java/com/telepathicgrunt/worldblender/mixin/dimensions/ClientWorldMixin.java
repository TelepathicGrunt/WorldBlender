package com.telepathicgrunt.worldblender.mixin.dimensions;

import com.telepathicgrunt.worldblender.blocks.WBPortalBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

// Culling optimization by Comp500
// https://github.com/comp500/PolyDungeons/blob/master/src/main/java/polydungeons/block/entity/DecorativeEndBlockEntity.java
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {
	protected ClientWorldMixin(ClientPlayNetHandler networkHandler, ClientWorld.ClientWorldInfo properties, RegistryKey<World> registryRef, DimensionType dimensionType, int loadDistance, Supplier<IProfiler> profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed) {
		super(properties, registryRef, dimensionType, profiler, true, debugWorld, seed);
	}

	/**
	 * When a block rerender is scheduled, check its neighbors for instances of DecorativeEndPortalBlockEntity
	 * and update the cull cache if they are found.
	 */
	@Inject(at = @At("HEAD"), method = "markBlockRangeForRenderUpdate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V")
	public void onBlockRerender(BlockPos pos, BlockState old, BlockState updated, CallbackInfo ci) {
		WBPortalBlockEntity.updateCullCache(pos, this);
	}
}