package com.telepathicgrunt.world_blender.features;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeatureConfig;

public class WBConfiguredFeatures
{
    public static final ConfiguredFeature<?,?> WB_PORTAL_ALTAR = WBFeatures.WB_PORTAL_ALTAR.configure(FeatureConfig.DEFAULT);
    public static final ConfiguredFeature<?,?> NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS = WBFeatures.NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS.configure(FeatureConfig.DEFAULT);
    public static final ConfiguredFeature<?,?> SEPARATE_LAVA_AND_WATER = WBFeatures.SEPARATE_LAVA_AND_WATER.configure(FeatureConfig.DEFAULT);
    
    public static void registerConfiguredFeatures()
    {
        MutableRegistry<ConfiguredFeature<?, ?>> registry = (MutableRegistry<ConfiguredFeature<?, ?>>) BuiltinRegistries.CONFIGURED_FEATURE;

        Registry.register(registry, new Identifier(WorldBlender.MODID, "portal_altar"), WB_PORTAL_ALTAR);
        Registry.register(registry, new Identifier(WorldBlender.MODID, "no_floating_liquids_or_falling_blocks"), NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS);
        Registry.register(registry, new Identifier(WorldBlender.MODID, "separate_lava_and_water"), SEPARATE_LAVA_AND_WATER);
    }
}
