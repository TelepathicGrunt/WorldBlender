package com.telepathicgrunt.world_blender.the_blender;

import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.WorldBlender;
import com.telepathicgrunt.world_blender.configs.WBBlendingConfigs;
import com.telepathicgrunt.world_blender.features.WBConfiguredFeatures;
import com.telepathicgrunt.world_blender.mixin.worldgen.*;
import com.telepathicgrunt.world_blender.surfacebuilder.BlendedSurfaceBuilder;
import com.telepathicgrunt.world_blender.surfacebuilder.WBSurfaceBuilders;
import com.telepathicgrunt.world_blender.the_blender.ConfigBlacklisting.BlacklistType;
import com.telepathicgrunt.world_blender.utils.ConfigHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.GenerationStage.Carving;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TheBlender {
	// MUST KEEP THESE SETS. They massively speed up World Blender at mod startup by preventing excessive running of .anyMatch and other streams/checks
	private static final Set<Supplier<?>> CHECKED_WORLDGEN_OBJECTS = new HashSet<>();
	private static final Set<MobSpawnInfo.Spawners> CHECKED_MOBS = new HashSet<>();
	
	// Prevent modded mobs from drowning out vanilla or other mod's mobs.
	private static final Map<EntityClassification, Integer> MAX_WEIGHT_PER_GROUP = new HashMap<>();
	
	static {
		MAX_WEIGHT_PER_GROUP.put(EntityClassification.CREATURE, 15);
		MAX_WEIGHT_PER_GROUP.put(EntityClassification.MONSTER, 120);
		MAX_WEIGHT_PER_GROUP.put(EntityClassification.WATER_AMBIENT, 30);
		MAX_WEIGHT_PER_GROUP.put(EntityClassification.WATER_CREATURE, 12);
		MAX_WEIGHT_PER_GROUP.put(EntityClassification.AMBIENT, 15);
	}
	
	/**
	 Kickstarts the blender. Should always be ran in MinecraftServer's init which is before the world is loaded
	 */
	public static void blendTheWorld(DynamicRegistries.Impl registryManager) {
		Optional<MutableRegistry<Biome>> biomes = registryManager.func_230521_a_(Registry.BIOME_KEY);
		if (!biomes.isPresent()) return;
		
		List<Biome> worldBlenderBiomes = biomes.get().getEntries().stream()
			.filter(entry -> entry.getKey().getLocation().getNamespace().equals(WorldBlender.MODID))
			.map(Map.Entry::getValue)
			.collect(Collectors.toList());
		
		// Only world blender biomes will be mutable
		worldBlenderBiomes.forEach(TheBlender::makeBiomeMutable);
		
		// Clear out world blender biomes of everything.
		if (WorldBlender.WBBlendingConfig.cleanSlateWBBiomesAtStartup.get()) {
			worldBlenderBiomes.forEach(biome -> {
				biome.getGenerationSettings().getFeatures().forEach(List::clear);
				biome.getGenerationSettings().getStructures().clear();
				((GenerationSettingsAccessor) biome.getGenerationSettings()).wb_getCarvers().forEach((stage, list) -> list.clear());
				((MobSpawnInfoAccessor) biome.getMobSpawnInfo()).wb_getSpawnCosts().clear();
				((MobSpawnInfoAccessor) biome.getMobSpawnInfo()).wb_getSpawners().forEach((group, list) -> list.clear());
			});
		}
		
		// Reset these before biome loop
		ConfigBlacklisting.setupBlackLists();
		FeatureGrouping.setupFeatureMaps();
		BlendedSurfaceBuilder.resetSurfaceList();
		
		// THE biome loop. Very magical!
		for (Map.Entry<RegistryKey<Biome>, Biome> biomeEntry : biomes.get().getEntries()) {
			
			if (!biomeEntry.getKey().getLocation().getNamespace().equals(WorldBlender.MODID)) {
				// begin blending into our biomes
				mainBlending(
					biomeEntry.getValue(), // Biome
					worldBlenderBiomes, // WB biomes
					biomeEntry.getKey().getLocation(), // ResourceLocation
					registryManager // all the registries
				);
			}
		}
		
		// wrap up the last bits that still needs to be blended but after the biome loop
		completeBlending(worldBlenderBiomes, registryManager.getRegistry(Registry.CONFIGURED_FEATURE_KEY));
		
		// free up some memory when we are done and ready it for the next world clicked on.
		FeatureGrouping.clearFeatureMaps();
		CHECKED_WORLDGEN_OBJECTS.clear();
		CHECKED_MOBS.clear();
	}
	
	/**
	 blends the given biome into WB biomes
	 */
	private static void mainBlending(Biome biome, List<Biome> worldBlenderBiomes, ResourceLocation biomeID, DynamicRegistries.Impl dynamicRegistryManager) {
		// Debugging breakpoint spot
//        if(biomeID.getPath().contains("nether")){
//            int t = 5;
//        }
		
		// ignore our own biomes to speed things up and prevent possible duplications
		if (biomeID.getNamespace().equals(WorldBlender.MODID)) return;
		
		// if the biome is a vanilla biome but config says no vanilla biome, skip this biome
		if (shouldSkip(
			biomeID,
			c -> c.allowVanillaBiomeImport,
			c -> c.allowModdedBiomeImport,
			BlacklistType.BLANKET
		)) return;
		
		/////////// FEATURES//////////////////
		addBiomeFeatures(biome, worldBlenderBiomes, dynamicRegistryManager.getRegistry(Registry.CONFIGURED_FEATURE_KEY));
		
		////////////////////// STRUCTURES////////////////////////
		addBiomeStructures(biome, worldBlenderBiomes, dynamicRegistryManager.getRegistry(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY));
		
		//////////////////////// CARVERS/////////////////////////
		addBiomeCarvers(biome, worldBlenderBiomes, dynamicRegistryManager.getRegistry(Registry.CONFIGURED_CARVER_KEY));
		
		//////////////////////// SPAWNER/////////////////////////
		addBiomeNaturalMobs(biome, worldBlenderBiomes);
		
		//////////////////////// SURFACE/////////////////////////
		addBiomeSurfaceConfig(biome, biomeID);
	}
	
	/**
	 Adds the last bit of stuff that needs to be added to WB biomes after everything else is added.
	 Like bamboo and flowers should be dead last so they don't crowd out tree spawning
	 */
	public static void completeBlending(List<Biome> worldBlenderBiomes, MutableRegistry<ConfiguredFeature<?, ?>> configuredFeaturesRegistry) {
		// add end spike directly to all biomes if not directly blacklisted. Turning off vanilla features will not prevent end spikes from spawning due to them marking the world origin nicely
		ResourceLocation endSpikeID = new ResourceLocation("minecraft", "end_spike");
		if (!ConfigBlacklisting.isResourceLocationBlacklisted(BlacklistType.FEATURE, endSpikeID)) {
			worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(GenerationStage.Decoration.SURFACE_STRUCTURES.ordinal()).add(() -> configuredFeaturesRegistry.getOrDefault(endSpikeID)));
		}
		
		
		// add grass, flower, and other small plants now so they are generated second to last
		for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
			for (ConfiguredFeature<?, ?> grassyFlowerFeature : FeatureGrouping.SMALL_PLANT_MAP.get(stage)) {
				List<Supplier<ConfiguredFeature<?, ?>>> stageFeatures = worldBlenderBiomes.get(0)
					.getGenerationSettings()
					.getFeatures()
					.get(stage.ordinal());
				boolean alreadyPresent = stageFeatures.stream().anyMatch(existing ->
					FeatureGrouping.serializeAndCompareFeature(existing.get(), grassyFlowerFeature, true)
				);
				if (alreadyPresent) continue;
				
				worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(() -> grassyFlowerFeature));
			}
		}
		
		
		if (!disallowLaggyFeatures() && FeatureGrouping.bambooFound) {
			// add 1 configured bamboo so it is dead last
			worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(GenerationStage.Decoration.VEGETAL_DECORATION.ordinal()).add(() -> configuredFeaturesRegistry.getOrDefault(new ResourceLocation("minecraft", "bamboo"))));
		}
		
		
		// Makes carvers be able to carve any underground blocks including netherrack, end stone, and modded blocks
		if (WorldBlender.WBDimensionConfig.carversCanCarveMoreBlocks.get()) {
			Set<Block> allBlocksToCarve = BlendedSurfaceBuilder.blocksToCarve();
			
			// get all carvable blocks
			for (GenerationStage.Carving carverStage : GenerationStage.Carving.values()) {
				for (Supplier<ConfiguredCarver<?>> carver : worldBlenderBiomes.get(0).getGenerationSettings().getCarvers(carverStage)) {
					allBlocksToCarve.addAll(((CarverAccessor) ((ConfiguredCarverAccessor) carver.get()).wb_getcarver()).wb_getalwaysCarvableBlocks());
				}
			}
			
			// update all carvers to carve the complete list of stuff to carve
			for (GenerationStage.Carving carverStage : GenerationStage.Carving.values()) {
				for (Supplier<ConfiguredCarver<?>> carver : worldBlenderBiomes.get(0).getGenerationSettings().getCarvers(carverStage)) {
					((CarverAccessor) ((ConfiguredCarverAccessor) carver.get()).wb_getcarver()).wb_setalwaysCarvableBlocks(allBlocksToCarve);
				}
			}
		}
		
		// add these last so that this can contain other local modification feature's liquids/falling blocks better
		if (!ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, new ResourceLocation("world_blender:anti_floating_blocks_and_separate_liquids"))) {
			worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(GenerationStage.Decoration.LOCAL_MODIFICATIONS.ordinal()).add(() -> WBConfiguredFeatures.ANTI_FLOATING_BLOCKS_AND_SEPARATE_LIQUIDS));
		}
	}
	
	/**
	 Helper method to make WB biomes mutable to add stuff to it later
	 */
	private static void makeBiomeMutable(Biome biome) {
		// Make the structure and features list mutable for modification late
		GenerationSettingsAccessor settingsAccessor = (GenerationSettingsAccessor) biome.getGenerationSettings();
		
		// Fill in generation stages so there are at least 10 or else Minecraft crashes.
		// (we need all stages for adding features/structures to the right stage too)
		List<List<Supplier<ConfiguredFeature<?, ?>>>> generationStages = new ArrayList<>(settingsAccessor.wb_getGSFeatures());
		int minSize = GenerationStage.Decoration.values().length;
		for (int i = generationStages.size(); i < minSize; i++) {
			generationStages.add(new ArrayList<>());
		}
		
		// Make the Structure and GenerationStages (features) list mutable for modification later
		settingsAccessor.wb_setGSFeatures(generationStages);
		settingsAccessor.wb_setGSStructureFeatures(new ArrayList<>(settingsAccessor.wb_getGSStructureFeatures()));
		settingsAccessor.wb_setGSStructureFeatures(new ArrayList<>(settingsAccessor.wb_getGSStructureFeatures()));
		
		settingsAccessor.wb_setCarvers(new HashMap<>(settingsAccessor.wb_getCarvers()));
		for (Carving carverGroup : Carving.values()) {
			settingsAccessor.wb_getCarvers().put(carverGroup, new ArrayList<>(biome.getGenerationSettings().getCarvers(carverGroup)));
		}
		
		((MobSpawnInfoAccessor) biome.getMobSpawnInfo()).wb_setSpawners(new HashMap<>(((MobSpawnInfoAccessor) biome.getMobSpawnInfo()).wb_getSpawners()));
		for (EntityClassification spawnGroup : EntityClassification.values()) {
			((MobSpawnInfoAccessor) biome.getMobSpawnInfo()).wb_getSpawners().put(spawnGroup, new ArrayList<>(biome.getMobSpawnInfo().getSpawners(spawnGroup)));
		}
		
		((MobSpawnInfoAccessor) biome.getMobSpawnInfo()).wb_setSpawnCosts(new HashMap<>(((MobSpawnInfoAccessor) biome.getMobSpawnInfo()).wb_getSpawnCosts()));
	}
	
	//--------------------------------------------------------------
	// The actual main blending below
	// Welcome to hell!
	
	private static void addBiomeFeatures(Biome biome, List<Biome> worldBlenderBiomes, MutableRegistry<ConfiguredFeature<?, ?>> configuredFeaturesRegistry) {
		for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
			if (stage.ordinal() >= biome.getGenerationSettings().getFeatures().size()) break;
			
			for (Supplier<ConfiguredFeature<?, ?>> configuredFeatureSupplier : biome.getGenerationSettings().getFeatures().get(stage.ordinal())) {
				if (CHECKED_WORLDGEN_OBJECTS.contains(configuredFeatureSupplier)) continue;
				CHECKED_WORLDGEN_OBJECTS.add(configuredFeatureSupplier);
				
				ConfiguredFeature<?, ?> configuredFeature = configuredFeatureSupplier.get();
				
				// Do deep check to see if this configuredfeature instance is actually the same as another configuredfeature
				List<Supplier<ConfiguredFeature<?, ?>>> features = worldBlenderBiomes.get(0)
					.getGenerationSettings()
					.getFeatures()
					.get(stage.ordinal());
				boolean alreadyPresent = features.stream().anyMatch(existing ->
					FeatureGrouping.serializeAndCompareFeature(
						existing.get(),
						configuredFeature,
						true
					)
				);
				if (alreadyPresent) continue;
				
				ResourceLocation configuredFeatureID = configuredFeaturesRegistry.getKey(configuredFeature);
				if (configuredFeatureID == null) {
					configuredFeatureID = WorldGenRegistries.CONFIGURED_FEATURE.getKey(configuredFeature);
				}
				
				if (shouldSkip(
					configuredFeatureID,
					c -> c.allowVanillaFeatures,
					c -> c.allowModdedFeatures,
					BlacklistType.FEATURE
				)) continue;
				
				//// begin adding features //////
				
				// add the vanilla grass and flowers to a map so we can add them
				// later to the feature list so trees have a chance to spawn
				if (FeatureGrouping.checksAndAddSmallPlantFeatures(stage, configuredFeature)) continue;
				
				boolean isVanilla = configuredFeatureID.getNamespace().equals("minecraft");
				if (!isVanilla) {
					// add modded features that might be trees to front
					// of feature list so they have priority over all vanilla features in same generation stage.
					if (FeatureGrouping.checksAndAddLargePlantFeatures(stage, configuredFeature)) {
						worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(0, configuredFeatureSupplier));
						continue;
					}
					// cannot be a bamboo feature as we will place them dead last in the feature
					// list so they don't overwhelm other features or cause as many bamboo breaking
					// because it got cut off
				}
				
				// if we have no laggy feature config on, then the feature must not be fire, lava, bamboo, etc in order to be added
				if (disallowLaggyFeatures() && FeatureGrouping.isLaggyFeature(configuredFeature)) continue;
				
				worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(configuredFeatureSupplier));
			}
		}
	}
	
	private static void addBiomeStructures(Biome biome, List<Biome> worldBlenderBiomes, MutableRegistry<StructureFeature<?, ?>> configuredStructuresRegistry) {
		for (Supplier<StructureFeature<?, ?>> supplier : biome.getGenerationSettings().getStructures()) {
			if (CHECKED_WORLDGEN_OBJECTS.contains(supplier)) continue;
			CHECKED_WORLDGEN_OBJECTS.add(supplier);
			
			StructureFeature<?, ?> configuredStructure = supplier.get();
			
			// Having multiple configured structures of the same structure spawns only the last one it seems. Booo mojang boooooo. I want multiple village types in 1 biome!
			Collection<Supplier<StructureFeature<?, ?>>> structures = worldBlenderBiomes.get(0)
				.getGenerationSettings()
				.getStructures();
			boolean alreadyPresent = structures.stream().anyMatch(existing ->
				existing.get() == configuredStructure
			);
			if (alreadyPresent) continue;
			
			// Have to do this computing as the feature in the registry is technically not the same
			// object as the feature in the biome. So I cannot get ID easily from the registry.
			// Instead, I have to check the JSON of the feature to find a match and store the ID of it
			// into a temporary map as a cache for later biomes.
			ResourceLocation configuredStructureID = configuredStructuresRegistry.getKey(configuredStructure);
			if (configuredStructureID == null) {
				configuredStructureID = WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE.getKey(configuredStructure);
			}
			
			if (shouldSkip(
				configuredStructureID,
				c -> c.allowVanillaStructures,
				c -> c.allowModdedStructures,
				BlacklistType.STRUCTURE
			)) continue;
			
			worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getStructures().add(supplier));
		}
	}
	
	private static void addBiomeCarvers(Biome biome, List<Biome> worldBlenderBiomes, MutableRegistry<ConfiguredCarver<?>> configuredCarversRegistry) {
		for (Carving carverStage : GenerationStage.Carving.values()) {
			for (Supplier<ConfiguredCarver<?>> supplier : biome.getGenerationSettings().getCarvers(carverStage)) {
				if (CHECKED_WORLDGEN_OBJECTS.contains(supplier)) continue;
				CHECKED_WORLDGEN_OBJECTS.add(supplier);
				
				ConfiguredCarver<?> configuredCarver = supplier.get();
				
				List<Supplier<ConfiguredCarver<?>>> carvers = worldBlenderBiomes.get(0)
					.getGenerationSettings()
					.getCarvers(carverStage);
				boolean alreadyPresent = carvers.stream().anyMatch(existing ->
					existing.get() == configuredCarver
				);
				if (alreadyPresent) continue;
				
				ResourceLocation configuredCarverID = configuredCarversRegistry.getKey(configuredCarver);
				if (configuredCarverID == null) {
					configuredCarverID = WorldGenRegistries.CONFIGURED_CARVER.getKey(configuredCarver);
				}
				
				if (shouldSkip(
					configuredCarverID,
					c -> c.allowVanillaCarvers,
					c -> c.allowModdedCarvers,
					BlacklistType.CARVER
				)) continue;
				
				worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getCarvers(carverStage).add(supplier));
			}
		}
	}
	
	private static void addBiomeNaturalMobs(Biome biome, List<Biome> worldBlenderBiomes) {
		for (EntityClassification spawnGroup : EntityClassification.values()) {
			for (MobSpawnInfo.Spawners spawnEntry : biome.getMobSpawnInfo().getSpawners(spawnGroup)) {
				if (CHECKED_MOBS.contains(spawnEntry)) continue;
				CHECKED_MOBS.add(spawnEntry);
				
				boolean alreadyPresent = worldBlenderBiomes.get(0)
					.getMobSpawnInfo()
					.getSpawners(spawnGroup)
					.stream()
					.anyMatch(existing -> existing.type == spawnEntry.type);
				if (alreadyPresent) continue;
				
				// no check needed for if entitytype is null because it is impossible for it to be null without Minecraft blowing up
				ResourceLocation entityTypeID = Registry.ENTITY_TYPE.getKey(spawnEntry.type);
				
				if (shouldSkip(
					entityTypeID,
					c -> c.allowVanillaSpawns,
					c -> c.allowModdedSpawns,
					BlacklistType.SPAWN
				)) continue;
				
				int maxWeight = MAX_WEIGHT_PER_GROUP.getOrDefault(spawnGroup, spawnEntry.itemWeight);
				MobSpawnInfo.Spawners newEntry = new MobSpawnInfo.Spawners(
					spawnEntry.type,
					Math.max(Math.min(spawnEntry.itemWeight, maxWeight), 1), // Cap the weight and make sure it isn't too low
					spawnEntry.minCount,
					spawnEntry.maxCount
				);
				
				worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getMobSpawnInfo().getSpawners(spawnGroup).add(newEntry));
			}
		}
	}
	
	private static void addBiomeSurfaceConfig(Biome biome, ResourceLocation biomeID) {
		if (shouldSkip(
			biomeID,
			c -> c.allowVanillaSurfaces,
			c -> c.allowModdedSurfaces,
			null
		)) return;
		
		ISurfaceBuilderConfig surfaceConfig = biome.getGenerationSettings().getSurfaceBuilderConfig();
		// Blacklisted by surface list. Checks top block
		BlockState topMaterial = surfaceConfig.getTop();
		
		// Also do null check as BYG actually managed to set the surfaceConfig's block to be null lol
		//noinspection ConstantConditions
		if (topMaterial == null) return;
		
		ResourceLocation topBlockID = Registry.BLOCK.getKey(topMaterial.getBlock());
		if (ConfigBlacklisting.isResourceLocationBlacklisted(BlacklistType.SURFACE_BLOCK, topBlockID)) return;
		
		((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER.get()).addConfigIfMissing(surfaceConfig);
	}
	
	//--------------------------------------------------------------
	// An attempt to make sure we always have the spacing config for all structures
	
	public static void addDimensionalSpacing(final WorldEvent.Load event) {
		if (!(event.getWorld() instanceof ServerWorld)) return;
		ServerWorld serverWorld = (ServerWorld) event.getWorld();
		
		ServerWorld wbServerWorld = serverWorld.getServer().getWorld(WBIdentifiers.WB_WORLD_KEY);
		if (wbServerWorld == null) return;
		
		// These maps map be immutable for some chunk generators. Our own won't be unless
		// someone messes with it. I take no chances so defensive programming incoming!
		Map<Structure<?>, StructureSeparationSettings> structureConfig = new HashMap<>();
		
		structureConfig.putAll(serverWorld.getChunkProvider().generator.func_235957_b_().func_236195_a_());
		// Grabs old entries already added into wb dimension
		structureConfig.putAll(wbServerWorld.getChunkProvider().generator.func_235957_b_().func_236195_a_());
		
		// Dunno why someone would set null but we should check anyway
		structureConfig.values().removeIf(Objects::isNull);
		
		// Set the structure spacing config in wb dimension and clear map so next saved world is fresh.
		((DimensionStructureSettingsAccessor) wbServerWorld.getChunkProvider().generator.func_235957_b_()).wb_setStructureConfigMap(structureConfig);
	}
	
	private static boolean shouldSkip(
		@Nullable ResourceLocation id,
		Function<WBBlendingConfigs.WBConfigValues, ConfigHelper.ConfigValueListener<Boolean>> allowVanilla,
		Function<WBBlendingConfigs.WBConfigValues, ConfigHelper.ConfigValueListener<Boolean>> allowModded,
		@Nullable ConfigBlacklisting.BlacklistType blacklist
	) {
		if (id == null) return true;
		
		boolean isVanilla = id.getNamespace().equals("minecraft");
		if (isVanilla && !allowVanilla.apply(WorldBlender.WBBlendingConfig).get()) return true;
		if (!isVanilla && !allowModded.apply(WorldBlender.WBBlendingConfig).get()) return true;
		
		return blacklist != null
			&& ConfigBlacklisting.isResourceLocationBlacklisted(blacklist, id);
	}
	
	private static boolean disallowLaggyFeatures() {
		return WorldBlender.WBBlendingConfig.disallowLaggyFeatures.get();
	}
}
