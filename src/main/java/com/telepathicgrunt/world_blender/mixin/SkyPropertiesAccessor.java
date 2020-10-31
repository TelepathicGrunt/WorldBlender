package com.telepathicgrunt.world_blender.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionRenderInfo.class)
public interface SkyPropertiesAccessor {

    @Accessor("field_239208_a_")
    static Object2ObjectMap<ResourceLocation, DimensionRenderInfo> getfield_239208_a_() {
        throw new UnsupportedOperationException();
    }
}