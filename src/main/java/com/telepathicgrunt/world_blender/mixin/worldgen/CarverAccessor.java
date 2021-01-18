package com.telepathicgrunt.world_blender.mixin.worldgen;

import net.minecraft.block.Block;
import net.minecraft.world.gen.carver.WorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(WorldCarver.class)
public interface CarverAccessor {

    @Accessor("carvableBlocks")
    Set<Block> wb_getalwaysCarvableBlocks();

    @Accessor("carvableBlocks")
    void wb_setalwaysCarvableBlocks(Set<Block> blockSet);
}