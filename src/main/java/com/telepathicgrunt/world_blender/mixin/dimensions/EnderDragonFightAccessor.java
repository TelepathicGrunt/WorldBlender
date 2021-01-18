package com.telepathicgrunt.world_blender.mixin.dimensions;

import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DragonFightManager.class)
public interface EnderDragonFightAccessor {

    @Accessor("world")
    ServerWorld wb_getworld();

    @Accessor("portalPattern")
    BlockPattern wb_getendPortalPattern();

    @Accessor("exitPortalLocation")
    BlockPos wb_getexitPortalLocation();

    @Accessor("exitPortalLocation")
    void wb_setexitPortalLocation(BlockPos pos);
}