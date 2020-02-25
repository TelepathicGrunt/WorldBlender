package net.telepathicgrunt.worldblender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
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
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.telepathicgrunt.worldblender.biome.BiomeInit;
import net.telepathicgrunt.worldblender.biome.biomes.surfacebuilder.BlendedSurfaceBuilder;


@Mod.EventBusSubscriber(modid = WorldBlender.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BiomeBlending
{
	@Mod.EventBusSubscriber(modid = WorldBlender.MODID)
	private static class ForgeEvents
	{

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void setupBiomes(FMLLoadCompleteEvent event)
		{
			//config for minecraft vanilla
			
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

			List<ConfiguredFeature<?, ?>> grassyFlowerList = new ArrayList<ConfiguredFeature<?, ?>>();
			List<ConfiguredFeature<?, ?>> bambooList = new ArrayList<ConfiguredFeature<?, ?>>();

			for (Biome biome : ForgeRegistries.BIOMES.getValues())
			{
				if (biome == BiomeInit.BLENDED_BIOME || biome == BiomeInit.MOUNTAINOUS_BLENDED_BIOME || biome == BiomeInit.OCEAN_BLENDED_BIOME)
					continue;

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
								if (!biome.getRegistryName().getNamespace().equals("minecraft") && stage == Decoration.VEGETAL_DECORATION && (configuredFeature.feature == Feature.RANDOM_BOOLEAN_SELECTOR || configuredFeature.feature == Feature.RANDOM_SELECTOR))
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

					for (Structure<?> structure : biome.structures.keySet())
					{
						if (!BiomeInit.BLENDED_BIOME.structures.keySet().stream().anyMatch(struct -> struct == structure))
						{
							BiomeInit.biomes.forEach(featureBiome -> featureBiome.addStructureFeature(new ConfiguredFeature(structure, biome.structures.get(structure))));
						}
					}
				}
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

				SurfaceBuilderConfig surfaceConfig = (SurfaceBuilderConfig) biome.getSurfaceBuilderConfig();
				if (!((BlendedSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).containsConfig(surfaceConfig))
				{
					((BlendedSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).addConfig(surfaceConfig);
				}
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
		}
		

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void Load(WorldEvent.Load event)
		{
			if(BlendedSurfaceBuilder.perlinSeed == event.getWorld().getSeed()) return; //We already set the stuff for this world (fires every time a dimension is loaded)
			
			((BlendedSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).setPerlinSeed(event.getWorld().getSeed());
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
