package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.world.gen.area.LazyArea;
import net.minecraft.world.gen.layer.Layer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Layer.class)
public interface BiomeLayerSamplerAccessor {

    @Accessor("field_215742_b")
    LazyArea wb_getSampler();
}
