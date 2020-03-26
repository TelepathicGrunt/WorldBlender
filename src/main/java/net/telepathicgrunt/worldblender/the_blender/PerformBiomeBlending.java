package net.telepathicgrunt.worldblender.the_blender;

import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.GenerationStage.Carving;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.MultipleRandomFeatureConfig;
import net.minecraft.world.gen.feature.MultipleWithChanceRandomFeatureConfig;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.feature.SingleRandomFeature;
import net.minecraft.world.gen.feature.TwoFeatureChoiceConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidWithNoiseConfig;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.registries.ForgeRegistries;
import net.telepathicgrunt.worldblender.biome.WBBiomes;
import net.telepathicgrunt.worldblender.biome.biomes.surfacebuilder.BlendedSurfaceBuilder;
import net.telepathicgrunt.worldblender.configs.WBConfig;
import net.telepathicgrunt.worldblender.features.WBFeatures;
import net.telepathicgrunt.worldblender.the_blender.ConfigBlacklisting.BlacklistType;


public class PerformBiomeBlending
{
	public static void setupBiomes()
	{
		FeatureGrouping.setupFeatureMaps();
		BlendedSurfaceBuilder.resetSurfaceList();
		
		//add end spike directly to all biomes if not directly blacklisted. Turning off vanilla features will not prevent end spikes from spawning due to them marking the world origin nicely
		if(!ConfigBlacklisting.isResourceLocationBlacklisted(BlacklistType.FEATURE, new ResourceLocation("minecraft:end_spike")))
			WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Feature.END_SPIKE.withConfiguration(new EndSpikeFeatureConfig(false, ImmutableList.of(), (BlockPos)null)).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG))));
	     
		
		for (Biome biome : ForgeRegistries.BIOMES.getValues())
		{
			//ignore our own biomes to speed things up and prevent possible duplications
			if (WBBiomes.biomes.contains(biome)) 
				continue;
			
			 // if the biome is a vanilla biome but config says no vanilla biome, skip this biome
			else if(biome.getRegistryName().getNamespace().equals("minecraft") && !WBConfig.allowVanillaBiomeImport) 
				continue;
			
			 // if the biome is a modded biome but config says no modded biome, skip this biome
			else if(!WBConfig.allowModdedBiomeImport)
				continue;
			
			//blacklisted by blanket list
			else if(ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.BLANKET, biome.getRegistryName())) 
				continue;
			

			///////////FEATURES//////////////////
			addBiomeFeatures(biome);

			//////////////////////STRUCTURES////////////////////////
			addBiomeStructures(biome);

			////////////////////////CARVERS/////////////////////////
			addBiomeCarvers(biome);

			////////////////////////SPAWNER/////////////////////////
			addBiomeNaturalMobs(biome);

			////////////////////////SURFACE/////////////////////////
			addBiomeSurfaceConfig(biome);
		}
		
		
		//////////Misc Features///////////////
		//Add these only after we have finally gone through all biomes
		
		//add grass, flower, and other small plants now so they are generated second to last
		for(GenerationStage.Decoration stage : GenerationStage.Decoration.values())
		{
			for (ConfiguredFeature<?, ?> grassyFlowerFeature : FeatureGrouping.SMALL_PLANT_MAP.get(stage))
			{
				if (!WBBiomes.BLENDED_BIOME.getFeatures(stage).stream().anyMatch(addedConfigFeature -> FeatureGrouping.serializeAndCompareFeature(addedConfigFeature, grassyFlowerFeature)))
				{
					WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(stage, grassyFlowerFeature));
				}
			}
		}

		
		if(!WBConfig.disallowLaggyFeatures && FeatureGrouping.bambooFound)
		{
			//add bamboo so it is dead last
			WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Feature.BAMBOO.withConfiguration(new ProbabilityConfig(0.2F)).withPlacement(Placement.TOP_SOLID_HEIGHTMAP_NOISE_BIASED.configure(new TopSolidWithNoiseConfig(160, 80.0D, 0.3D, Heightmap.Type.WORLD_SURFACE_WG)))));
		}
		
		

		//Makes carvers be able to carve any underground blocks including netherrack, end stone, and modded blocks
		Set<Block> allBlocksToCarve = BlendedSurfaceBuilder.blocksToCarve();
		for (Carving carverStage : GenerationStage.Carving.values())
		{
			for (ConfiguredCarver<?> carver : WBBiomes.BLENDED_BIOME.getCarvers(carverStage))
			{
				allBlocksToCarve.addAll(carver.carver.carvableBlocks);
				carver.carver.carvableBlocks = allBlocksToCarve;
			}
		}

		//add this last so that this can contain other local modification feature's liquids/falling blocks better
		if(!ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, new ResourceLocation("world_blender:no_floating_liquids_or_falling_blocks")))
		{
			WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(Decoration.LOCAL_MODIFICATIONS, WBFeatures.NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG))));
		}
		
		
		//free up memory when we are done.
		FeatureGrouping.clearFeatureMaps();
		WBBiomes.VANILLA_TEMP_BIOME = null;
	}

	private static void addBiomeFeatures(Biome biome)
	{
		for (Decoration stage : GenerationStage.Decoration.values())
		{
			for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeatures(stage))
			{
				if (!WBBiomes.BLENDED_BIOME.getFeatures(stage).stream().anyMatch(addedConfigFeature -> FeatureGrouping.serializeAndCompareFeature(addedConfigFeature, configuredFeature)))
				{
					/////////Check feature blacklist from config
					if(configuredFeature.config instanceof DecoratedFeatureConfig)
					{
						ConfiguredFeature<?, ?> insideFeature = ((DecoratedFeatureConfig)configuredFeature.config).feature;
						
						if(ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, insideFeature.feature.getRegistryName()))
						{
							continue;
						}
						
						
						//A bunch of edge cases that have to handled because features can hold other features.
						//If a mod makes a custom feature to hold features, welp, we are screwed. Nothing we can do about it. 
						if(insideFeature.feature == Feature.RANDOM_RANDOM_SELECTOR)
						{
							if(((MultipleWithChanceRandomFeatureConfig)insideFeature.config).features.stream().anyMatch(buriedFeature -> ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, buriedFeature.feature.getRegistryName()))) 
							{
								continue;
							}
						}
						
						if(insideFeature.feature == Feature.RANDOM_SELECTOR)
						{
							if(((MultipleRandomFeatureConfig)insideFeature.config).features.stream().anyMatch(buriedFeature -> ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, buriedFeature.feature.feature.getRegistryName()))) 
							{
								continue;
							}
						}
						
						if(insideFeature.feature == Feature.SIMPLE_RANDOM_SELECTOR)
						{
							if(((SingleRandomFeature)insideFeature.config).features.stream().anyMatch(buriedFeature -> ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, buriedFeature.feature.getRegistryName()))) 
							{
								continue;
							}
						}
						
						if(insideFeature.feature == Feature.RANDOM_BOOLEAN_SELECTOR)
						{
							if(ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, ((TwoFeatureChoiceConfig)insideFeature.config).field_227285_a_.feature.getRegistryName()) ||
								ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, ((TwoFeatureChoiceConfig)insideFeature.config).field_227286_b_.feature.getRegistryName())) 
							{
								continue;
							}
						}
					}
					
					
					
					////begin adding features//////
					
					//check if feature is already added
					if(WBBiomes.VANILLA_TEMP_BIOME.getFeatures(stage).stream().anyMatch(vanillaConfigFeature -> FeatureGrouping.serializeAndCompareFeature(vanillaConfigFeature, configuredFeature))) 
					{
						
						if (WBConfig.SERVER.allowVanillaFeatures.get())
						{
							//add the vanilla grass and flowers to a map so we can add them 
							//later to the feature list so trees have a chance to spawn
							if(FeatureGrouping.checksAndAddSmallPlantFeatures(stage, configuredFeature))
							{
								continue;
							}
								
							//if we have no laggy feature config on, then the feature must not be fire, lava, bamboo, etc in order to be added
							if((!FeatureGrouping.isLaggyFeature(stage, configuredFeature) || !WBConfig.disallowLaggyFeatures) &&
								!(configuredFeature.config instanceof DecoratedFeatureConfig &&
								 ((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.BAMBOO))
							{
								WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(stage, configuredFeature));
							}
						}
					}
					else if (WBConfig.SERVER.allowModdedFeatures.get())
					{
						//add the vanilla grass and flowers to a map so we can add them 
						//later to the feature list so trees have a chance to spawn
						if(FeatureGrouping.checksAndAddSmallPlantFeatures(stage, configuredFeature))
						{
							continue;
						}
						
						//adds modded features that might be trees to front of feature list so 
						//they have priority over all vanilla features in same generation stage.
						else if(FeatureGrouping.checksAndAddLargePlantFeatures(stage, configuredFeature))
						{
							WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.features.get(stage).add(0, configuredFeature));
						}
						else
						{
							//cannot be a bamboo feature as we will place them dead last in the feature
							//list so they don't overwhelm other features or cause as many bamboo breaking
							//because it got cut off
							//if we have no laggy feature config on, then the feature must not be fire, lava, bamboo, etc in order to be added
							if((!FeatureGrouping.isLaggyFeature(stage, configuredFeature) || !WBConfig.disallowLaggyFeatures) &&
								!(configuredFeature.config instanceof DecoratedFeatureConfig &&
								 ((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.BAMBOO))
							{
								WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(stage, configuredFeature));
							}
						}
					}
				}
			}
		}
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void addBiomeStructures(Biome biome)
	{
		for (Structure<?> structure : biome.structures.keySet())
		{
			if (!WBBiomes.BLENDED_BIOME.structures.keySet().stream().anyMatch(struct -> struct == structure))
			{
				//blacklisted by structure list
				if(ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.STRUCTURE, structure.getRegistryName())) {
					continue;
				}
				
				if (WBBiomes.VANILLA_TEMP_BIOME.structures.keySet().stream().anyMatch(vanillaStructure -> vanillaStructure.getClass().equals(structure.getClass())))
				{
					if (WBConfig.SERVER.allowVanillaStructures.get())
					{
						//add the structure version of the structure
						WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addStructure(new ConfiguredFeature(structure, biome.structures.get(structure))));
						boolean finishedFeaturePortion = false;
						
						//find the feature version of the structure in this biome and add it so it can spawn
						for (Decoration stage : GenerationStage.Decoration.values())
						{
							for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeatures(stage))
							{
								if(configuredFeature.config instanceof DecoratedFeatureConfig && 
								   ((DecoratedFeatureConfig)configuredFeature.config).feature.feature.getClass().equals(structure.getClass())) 
								{
									if(!WBBiomes.BLENDED_BIOME.features.get(stage).stream().anyMatch(addedFeature -> FeatureGrouping.serializeAndCompareFeature(addedFeature, configuredFeature)))
									{
										WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(stage, configuredFeature));
									}
									
									finishedFeaturePortion = true;
									break;
								}
							}
							
							if(finishedFeaturePortion) break;
						}
					}
						
				}
				else if (WBConfig.SERVER.allowModdedStructures.get())
				{
					//add the structure version of the structure
					WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addStructure(new ConfiguredFeature(structure, biome.structures.get(structure))));
					boolean finishedFeaturePortion = false;
					
					//find the feature version of the structure in this biome and add it so it can spawn
					for (Decoration stage : GenerationStage.Decoration.values())
					{
						for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeatures(stage))
						{
							if(configuredFeature.feature.getClass().equals(biome.structures.get(structure).getClass())) {

								if(!WBBiomes.BLENDED_BIOME.features.get(stage).stream().anyMatch(addedFeature -> FeatureGrouping.serializeAndCompareFeature(addedFeature, configuredFeature)))
								{
									WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(stage, configuredFeature));
								}
								
								finishedFeaturePortion = true;
								break;
							}
						}
						
						if(finishedFeaturePortion) break;
					}
				}
			}
		}
	}


	private static void addBiomeCarvers(Biome biome)
	{
		for (Carving carverStage : GenerationStage.Carving.values())
		{
			for (ConfiguredCarver<?> carver : biome.getCarvers(carverStage))
			{
				//blacklisted by carver list
				if(ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.CARVER, carver.carver.getRegistryName())) {
					continue;
				}
				
				if (!WBBiomes.BLENDED_BIOME.getCarvers(carverStage).stream().anyMatch(config -> config.carver == carver.carver))
				{
					if (carver.carver.getRegistryName() != null && carver.carver.getRegistryName().getNamespace().equals("minecraft"))
					{
						if (WBConfig.SERVER.allowVanillaCarvers.get())
							WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addCarver(carverStage, carver));
					}
					else if (WBConfig.SERVER.allowModdedCarvers.get())
					{
						WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addCarver(carverStage, carver));
					}
				}
			}
		}
	}


	private static void addBiomeNaturalMobs(Biome biome)
	{
		for (EntityClassification entityClass : EntityClassification.values())
		{
			for (SpawnListEntry spawnEntry : biome.getSpawns(entityClass))
			{
				//blacklisted by natural spawn list
				if(ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SPAWN, spawnEntry.entityType.getRegistryName())) {
					continue;
				}
				
				if (!WBBiomes.BLENDED_BIOME.getSpawns(entityClass).stream().anyMatch(spawn -> spawn.entityType == spawnEntry.entityType))
				{
					if (spawnEntry.entityType.getRegistryName() != null && spawnEntry.entityType.getRegistryName().getNamespace().equals("minecraft"))
					{
						if (WBConfig.SERVER.allowVanillaSpawns.get())
							WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addSpawn(entityClass, spawnEntry));
					}
					else if (WBConfig.SERVER.allowModdedSpawns.get())
					{
						WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addSpawn(entityClass, spawnEntry));
					}
				}
			}
		}
	}


	private static void addBiomeSurfaceConfig(Biome biome)
	{
		//return early if biome's is turned off by the vanilla surface configs.
		if (biome.getRegistryName() != null && biome.getRegistryName().getNamespace().equals("minecraft"))
		{
			if (!WBConfig.SERVER.allowVanillaSurfaces.get())
				return;
		}
		else if (!WBConfig.SERVER.allowModdedSurfaces.get())
		{
			return;
		}
		
		SurfaceBuilderConfig surfaceConfig = (SurfaceBuilderConfig) biome.getSurfaceBuilderConfig();
		
		//blacklisted by surface list. Checks top block
		if(ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, surfaceConfig.getTop().getBlock().getRegistryName()))
		{
			return;
		}
		
		if (!((BlendedSurfaceBuilder) WBBiomes.BLENDED_SURFACE_BUILDER).containsConfig(surfaceConfig))
		{
			((BlendedSurfaceBuilder) WBBiomes.BLENDED_SURFACE_BUILDER).addConfig(surfaceConfig);
		}
	}
}
