package com.telepathicgrunt.world_blender.mixin.worldgen;

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
    List<List<Supplier<ConfiguredFeature<?, ?>>>> wb_getGSFeatures();

    @Accessor("features")
    void wb_setGSFeatures(List<List<Supplier<ConfiguredFeature<?, ?>>>> features);


    @Accessor("structures")
    List<Supplier<StructureFeature<?, ?>>> wb_getGSStructureFeatures();

    @Accessor("structures")
    void wb_setGSStructureFeatures(List<Supplier<StructureFeature<?, ?>>> structureFeatures);


    @Accessor("carvers")
    Map<GenerationStage.Carving, List<Supplier<ConfiguredCarver<?>>>> wb_getCarvers();

    @Accessor("carvers")
    void wb_setCarvers(Map<GenerationStage.Carving, List<Supplier<ConfiguredCarver<?>>>> features);

}
