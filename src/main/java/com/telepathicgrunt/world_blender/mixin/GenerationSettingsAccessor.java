package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(BiomeGenerationSettings.class)
public interface GenerationSettingsAccessor {

    @Accessor("features")
    List<List<Supplier<ConfiguredFeature<?, ?>>>> getGSFeatures();

    @Accessor("features")
    void setGSFeatures(List<List<Supplier<ConfiguredFeature<?, ?>>>> features);


    @Accessor("structures")
    List<Supplier<StructureFeature<?, ?>>> getGSStructureFeatures();

    @Accessor("structures")
    void setGSStructureFeatures(List<Supplier<StructureFeature<?, ?>>> structureFeatures);


    @Accessor("carvers")
    Map<GenerationStage.Carving, List<Supplier<ConfiguredCarver<?>>>> getCarvers();

    @Accessor("carvers")
    void setCarvers(Map<GenerationStage.Carving, List<Supplier<ConfiguredCarver<?>>>> features);

}
