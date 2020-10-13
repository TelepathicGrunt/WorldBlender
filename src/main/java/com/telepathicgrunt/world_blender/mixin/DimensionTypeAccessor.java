package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.world.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {

    @Accessor("field_236015_u_")
    void setEnderDragonFight(boolean hasDragon);
}