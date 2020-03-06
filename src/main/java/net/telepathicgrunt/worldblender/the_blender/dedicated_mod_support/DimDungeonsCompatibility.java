package net.telepathicgrunt.worldblender.the_blender.dedicated_mod_support;

import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.telepathicgrunt.worldblender.biome.WBBiomes;
import net.telepathicgrunt.worldblender.features.WBFeatures;

public class DimDungeonsCompatibility
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
				DecoratedFeatureConfig decoratedFeatureConfig;
				if(cflist.get(i).config instanceof DecoratedFeatureConfig)
					decoratedFeatureConfig = (DecoratedFeatureConfig) cflist.get(i).config;
				else 
					continue;
				
				//only add our DD feature if the biome contains the actual mod's feature
				if(decoratedFeatureConfig.feature.feature.getRegistryName().equals(DD_BASIC_DUNGEON_RL) ||
				   decoratedFeatureConfig.feature.feature.getRegistryName().equals(DD_ADVANCED_DUNGEON_RL)) 
				{
					//remove DD's dungeon since it wont spawn normally in our biome
					blendedBiome.features.get(GenerationStage.Decoration.SURFACE_STRUCTURES).remove(cflist.get(i));
					
					//adds our DD dungeon feature if it isn't added yet
					if(!blendedBiome.getFeatures(GenerationStage.Decoration.UNDERGROUND_STRUCTURES).stream().anyMatch(alreadyAddedFeature -> alreadyAddedFeature.config instanceof DecoratedFeatureConfig ? ((DecoratedFeatureConfig)alreadyAddedFeature.config).feature.feature.getRegistryName().equals(WBFeatures.DD_DUNGEON_FEATURE.getRegistryName()) : false)) 
					{
						
						//add our dungeon instead
						blendedBiome.addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, 
								WBFeatures.DD_DUNGEON_FEATURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
					}
				}
			}
		}
	}
}
