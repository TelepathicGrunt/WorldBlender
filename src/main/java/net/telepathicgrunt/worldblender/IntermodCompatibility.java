package net.telepathicgrunt.worldblender;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.telepathicgrunt.worldblender.biome.WBBiomes;
import net.telepathicgrunt.worldblender.features.WBFeatures;

public class IntermodCompatibility
{
	public static void addDDDunegons() {
		//add our feature to handle their dungeons
		WBBiomes.biomes.forEach(blendedBiome -> 
			blendedBiome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, 
					WBFeatures.DD_DUNGEON_FEATURE.configure(IFeatureConfig.NO_FEATURE_CONFIG).createDecoratedFeature(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG))));
		
		//remove dimdungeon's dungeons so they dont spam the logs
		WBBiomes.biomes.forEach(blendedBiome -> 
			blendedBiome.features.get(GenerationStage.Decoration.UNDERGROUND_STRUCTURES).removeIf(feature -> feature.feature.getRegistryName().equals(new ResourceLocation("dimdungeons:feature_basic_dungeon")) || feature.feature.getRegistryName().equals(new ResourceLocation("dimdungeons:feature_advanced_dungeon"))));
	}
}
