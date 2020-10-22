package com.telepathicgrunt.world_blender.the_blender;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.WorldBlender;
import com.telepathicgrunt.world_blender.features.WBConfiguredFeatures;
import com.telepathicgrunt.world_blender.mixin.*;
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
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class TheBlender {

    private static final Set<String> COLLECTED_UNREGISTERED_STUFF = new HashSet<>(); // not perfect but will try its best to show possible problematic mods.
    private static final Pattern WORLDGEN_OBJECT_REGEX = Pattern.compile("\"(?:Name|type|location)\": *\"([a-z_:]+)\"");

    /**
     * Kickstarts the blender. Should always be ran in MinecraftServer's init which is before the world is loaded
     */
    public static void blendTheWorld(DynamicRegistries.Impl registryManager){
        if(!registryManager.func_230521_a_(Registry.BIOME_KEY).isPresent()) return;

        List<Biome> world_blender_biomes = registryManager.func_230521_a_(Registry.BIOME_KEY).get().getEntries().stream()
                .filter(entry -> entry.getKey().func_240901_a_().getNamespace().equals(WorldBlender.MODID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        // Only world blender biomes will be mutable
        world_blender_biomes.forEach(TheBlender::makeBiomeMutable);


        // Reset these before biome loop
        ConfigBlacklisting.setupBlackLists();
        FeatureGrouping.setupFeatureMaps();
        BlendedSurfaceBuilder.resetSurfaceList();

        // THE biome loop. Very magical!
        for (Map.Entry<RegistryKey<Biome>, Biome> biomeEntry : registryManager.func_230521_a_(Registry.BIOME_KEY).get().getEntries()) {

            if(!biomeEntry.getKey().func_240901_a_().getNamespace().equals(WorldBlender.MODID)){
                // begin blending into our biomes
                TheBlender.mainBlending(
                        biomeEntry.getValue(), // Biome
                        world_blender_biomes, // WB biomes
                        biomeEntry.getKey().func_240901_a_(), // ResourceLocation
                        registryManager); // all the registries
            }
        }

        // wrap up the last bits that still needs to be blended but after the biome loop
        TheBlender.completeBlending(world_blender_biomes, registryManager.func_243612_b(Registry.field_243552_au));


        if(COLLECTED_UNREGISTERED_STUFF.size() != 0){
            // Add extra info to the log.
            String errorReport = "\n****************** World Blender ******************" +
                    "\n\n Found some unregistered ConfiguredFeatures, ConfiguredStructures, and/or" +
                    "\n ConfiguredCarvers. These unregistered stuff will not spawn in WorldBlender's dimension" +
                    "\n as unregistered stuff can wipe out everyone else's registered stuff from biomes." +
                    "\n The creators of those mods need to register their stuff." +
                    "\n Here are the following that will not show up in WB's dimension: \n\n" +
                    COLLECTED_UNREGISTERED_STUFF.stream().sorted().collect(Collectors.joining("\n")) + "\n\n";

            // Log it to the latest.log file.
            WorldBlender.LOGGER.log(Level.ERROR, errorReport);
        }

        // free up some memory when we are done and ready it for the next world clicked on.
        FeatureGrouping.clearFeatureMaps();
        COLLECTED_UNREGISTERED_STUFF.clear();
    }


    /**
     * Helper method to pull out the base feature/structure/carver name from the
     * stringified json of the unregistered form. Store result into COLLECTED_UNREGISTERED_STUFF
     */
    private static void extractModNames(String unconfigured_worldgen_object) {
        Matcher match = WORLDGEN_OBJECT_REGEX.matcher(unconfigured_worldgen_object);
        while(match.find()) {
            if(!match.group(1).contains("minecraft:")){
                COLLECTED_UNREGISTERED_STUFF.add(match.group(1));
            }
        }
    }

    /**
     * blends the given biome into WB biomes
     */
    private static void mainBlending(Biome biome, List<Biome> world_blender_biomes, ResourceLocation biomeID, DynamicRegistries.Impl dynamicRegistryManager) {

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
        addBiomeFeatures(biome, world_blender_biomes, dynamicRegistryManager.func_243612_b(Registry.field_243552_au));

        ////////////////////// STRUCTURES////////////////////////
        addBiomeStructures(biome, world_blender_biomes, dynamicRegistryManager.func_243612_b(Registry.field_243553_av));

        //////////////////////// CARVERS/////////////////////////
        addBiomeCarvers(biome, world_blender_biomes, dynamicRegistryManager.func_243612_b(Registry.field_243551_at));

        //////////////////////// SPAWNER/////////////////////////
        addBiomeNaturalMobs(biome, world_blender_biomes);

        //////////////////////// SURFACE/////////////////////////
        addBiomeSurfaceConfig(biome, biomeID);
    }


    /**
     * Adds the last bit of stuff that needs to be added to WB biomes after everything else is added.
     * Like bamboo and flowers should be dead last so they don't crowd out tree spawning
     */
    public static void completeBlending(List<Biome> world_blender_biomes, MutableRegistry<ConfiguredFeature<?, ?>> configuredFeaturesRegistry) {


        // add end spike directly to all biomes if not directly blacklisted. Turning off vanilla features will not prevent end spikes from spawning due to them marking the world origin nicely
        if (!ConfigBlacklisting.isResourceLocationBlacklisted(BlacklistType.FEATURE, new ResourceLocation("minecraft:end_spike")))
            world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242498_c().get(GenerationStage.Decoration.SURFACE_STRUCTURES.ordinal()).add(() -> configuredFeaturesRegistry.getOrDefault(new ResourceLocation("minecraft", "end_spike"))));


        // add grass, flower, and other small plants now so they are generated second to last
        for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
            for (ConfiguredFeature<?, ?> grassyFlowerFeature : FeatureGrouping.SMALL_PLANT_MAP.get(stage)) {
                if (world_blender_biomes.get(0).func_242440_e().func_242498_c().get(stage.ordinal()).stream().noneMatch(addedConfigFeature -> FeatureGrouping.serializeAndCompareFeature(addedConfigFeature.get(), grassyFlowerFeature, true))) {
                    world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242498_c().get(stage.ordinal()).add(() -> grassyFlowerFeature));
                }
            }
        }


        if (!WorldBlender.WBBlendingConfig.disallowLaggyFeatures.get() && FeatureGrouping.bambooFound) {
            // add 1 configured bamboo so it is dead last
            world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242498_c().get(GenerationStage.Decoration.VEGETAL_DECORATION.ordinal()).add(() -> configuredFeaturesRegistry.getOrDefault(new ResourceLocation("minecraft", "bamboo"))));
        }


        // Makes carvers be able to carve any underground blocks including netherrack, end stone, and modded blocks
        if (WorldBlender.WBDimensionConfig.carversCanCarveMoreBlocks.get()) {
            Set<Block> allBlocksToCarve = BlendedSurfaceBuilder.blocksToCarve();

            //get all carvable blocks
            for (GenerationStage.Carving carverStage : GenerationStage.Carving.values()) {
                for (Supplier<ConfiguredCarver<?>> carver : world_blender_biomes.get(0).func_242440_e().func_242489_a(carverStage)) {
                    allBlocksToCarve.addAll(((CarverAccessor)((ConfiguredCarverAccessor)carver.get()).getcarver()).getalwaysCarvableBlocks());
                }
            }

            //update all carvers to carve the complete list of stuff to carve
            for (GenerationStage.Carving carverStage : GenerationStage.Carving.values()) {
                for (Supplier<ConfiguredCarver<?>> carver : world_blender_biomes.get(0).func_242440_e().func_242489_a(carverStage)) {
                    ((CarverAccessor)((ConfiguredCarverAccessor)carver.get()).getcarver()).setalwaysCarvableBlocks(allBlocksToCarve);
                }
            }
        }

        // add these last so that this can contain other local modification feature's liquids/falling blocks better
        if (!ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, new ResourceLocation("world_blender:no_floating_liquids_or_falling_blocks"))) {
            world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242498_c().get(GenerationStage.Decoration.LOCAL_MODIFICATIONS.ordinal()).add(() -> WBConfiguredFeatures.NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS));
        }

        if (!ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.FEATURE, new ResourceLocation("world_blender:separate_lava_and_water"))) {
            world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242498_c().get(GenerationStage.Decoration.LOCAL_MODIFICATIONS.ordinal()).add(() -> WBConfiguredFeatures.SEPARATE_LAVA_AND_WATER));
        }
    }

    /**
     * Helper method to make WB biomes mutable to add stuff to it later
     */
    private static void makeBiomeMutable(Biome biome){
        // Make the structure and features list mutable for modification late
        List<List<Supplier<ConfiguredFeature<?, ?>>>> tempFeature = ((GenerationSettingsAccessor)biome.func_242440_e()).getGSFeatures();
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
        ((GenerationSettingsAccessor)biome.func_242440_e()).setGSFeatures(mutableGenerationStages);
        ((GenerationSettingsAccessor)biome.func_242440_e()).setGSStructureFeatures(new ArrayList<>(((GenerationSettingsAccessor)biome.func_242440_e()).getGSStructureFeatures()));
        ((GenerationSettingsAccessor)biome.func_242440_e()).setGSStructureFeatures(new ArrayList<>(((GenerationSettingsAccessor)biome.func_242440_e()).getGSStructureFeatures()));

        ((GenerationSettingsAccessor)biome.func_242440_e()).setCarvers(new HashMap<>(((GenerationSettingsAccessor)biome.func_242440_e()).getCarvers()));
        for(Carving carverGroup : Carving.values()){
            ((GenerationSettingsAccessor) biome.func_242440_e()).getCarvers().put(carverGroup, new ArrayList<>(biome.func_242440_e().func_242489_a(carverGroup)));
        }

        ((MobSpawnInfoAccessor)biome.func_242433_b()).setSpawners(new HashMap<>(((MobSpawnInfoAccessor) biome.func_242433_b()).getSpawners()));
        for(EntityClassification spawnGroup : EntityClassification.values()){
            ((MobSpawnInfoAccessor) biome.func_242433_b()).getSpawners().put(spawnGroup, new ArrayList<>(biome.func_242433_b().func_242559_a(spawnGroup)));
        }

        ((MobSpawnInfoAccessor)biome.func_242433_b()).setSpawnCosts(new HashMap<>(((MobSpawnInfoAccessor) biome.func_242433_b()).getSpawnCosts()));
    }


    //--------------------------------------------------------------
            // The actual main blending below
            // Welcome to hell!


    private static void addBiomeFeatures(Biome biome, List<Biome> world_blender_biomes, MutableRegistry<ConfiguredFeature<?, ?>> configuredFeaturesRegistry) {
        for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
            if(stage.ordinal() >= biome.func_242440_e().func_242498_c().size()) break;

            for (Supplier<ConfiguredFeature<?, ?>> configuredFeatureSupplier : biome.func_242440_e().func_242498_c().get(stage.ordinal())) {
                ConfiguredFeature<?, ?> configuredFeature = configuredFeatureSupplier.get();
                if (world_blender_biomes.get(0).func_242440_e().func_242498_c().get(stage.ordinal()).stream().noneMatch(addedConfigFeature -> FeatureGrouping.serializeAndCompareFeature(addedConfigFeature.get(), configuredFeatureSupplier.get(), true))) {

                    ResourceLocation configuredFeatureID = configuredFeaturesRegistry.getKey(configuredFeature);
                    if(configuredFeatureID == null){
                        configuredFeatureID = WorldGenRegistries.field_243653_e.getKey(configuredFeature);
                    }

                    if(configuredFeatureID == null){
                        Optional<JsonElement> configuredFeatureJSON = ConfiguredFeature.field_236264_b_.encode(configuredFeatureSupplier, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();
                        configuredFeatureJSON.ifPresent(json -> extractModNames(json.toString()));
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
                                world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242498_c().get(stage.ordinal()).add(configuredFeatureSupplier));
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
                            world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242498_c().get(stage.ordinal()).add(0, configuredFeatureSupplier));
                        }
                        else {
                            // cannot be a bamboo feature as we will place them dead last in the feature
                            // list so they don't overwhelm other features or cause as many bamboo breaking
                            // because it got cut off
                            // if we have no laggy feature config on, then the feature must not be fire, lava, bamboo, etc in order to be added
                            if ((!FeatureGrouping.isLaggyFeature(configuredFeature) || !WorldBlender.WBBlendingConfig.disallowLaggyFeatures.get())) {
                                world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242498_c().get(stage.ordinal()).add(configuredFeatureSupplier));
                            }
                        }
                    }
                }
            }
        }
    }

    private static void addBiomeStructures(Biome biome, List<Biome> world_blender_biomes, MutableRegistry<StructureFeature<?, ?>> configuredStructuresRegistry) {
        for (Supplier<StructureFeature<?, ?>> configuredStructureSupplier : biome.func_242440_e().func_242487_a()) {
            StructureFeature<?, ?> configuredStructure = configuredStructureSupplier.get();

            // Having multiple configured structures of the same structure spawns only the last one it seems. Booo mojang boooooo. I want multiple village types in 1 biome!
            if (world_blender_biomes.get(0).func_242440_e().func_242487_a().stream().noneMatch(addedConfiguredStructure -> addedConfiguredStructure.get().field_236268_b_ == configuredStructure.field_236268_b_)) {

                // Have to do this computing as the feature in the registry is technically not the same
                // object as the feature in the biome. So I cannot get ID easily from the registry.
                // Instead, I have to check the JSON of the feature to find a match and store the ID of it
                // into a temporary map as a cache for later biomes.
                ResourceLocation configuredStructureID = configuredStructuresRegistry.getKey(configuredStructure);
                if(configuredStructureID == null){
                    configuredStructureID = WorldGenRegistries.field_243654_f.getKey(configuredStructure);
                }

                if(configuredStructureID == null){
                    Optional<JsonElement> configuredStructureJSON = StructureFeature.field_236267_a_.encode(configuredStructure, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();
                    configuredStructureJSON.ifPresent(json -> extractModNames(json.toString()));
                    continue;
                }

                // blacklisted by structure list
                if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.STRUCTURE, configuredStructureID)) {
                    continue;
                }

                if (configuredStructureID.getNamespace().equals("minecraft")) {
                    if (WorldBlender.WBBlendingConfig.allowVanillaStructures.get()) {
                        // add the structure version of the structure
                        world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242487_a().add(configuredStructureSupplier));
                    }
                }
                else if (WorldBlender.WBBlendingConfig.allowModdedStructures.get()) {
                    // add the structure version of the structure
                    world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242487_a().add(configuredStructureSupplier));
                }
            }
        }
    }


    private static void addBiomeCarvers(Biome biome, List<Biome> world_blender_biomes, MutableRegistry<ConfiguredCarver<?>> configuredCarversRegistry) {
        for (Carving carverStage : GenerationStage.Carving.values()) {
            for (Supplier<ConfiguredCarver<?>> configuredCarverSupplier : biome.func_242440_e().func_242489_a(carverStage)) {
                ConfiguredCarver<?> configuredCarver = configuredCarverSupplier.get();
                if (world_blender_biomes.get(0).func_242440_e().func_242489_a(carverStage).stream().noneMatch(addedConfiguredCarver -> addedConfiguredCarver.get() == configuredCarver)) {

                    ResourceLocation configuredCarverID = configuredCarversRegistry.getKey(configuredCarver);
                    if(configuredCarverID == null){
                        configuredCarverID = WorldGenRegistries.field_243652_d.getKey(configuredCarver);
                    }

                    if(configuredCarverID == null){
                        Optional<JsonElement> configuredCarverJSON = ConfiguredCarver.field_236235_a_.encode(configuredCarver, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();
                        configuredCarverJSON.ifPresent(json -> extractModNames(json.toString()));
                        continue;
                    }

                    // blacklisted by carver list
                    if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.CARVER, configuredCarverID)) {
                        continue;
                    }

                    if (configuredCarverID.getNamespace().equals("minecraft")) {
                        if (WorldBlender.WBBlendingConfig.allowVanillaCarvers.get())
                            world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242489_a(carverStage).add(configuredCarverSupplier));
                    }
                    else if (WorldBlender.WBBlendingConfig.allowModdedCarvers.get()) {
                        world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242440_e().func_242489_a(carverStage).add(configuredCarverSupplier));
                    }
                }
            }
        }
    }


    private static void addBiomeNaturalMobs(Biome biome, List<Biome> world_blender_biomes) {
        for (EntityClassification spawnGroup : EntityClassification.values()) {
            for (MobSpawnInfo.Spawners spawnEntry : biome.func_242433_b().func_242559_a(spawnGroup)) {
                if (world_blender_biomes.get(0).func_242433_b().func_242559_a(spawnGroup).stream().noneMatch(spawn -> spawn.field_242588_c == spawnEntry.field_242588_c)) {

                    //no check needed for if entitytype is null because it is impossible for it to be null without Minecraft blowing up
                    ResourceLocation entityTypeID = Registry.ENTITY_TYPE.getKey(spawnEntry.field_242588_c);

                    // blacklisted by natural spawn list
                    if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SPAWN, entityTypeID)) {
                        continue;
                    }

                    if (entityTypeID.getNamespace().equals("minecraft")) {
                        if (WorldBlender.WBBlendingConfig.allowVanillaSpawns.get())
                            world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242433_b().func_242559_a(spawnGroup).add(spawnEntry));
                    } else if (WorldBlender.WBBlendingConfig.allowModdedSpawns.get()) {
                        world_blender_biomes.forEach(blendedBiome -> blendedBiome.func_242433_b().func_242559_a(spawnGroup).add(spawnEntry));
                    }
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
        if (biome.func_242440_e().func_242502_e() instanceof SurfaceBuilderConfig) {
            SurfaceBuilderConfig surfaceConfig = (SurfaceBuilderConfig) biome.func_242440_e().func_242502_e();

            // Blacklisted by surface list. Checks top block
            // Also do null check as BYG actually managed to set the surfaceConfig's block to be null lol
            if (surfaceConfig.getTop() == null || ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, Registry.BLOCK.getKey(surfaceConfig.getTop().getBlock()))) {
                return;
            }

            if (!((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER).containsConfig(surfaceConfig)) {
                ((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER).addConfig(surfaceConfig);
            }
        }
        else {
            //backup way to get the surface config (should always be safe as getSurfaceBuilderConfig always returns a ISurfaceBuilderConfig)
            //Downside is we cannot get the underwater block now.
            ISurfaceBuilderConfig surfaceConfig = biome.func_242440_e().func_242502_e();

            // blacklisted by surface list. Checks top block
            if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, Registry.BLOCK.getKey(surfaceConfig.getTop().getBlock()))) {
                return;
            }

            if (!((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER).containsConfig(surfaceConfig)) {
                ((BlendedSurfaceBuilder) WBSurfaceBuilders.BLENDED_SURFACE_BUILDER).addConfig(surfaceConfig);
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
                ((DimensionStructureSettingsAccessor)wbServerWorld.getChunkProvider().generator.func_235957_b_()).setStructureConfigMap(tempMap);
                tempMap = new HashMap<>(); // DO NOT DO .clear();  WE STORED THE MAP REFERENCE INTO THE CHUNKGENERATOR
            }
        }
    }

}
