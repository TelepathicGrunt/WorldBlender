package com.telepathicgrunt.world_blender.features;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class WBConfiguredFeatures
{
    public static final ConfiguredFeature<?,?> WB_PORTAL_ALTAR = WBFeatures.WB_PORTAL_ALTAR.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
    public static final ConfiguredFeature<?,?> NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS = WBFeatures.NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
    public static final ConfiguredFeature<?,?> SEPARATE_LAVA_AND_WATER = WBFeatures.SEPARATE_LAVA_AND_WATER.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
    
    public static void registerConfiguredFeatures()
    {
        MutableRegistry<ConfiguredFeature<?, ?>> registry = (MutableRegistry<ConfiguredFeature<?, ?>>) WorldGenRegistries.field_243653_e;

        Registry.register(registry, new ResourceLocation(WorldBlender.MODID, "portal_altar"), WB_PORTAL_ALTAR);
        Registry.register(registry, new ResourceLocation(WorldBlender.MODID, "no_floating_liquids_or_falling_blocks"), NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS);
        Registry.register(registry, new ResourceLocation(WorldBlender.MODID, "separate_lava_and_water"), SEPARATE_LAVA_AND_WATER);
    }
}
