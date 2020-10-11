package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(GenerationSettings.class)
public interface GenerationSettingsAccessor {

    @Accessor("features")
    List<List<Supplier<ConfiguredFeature<?, ?>>>> getGSFeatures();

    @Accessor("features")
    void setGSFeatures(List<List<Supplier<ConfiguredFeature<?, ?>>>> features);


    @Accessor("structureFeatures")
    List<Supplier<ConfiguredStructureFeature<?, ?>>> getGSStructureFeatures();

    @Accessor("structureFeatures")
    void setGSStructureFeatures(List<Supplier<ConfiguredStructureFeature<?, ?>>> structureFeatures);


    @Accessor("carvers")
    Map<GenerationStep.Carver, List<Supplier<ConfiguredCarver<?>>>> getCarvers();

    @Accessor("carvers")
    void setCarvers(Map<GenerationStep.Carver, List<Supplier<ConfiguredCarver<?>>>> features);

}