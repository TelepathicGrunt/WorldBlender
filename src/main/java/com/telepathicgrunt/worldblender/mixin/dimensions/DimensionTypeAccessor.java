package com.telepathicgrunt.worldblender.mixin.dimensions;

import net.minecraft.world.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {

    @Accessor(value = "hasDragonFight")
    void wb_setHasDragonFight(boolean hasDragon);
}