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

    @Accessor("field_242484_f")
    List<List<Supplier<ConfiguredFeature<?, ?>>>> getGSFeatures();

    @Accessor("field_242484_f")
    void setGSFeatures(List<List<Supplier<ConfiguredFeature<?, ?>>>> features);


    @Accessor("field_242485_g")
    List<Supplier<StructureFeature<?, ?>>> getGSStructureFeatures();

    @Accessor("field_242485_g")
    void setGSStructureFeatures(List<Supplier<StructureFeature<?, ?>>> structureFeatures);


    @Accessor("field_242483_e")
    Map<GenerationStage.Carving, List<Supplier<ConfiguredCarver<?>>>> getCarvers();

    @Accessor("field_242483_e")
    void setCarvers(Map<GenerationStage.Carving, List<Supplier<ConfiguredCarver<?>>>> features);

}