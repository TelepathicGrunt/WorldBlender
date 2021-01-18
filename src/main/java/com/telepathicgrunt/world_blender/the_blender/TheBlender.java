package com.telepathicgrunt.world_blender.the_blender;

import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.WorldBlender;
import com.telepathicgrunt.world_blender.features.WBConfiguredFeatures;
import com.telepathicgrunt.world_blender.mixin.worldgen.*;
import com.telepathicgrunt.world_blender.surfacebuilder.BlendedSurfaceBuilder;
import com.telepathicgrunt.world_blender.surfacebuilder.WBSurfaceBuilders;
import com.telepathicgrunt.world_blender.the_blender.ConfigBlacklisting.BlacklistType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
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
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class TheBlender {

    // MUST KEEP THESE SETS. They massively speed up World Blender at mod startup by preventing excessive running of .anyMatch and other streams/checks
    private static final Set<Supplier<?>> CHECKED_WORLDGEN_OBJECTS = new HashSet<>();
    private static final Set<MobSpawnInfo.Spawners> CHECKED_MOBS = new HashSet<>();

    /**
     * Kickstarts the blender. Should always be ran in MinecraftServer's init which is before the world is loaded
     */
    public static void blendTheWorld(DynamicRegistries.Impl registryManager){
        if(!registryManager.func_230521_a_(Registry.BIOME_KEY).isPresent()) return;

        List<Biome> worldBlenderBiomes = registryManager.func_230521_a_(Registry.BIOME_KEY).get().getEntries().stream()
                .filter(entry -> entry.getKey().getLocation().getNamespace().equals(WorldBlender.MODID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        // Only world blender biomes will be mutable
        worldBlenderBiomes.forEach(TheBlender::makeBiomeMutable);

        // Clear out world blender biomes of everything.
        if(WorldBlender.WBBlendingConfig.cleanSlateWBBiomesAtStartup.get()){
            worldBlenderBiomes.forEach(biome -> {
                biome.getGenerationSettings().getFeatures().forEach(List::clear);
                biome.getGenerationSettings().getStructures().clear();
                ((GenerationSettingsAccessor)biome.getGenerationSettings()).wb_getCarvers().forEach((stage, list) -> list.clear());
                ((MobSpawnInfoAccessor)biome.getMobSpawnInfo()).wb_getSpawnCosts().clear();
                ((MobSpawnInfoAccessor)biome.getMobSpawnInfo()).wb_getSpawners().forEach((group, list) -> list.clear());
            });
        }

        // Reset these before biome loop
        ConfigBlacklisting.setupBlackLists();
        FeatureGrouping.setupFeatureMaps();
        BlendedSurfaceBuilder.resetSurfaceList();

        // THE biome loop. Very magical!
        for (Map.Entry<RegistryKey<Biome>, Biome> biomeEntry : registryManager.func_230521_a_(Registry.BIOME_KEY).get().getEntries()) {

            if(!biomeEntry.getKey().getLocation().getNamespace().equals(WorldBlender.MODID)){
                // begin blending into our biomes
                TheBlender.mainBlending(
                        biomeEntry.getValue(), // Biome
                        worldBlenderBiomes, // WB biomes
                        biomeEntry.getKey().getLocation(), // ResourceLocation
                        registryManager); // all the registries
            }
        }

        // wrap up the last bits that still needs to be blended but after the biome loop
        TheBlender.completeBlending(worldBlenderBiomes, registryManager.getRegistry(Registry.CONFIGURED_FEATURE_KEY));

        // free up some memory when we are done and ready it for the next world clicked on.
        FeatureGrouping.clearFeatureMaps();
        CHECKED_WORLDGEN_OBJECTS.clear();
        CHECKED_MOBS.clear();
    }

    /**
     * blends the given biome into WB biomes
     */
    private static void mainBlending(Biome biome, List<Biome> worldBlenderBiomes, ResourceLocation biomeID, DynamicRegistries.Impl dynamicRegistryManager) {

        // Debugging breakpoint spot
//        if(biomeID.getPath().contains("nether")){
//            int t = 5;
//        }

        // ignore our own biomes to speed things up and prevent possible duplications
        if (biomeID.getNamespace().equals("world_blender"))
            return;

            // if the biome is a vanilla biome but config says no vanilla biome, skip this biome
        else if (biomeID.getNamespace().equals("minecraft") && !WorldBlender.WBBlendingConfig.allowVanillaBiomeImport.get())
            return;

            // if the biome is a modded biome but config says no modded biome, skip this biome
        else if (!biomeID.getNamespace().equals("minecraft") && !WorldBlender.WBBlendingConfig.allowModdedBiomeImport.get())
            return;

            // blacklisted by blanket list
        else if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.BLANKET, biomeID))
            return;


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
     * Adds the last bit of stuff that needs to be added to WB biomes after everything else is added.
     * Like bamboo and flowers should be dead last so they don't crowd out tree spawning
     */
    public static void completeBlending(List<Biome> worldBlenderBiomes, MutableRegistry<ConfiguredFeature<?, ?>> configuredFeaturesRegistry) {


        // add end spike directly to all biomes if not directly blacklisted. Turning off vanilla features will not prevent end spikes from spawning due to them marking the world origin nicely
        if (!ConfigBlacklisting.isResourceLocationBlacklisted(BlacklistType.FEATURE, new ResourceLocation("minecraft:end_spike")))
            worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(GenerationStage.Decoration.SURFACE_STRUCTURES.ordinal()).add(() -> configuredFeaturesRegistry.getOrDefault(new ResourceLocation("minecraft", "end_spike"))));


        // add grass, flower, and other small plants now so they are generated second to last
        for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
            for (ConfiguredFeature<?, ?> grassyFlowerFeature : FeatureGrouping.SMALL_PLANT_MAP.get(stage)) {
                if (worldBlenderBiomes.get(0).getGenerationSettings().getFeatures().get(stage.ordinal()).stream().noneMatch(addedConfigFeature -> FeatureGrouping.serializeAndCompareFeature(addedConfigFeature.get(), grassyFlowerFeature, true))) {
                    worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(() -> grassyFlowerFeature));
                }
            }
        }


        if (!WorldBlender.WBBlendingConfig.disallowLaggyFeatures.get() && FeatureGrouping.bambooFound) {
            // add 1 configured bamboo so it is dead last
            worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(GenerationStage.Decoration.VEGETAL_DECORATION.ordinal()).add(() -> configuredFeaturesRegistry.getOrDefault(new ResourceLocation("minecraft", "bamboo"))));
        }


        // Makes carvers be able to carve any underground blocks including netherrack, end stone, and modded blocks
        if (WorldBlender.WBDimensionConfig.carversCanCarveMoreBlocks.get()) {
            Set<Block> allBlocksToCarve = BlendedSurfaceBuilder.blocksToCarve();

            //get all carvable blocks
            for (GenerationStage.Carving carverStage : GenerationStage.Carving.values()) {
                for (Supplier<ConfiguredCarver<?>> carver : worldBlenderBiomes.get(0).getGenerationSettings().getCarvers(carverStage)) {
                    allBlocksToCarve.addAll(((CarverAccessor)((ConfiguredCarverAccessor)carver.get()).wb_getcarver()).wb_getalwaysCarvableBlocks());
                }
            }

            //update all carvers to carve the complete list of stuff to carve
            for (GenerationStage.Carving carverStage : GenerationStage.Carving.values()) {
                for (Supplier<ConfiguredCarver<?>> carver : worldBlenderBiomes.get(0).getGenerationSettings().getCarvers(carverStage)) {
                    ((CarverAccessor)((ConfiguredCarverAccessor)carver.get()).wb_getcarver()).wb_setalwaysCarvableBlocks(allBlocksToCarve);
                }
            }
        }

        // add these last so that this can contain other local modification feature's liquids/falling blocks better
        if (!ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, new ResourceLocation("world_blender:anti_floating_blocks_and_separate_liquids"))) {
            worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(GenerationStage.Decoration.LOCAL_MODIFICATIONS.ordinal()).add(() -> WBConfiguredFeatures.ANTI_FLOATING_BLOCKS_AND_SEPARATE_LIQUIDS));
        }
    }

    /**
     * Helper method to make WB biomes mutable to add stuff to it later
     */
    private static void makeBiomeMutable(Biome biome){
        // Make the structure and features list mutable for modification late
        List<List<Supplier<ConfiguredFeature<?, ?>>>> tempFeature = ((GenerationSettingsAccessor)biome.getGenerationSettings()).wb_getGSFeatures();
        List<List<Supplier<ConfiguredFeature<?, ?>>>> mutableGenerationStages = new ArrayList<>();

        // Fill in generation stages so there are at least 10 or else Minecraft crashes.
        // (we need all stages for adding features/structures to the right stage too)
        for(int currentStageIndex = 0; currentStageIndex < Math.max(GenerationStage.Decoration.values().length, tempFeature.size()); currentStageIndex++){
            if(currentStageIndex >= tempFeature.size()){
                mutableGenerationStages.add(new ArrayList<>());
            }else{
                mutableGenerationStages.add(new ArrayList<>(tempFeature.get(currentStageIndex)));
            }
        }

        // Make the Structure and GenerationStages (features) list mutable for modification later
        ((GenerationSettingsAccessor)biome.getGenerationSettings()).wb_setGSFeatures(mutableGenerationStages);
        ((GenerationSettingsAccessor)biome.getGenerationSettings()).wb_setGSStructureFeatures(new ArrayList<>(((GenerationSettingsAccessor)biome.getGenerationSettings()).wb_getGSStructureFeatures()));
        ((GenerationSettingsAccessor)biome.getGenerationSettings()).wb_setGSStructureFeatures(new ArrayList<>(((GenerationSettingsAccessor)biome.getGenerationSettings()).wb_getGSStructureFeatures()));

        ((GenerationSettingsAccessor)biome.getGenerationSettings()).wb_setCarvers(new HashMap<>(((GenerationSettingsAccessor)biome.getGenerationSettings()).wb_getCarvers()));
        for(Carving carverGroup : Carving.values()){
            ((GenerationSettingsAccessor) biome.getGenerationSettings()).wb_getCarvers().put(carverGroup, new ArrayList<>(biome.getGenerationSettings().getCarvers(carverGroup)));
        }

        ((MobSpawnInfoAccessor)biome.getMobSpawnInfo()).wb_setSpawners(new HashMap<>(((MobSpawnInfoAccessor) biome.getMobSpawnInfo()).wb_getSpawners()));
        for(EntityClassification spawnGroup : EntityClassification.values()){
            ((MobSpawnInfoAccessor) biome.getMobSpawnInfo()).wb_getSpawners().put(spawnGroup, new ArrayList<>(biome.getMobSpawnInfo().getSpawners(spawnGroup)));
        }

        ((MobSpawnInfoAccessor)biome.getMobSpawnInfo()).wb_setSpawnCosts(new HashMap<>(((MobSpawnInfoAccessor) biome.getMobSpawnInfo()).wb_getSpawnCosts()));
    }


    //--------------------------------------------------------------
            // The actual main blending below
            // Welcome to hell!


    private static void addBiomeFeatures(Biome biome, List<Biome> worldBlenderBiomes, MutableRegistry<ConfiguredFeature<?, ?>> configuredFeaturesRegistry) {
        for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
            if(stage.ordinal() >= biome.getGenerationSettings().getFeatures().size()) break;

            for (Supplier<ConfiguredFeature<?, ?>> configuredFeatureSupplier : biome.getGenerationSettings().getFeatures().get(stage.ordinal())) {
                ConfiguredFeature<?, ?> configuredFeature = configuredFeatureSupplier.get();

                if(!CHECKED_WORLDGEN_OBJECTS.contains(configuredFeatureSupplier)){
                    // Do deep check to see if this configuredfeature instance is actually the same as another configuredfeature
                    if (worldBlenderBiomes.get(0).getGenerationSettings().getFeatures().get(stage.ordinal()).stream().noneMatch(addedConfigFeature -> FeatureGrouping.serializeAndCompareFeature(addedConfigFeature.get(), configuredFeatureSupplier.get(), true))) {
                        ResourceLocation configuredFeatureID = configuredFeaturesRegistry.getKey(configuredFeature);
                        if(configuredFeatureID == null){
                            configuredFeatureID = WorldGenRegistries.CONFIGURED_FEATURE.getKey(configuredFeature);
                        }

                        // Skip unregistered configuredfeatures
                        if(configuredFeatureID == null){
                            continue;
                        }

                        // Check feature blacklist from config
                        if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, configuredFeatureID)) {
                            continue;
                        }

                        //// begin adding features//////

                        // check if feature is already added
                        if (configuredFeatureID.getNamespace().equals("minecraft")) {

                            if (WorldBlender.WBBlendingConfig.allowVanillaFeatures.get()) {
                                // add the vanilla grass and flowers to a map so we can add them
                                // later to the feature list so trees have a chance to spawn
                                if (FeatureGrouping.checksAndAddSmallPlantFeatures(stage, configuredFeature)) {
                                    continue;
                                }

                                // if we have no laggy feature config on, then the feature must not be fire, lava, bamboo, etc in order to be added
                                if ((!FeatureGrouping.isLaggyFeature(configuredFeature) || !WorldBlender.WBBlendingConfig.disallowLaggyFeatures.get())) {
                                    worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(configuredFeatureSupplier));
                                }
                            }
                        }
                        else if (WorldBlender.WBBlendingConfig.allowModdedFeatures.get()) {
                            // checksAndAddSmallPlantFeatures add the vanilla grass and flowers to a map
                            // so we can add them later to the feature list so trees have a chance to spawn
                            //
                            // checksAndAddLargePlantFeatures adds modded features that might be trees to front
                            // of feature list so they have priority over all vanilla features in same generation stage.
                            if (!FeatureGrouping.checksAndAddSmallPlantFeatures(stage, configuredFeature) && FeatureGrouping.checksAndAddLargePlantFeatures(stage, configuredFeature)) {
                                worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(0, configuredFeatureSupplier));
                            }
                            else {
                                // cannot be a bamboo feature as we will place them dead last in the feature
                                // list so they don't overwhelm other features or cause as many bamboo breaking
                                // because it got cut off
                                // if we have no laggy feature config on, then the feature must not be fire, lava, bamboo, etc in order to be added
                                if ((!FeatureGrouping.isLaggyFeature(configuredFeature) || !WorldBlender.WBBlendingConfig.disallowLaggyFeatures.get())) {
                                    worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getFeatures().get(stage.ordinal()).add(configuredFeatureSupplier));
                                }
                            }
                        }
                    }

                    CHECKED_WORLDGEN_OBJECTS.add(configuredFeatureSupplier);
                }
            }
        }
    }

    private static void addBiomeStructures(Biome biome, List<Biome> worldBlenderBiomes, MutableRegistry<StructureFeature<?, ?>> configuredStructuresRegistry) {
        for (Supplier<StructureFeature<?, ?>> configuredStructureSupplier : biome.getGenerationSettings().getStructures()) {
            StructureFeature<?, ?> configuredStructure = configuredStructureSupplier.get();

            if(!CHECKED_WORLDGEN_OBJECTS.contains(configuredStructureSupplier)){

                // Having multiple configured structures of the same structure spawns only the last one it seems. Booo mojang boooooo. I want multiple village types in 1 biome!
                if (worldBlenderBiomes.get(0).getGenerationSettings().getStructures().stream().noneMatch(addedConfiguredStructure -> addedConfiguredStructure.get() == configuredStructure)) {

                    // Have to do this computing as the feature in the registry is technically not the same
                    // object as the feature in the biome. So I cannot get ID easily from the registry.
                    // Instead, I have to check the JSON of the feature to find a match and store the ID of it
                    // into a temporary map as a cache for later biomes.
                    ResourceLocation configuredStructureID = configuredStructuresRegistry.getKey(configuredStructure);
                    if(configuredStructureID == null){
                        configuredStructureID = WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE.getKey(configuredStructure);
                    }

                    // Skip unregistered configuredfeatures
                    if(configuredStructureID == null){
                        continue;
                    }

                    // blacklisted by structure list
                    if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.STRUCTURE, configuredStructureID)) {
                        continue;
                    }

                    if (configuredStructureID.getNamespace().equals("minecraft")) {
                        if (WorldBlender.WBBlendingConfig.allowVanillaStructures.get()) {
                            // add the structure version of the structure
                            worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getStructures().add(configuredStructureSupplier));
                        }
                    }
                    else if (WorldBlender.WBBlendingConfig.allowModdedStructures.get()) {
                        // add the structure version of the structure
                        worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getStructures().add(configuredStructureSupplier));
                    }
                }
                CHECKED_WORLDGEN_OBJECTS.add(configuredStructureSupplier);
            }
        }
    }


    private static void addBiomeCarvers(Biome biome, List<Biome> worldBlenderBiomes, MutableRegistry<ConfiguredCarver<?>> configuredCarversRegistry) {
        for (Carving carverStage : GenerationStage.Carving.values()) {
            for (Supplier<ConfiguredCarver<?>> configuredCarverSupplier : biome.getGenerationSettings().getCarvers(carverStage)) {
                ConfiguredCarver<?> configuredCarver = configuredCarverSupplier.get();
                if(!CHECKED_WORLDGEN_OBJECTS.contains(configuredCarverSupplier)){

                    if (worldBlenderBiomes.get(0).getGenerationSettings().getCarvers(carverStage).stream().noneMatch(addedConfiguredCarver -> addedConfiguredCarver.get() == configuredCarver)) {

                        ResourceLocation configuredCarverID = configuredCarversRegistry.getKey(configuredCarver);
                        if(configuredCarverID == null){
                            configuredCarverID = WorldGenRegistries.CONFIGURED_CARVER.getKey(configuredCarver);
                        }

                        // Skip unregistered configuredfeatures
                        if(configuredCarverID == null){
                            continue;
                        }

                        // blacklisted by carver list
                        if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.CARVER, configuredCarverID)) {
                            continue;
                        }

                        if (configuredCarverID.getNamespace().equals("minecraft")) {
                            if (WorldBlender.WBBlendingConfig.allowVanillaCarvers.get())
                                worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getCarvers(carverStage).add(configuredCarverSupplier));
                        }
                        else if (WorldBlender.WBBlendingConfig.allowModdedCarvers.get()) {
                            worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getGenerationSettings().getCarvers(carverStage).add(configuredCarverSupplier));
                        }
                    }

                    CHECKED_WORLDGEN_OBJECTS.add(configuredCarverSupplier);
                }
            }
        }
    }


    private static void addBiomeNaturalMobs(Biome biome, List<Biome> worldBlenderBiomes) {
        for (EntityClassification spawnGroup : EntityClassification.values()) {
            for (MobSpawnInfo.Spawners spawnEntry : biome.getMobSpawnInfo().getSpawners(spawnGroup)) {

                if(!CHECKED_MOBS.contains(spawnEntry)){

                    if (worldBlenderBiomes.get(0).getMobSpawnInfo().getSpawners(spawnGroup).stream().noneMatch(spawn -> spawn.type == spawnEntry.type)) {
                        //no check needed for if entitytype is null because it is impossible for it to be null without Minecraft blowing up
                        ResourceLocation entityTypeID = Registry.ENTITY_TYPE.getKey(spawnEntry.type);

                        // blacklisted by natural spawn list
                        if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SPAWN, entityTypeID)) {
                            continue;
                        }

                        if (entityTypeID.getNamespace().equals("minecraft")) {
                            if (WorldBlender.WBBlendingConfig.allowVanillaSpawns.get())
                                worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getMobSpawnInfo().getSpawners(spawnGroup).add(spawnEntry));
                        } else if (WorldBlender.WBBlendingConfig.allowModdedSpawns.get()) {
                            worldBlenderBiomes.forEach(blendedBiome -> blendedBiome.getMobSpawnInfo().getSpawners(spawnGroup).add(spawnEntry));
                        }
                    }

                    CHECKED_MOBS.add(spawnEntry);
                }
            }
        }
    }


    private static void addBiomeSurfaceConfig(Biome biome, ResourceLocation biomeID) {

        // return early if biome's is turned off by the vanilla surface configs.
        if (biomeID.getNamespace().equals("minecraft")) {
            if (!WorldBlender.WBBlendingConfig.allowVanillaSurfaces.get())
                return;
        }
        else if (!WorldBlender.WBBlendingConfig.allowModdedSurfaces.get()) {
            return;
        }


        //A check to make sure we can safely cast
        if (biome.getGenerationSettings().getSurfaceBuilderConfig() instanceof SurfaceBuilderConfig) {
            SurfaceBuilderConfig surfaceConfig = (SurfaceBuilderConfig) biome.getGenerationSettings().getSurfaceBuilderConfig();

            // Blacklisted by surface list. Checks top block
            // Also do null check as BYG actually managed to set the surfaceConfig's block to be null lol
            if (surfaceConfig.getTop() == null || ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, Registry.BLOCK.getKey(surfaceConfig.getTop().getBlock()))) {
                return;
            }

            if (!((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER.get()).containsConfig(surfaceConfig)) {
                ((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER.get()).addConfig(surfaceConfig);
            }
        }
        else {
            //backup way to get the surface config (should always be safe as getSurfaceBuilderConfig always returns a ISurfaceBuilderConfig)
            //Downside is we cannot get the underwater block now.
            ISurfaceBuilderConfig surfaceConfig = biome.getGenerationSettings().getSurfaceBuilderConfig();

            // blacklisted by surface list. Checks top block
            // Check if null cause someone actually put null into this lmao
            if (surfaceConfig.getTop() == null || ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, Registry.BLOCK.getKey(surfaceConfig.getTop().getBlock()))) {
                return;
            }

            if (!((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER.get()).containsConfig(surfaceConfig)) {
                ((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER.get()).addConfig(surfaceConfig);
            }
        }
    }


    //--------------------------------------------------------------
    // An attempt to make sure we always have the spacing config for all structures

    private static Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>();

    public static void addDimensionalSpacing(final WorldEvent.Load event) {
        if(event.getWorld() instanceof ServerWorld){
            ServerWorld serverWorld = (ServerWorld)event.getWorld();

            // These maps map be immutable for some chunk generators. Our own won't be unless
            // someone messes with it. I take no chances so defensive programming incoming!
            tempMap.putAll(serverWorld.getChunkProvider().generator.func_235957_b_().func_236195_a_());

            ServerWorld wbServerWorld = serverWorld.getServer().getWorld(WBIdentifiers.WB_WORLD_KEY);
            if(wbServerWorld != null){

                // Grabs old entries already added into wb dimension
                tempMap.putAll(wbServerWorld.getChunkProvider().generator.func_235957_b_().func_236195_a_());

                // Dunno why someone would set null but we should check anyway
                tempMap.values().removeIf(Objects::isNull);

                // Set the structure spacing config in wb dimension and clear map so next saved world is fresh.
                ((DimensionStructureSettingsAccessor)wbServerWorld.getChunkProvider().generator.func_235957_b_()).wb_setStructureConfigMap(tempMap);
                tempMap = new HashMap<>(); // DO NOT DO .clear();  WE STORED THE MAP REFERENCE INTO THE CHUNKGENERATOR
            }
        }
    }

}
