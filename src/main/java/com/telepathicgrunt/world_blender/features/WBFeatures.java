package com.telepathicgrunt.world_blender.features;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class WBFeatures
{
    public static Feature<DefaultFeatureConfig> WB_PORTAL_ALTAR = new WBPortalAltar();
    public static Feature<DefaultFeatureConfig> NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS = new NoFloatingLiquidsOrFallingBlocks();
    public static Feature<DefaultFeatureConfig> SEPARATE_LAVA_AND_WATER = new SeparateLavaAndWater();
    
    public static void registerFeatures()
    {
        Registry.register(Registry.FEATURE, new Identifier(WorldBlender.MODID, "portal_altar"), WB_PORTAL_ALTAR);
        Registry.register(Registry.FEATURE, new Identifier(WorldBlender.MODID, "no_floating_liquids_or_falling_blocks"), NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS);
        Registry.register(Registry.FEATURE, new Identifier(WorldBlender.MODID, "separate_lava_and_water"), SEPARATE_LAVA_AND_WATER);
    }
}
