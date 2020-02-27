package net.telepathicgrunt.worldblender;

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
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.registries.ForgeRegistries;
import net.telepathicgrunt.worldblender.biome.BiomeInit;
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
			if (BiomeInit.biomes.contains(biome)) 
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
			
			

			///////////FEATURES//////////////////
			addBiomeFeatures(biome, bambooList, grassyFlowerList);

			//////////////////////STRUCTURES////////////////////////
			addBiomeStructures(biome);

			////////////////////////CARVERS/////////////////////////
			addBiomeCarvers(biome);

			////////////////////////SPAWNER/////////////////////////
			addBiomeNaturalMobs(biome);

			////////////////////////SURFACE/////////////////////////
			if (biome.getRegistryName() != null && biome.getRegistryName().getNamespace().equals("minecraft"))
			{
				if (WBConfig.SERVER.allowVanillaSurfaces.get())
					addBiomeSurfaceConfig(biome);
			}
			else if (WBConfig.SERVER.allowModdedSurfaces.get())
			{
				addBiomeSurfaceConfig(biome);
			}
		}
		
		
		
		//////////Misc Features///////////////
		
		//add grass and flowers now so they are generated second to last
		for (ConfiguredFeature<?, ?> grassyFlowerFeature : grassyFlowerList)
		{
			BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, grassyFlowerFeature));
		}

		//add bamboo so it is dead last
		for (ConfiguredFeature<?, ?> bambooFeature : bambooList)
		{
			BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, bambooFeature));
		}
	}

	public static void setupPerlinSeed(long seed)
	{
		if (BlendedSurfaceBuilder.perlinSeed == seed)
			return; //We already set the stuff for this world (fires every time a dimension is loaded)

		((BlendedSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).setPerlinSeed(seed);
	}


	private static void addBiomeFeatures(Biome biome, List<ConfiguredFeature<?, ?>> bambooList, List<ConfiguredFeature<?, ?>> grassyFlowerList)
	{
		for (Decoration stage : GenerationStage.Decoration.values())
		{
			for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeatures(stage))
			{
				if (!BiomeInit.BLENDED_BIOME.getFeatures(stage).stream().anyMatch(addedConfigFeature -> serializeAndCompareFeature(addedConfigFeature, configuredFeature)))
				{
					if(BiomeInit.TEMP_BIOME.getFeatures(stage).stream().anyMatch(vanillaConfigFeature -> serializeAndCompareFeature(vanillaConfigFeature, configuredFeature))) {
						if (WBConfig.SERVER.allowVanillaFeatures.get())
						{
							if (configuredFeature.feature == Feature.field_227248_z_ || configuredFeature.feature == Feature.SIMPLE_RANDOM_SELECTOR || configuredFeature.feature == Feature.RANDOM_RANDOM_SELECTOR || configuredFeature.feature == Feature.FLOWER || configuredFeature.feature == Feature.DECORATED_FLOWER)
							{
								//add the grass and flowers later so trees have a chance to spawn
								grassyFlowerList.add(configuredFeature);
							}
							else
							{
								if (configuredFeature.feature == Feature.BAMBOO)
								{
									//MAKE BAMBOO GENERATE VERY LAST. SCREW BAMBOO
									bambooList.add(configuredFeature);
								}
								else
								{
									BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.addFeature(stage, configuredFeature));
								}
							}
						}
					}
					else if (WBConfig.SERVER.allowModdedFeatures.get())
					{
						if (configuredFeature.feature == Feature.field_227248_z_ || configuredFeature.feature == Feature.SIMPLE_RANDOM_SELECTOR || configuredFeature.feature == Feature.RANDOM_RANDOM_SELECTOR || configuredFeature.feature == Feature.FLOWER || configuredFeature.feature == Feature.DECORATED_FLOWER)
						{
							//add the grass and flowers later so trees have a chance to spawn
							grassyFlowerList.add(configuredFeature);
						}
						else
						{
							if (configuredFeature.feature == Feature.BAMBOO)
							{
								//MAKE BAMBOO GENERATE VERY LAST. SCREW BAMBOO
								bambooList.add(configuredFeature);
							}
							else if (stage == Decoration.VEGETAL_DECORATION && (configuredFeature.feature == Feature.RANDOM_BOOLEAN_SELECTOR || configuredFeature.feature == Feature.RANDOM_SELECTOR))
							{
								//adds modded features that might be trees to front of array so they have priority
								//over vanilla features.
								BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.features.get(stage).add(0, configuredFeature));
							}
							else
							{
								BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.addFeature(stage, configuredFeature));
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
			if (!BiomeInit.BLENDED_BIOME.structures.keySet().stream().anyMatch(struct -> struct == structure))
			{
				if (structure.getRegistryName() != null && structure.getRegistryName().getNamespace().equals("minecraft"))
				{
					if (WBConfig.SERVER.allowVanillaStructures.get())
						BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.addStructureFeature(new ConfiguredFeature(structure, biome.structures.get(structure))));
				}
				else if (WBConfig.SERVER.allowModdedStructures.get())
				{
					BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.addStructureFeature(new ConfiguredFeature(structure, biome.structures.get(structure))));
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
				if (!BiomeInit.BLENDED_BIOME.getCarvers(carverStage).stream().anyMatch(config -> config.carver == carver.carver))
				{
					if (carver.carver.getRegistryName() != null && carver.carver.getRegistryName().getNamespace().equals("minecraft"))
					{
						if (WBConfig.SERVER.allowVanillaCarvers.get())
							BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.addCarver(carverStage, carver));
					}
					else if (WBConfig.SERVER.allowModdedCarvers.get())
					{
						BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.addCarver(carverStage, carver));
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
				if (!BiomeInit.BLENDED_BIOME.getSpawns(entityClass).stream().anyMatch(spawn -> spawn.entityType == spawnEntry.entityType))
				{
					if (spawnEntry.entityType.getRegistryName() != null && spawnEntry.entityType.getRegistryName().getNamespace().equals("minecraft"))
					{
						if (WBConfig.SERVER.allowVanillaSpawns.get())
							BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.addSpawn(entityClass, spawnEntry));
					}
					else if (WBConfig.SERVER.allowModdedSpawns.get())
					{
						BiomeInit.biomes.forEach(blendedBiome -> blendedBiome.addSpawn(entityClass, spawnEntry));
					}
				}
			}
		}
	}


	private static void addBiomeSurfaceConfig(Biome biome)
	{
		SurfaceBuilderConfig surfaceConfig = (SurfaceBuilderConfig) biome.getSurfaceBuilderConfig();
		if (!((BlendedSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).containsConfig(surfaceConfig))
		{
			((BlendedSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).addConfig(surfaceConfig);
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
			return true;
		}

		return false;
	}
}
