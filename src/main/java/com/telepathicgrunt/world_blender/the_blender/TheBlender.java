package com.telepathicgrunt.world_blender.the_blender;

import com.google.common.collect.ImmutableMap;
import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.WorldBlender;
import com.telepathicgrunt.world_blender.configs.WBBlendingConfigs;
import com.telepathicgrunt.world_blender.features.WBConfiguredFeatures;
import com.telepathicgrunt.world_blender.mixin.worldgen.*;
import com.telepathicgrunt.world_blender.surfacebuilder.BlendedSurfaceBuilder;
import com.telepathicgrunt.world_blender.surfacebuilder.SurfaceBlender;
import com.telepathicgrunt.world_blender.the_blender.ConfigBlacklisting.BlacklistType;
import com.telepathicgrunt.world_blender.utils.ConfigHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.GenerationStage.Carving;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.world.WorldEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TheBlender {
	// Prevent modded mobs from drowning out vanilla or other mod's mobs.
	private static final Map<EntityClassification, Integer> MAX_WEIGHT_PER_GROUP = ImmutableMap.of(
		EntityClassification.CREATURE, 15,
		EntityClassification.MONSTER, 120,
		EntityClassification.WATER_AMBIENT, 30,
		EntityClassification.WATER_CREATURE, 12,
		EntityClassification.AMBIENT, 15
	);
	
	/**
	 Kickstarts the blender. Should always be run in MinecraftServer's init which is before the world is loaded
	 */
	public static void blendTheWorld(DynamicRegistries.Impl registryManager) {
		Optional<MutableRegistry<Biome>> _biomes = registryManager.func_230521_a_(Registry.BIOME_KEY);
		if (!_biomes.isPresent()) return;
		Registry<Biome> biomes = _biomes.get();
		
		TheBlender blender = new TheBlender(registryManager);
		blender.blendTheWorld(biomes);
		
		biomes.getEntries().stream()
			.filter(entry -> entry.getKey().getLocation().getNamespace().equals(WorldBlender.MODID))
			.map(Map.Entry::getValue)
			.forEach(blender::apply);
	}
	
	// store all the data we're blending
	private final List<List<Supplier<ConfiguredFeature<?, ?>>>> blendedFeaturesByStage = new ArrayList<>();
	private final Collection<Supplier<StructureFeature<?, ?>>> blendedStructures = new ArrayList<>();
	private final Map<Carving, List<Supplier<ConfiguredCarver<?>>>> blendedCarversByStage = new HashMap<>();
	private final MobSpawnInfoBuilder blendedSpawnInfo = new MobSpawnInfoBuilder(MobSpawnInfo.EMPTY);
	private final SurfaceBlender blendedSurface;
	
	// some registries we access
	private final Registry<ConfiguredFeature<?, ?>> configuredFeatureRegistry;
	private final Registry<StructureFeature<?, ?>> configuredStructureRegistry;
	private final Registry<ConfiguredCarver<?>> configuredCarverRegistry;
	private final Registry<Block> blockRegistry;
	private final Registry<EntityType<?>> entityTypeRegistry;
	
	// MUST KEEP THESE SETS. They massively speed up World Blender at mod startup by preventing excessive running of .anyMatch and other streams/checks
	private final Set<Supplier<?>> checkedWorldgenObjects = new HashSet<>();
	private final Set<MobSpawnInfo.Spawners> checkedMobs = new HashSet<>();
	
	// recognizes and tracks features we need to handle specially
	private final FeatureGrouping featureGrouping = new FeatureGrouping();
	
	private TheBlender(DynamicRegistries.Impl registryManager) {
		// set up collections of nested lists
		Arrays.stream(GenerationStage.Decoration.values())
			.forEach(stage -> blendedFeaturesByStage.add(new ArrayList<>()));
		Arrays.stream(Carving.values())
			.forEach(stage -> blendedCarversByStage.put(stage, new ArrayList<>()));
		
		ConfigBlacklisting.setupBlackLists();
		blendedSurface = new SurfaceBlender(); // this initializer depends on the blacklists being set up
		
		configuredFeatureRegistry = registryManager.getRegistry(Registry.CONFIGURED_FEATURE_KEY);
		configuredStructureRegistry = registryManager.getRegistry(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY);
		configuredCarverRegistry = registryManager.getRegistry(Registry.CONFIGURED_CARVER_KEY);
		blockRegistry = Registry.BLOCK;
		entityTypeRegistry = Registry.ENTITY_TYPE;
	}
	
	private List<Supplier<ConfiguredFeature<?, ?>>> blendedFeatures(GenerationStage.Decoration stage) {
		return blendedFeaturesByStage.get(stage.ordinal());
	}
	
	private void blendTheWorld(Registry<Biome> biomes) {
		for (Map.Entry<RegistryKey<Biome>, Biome> biomeEntry : biomes.getEntries()) {
			if (!biomeEntry.getKey().getLocation().getNamespace().equals(WorldBlender.MODID)) {
				// begin blending into our biomes
				blend(
					biomeEntry.getValue(), // Biome
					biomeEntry.getKey().getLocation() // ResourceLocation
				);
			}
		}
		
		// wrap up the last bits that still needs to be blended but after the biome loop
		completeBlending();
		
		blendedSurface.save();
	}
	
	private void apply(Biome blendedBiome) {
		makeBiomeMutable(blendedBiome);
		
		// TODO: it's possible that there will be issues with just passing these nested lists and stuff by reference.
		// it would be easy enough to just deep clone the lists.
		// we should test if this is necessary, ideally in a large modpack.
		
		blendedBiome.getGenerationSettings().getFeatures().clear();
		blendedBiome.getGenerationSettings().getFeatures().addAll(blendedFeaturesByStage);
		
		blendedBiome.getGenerationSettings().getStructures().clear();
		blendedBiome.getGenerationSettings().getStructures().addAll(blendedStructures);
		
		Map<Carving, List<Supplier<ConfiguredCarver<?>>>> carvers = ((GenerationSettingsAccessor) blendedBiome.getGenerationSettings()).wb_getCarvers();
		carvers.clear();
		carvers.putAll(blendedCarversByStage);
		
		MobSpawnInfo spawnInfo = blendedBiome.getMobSpawnInfo();
		MobSpawnInfoAccessor spawnInfoAccessor = (MobSpawnInfoAccessor) spawnInfo;
		MobSpawnInfoAccessor blendedAccessor = (MobSpawnInfoAccessor) blendedSpawnInfo.copy();
		spawnInfoAccessor.wb_setSpawnCosts(blendedAccessor.wb_getSpawnCosts());
		spawnInfoAccessor.wb_setSpawners(blendedAccessor.wb_getSpawners());
	}
	
	// TODO: do we still need this?
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
	
	/**
	 blends the given biome into WB biomes
	 */
	private void blend(Biome biome, ResourceLocation biomeID) {
		// ignore our own biomes to speed things up and prevent possible duplications
		if (biomeID.getNamespace().equals(WorldBlender.MODID)) return;
		
		if (shouldSkip(
			biomeID,
			c -> c.allowVanillaBiomeImport,
			c -> c.allowModdedBiomeImport,
			BlacklistType.BLANKET
		)) return;
		
		BiomeGenerationSettings settings = biome.getGenerationSettings();
		GenerationSettingsAccessor settingsAccessor = (GenerationSettingsAccessor) settings;
		
		addBiomeFeatures(settings.getFeatures());
		addBiomeStructures(settings.getStructures());
		addBiomeCarvers(settingsAccessor.wb_getCarvers());
		addBiomeNaturalMobs(biome.getMobSpawnInfo());
		addBiomeSurfaceConfig(settings.getSurfaceBuilderConfig(), biomeID);
	}
	
	/**
	 Adds the last bit of stuff that needs to be added to WB biomes after everything else is added.
	 Like bamboo and flowers should be dead last so they don't crowd out tree spawning
	 */
	private void completeBlending() {
		// add end spike directly to all biomes if not directly blacklisted. Turning off vanilla features will not prevent end spikes from spawning due to them marking the world origin nicely
		ResourceLocation endSpikeID = new ResourceLocation("minecraft", "end_spike");
		if (!ConfigBlacklisting.isResourceLocationBlacklisted(BlacklistType.FEATURE, endSpikeID)) {
			blendedFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES)
				.add(() -> configuredFeatureRegistry.getOrDefault(endSpikeID));
		}
		
		
		// add grass, flower, and other small plants now so they are generated second to last
		for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
			featureGrouping.smallPlants.get(stage).forEach(grassyFlowerFeature -> {
				List<Supplier<ConfiguredFeature<?, ?>>> stageFeatures = blendedFeatures(stage);
				
				boolean alreadyPresent = stageFeatures.stream().anyMatch(existing ->
					FeatureGrouping.serializeAndCompareFeature(
						existing.get(),
						grassyFlowerFeature,
						true
					)
				);
				if (alreadyPresent) return;
				
				stageFeatures.add(() -> grassyFlowerFeature);
			});
		}
		
		
		if (!disallowLaggyFeatures() && featureGrouping.bambooFound) {
			// add 1 configured bamboo so it is dead last
			blendedFeatures(GenerationStage.Decoration.VEGETAL_DECORATION)
				.add(() -> configuredFeatureRegistry.getOrDefault(new ResourceLocation("minecraft", "bamboo")));
		}
		
		
		// make carvers be able to carve any underground blocks including netherrack, end stone, and modded blocks
		if (WorldBlender.WBDimensionConfig.carversCanCarveMoreBlocks.get()) {
			List<CarverAccessor> carvers = blendedCarversByStage.values().stream()
				.flatMap(Collection::stream)
				.map(carver -> (ConfiguredCarverAccessor) carver.get())
				.map(carver -> (CarverAccessor) carver.wb_getcarver())
				.collect(Collectors.toList());
			
			Set<Block> allCarvableBlocks = carvers.stream()
				.flatMap(carver -> carver.wb_getalwaysCarvableBlocks().stream())
				.collect(Collectors.toSet());
			
			carvers.forEach(carver -> carver.wb_setalwaysCarvableBlocks(allCarvableBlocks));
		}
		
		// add these last so that this can contain other local modification feature's liquids/falling blocks better
		boolean isModificationBlacklisted = ConfigBlacklisting.isResourceLocationBlacklisted(
			BlacklistType.FEATURE,
			new ResourceLocation(WorldBlender.MODID, "anti_floating_blocks_and_separate_liquids")
		);
		if (!isModificationBlacklisted) {
			blendedFeatures(GenerationStage.Decoration.LOCAL_MODIFICATIONS)
				.add(() -> WBConfiguredFeatures.ANTI_FLOATING_BLOCKS_AND_SEPARATE_LIQUIDS);
		}
	}
	
	private void addBiomeFeatures(List<List<Supplier<ConfiguredFeature<?, ?>>>> biomeFeaturesByStage) {
		for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
			if (stage.ordinal() >= biomeFeaturesByStage.size()) break;
			
			List<Supplier<ConfiguredFeature<?, ?>>> blendedFeatures = blendedFeatures(stage);
			
			for (Supplier<ConfiguredFeature<?, ?>> featureSupplier : biomeFeaturesByStage.get(stage.ordinal())) {
				if (checkedWorldgenObjects.contains(featureSupplier)) continue;
				checkedWorldgenObjects.add(featureSupplier);
				
				ConfiguredFeature<?, ?> feature = featureSupplier.get();
				
				// Do deep check to see if this configuredfeature instance is actually the same as another configuredfeature
				boolean alreadyPresent = blendedFeatures.stream().anyMatch(existing ->
					FeatureGrouping.serializeAndCompareFeature(
						existing.get(),
						feature,
						true
					)
				);
				if (alreadyPresent) continue;
				
				ResourceLocation featureID = configuredFeatureRegistry.getKey(feature);
				if (featureID == null) {
					featureID = WorldGenRegistries.CONFIGURED_FEATURE.getKey(feature);
				}
				
				if (shouldSkip(
					featureID,
					c -> c.allowVanillaFeatures,
					c -> c.allowModdedFeatures,
					BlacklistType.FEATURE
				)) continue;
				
				//// begin adding features //////
				
				// add the vanilla grass and flowers to a map so we can add them
				// later to the feature list so trees have a chance to spawn
				if (featureGrouping.checkAndAddSmallPlantFeatures(stage, feature)) continue;
				
				// add modded features that might be trees to front
				// of feature list so they have priority over all vanilla features in same generation stage.
				boolean isVanilla = featureID.getNamespace().equals("minecraft");
				if (!isVanilla && featureGrouping.checkAndAddLargePlantFeatures(stage, feature)) {
					blendedFeatures.add(0, featureSupplier);
					continue;
				}
				
				// if we have no laggy feature config on, then the feature must not be fire, lava, bamboo, etc in order to be added
				if (disallowLaggyFeatures() && featureGrouping.isLaggy(feature)) continue;
				
				blendedFeatures.add(featureSupplier);
			}
		}
	}
	
	private void addBiomeStructures(Collection<Supplier<StructureFeature<?, ?>>> biomeStructures) {
		for (Supplier<StructureFeature<?, ?>> structureSupplier : biomeStructures) {
			if (checkedWorldgenObjects.contains(structureSupplier)) continue;
			checkedWorldgenObjects.add(structureSupplier);
			
			StructureFeature<?, ?> configuredStructure = structureSupplier.get();
			
			// Having multiple configured structures of the same structure spawns only the last one it seems. Booo mojang boooooo. I want multiple village types in 1 biome!
			// TODO: couldn't this just be a contains with the supplier?
			boolean alreadyPresent = blendedStructures.stream()
				.anyMatch(existing -> existing.get() == configuredStructure);
			if (alreadyPresent) continue;
			
			// Have to do this computing as the feature in the registry is technically not the same
			// object as the feature in the biome. So I cannot get ID easily from the registry.
			// Instead, I have to check the JSON of the feature to find a match and store the ID of it
			// into a temporary map as a cache for later biomes.
			ResourceLocation configuredStructureID = configuredStructureRegistry.getKey(configuredStructure);
			if (configuredStructureID == null) {
				configuredStructureID = WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE.getKey(configuredStructure);
			}
			
			if (shouldSkip(
				configuredStructureID,
				c -> c.allowVanillaStructures,
				c -> c.allowModdedStructures,
				BlacklistType.STRUCTURE
			)) continue;
			
			blendedStructures.add(structureSupplier);
		}
	}
	
	private void addBiomeCarvers(Map<Carving, List<Supplier<ConfiguredCarver<?>>>> biomeCarversByStage) {
		for (Carving carverStage : GenerationStage.Carving.values()) {
			List<Supplier<ConfiguredCarver<?>>> blendedCarvers = blendedCarversByStage.get(carverStage);
			List<Supplier<ConfiguredCarver<?>>> biomeCarvers = biomeCarversByStage.get(carverStage);
			if (biomeCarvers == null) continue;
			for (Supplier<ConfiguredCarver<?>> carverSupplier : biomeCarvers) {
				if (checkedWorldgenObjects.contains(carverSupplier)) continue;
				checkedWorldgenObjects.add(carverSupplier);
				
				ConfiguredCarver<?> configuredCarver = carverSupplier.get();
				
				// TODO: couldn't this just be a contains with the supplier?
				boolean alreadyPresent = blendedCarvers.stream()
					.anyMatch(existing -> existing.get() == configuredCarver);
				if (alreadyPresent) continue;
				
				ResourceLocation configuredCarverID = configuredCarverRegistry.getKey(configuredCarver);
				if (configuredCarverID == null) {
					configuredCarverID = WorldGenRegistries.CONFIGURED_CARVER.getKey(configuredCarver);
				}
				
				if (shouldSkip(
					configuredCarverID,
					c -> c.allowVanillaCarvers,
					c -> c.allowModdedCarvers,
					BlacklistType.CARVER
				)) continue;
				
				blendedCarvers.add(carverSupplier);
			}
		}
	}
	
	private void addBiomeNaturalMobs(MobSpawnInfo biomeSpawnInfo) {
		for (EntityClassification spawnGroup : EntityClassification.values()) {
			Integer maxWeight = MAX_WEIGHT_PER_GROUP.getOrDefault(spawnGroup, Integer.MAX_VALUE);
			List<MobSpawnInfo.Spawners> blendedSpawns = blendedSpawnInfo.getSpawner(spawnGroup);
			for (MobSpawnInfo.Spawners spawnEntry : biomeSpawnInfo.getSpawners(spawnGroup)) {
				if (checkedMobs.contains(spawnEntry)) continue;
				checkedMobs.add(spawnEntry);
				
				boolean alreadyPresent = blendedSpawns.stream()
					.anyMatch(existing -> existing.type == spawnEntry.type);
				if (alreadyPresent) continue;
				
				// no check needed for if entitytype is null because it is impossible for it to be null without Minecraft blowing up
				ResourceLocation entityTypeID = entityTypeRegistry.getKey(spawnEntry.type);
				
				if (shouldSkip(
					entityTypeID,
					c -> c.allowVanillaSpawns,
					c -> c.allowModdedSpawns,
					BlacklistType.SPAWN
				)) continue;
				
				MobSpawnInfo.Spawners newEntry = new MobSpawnInfo.Spawners(
					spawnEntry.type,
					// Cap the weight and make sure it isn't too low
					Math.max(1, Math.min(maxWeight, spawnEntry.itemWeight)),
					spawnEntry.minCount,
					spawnEntry.maxCount
				);
				
				blendedSpawns.add(newEntry);
			}
		}
	}
	
	private void addBiomeSurfaceConfig(ISurfaceBuilderConfig biomeSurface, ResourceLocation biomeID) {
		if (shouldSkip(
			biomeID,
			c -> c.allowVanillaSurfaces,
			c -> c.allowModdedSurfaces,
			null
		)) return;
		
		// Blacklisted by surface list. Checks top block
		BlockState topMaterial = biomeSurface.getTop();
		
		// Also do null check as BYG actually managed to set the surfaceConfig's block to be null lol
		//noinspection ConstantConditions
		if (topMaterial == null) return;
		
		ResourceLocation topBlockID = blockRegistry.getKey(topMaterial.getBlock());
		if (ConfigBlacklisting.isResourceLocationBlacklisted(BlacklistType.SURFACE_BLOCK, topBlockID)) return;
		
		blendedSurface.addIfMissing(biomeSurface);
	}
	
	/**
	 An attempt to make sure we always have the spacing config for all structures
	 */
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
	
	/**
	 Checks if the given resource should be skipped based on config (checking for null in the process).
	 */
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
