package net.telepathicgrunt.allthefeatures;

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
		
		for (Biome biome : ForgeRegistries.BIOMES.getValues()){
			if (biome == BiomeInit.FEATURE_BIOME || biome == BiomeInit.MOUNTAIN_FEATURE_BIOME || biome == BiomeInit.OCEAN_FEATURE_BIOME)
				continue;

			for (Decoration stage : GenerationStage.Decoration.values()){
				for (ConfiguredFeature<?, ?> feature : biome.getFeatures(stage)){
					if (!BiomeInit.FEATURE_BIOME.getFeatures(stage).stream().anyMatch(feat -> serializeAndCompareFeature(feat, feature))) {
						BiomeInit.FEATURE_BIOME.addFeature(stage, feature);
						BiomeInit.MOUNTAIN_FEATURE_BIOME.addFeature(stage, feature);
						BiomeInit.OCEAN_FEATURE_BIOME.addFeature(stage, feature);
					}
				}

				for (Structure<?> structure : biome.structures.keySet()){
					if (!BiomeInit.FEATURE_BIOME.structures.keySet().stream().anyMatch(struct -> struct == structure)){
						BiomeInit.FEATURE_BIOME.addStructureFeature(new ConfiguredFeature(structure, biome.structures.get(structure)));
						BiomeInit.MOUNTAIN_FEATURE_BIOME.addStructureFeature(new ConfiguredFeature(structure, biome.structures.get(structure)));
						BiomeInit.OCEAN_FEATURE_BIOME.addStructureFeature(new ConfiguredFeature(structure, biome.structures.get(structure)));
					}
				}
			}
			for (Carving carverStage : GenerationStage.Carving.values()){
				for (ConfiguredCarver<?> carver : biome.getCarvers(carverStage)){
					if (!BiomeInit.FEATURE_BIOME.getCarvers(carverStage).stream().anyMatch(config -> config.carver == carver.carver)) {
						BiomeInit.FEATURE_BIOME.addCarver(carverStage, carver);
						BiomeInit.MOUNTAIN_FEATURE_BIOME.addCarver(carverStage, carver);
						BiomeInit.OCEAN_FEATURE_BIOME.addCarver(carverStage, carver);
					}
				}
			}
			for (EntityClassification entityClass : EntityClassification.values()){
				for (SpawnListEntry spawnEntry : biome.getSpawns(entityClass)){
					if (!BiomeInit.FEATURE_BIOME.getSpawns(entityClass).stream().anyMatch(spawn -> spawn.entityType == spawnEntry.entityType)) {
						BiomeInit.FEATURE_BIOME.addSpawn(entityClass, spawnEntry);
						BiomeInit.MOUNTAIN_FEATURE_BIOME.addSpawn(entityClass, spawnEntry);
						BiomeInit.OCEAN_FEATURE_BIOME.addSpawn(entityClass, spawnEntry);
					}
				}
			}
			
			SurfaceBuilderConfig config = (SurfaceBuilderConfig) biome.getSurfaceBuilderConfig();
			if(!((FeatureSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).containsConfig(config)) {
				((FeatureSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).addConfig(config);
			}
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
