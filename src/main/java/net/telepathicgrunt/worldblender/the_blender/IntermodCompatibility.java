package net.telepathicgrunt.worldblender.the_blender;

import java.util.List;

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
	
	public static void addDDDungeons() 
	{
		
		//add our feature to handle their dungeons
		for(Biome blendedBiome : WBBiomes.biomes)
		{
			List<ConfiguredFeature<?, ?>> cflist = blendedBiome.getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES);
			for (int i = cflist.size() - 1; i >= 0; i--)
			{
				//only add our DD feature if the biome contains the actual mod's feature
				if(cflist.get(i).feature.getRegistryName().equals(DD_BASIC_DUNGEON_RL) ||
				   cflist.get(i).feature.getRegistryName().equals(DD_ADVANCED_DUNGEON_RL)) 
				{
					//remove DD's dungeon since it wont spawn normally in our biome
					blendedBiome.features.get(GenerationStage.Decoration.SURFACE_STRUCTURES).remove(cflist.get(i));
					
					//adds our DD dungeon feature if it isn't added yet
					if(!blendedBiome.getFeatures(GenerationStage.Decoration.UNDERGROUND_STRUCTURES).stream().anyMatch(alreadyAddedFeature -> alreadyAddedFeature.feature.getRegistryName().equals(WBFeatures.DD_DUNGEON_FEATURE.getRegistryName()))) 
					{
						
						//add our dungeon instead
						blendedBiome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, 
								WBFeatures.DD_DUNGEON_FEATURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
					}
				}
			}
		}
	}
	public static void addTerraForgedTrees() 
	{
		
		//add our feature to handle their dungeons
		for(Biome blendedBiome : WBBiomes.biomes)
		{
			blendedBiome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, 
					WBFeatures.DD_DUNGEON_FEATURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
		
		}
		
		//remove DD's dungeons here so we do not cause concurrent error if we were to remove it in the above loop 
		WBBiomes.biomes.forEach(blendedBiome -> 
			blendedBiome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, 
					WBFeatures.DD_DUNGEON_FEATURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG))));
		
	}
}
