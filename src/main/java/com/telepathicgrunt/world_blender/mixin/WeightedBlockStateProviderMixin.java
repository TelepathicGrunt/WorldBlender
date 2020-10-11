package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

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
