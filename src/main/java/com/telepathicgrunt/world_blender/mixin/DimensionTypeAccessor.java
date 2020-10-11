package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {

    @Accessor("hasEnderDragonFight")
    void setEnderDragonFight(boolean hasDragon);
}