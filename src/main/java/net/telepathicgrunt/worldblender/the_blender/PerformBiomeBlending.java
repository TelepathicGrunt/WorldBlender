package net.telepathicgrunt.worldblender.the_blender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.Dynamic;

import net.minecraft.entity.EntityClassification;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.GenerationStage.Carving;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.MultipleRandomFeatureConfig;
import net.minecraft.world.gen.feature.MultipleWithChanceRandomFeatureConfig;
import net.minecraft.world.gen.feature.SingleRandomFeature;
import net.minecraft.world.gen.feature.TwoFeatureChoiceConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.registries.ForgeRegistries;
import net.telepathicgrunt.worldblender.biome.WBBiomes;
import net.telepathicgrunt.worldblender.biome.biomes.surfacebuilder.BlendedSurfaceBuilder;
import net.telepathicgrunt.worldblender.configs.WBConfig;


public class PerformBiomeBlending
{
	private static List<ConfiguredFeature<?, ?>> grassyFlowerList = new ArrayList<ConfiguredFeature<?, ?>>();
	private static List<ConfiguredFeature<?, ?>> bambooList = new ArrayList<ConfiguredFeature<?, ?>>();
	
	public static void setupBiomes()
	{
		BlendedSurfaceBuilder.resetSurfaceList();
		grassyFlowerList = new ArrayList<ConfiguredFeature<?, ?>>();
		bambooList = new ArrayList<ConfiguredFeature<?, ?>>();
		
		for (Biome biome : ForgeRegistries.BIOMES.getValues())
		{
			//ignore our own biomes to speed things up and prevent possible duplications
			if (WBBiomes.biomes.contains(biome)) 
				continue;
			
			 // if the biome is a vanilla biome but config says no vanilla biome, skip this biome
			if(biome.getRegistryName().getNamespace().equals("minecraft")) {
				if(!WBConfig.allowVanillaBiomeImport)
					continue;
			}
			 // if the biome is a modded biome but config says no modded biome, skip this biome
			else if(!WBConfig.allowModdedBiomeImport){
				continue;
			}
			
			//blacklisted by blanket list
			if(ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.BLANKET, biome.getRegistryName())) {
				continue;
			}
			

			///////////FEATURES//////////////////
			addBiomeFeatures(biome, bambooList, grassyFlowerList);

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
		
		//add grass and flowers now so they are generated second to last
		for (ConfiguredFeature<?, ?> grassyFlowerFeature : grassyFlowerList)
		{
			if (!WBBiomes.BLENDED_BIOME.getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).stream().anyMatch(addedConfigFeature -> serializeAndCompareFeature(addedConfigFeature, grassyFlowerFeature)))
			{
				WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, grassyFlowerFeature));
			}
		}

		if(!WBConfig.disallowLaggyVanillaFeatures)
		{
			//add bamboo so it is dead last
			for (ConfiguredFeature<?, ?> bambooFeature : bambooList)
			{
				if (!WBBiomes.BLENDED_BIOME.getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).stream().anyMatch(addedConfigFeature -> serializeAndCompareFeature(addedConfigFeature, bambooFeature)))
				{
					WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, bambooFeature));
				}
			}
		}
	}

	private static void addBiomeFeatures(Biome biome, List<ConfiguredFeature<?, ?>> bambooList, List<ConfiguredFeature<?, ?>> grassyFlowerList)
	{
		for (Decoration stage : GenerationStage.Decoration.values())
		{
			for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeatures(stage))
			{
				if (!WBBiomes.BLENDED_BIOME.getFeatures(stage).stream().anyMatch(addedConfigFeature -> serializeAndCompareFeature(addedConfigFeature, configuredFeature)))
				{
					//feature blacklisted
					if(configuredFeature.config instanceof DecoratedFeatureConfig)
					{
						ConfiguredFeature<?, ?> insideFeature = ((DecoratedFeatureConfig)configuredFeature.config).feature;
						
						if(ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.FEATURE, insideFeature.feature.getRegistryName()))
						{
							continue;
						}
						
						
						//A bunch of edge cases that have to handled because features can hold other features.
						//If a mod makes a custom feature to hold features, welp, we are screwed. Nothing we can do about it. 
						if(insideFeature.feature == Feature.RANDOM_RANDOM_SELECTOR)
						{
							if(((MultipleWithChanceRandomFeatureConfig)insideFeature.config).features.stream().anyMatch(buriedFeature -> ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.FEATURE, buriedFeature.feature.getRegistryName()))) 
							{
								continue;
							}
						}
						
						if(insideFeature.feature == Feature.RANDOM_SELECTOR)
						{
							if(((MultipleRandomFeatureConfig)insideFeature.config).features.stream().anyMatch(buriedFeature -> ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.FEATURE, buriedFeature.feature.feature.getRegistryName()))) 
							{
								continue;
							}
						}
						
						if(insideFeature.feature == Feature.SIMPLE_RANDOM_SELECTOR)
						{
							if(((SingleRandomFeature)insideFeature.config).features.stream().anyMatch(buriedFeature -> ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.FEATURE, buriedFeature.feature.getRegistryName()))) 
							{
								continue;
							}
						}
						
						if(insideFeature.feature == Feature.RANDOM_BOOLEAN_SELECTOR)
						{
							if(ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.FEATURE, ((TwoFeatureChoiceConfig)insideFeature.config).field_227285_a_.feature.getRegistryName()) ||
								ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.FEATURE, ((TwoFeatureChoiceConfig)insideFeature.config).field_227286_b_.feature.getRegistryName())) 
							{
								continue;
							}
						}
					}
					
					if(WBBiomes.VANILLA_TEMP_BIOME.getFeatures(stage).stream().anyMatch(vanillaConfigFeature -> serializeAndCompareFeature(vanillaConfigFeature, configuredFeature))) 
					{
						
						if (WBConfig.SERVER.allowVanillaFeatures.get())
						{
							if (configuredFeature.config instanceof DecoratedFeatureConfig &&
								(((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.RANDOM_PATCH
								|| ((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.SIMPLE_RANDOM_SELECTOR 
								|| ((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.RANDOM_RANDOM_SELECTOR 
								|| ((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.FLOWER 
								|| ((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.DECORATED_FLOWER))
							{
								//add the grass and flowers later so trees have a chance to spawn
								grassyFlowerList.add(configuredFeature);
							}
							else
							{
								if (configuredFeature.config instanceof DecoratedFeatureConfig &&
									((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.BAMBOO)
								{
									//MAKE BAMBOO GENERATE VERY LAST. SCREW BAMBOO
									bambooList.add(configuredFeature);
								}
								else
								{
									//if we have no laggy feature config on, then the feature must not be fire or lava in order to be added
									if(!WBConfig.disallowLaggyVanillaFeatures || !VanillaFeatureGrouping.lavaAndFirefeatures.get(stage).stream().anyMatch(vanillaConfigFeature -> serializeAndCompareFeature(vanillaConfigFeature, configuredFeature)))
									{
										WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(stage, configuredFeature));
									}
								}
							}
						}
					}
					else if (WBConfig.SERVER.allowModdedFeatures.get())
					{
						if (configuredFeature.config instanceof DecoratedFeatureConfig &&
							(((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.RANDOM_PATCH
							|| ((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.SIMPLE_RANDOM_SELECTOR 
							|| ((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.RANDOM_RANDOM_SELECTOR 
							|| ((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.FLOWER 
							|| ((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.DECORATED_FLOWER))
						{
							//add the grass and flowers later so trees have a chance to spawn
							grassyFlowerList.add(configuredFeature);
						}
						else
						{
							if (configuredFeature.config instanceof DecoratedFeatureConfig &&
								((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.BAMBOO)
							{
								//MAKE BAMBOO GENERATE VERY LAST. SCREW BAMBOO
								bambooList.add(configuredFeature);
							}
							else if (stage == Decoration.VEGETAL_DECORATION && 
									(configuredFeature.config instanceof DecoratedFeatureConfig &&
									((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.RANDOM_BOOLEAN_SELECTOR || 
									((DecoratedFeatureConfig)configuredFeature.config).feature.feature == Feature.RANDOM_SELECTOR))
							{
								//adds modded features that might be trees to front of array so they have priority
								//over vanilla features.
								WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.features.get(stage).add(0, configuredFeature));
							}
							else
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
				if(ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.STRUCTURE, structure.getRegistryName())) {
					continue;
				}
				
				if (WBBiomes.VANILLA_TEMP_BIOME.structures.keySet().stream().anyMatch(vanillaStructure -> vanillaStructure.getClass().equals(structure.getClass())))
				{
					if (WBConfig.SERVER.allowVanillaStructures.get())
					{
						//add the structure version of the structure
						WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addStructure(new ConfiguredFeature(structure, biome.structures.get(structure))));
						
						//find the feature version of the structure in this biome and add it so it can spawn
						for (Decoration stage : GenerationStage.Decoration.values())
						{
							for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeatures(stage))
							{
								if(configuredFeature.config instanceof DecoratedFeatureConfig && 
								((DecoratedFeatureConfig)configuredFeature.config).feature.feature.getClass().equals(structure.getClass())) 
								{
									WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(stage, configuredFeature));
								}
							}
						}
					}
						
				}
				else if (WBConfig.SERVER.allowModdedStructures.get())
				{
					//add the structure version of the structure
					WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addStructure(new ConfiguredFeature(structure, biome.structures.get(structure))));
				
					//find the feature version of the structure in this biome and add it so it can spawn
					for (Decoration stage : GenerationStage.Decoration.values())
					{
						for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeatures(stage))
						{
							if(configuredFeature.feature.getClass().equals(biome.structures.get(structure).getClass())) {
								WBBiomes.biomes.forEach(blendedBiome -> blendedBiome.addFeature(stage, configuredFeature));
							}
						}
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
				if(ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.CARVER, carver.carver.getRegistryName())) {
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
				if(ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.SPAWN, spawnEntry.entityType.getRegistryName())) {
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
		if(ConfigBlacklisting.isBiomeNotAllowed(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, surfaceConfig.getTop().getBlock().getRegistryName()))
		{
			return;
		}
		
		if (!((BlendedSurfaceBuilder) WBBiomes.FEATURE_SURFACE_BUILDER).containsConfig(surfaceConfig))
		{
			((BlendedSurfaceBuilder) WBBiomes.FEATURE_SURFACE_BUILDER).addConfig(surfaceConfig);
		}
	}


	private static boolean serializeAndCompareFeature(ConfiguredFeature<?, ?> feature1, ConfiguredFeature<?, ?> feature2)
	{
		try
		{
			Map<Dynamic<INBT>, Dynamic<INBT>> feature1Map = feature1.serialize(NBTDynamicOps.INSTANCE).getMapValues().get();
			Map<Dynamic<INBT>, Dynamic<INBT>> feature2Map = feature2.serialize(NBTDynamicOps.INSTANCE).getMapValues().get();

			if (feature1Map != null && feature2Map != null)
			{
				return feature1Map.equals(feature2Map);
			}
		}
		catch (Exception e)
		{
			//One of the features cannot be serialized which can only happen with custom modded features
			//Check if the features are the same feature even though the placement or config for the feature might be different. 
			//This is the best way we can remove duplicate modded features as best as we can. (I think)
			if ((feature1.config instanceof DecoratedFeatureConfig && feature2.config instanceof DecoratedFeatureConfig) && 
				((DecoratedFeatureConfig) feature1.config).feature.feature == ((DecoratedFeatureConfig) feature2.config).feature.feature)
			{
				return true;
			}
		}

		return false;
	}
}
