package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.blockstateprovider.WeightedBlockStateProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.locks.ReentrantLock;

@Mixin(WeightedBlockStateProvider.class)
public class WeightedBlockStateProviderMixin {
	private final ReentrantLock lock = new ReentrantLock();

	@Inject(method = "getBlockState",
			at = @At(value = "HEAD"))
	public void lockGetBlockState(CallbackInfoReturnable<BlockState> cir) {
		lock.lock();
	}

	@Inject(method = "getBlockState",
			at = @At(value = "RETURN"))
	public void unlockGetBlockState(CallbackInfoReturnable<BlockState> cir) {
		lock.unlock();
	}
}
