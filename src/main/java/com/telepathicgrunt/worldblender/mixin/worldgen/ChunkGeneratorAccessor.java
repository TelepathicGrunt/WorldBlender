package com.telepathicgrunt.worldblender.mixin.worldgen;

import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorAccessor {
    @Accessor("biomeProvider")
    BiomeProvider getBiomeSource();

    @Accessor("field_235949_c_")
    BiomeProvider getPopulationSource();
}
