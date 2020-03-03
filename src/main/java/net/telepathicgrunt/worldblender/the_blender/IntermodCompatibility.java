package net.telepathicgrunt.worldblender.the_blender;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.telepathicgrunt.worldblender.biome.WBBiomes;
import net.telepathicgrunt.worldblender.features.WBFeatures;

public class IntermodCompatibility
{
	private static ResourceLocation DD_BASIC_DUNGEON_RL = new ResourceLocation("dimdungeons:feature_basic_dungeon");
	private static ResourceLocation DD_ADVANCED_DUNGEON_RL = new ResourceLocation("dimdungeons:feature_advanced_dungeon");
	
	public static void addDDDunegons() 
	{
		
		//add our feature to handle their dungeons
		for(Biome blendedBiome : WBBiomes.biomes)
		{
			for (ConfiguredFeature<?, ?> configuredFeature : blendedBiome.getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES))
			{
				//only add our DD feature if the biome contains the actual mod's feature and our biome doesn't have our version yet
				if(configuredFeature.feature.getRegistryName().equals(DD_BASIC_DUNGEON_RL) ||
				   configuredFeature.feature.getRegistryName().equals(DD_ADVANCED_DUNGEON_RL) &&
				   !blendedBiome.getFeatures(GenerationStage.Decoration.UNDERGROUND_STRUCTURES).stream().anyMatch(alreadyAddedFeature -> alreadyAddedFeature.feature.getRegistryName().equals(WBFeatures.DD_DUNGEON_FEATURE.getRegistryName()))) 
				{
					blendedBiome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, 
							WBFeatures.DD_DUNGEON_FEATURE.configure(IFeatureConfig.NO_FEATURE_CONFIG).createDecoratedFeature(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
				}
			}
		}
		
		//remove DD's dungeons here so we do not cause concurrent error if we were to remove it in the above loop 
		WBBiomes.biomes.forEach(blendedBiome -> 
			blendedBiome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, 
					WBFeatures.DD_DUNGEON_FEATURE.configure(IFeatureConfig.NO_FEATURE_CONFIG).createDecoratedFeature(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG))));
		
	}
}
