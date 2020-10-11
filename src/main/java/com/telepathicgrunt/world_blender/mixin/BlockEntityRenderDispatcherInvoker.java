package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockEntityRenderDispatcher.class)
public interface BlockEntityRenderDispatcherInvoker {

    @Invoker("register")
    <E extends BlockEntity> void callRegister(BlockEntityType<E> blockEntityType, BlockEntityRenderer<E> blockEntityRenderer);
}