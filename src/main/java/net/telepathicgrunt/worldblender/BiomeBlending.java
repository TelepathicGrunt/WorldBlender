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


public class BiomeBlending
{
	public static void setupBiomes()
	{
		//			//reset everything
		//			FeatureSurfaceBuilder.resetSurfaceList();
		//			for (Biome biome : BiomeInit.biomes)
		//			{
		//				biome.features.clear();
		//				biome.structures.clear();
		//				biome.structures.clear();
		//				biome.carvers.clear();
		//				biome.spawns.clear();
		//
		//				for (GenerationStage.Decoration generationstage$decoration : GenerationStage.Decoration.values())
		//				{
		//					biome.features.put(generationstage$decoration, Lists.newArrayList());
		//				}
		//
		//				for (EntityClassification entityclassification : EntityClassification.values())
		//				{
		//					biome.spawns.put(entityclassification, Lists.newArrayList());
		//				}
		//			}

		BlendedSurfaceBuilder.resetSurfaceList();

		List<ConfiguredFeature<?, ?>> grassyFlowerList = new ArrayList<ConfiguredFeature<?, ?>>();
		List<ConfiguredFeature<?, ?>> bambooList = new ArrayList<ConfiguredFeature<?, ?>>();

		for (Biome biome : ForgeRegistries.BIOMES.getValues())
		{
			if (BiomeInit.biomes.contains(biome)) //ignore our own biomes to speed things up
				continue;

			///////////FEATURES//////////////////
			if (biome.getRegistryName().getNamespace().equals("minecraft"))
			{
				if (WBConfig.SERVER.allowVanillaFeatures.get())
					addVanillaBiomeFeatures(biome, bambooList, grassyFlowerList);
			}
			else if (WBConfig.SERVER.allowModdedFeatures.get())
			{
				addModdedBiomeFeatures(biome, bambooList, grassyFlowerList);
			}

			//add grass and flowers now so they are generated second to last
			for (ConfiguredFeature<?, ?> grassyFlowerFeature : grassyFlowerList)
			{
				BiomeInit.biomes.forEach(featureBiome -> featureBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, grassyFlowerFeature));
			}

			//add bamboo so it is dead last
			for (ConfiguredFeature<?, ?> bambooFeature : bambooList)
			{
				BiomeInit.biomes.forEach(featureBiome -> featureBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, bambooFeature));
			}

			//////////////////////STRUCTURES////////////////////////
			if (biome.getRegistryName().getNamespace().equals("minecraft"))
			{
				if (WBConfig.SERVER.allowVanillaStructures.get())
					addBiomeStructures(biome);
			}
			else if (WBConfig.SERVER.allowModdedStructures.get())
			{
				addBiomeStructures(biome);
			}

			////////////////////////CARVERS/////////////////////////
			if (biome.getRegistryName().getNamespace().equals("minecraft"))
			{
				if (WBConfig.SERVER.allowVanillaCarvers.get())
					addBiomeCarvers(biome);
			}
			else if (WBConfig.SERVER.allowModdedCarvers.get())
			{
				addBiomeCarvers(biome);
			}

			////////////////////////SPAWNER/////////////////////////
			if (biome.getRegistryName().getNamespace().equals("minecraft"))
			{
				if (WBConfig.SERVER.allowVanillaSpawns.get())
					addBiomeNaturalMobs(biome);
			}
			else if (WBConfig.SERVER.allowModdedSpawns.get())
			{
				addBiomeNaturalMobs(biome);
			}

			////////////////////////SURFACE/////////////////////////
			if (biome.getRegistryName().getNamespace().equals("minecraft"))
			{
				if (WBConfig.SERVER.allowVanillaSurfaces.get())
					addBiomeSurfaceConfig(biome);
			}
			else if (WBConfig.SERVER.allowModdedSurfaces.get())
			{
				addBiomeSurfaceConfig(biome);
			}
		}
	}


	public static void setupPerlinSeed(long seed)
	{
		if (BlendedSurfaceBuilder.perlinSeed == seed)
			return; //We already set the stuff for this world (fires every time a dimension is loaded)

		((BlendedSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).setPerlinSeed(seed);
	}


	private static void addVanillaBiomeFeatures(Biome biome, List<ConfiguredFeature<?, ?>> bambooList, List<ConfiguredFeature<?, ?>> grassyFlowerList)
	{
		for (Decoration stage : GenerationStage.Decoration.values())
		{
			for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeatures(stage))
			{
				if (!BiomeInit.BLENDED_BIOME.getFeatures(stage).stream().anyMatch(addedConfigFeature -> serializeAndCompareFeature(addedConfigFeature, configuredFeature)))
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
							BiomeInit.biomes.forEach(featureBiome -> featureBiome.addFeature(stage, configuredFeature));
						}
					}
				}
			}
		}
	}


	private static void addModdedBiomeFeatures(Biome biome, List<ConfiguredFeature<?, ?>> bambooList, List<ConfiguredFeature<?, ?>> grassyFlowerList)
	{
		for (Decoration stage : GenerationStage.Decoration.values())
		{
			for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeatures(stage))
			{
				if (!BiomeInit.BLENDED_BIOME.getFeatures(stage).stream().anyMatch(addedConfigFeature -> serializeAndCompareFeature(addedConfigFeature, configuredFeature)))
				{

					if (configuredFeature.feature == Feature.field_227248_z_ || configuredFeature.feature == Feature.SIMPLE_RANDOM_SELECTOR || configuredFeature.feature == Feature.RANDOM_RANDOM_SELECTOR || configuredFeature.feature == Feature.FLOWER || configuredFeature.feature == Feature.DECORATED_FLOWER)
					{
						//add the grass and flowers later so trees have a chance to spawn
						grassyFlowerList.add(configuredFeature);
					}
					else
					{
						if (stage == Decoration.VEGETAL_DECORATION && (configuredFeature.feature == Feature.RANDOM_BOOLEAN_SELECTOR || configuredFeature.feature == Feature.RANDOM_SELECTOR))
						{
							//adds modded features that might be trees to front of array so they have priority
							//over vanilla features.
							BiomeInit.biomes.forEach(featureBiome -> featureBiome.features.get(stage).add(0, configuredFeature));
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
								BiomeInit.biomes.forEach(featureBiome -> featureBiome.addFeature(stage, configuredFeature));
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
				BiomeInit.biomes.forEach(featureBiome -> featureBiome.addStructureFeature(new ConfiguredFeature(structure, biome.structures.get(structure))));
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
					BiomeInit.biomes.forEach(featureBiome -> featureBiome.addCarver(carverStage, carver));
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
					BiomeInit.biomes.forEach(featureBiome -> featureBiome.addSpawn(entityClass, spawnEntry));
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
