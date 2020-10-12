package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.block.entity.TileEntity;
import net.minecraft.block.entity.TileEntityType;
import net.minecraft.client.render.block.entity.TileEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.TileEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TileEntityRenderDispatcher.class)
public interface BlockEntityRenderDispatcherInvoker {

    @Invoker("register")
    <E extends TileEntity> void callRegister(TileEntityType<E> blockEntityType, TileEntityRenderer<E> blockEntityRenderer);
}