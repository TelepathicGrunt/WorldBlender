package com.telepathicgrunt.world_blender.features;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.event.RegistryEvent;

public class WBFeatures
{
    public static Feature<NoFeatureConfig> WB_PORTAL_ALTAR = new WBPortalAltar();
    public static Feature<NoFeatureConfig> NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS = new NoFloatingLiquidsOrFallingBlocks();
    public static Feature<NoFeatureConfig> SEPARATE_LAVA_AND_WATER = new SeparateLavaAndWater();
    
    public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event)
    {
        WorldBlender.register(event.getRegistry(), WB_PORTAL_ALTAR, "portal_altar");
        WorldBlender.register(event.getRegistry(), NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS, "no_floating_liquids_or_falling_blocks");
        WorldBlender.register(event.getRegistry(), SEPARATE_LAVA_AND_WATER, "separate_lava_and_water");
    }
}
