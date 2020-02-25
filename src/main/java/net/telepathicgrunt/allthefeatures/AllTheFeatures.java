package net.telepathicgrunt.allthefeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.datafixers.Dynamic;

import net.minecraft.entity.EntityClassification;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.telepathicgrunt.allthefeatures.biome.BiomeInit;
import net.telepathicgrunt.allthefeatures.biome.biomes.surfacebuilder.FeatureSurfaceBuilder;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(AllTheFeatures.MODID)
public class AllTheFeatures
{
	public static final String MODID = "all_the_features";
	public static final Logger LOGGER = LogManager.getLogger(MODID);


	public AllTheFeatures()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		MinecraftForge.EVENT_BUS.register(this);

		modEventBus.addListener(this::setup);
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setup(final FMLLoadCompleteEvent event){

		List<ConfiguredFeature<?, ?>> miscList = new ArrayList<ConfiguredFeature<?, ?>>();
		List<ConfiguredFeature<?, ?>> grassyFlowerList = new ArrayList<ConfiguredFeature<?, ?>>();
		List<ConfiguredFeature<?, ?>> bambooList = new ArrayList<ConfiguredFeature<?, ?>>();
		
		for (Biome biome : ForgeRegistries.BIOMES.getValues()){
			if (biome == BiomeInit.FEATURE_BIOME || biome == BiomeInit.MOUNTAIN_FEATURE_BIOME || biome == BiomeInit.OCEAN_FEATURE_BIOME)
				continue;

			for (Decoration stage : GenerationStage.Decoration.values()){
				for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeatures(stage)){
					if (!BiomeInit.FEATURE_BIOME.getFeatures(stage).stream().anyMatch(addedConfigFeature -> serializeAndCompareFeature(addedConfigFeature, configuredFeature))) {
						if(configuredFeature.feature == Feature.field_227248_z_ || configuredFeature.feature == Feature.RANDOM_RANDOM_SELECTOR || configuredFeature.feature == Feature.FLOWER || configuredFeature.feature == Feature.DECORATED_FLOWER) {
							//add the grass and flowers later so trees have a chance to spawn
							grassyFlowerList.add(configuredFeature);
						}
						else if(configuredFeature.feature != Feature.RANDOM_BOOLEAN_SELECTOR || configuredFeature.feature != Feature.RANDOM_SELECTOR) {
							//testing something out
							miscList.add(configuredFeature);
						}
						else {
							if(!biome.getRegistryName().getNamespace().equals("minecraft")) {
								//adds modded features that isnt grass/flowers to front of array so they have priority
								//over vanilla features.
								BiomeInit.biomes.forEach(featureBiome -> featureBiome.features.get(stage).add(0, configuredFeature));
							}
							else{
								if(configuredFeature.feature == Feature.BAMBOO) {
									//MAKE BAMBOO GENERATE VERY LAST. SCREW BAMBOO
									bambooList.add(configuredFeature); 
								}
								else {
									BiomeInit.biomes.forEach(featureBiome -> featureBiome.addFeature(stage, configuredFeature));
								}
							}
						}
					}
				}

				for (Structure<?> structure : biome.structures.keySet()){
					if (!BiomeInit.FEATURE_BIOME.structures.keySet().stream().anyMatch(struct -> struct == structure)){
						BiomeInit.biomes.forEach(featureBiome -> featureBiome.addStructureFeature(new ConfiguredFeature(structure, biome.structures.get(structure))));
					}
				}
			}
			for (Carving carverStage : GenerationStage.Carving.values()){
				for (ConfiguredCarver<?> carver : biome.getCarvers(carverStage)){
					if (!BiomeInit.FEATURE_BIOME.getCarvers(carverStage).stream().anyMatch(config -> config.carver == carver.carver)) {
						BiomeInit.biomes.forEach(featureBiome -> featureBiome.addCarver(carverStage, carver));
					}
				}
			}
			for (EntityClassification entityClass : EntityClassification.values()){
				for (SpawnListEntry spawnEntry : biome.getSpawns(entityClass)){
					if (!BiomeInit.FEATURE_BIOME.getSpawns(entityClass).stream().anyMatch(spawn -> spawn.entityType == spawnEntry.entityType)) {
						BiomeInit.biomes.forEach(featureBiome -> featureBiome.addSpawn(entityClass, spawnEntry));
					}
				}
			}
			
			SurfaceBuilderConfig surfaceConfig = (SurfaceBuilderConfig) biome.getSurfaceBuilderConfig();
			if(!((FeatureSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).containsConfig(surfaceConfig)) {
				((FeatureSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).addConfig(surfaceConfig);
			}
		}

		
		//add misc stuff now
		for (ConfiguredFeature<?, ?> miscFeature : miscList){
			BiomeInit.biomes.forEach(featureBiome -> featureBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, miscFeature));
		}
		
		//add grass and flowers now so they are generated second to last
		for (ConfiguredFeature<?, ?> grassyFlowerFeature : grassyFlowerList){
			BiomeInit.biomes.forEach(featureBiome -> featureBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, grassyFlowerFeature));
		}

		//add bamboo so it is dead last
		for (ConfiguredFeature<?, ?> bambooFeature : bambooList){
			BiomeInit.biomes.forEach(featureBiome -> featureBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, bambooFeature));
		}
		
		return;
	}
	
	private static boolean serializeAndCompareFeature(ConfiguredFeature<?, ?> feature1, ConfiguredFeature<?, ?> feature2) {
		
		try {
			Map<Dynamic<INBT>, Dynamic<INBT>> feature1Map = feature1.serialize(NBTDynamicOps.INSTANCE).getMapValues().get();
			Map<Dynamic<INBT>, Dynamic<INBT>> feature2Map = feature2.serialize(NBTDynamicOps.INSTANCE).getMapValues().get();
			
			if(feature1Map != null && feature2Map != null) {
				return feature1Map.equals(feature2Map);
			}
		}
		catch(Exception e) {
			return true;
		}
		
		
		return false;
	}

	// You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents
	{

		@SubscribeEvent
		public static void registerBiomes(final RegistryEvent.Register<Biome> event)
		{
			//registers all my modified biomes
			BiomeInit.registerBiomes(event);
		}
	}


	/*
	 * Helper method to quickly register features, blocks, items, structures, biomes, anything that can be registered.
	 */
	public static <T extends IForgeRegistryEntry<T>> T register(IForgeRegistry<T> registry, T entry, String registryKey)
	{
		entry.setRegistryName(new ResourceLocation(MODID, registryKey.toLowerCase().replace(' ', '_')));
		registry.register(entry);
		return entry;
	}
}
