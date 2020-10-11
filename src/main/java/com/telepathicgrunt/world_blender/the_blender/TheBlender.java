package com.telepathicgrunt.world_blender.the_blender;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.telepathicgrunt.world_blender.WorldBlender;
import com.telepathicgrunt.world_blender.features.WBConfiguredFeatures;
import com.telepathicgrunt.world_blender.mixin.CarverAccessor;
import com.telepathicgrunt.world_blender.mixin.ConfiguredCarverAccessor;
import com.telepathicgrunt.world_blender.mixin.GenerationSettingsAccessor;
import com.telepathicgrunt.world_blender.mixin.SpawnSettingsAccessor;
import com.telepathicgrunt.world_blender.surfacebuilder.BlendedSurfaceBuilder;
import com.telepathicgrunt.world_blender.surfacebuilder.WBSurfaceBuilders;
import com.telepathicgrunt.world_blender.the_blender.ConfigBlacklisting.BlacklistType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.GenerationStep.Carver;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.surfacebuilder.SurfaceConfig;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class TheBlender {

    private static final Gson GSON_PRINTER = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Kickstarts the blender. Should always be ran in MinecraftServer's init which is before the world is loaded
     */
    public static void blendTheWorld(DynamicRegistryManager.Impl registryManager){
        List<Biome> world_blender_biomes = registryManager.getOptional(Registry.BIOME_KEY).get().getEntries().stream()
                .filter(entry -> entry.getKey().getValue().getNamespace().equals(WorldBlender.MODID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        // Only world blender biomes will be mutable
        world_blender_biomes.forEach(TheBlender::makeBiomeMutable);


        // Reset these before biome loop
        ConfigBlacklisting.setupBlackLists();
        FeatureGrouping.setupFeatureMaps();
        BlendedSurfaceBuilder.resetSurfaceList();

        // THE biome loop. Very magical!
        for (Map.Entry<RegistryKey<Biome>, Biome> biomeEntry : registryManager.getOptional(Registry.BIOME_KEY).get().getEntries()) {

            if(!biomeEntry.getKey().getValue().getNamespace().equals(WorldBlender.MODID)){
                // begin blending into our biomes
                TheBlender.mainBlending(
                        biomeEntry.getValue(), // Biome
                        world_blender_biomes, // WB biomes
                        biomeEntry.getKey().getValue(), // Identifier
                        registryManager); // all the registries
            }
        }

        // wrap up the last bits that still needs to be blended but after the biome loop
        TheBlender.completeBlending(world_blender_biomes, registryManager.getOptional(Registry.CONFIGURED_FEATURE_WORLDGEN).get());

        // free up some memory when we are done.
        FeatureGrouping.clearFeatureMaps();
        FEATURE_MAP_CACHE.clear();
        STRUCTURE_MAP_CACHE.clear();
    }

    /**
     * blends the given biome into WB biomes
     */
    private static void mainBlending(Biome biome, List<Biome> world_blender_biomes, Identifier biomeID, DynamicRegistryManager.Impl dynamicRegistryManager) {

        // Debugging breakpoint spot
//        if(biomeID.getPath().contains("nether")){
//            int t = 5;
//        }

        // ignore our own biomes to speed things up and prevent possible duplications
        if (biomeID.getNamespace().equals("world_blender"))
            return;

            // if the biome is a vanilla biome but config says no vanilla biome, skip this biome
        else if (biomeID.getNamespace().equals("minecraft") && !WorldBlender.WB_CONFIG.WBBlendingConfig.allowVanillaBiomeImport)
            return;

            // if the biome is a modded biome but config says no modded biome, skip this biome
        else if (!biomeID.getNamespace().equals("minecraft") && !WorldBlender.WB_CONFIG.WBBlendingConfig.allowModdedBiomeImport)
            return;

            // blacklisted by blanket list
        else if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.BLANKET, biomeID))
            return;


        /////////// FEATURES//////////////////
        addBiomeFeatures(biome, world_blender_biomes, dynamicRegistryManager.get(Registry.CONFIGURED_FEATURE_WORLDGEN));

        ////////////////////// STRUCTURES////////////////////////
        addBiomeStructures(biome, world_blender_biomes, dynamicRegistryManager.get(Registry.CONFIGURED_STRUCTURE_FEATURE_WORLDGEN));

        //////////////////////// CARVERS/////////////////////////
        addBiomeCarvers(biome, world_blender_biomes, dynamicRegistryManager.get(Registry.CONFIGURED_CARVER_WORLDGEN));

        //////////////////////// SPAWNER/////////////////////////
        addBiomeNaturalMobs(biome, world_blender_biomes, Registry.ENTITY_TYPE);

        //////////////////////// SURFACE/////////////////////////
        addBiomeSurfaceConfig(biome, biomeID, Registry.BLOCK);
    }


    /**
     * Adds the last bit of stuff that needs to be added to WB biomes after everything else is added.
     * Like bamboo and flowers should be dead last so they don't crowd out tree spawning
     */
    public static void completeBlending(List<Biome> world_blender_biomes, MutableRegistry<ConfiguredFeature<?, ?>> configuredFeaturesRegistry) {


        // add end spike directly to all biomes if not directly blacklisted. Turning off vanilla features will not prevent end spikes from spawning due to them marking the world origin nicely
        if (!ConfigBlacklisting.isResourceLocationBlacklisted(BlacklistType.FEATURE, new Identifier("minecraft:end_spike")))
            world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(GenerationStep.Feature.SURFACE_STRUCTURES.ordinal()).add(() -> configuredFeaturesRegistry.get(new Identifier("minecraft", "end_spike"))));


        // add grass, flower, and other small plants now so they are generated second to last
        for (GenerationStep.Feature stage : GenerationStep.Feature.values()) {
            for (ConfiguredFeature<?, ?> grassyFlowerFeature : FeatureGrouping.SMALL_PLANT_MAP.get(stage)) {
                if (world_blender_biomes.get(0).getGenerationSettings().getFeatures().get(stage.ordinal()).stream().noneMatch(addedConfigFeature -> FeatureGrouping.serializeAndCompareFeature(addedConfigFeature.get(), grassyFlowerFeature, true))) {
                    world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(() -> grassyFlowerFeature));
                }
            }
        }


        if (!WorldBlender.WB_CONFIG.WBBlendingConfig.disallowLaggyFeatures && FeatureGrouping.bambooFound) {
            // add 1 configured bamboo so it is dead last
            world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(GenerationStep.Feature.VEGETAL_DECORATION.ordinal()).add(() -> configuredFeaturesRegistry.get(new Identifier("minecraft", "bamboo"))));
        }


        // Makes carvers be able to carve any underground blocks including netherrack, end stone, and modded blocks
        if (WorldBlender.WB_CONFIG.WBDimensionConfig.carversCanCarveMoreBlocks) {
            Set<Block> allBlocksToCarve = BlendedSurfaceBuilder.blocksToCarve();

            //get all carvable blocks
            for (Carver carverStage : GenerationStep.Carver.values()) {
                for (Supplier<ConfiguredCarver<?>> carver : world_blender_biomes.get(0).getGenerationSettings().getCarversForStep(carverStage)) {
                    allBlocksToCarve.addAll(((CarverAccessor)((ConfiguredCarverAccessor)carver.get()).getcarver()).getalwaysCarvableBlocks());
                }
            }

            //update all carvers to carve the complete list of stuff to carve
            for (Carver carverStage : GenerationStep.Carver.values()) {
                for (Supplier<ConfiguredCarver<?>> carver : world_blender_biomes.get(0).getGenerationSettings().getCarversForStep(carverStage)) {
                    ((CarverAccessor)((ConfiguredCarverAccessor)carver.get()).getcarver()).setalwaysCarvableBlocks(allBlocksToCarve);
                }
            }
        }

        // add these last so that this can contain other local modification feature's liquids/falling blocks better
        if (!ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, new Identifier("world_blender:no_floating_liquids_or_falling_blocks"))) {
            world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(GenerationStep.Feature.LOCAL_MODIFICATIONS.ordinal()).add(() -> WBConfiguredFeatures.NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS));
        }

        if (!ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, new Identifier("world_blender:separate_lava_and_water"))) {
            world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(GenerationStep.Feature.LOCAL_MODIFICATIONS.ordinal()).add(() -> WBConfiguredFeatures.SEPARATE_LAVA_AND_WATER));
        }
    }

    /**
     * Helper method to make WB biomes mutable to add stuff to it later
     */
    private static void makeBiomeMutable(Biome biome){
        // Make the structure and features list mutable for modification late
        List<List<Supplier<ConfiguredFeature<?, ?>>>> tempFeature = ((GenerationSettingsAccessor)biome.getGenerationSettings()).getGSFeatures();
        List<List<Supplier<ConfiguredFeature<?, ?>>>> mutableGenerationStages = new ArrayList<>();

        // Fill in generation stages so there are at least 10 or else Minecraft crashes.
        // (we need all stages for adding features/structures to the right stage too)
        for(int currentStageIndex = 0; currentStageIndex < Math.max(GenerationStep.Feature.values().length, tempFeature.size()); currentStageIndex++){
            if(currentStageIndex >= tempFeature.size()){
                mutableGenerationStages.add(new ArrayList<>());
            }else{
                mutableGenerationStages.add(new ArrayList<>(tempFeature.get(currentStageIndex)));
            }
        }

        // Make the Structure and GenerationStages (features) list mutable for modification later
        ((GenerationSettingsAccessor)biome.getGenerationSettings()).setGSFeatures(mutableGenerationStages);
        ((GenerationSettingsAccessor)biome.getGenerationSettings()).setGSStructureFeatures(new ArrayList<>(((GenerationSettingsAccessor)biome.getGenerationSettings()).getGSStructureFeatures()));
        ((GenerationSettingsAccessor)biome.getGenerationSettings()).setGSStructureFeatures(new ArrayList<>(((GenerationSettingsAccessor)biome.getGenerationSettings()).getGSStructureFeatures()));

        ((GenerationSettingsAccessor)biome.getGenerationSettings()).setCarvers(new HashMap<>(((GenerationSettingsAccessor)biome.getGenerationSettings()).getCarvers()));
        for(Carver carverGroup : Carver.values()){
            ((GenerationSettingsAccessor) biome.getGenerationSettings()).getCarvers().put(carverGroup, new ArrayList<>(biome.getGenerationSettings().getCarversForStep(carverGroup)));
        }

        ((SpawnSettingsAccessor)biome.getSpawnSettings()).setSpawners(new HashMap<>(((SpawnSettingsAccessor) biome.getSpawnSettings()).getSpawners()));
        for(SpawnGroup spawnGroup : SpawnGroup.values()){
            ((SpawnSettingsAccessor) biome.getSpawnSettings()).getSpawners().put(spawnGroup, new ArrayList<>(biome.getSpawnSettings().getSpawnEntry(spawnGroup)));
        }

        ((SpawnSettingsAccessor)biome.getSpawnSettings()).setSpawnCosts(new HashMap<>(((SpawnSettingsAccessor) biome.getSpawnSettings()).getSpawnCosts()));
    }


    //--------------------------------------------------------------
            // The actual main blending below
            // Welcome to hell!

    private static HashBiMap<Identifier, ConfiguredFeature<?,?>> FEATURE_MAP_CACHE = HashBiMap.create();


    private static void addBiomeFeatures(Biome biome, List<Biome> world_blender_biomes, MutableRegistry<ConfiguredFeature<?, ?>> configuredFeaturesRegistry) {
        for (GenerationStep.Feature stage : GenerationStep.Feature.values()) {
            if(stage.ordinal() >= biome.getGenerationSettings().getFeatures().size()) break;

            for (Supplier<ConfiguredFeature<?, ?>> configuredFeatureSupplier : biome.getGenerationSettings().getFeatures().get(stage.ordinal())) {
                ConfiguredFeature<?, ?> configuredFeature = configuredFeatureSupplier.get();
                if (world_blender_biomes.get(0).getGenerationSettings().getFeatures().get(stage.ordinal()).stream().noneMatch(addedConfigFeature -> FeatureGrouping.serializeAndCompareFeature(addedConfigFeature.get(), configuredFeatureSupplier.get(), true))) {

                    Identifier configuredFeatureID = configuredFeaturesRegistry.getId(configuredFeature);
                    if(configuredFeatureID == null){
                        configuredFeatureID = BuiltinRegistries.CONFIGURED_FEATURE.getId(configuredFeature);
                    }
                    if(configuredFeatureID == null){
                        configuredFeatureID = FEATURE_MAP_CACHE.inverse().get(configuredFeature);
                    }

                    // The cache is to prevent unregistered configuredfeatures
                    // from being added multiple times by storing its json as an
                    // ID in the cache. This only gets triggered when we find
                    // that specific unregistered configuredfeature for the first time
                    if(configuredFeatureID == null){
                        configuredFeatureID = configuredFeaturesRegistry.getId(configuredFeature);
                        if(configuredFeatureID != null){
                            FEATURE_MAP_CACHE.put(configuredFeatureID, configuredFeature);
                        }
                        else{
                            for(Map.Entry<RegistryKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> entry : configuredFeaturesRegistry.getEntries()){
                                if(FeatureGrouping.serializeAndCompareFeature(entry.getValue(), configuredFeature, false)){
                                    FEATURE_MAP_CACHE.put(entry.getKey().getValue(), entry.getValue());
                                    configuredFeatureID = entry.getKey().getValue();
                                }
                            }
                        }
                    }

                    if(configuredFeatureID == null){
                        Optional<JsonElement> configuredFeatureJSON = ConfiguredFeature.CODEC.encode(configuredFeatureSupplier, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();

                        WorldBlender.LOGGER.log(Level.WARN, "---------------\n" +
                                "WORLD BLENDER:\n\n" +
                                "Found a ConfiguredFeature not registered. Some mod forgot to registered their stuff.\n" +
                                "Please look at the following JSON and report this registration issue to the mod that this ConfiguredFeature belongs to.\n" +
                                "The json looks like: " + (configuredFeatureJSON.isPresent() ? GSON_PRINTER.toJson(configuredFeatureJSON.get()) : "Error, unable to show JSON"));
                        continue;
                    }

                    // Check feature blacklist from config
                    if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, configuredFeatureID)) {
                        continue;
                    }

                    //// begin adding features//////

                    // check if feature is already added
                    if (configuredFeatureID.getNamespace().equals("minecraft")) {

                        if (WorldBlender.WB_CONFIG.WBBlendingConfig.allowVanillaFeatures) {
                            // add the vanilla grass and flowers to a map so we can add them
                            // later to the feature list so trees have a chance to spawn
                            if (FeatureGrouping.checksAndAddSmallPlantFeatures(stage, configuredFeature)) {
                                continue;
                            }

                            // if we have no laggy feature config on, then the feature must not be fire, lava, bamboo, etc in order to be added
                            if ((!FeatureGrouping.isLaggyFeature(configuredFeature) || !WorldBlender.WB_CONFIG.WBBlendingConfig.disallowLaggyFeatures)) {
                                world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(configuredFeatureSupplier));
                            }
                        }
                    }
                    else if (WorldBlender.WB_CONFIG.WBBlendingConfig.allowModdedFeatures) {
                        // checksAndAddSmallPlantFeatures add the vanilla grass and flowers to a map
                        // so we can add them later to the feature list so trees have a chance to spawn
                        //
                        // checksAndAddLargePlantFeatures adds modded features that might be trees to front
                        // of feature list so they have priority over all vanilla features in same generation stage.
                        if (!FeatureGrouping.checksAndAddSmallPlantFeatures(stage, configuredFeature) && FeatureGrouping.checksAndAddLargePlantFeatures(stage, configuredFeature)) {
                            world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(0, configuredFeatureSupplier));
                        }
                        else {
                            // cannot be a bamboo feature as we will place them dead last in the feature
                            // list so they don't overwhelm other features or cause as many bamboo breaking
                            // because it got cut off
                            // if we have no laggy feature config on, then the feature must not be fire, lava, bamboo, etc in order to be added
                            if ((!FeatureGrouping.isLaggyFeature(configuredFeature) || !WorldBlender.WB_CONFIG.WBBlendingConfig.disallowLaggyFeatures)) {
                                world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(configuredFeatureSupplier));
                            }
                        }
                    }
                }
            }
        }
    }

    private static HashBiMap<Identifier, ConfiguredStructureFeature<?,?>> STRUCTURE_MAP_CACHE = HashBiMap.create();

    private static void addBiomeStructures(Biome biome, List<Biome> world_blender_biomes, MutableRegistry<ConfiguredStructureFeature<?, ?>> configuredStructuresRegistry) {
        for (Supplier<ConfiguredStructureFeature<?, ?>> configuredStructureSupplier : biome.getGenerationSettings().getStructureFeatures()) {
            ConfiguredStructureFeature<?, ?> configuredStructure = configuredStructureSupplier.get();

            // Having multiple configured structures of the same structure spawns only the last one it seems. Booo mojang boooooo. I want multiple village types in 1 biome!
            if (world_blender_biomes.get(0).getGenerationSettings().getStructureFeatures().stream().noneMatch(addedConfiguredStructure -> addedConfiguredStructure.get().feature == configuredStructure.feature)) {

                // Have to do this computing as the feature in the registry is technically not the same
                // object as the feature in the biome. So I cannot get ID easily from the registry.
                // Instead, I have to check the JSON of the feature to find a match and store the ID of it
                // into a temporary map as a cache for later biomes.
                Identifier configuredStructureID = configuredStructuresRegistry.getId(configuredStructure);
                if(configuredStructureID == null){
                    configuredStructureID = BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getId(configuredStructure);
                }
                if(configuredStructureID == null){
                    configuredStructureID = STRUCTURE_MAP_CACHE.inverse().get(configuredStructure);
                }

                // The cache is to prevent unregistered configuredstructures
                // from being added multiple times by storing its json as an
                // ID in the cache. This only gets triggered when we find
                // that specific unregistered configuredstructures for the first time
                if(configuredStructureID == null){
                    configuredStructureID = configuredStructuresRegistry.getId(configuredStructure);
                    if(configuredStructureID != null){
                        STRUCTURE_MAP_CACHE.put(configuredStructureID, configuredStructure);
                    }
                    else {
                        for (Map.Entry<RegistryKey<ConfiguredStructureFeature<?, ?>>, ConfiguredStructureFeature<?, ?>> entry : configuredStructuresRegistry.getEntries()) {
                            if (FeatureGrouping.serializeAndCompareStructureJSONOnly(entry.getValue(), configuredStructure)) {
                                STRUCTURE_MAP_CACHE.put(entry.getKey().getValue(), entry.getValue());
                                configuredStructureID = entry.getKey().getValue();
                            }
                        }
                    }
                }

                if(configuredStructureID == null){
                    Optional<JsonElement> configuredStructureJSON = ConfiguredStructureFeature.CODEC.encode(configuredStructure, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();

                    WorldBlender.LOGGER.log(Level.WARN, "---------------\n" +
                            "WORLD BLENDER:\n\n" +
                            "Found a ConfiguredStructure not registered. Some mod forgot to registered their stuff." +
                            "Please look at the following JSON and report this registration issue to the mod that this ConfiguredStructure belongs to." +
                            "The json looks like: " + (configuredStructureJSON.isPresent() ? GSON_PRINTER.toJson(configuredStructureJSON.get()) : "Error, unable to show JSON"));
                    continue;
                }

                // blacklisted by structure list
                if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.STRUCTURE, configuredStructureID)) {
                    continue;
                }

                if (configuredStructureID.getNamespace().equals("minecraft")) {
                    if (WorldBlender.WB_CONFIG.WBBlendingConfig.allowVanillaStructures) {
                        // add the structure version of the structure
                        world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getStructureFeatures().add(configuredStructureSupplier));
                    }
                }
                else if (WorldBlender.WB_CONFIG.WBBlendingConfig.allowModdedStructures) {
                    // add the structure version of the structure
                    world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getStructureFeatures().add(configuredStructureSupplier));
                }
            }
        }
    }


    private static void addBiomeCarvers(Biome biome, List<Biome> world_blender_biomes, MutableRegistry<ConfiguredCarver<?>> configuredCarversRegistry) {
        for (Carver carverStage : GenerationStep.Carver.values()) {
            for (Supplier<ConfiguredCarver<?>> configuredCarverSupplier : biome.getGenerationSettings().getCarversForStep(carverStage)) {
                ConfiguredCarver<?> configuredCarver = configuredCarverSupplier.get();
                if (world_blender_biomes.get(0).getGenerationSettings().getCarversForStep(carverStage).stream().noneMatch(addedConfiguredCarver -> addedConfiguredCarver.get() == configuredCarver)) {

                    Identifier configuredCarverID = configuredCarversRegistry.getId(configuredCarver);
                    if(configuredCarverID == null){
                        Optional<JsonElement> configuredStructureJSON = ConfiguredCarver.CODEC.encode(configuredCarver, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();

                        WorldBlender.LOGGER.log(Level.WARN, "---------------\n" +
                                "WORLD BLENDER:\n\n" +
                                "Found a ConfiguredCarver not registered. Some mod forgot to registered their stuff." +
                                "Please look at the following JSON and report this registration issue to the mod that this ConfiguredCarver belongs to." +
                                "The json looks like: " + (configuredStructureJSON.isPresent() ? GSON_PRINTER.toJson(configuredStructureJSON.get()) : "Error, unable to show JSON"));
                        continue;
                    }

                    // blacklisted by carver list
                    if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.CARVER, configuredCarverID)) {
                        continue;
                    }

                    if (configuredCarverID.getNamespace().equals("minecraft")) {
                        if (WorldBlender.WB_CONFIG.WBBlendingConfig.allowVanillaCarvers)
                            world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getCarversForStep(carverStage).add(configuredCarverSupplier));
                    }
                    else if (WorldBlender.WB_CONFIG.WBBlendingConfig.allowModdedCarvers) {
                        world_blender_biomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getCarversForStep(carverStage).add(configuredCarverSupplier));
                    }
                }
            }
        }
    }


    private static void addBiomeNaturalMobs(Biome biome, List<Biome> world_blender_biomes, MutableRegistry<EntityType<?>> entityTypeRegistry) {
        for (SpawnGroup spawnGroup : SpawnGroup.values()) {
            for (SpawnSettings.SpawnEntry spawnEntry : biome.getSpawnSettings().getSpawnEntry(spawnGroup)) {
                if (world_blender_biomes.get(0).getSpawnSettings().getSpawnEntry(spawnGroup).stream().noneMatch(spawn -> spawn.type == spawnEntry.type)) {

                    //no check needed for if entitytype is null because it is impossible for it to be null without Minecraft blowing up
                    Identifier entityTypeID = entityTypeRegistry.getId(spawnEntry.type);

                    // blacklisted by natural spawn list
                    if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SPAWN, entityTypeID)) {
                        continue;
                    }

                    if (entityTypeID.getNamespace().equals("minecraft")) {
                        if (WorldBlender.WB_CONFIG.WBBlendingConfig.allowVanillaSpawns)
                            world_blender_biomes.forEach(blendedBiome -> blendedBiome.getSpawnSettings().getSpawnEntry(spawnGroup).add(spawnEntry));
                    } else if (WorldBlender.WB_CONFIG.WBBlendingConfig.allowModdedSpawns) {
                        world_blender_biomes.forEach(blendedBiome -> blendedBiome.getSpawnSettings().getSpawnEntry(spawnGroup).add(spawnEntry));
                    }
                }
            }
        }
    }


    private static void addBiomeSurfaceConfig(Biome biome, Identifier biomeID, MutableRegistry<Block> blockRegistry) {

        // return early if biome's is turned off by the vanilla surface configs.
        if (biomeID.getNamespace().equals("minecraft")) {
            if (!WorldBlender.WB_CONFIG.WBBlendingConfig.allowVanillaSurfaces)
                return;
        }
        else if (!WorldBlender.WB_CONFIG.WBBlendingConfig.allowModdedSurfaces) {
            return;
        }


        //A check to make sure we can safely cast
        if (biome.getGenerationSettings().getSurfaceConfig() instanceof TernarySurfaceConfig) {
            TernarySurfaceConfig surfaceConfig = (TernarySurfaceConfig) biome.getGenerationSettings().getSurfaceConfig();

            // blacklisted by surface list. Checks top block
            if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, blockRegistry.getId(surfaceConfig.getTopMaterial().getBlock()))) {
                return;
            }

            if (!((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER).containsConfig(surfaceConfig)) {
                ((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER).addConfig(surfaceConfig);
            }
        }
        else {
            //backup way to get the surface config (should always be safe as getSurfaceBuilderConfig always returns a ISurfaceBuilderConfig)
            //Downside is we cannot get the underwater block now.
            SurfaceConfig surfaceConfig = biome.getGenerationSettings().getSurfaceConfig();

            // blacklisted by surface list. Checks top block
            if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, blockRegistry.getId(surfaceConfig.getTopMaterial().getBlock()))) {
                return;
            }

            if (!((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER).containsConfig(surfaceConfig)) {
                ((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER).addConfig(surfaceConfig);
            }
        }
    }
}
