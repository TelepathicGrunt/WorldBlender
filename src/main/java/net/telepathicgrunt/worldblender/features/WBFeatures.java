package net.telepathicgrunt.worldblender.features;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.telepathicgrunt.worldblender.WorldBlender;

public class WBFeatures
{
    public static Feature<NoFeatureConfig> WB_PORTAL_ALTAR = new WBPortalAltar(NoFeatureConfig::deserialize);
    public static Feature<NoFeatureConfig> DD_DUNGEON_FEATURE = new DDDungeonFeature(NoFeatureConfig::deserialize);
    public static Feature<NoFeatureConfig> NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS = new NoFloatingLiquidsOrFallingBlocks(NoFeatureConfig::deserialize);

    public static void registerFeatures(RegistryEvent.Register<Feature<?>> event)
    {
    	IForgeRegistry<Feature<?>> registry = event.getRegistry();
        WorldBlender.register(registry, WB_PORTAL_ALTAR, "world_blender_portal_altar");
        WorldBlender.register(registry, DD_DUNGEON_FEATURE, "dd_dungeon_feature");
        WorldBlender.register(registry, NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS, "no_floating_liquids_or_falling_blocks");
    }
}
