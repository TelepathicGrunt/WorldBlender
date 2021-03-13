package com.telepathicgrunt.worldblender.dimension;

import com.telepathicgrunt.worldblender.mixin.worldgen.ChunkGeneratorAccessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.StructureSeparationSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public class ChunkGeneratorBehavior {

    private static final Map<Biome, Map<Structure<?>, List<StructureFeature<?, ?>>>> MULTIPLE_CONFIGURED_STRUCTURE_BIOMES = new Reference2ObjectOpenHashMap<>();

    public static boolean placeAllConfiguredStructures(ChunkGenerator chunkGenerator,
                                                       StructureFeature<?, ?> configuredStructureFeature,
                                                       DynamicRegistries dynamicRegistryManager,
                                                       StructureManager structureAccessor, IChunk chunk,
                                                       TemplateManager structureManager, long worldSeed,
                                                       ChunkPos chunkPos, Biome biome)
    {
        // Need to create list of structures for this biome
        if(!MULTIPLE_CONFIGURED_STRUCTURE_BIOMES.containsKey(biome)){
            MULTIPLE_CONFIGURED_STRUCTURE_BIOMES.put(biome, new Reference2ObjectOpenHashMap<>());
            Map<Structure<?>, List<StructureFeature<?, ?>>> structureMap = MULTIPLE_CONFIGURED_STRUCTURE_BIOMES.get(biome);

            // Stores all configuredforms of a structures in a list for random access later
            for(Supplier<StructureFeature<?, ?>> supplierCS : biome.getGenerationSettings().getStructures()){
                Structure<?> structure = supplierCS.get().field_236268_b_;
                if(!structureMap.containsKey(structure)){
                    structureMap.put(structure, new ArrayList<>());
                }
                structureMap.get(structure).add(supplierCS.get());
            }
        }


        if(MULTIPLE_CONFIGURED_STRUCTURE_BIOMES.get(biome).containsKey(configuredStructureFeature.field_236268_b_)){
            StructureStart<?> structureStart = structureAccessor.getStructureStart(SectionPos.from(chunk.getPos(), 0), configuredStructureFeature.field_236268_b_, chunk);
            int ref = structureStart != null ? structureStart.getRefCount() : 0;
            StructureSeparationSettings structureConfig = chunkGenerator.func_235957_b_().func_236197_a_(configuredStructureFeature.field_236268_b_);
            if (structureConfig != null) {
                List<StructureFeature<?, ?>> randomStructureList = MULTIPLE_CONFIGURED_STRUCTURE_BIOMES.get(biome).get(configuredStructureFeature.field_236268_b_);

                Random random = new Random();
                random.setSeed(worldSeed + chunkPos.asLong());

                StructureStart<?> structureStart2 = randomStructureList.get(random.nextInt(randomStructureList.size()))
                        .func_242771_a(
                            dynamicRegistryManager,
                            chunkGenerator,
                            ((ChunkGeneratorAccessor)chunkGenerator).wb_getPopulationSource(),
                            structureManager,
                            worldSeed, chunkPos,
                            biome,
                            ref,
                            structureConfig);

                structureAccessor.addStructureStart(SectionPos.from(chunk.getPos(), 0), configuredStructureFeature.field_236268_b_, structureStart2, chunk);
            }

            return true;
        }

        return false;
    }

}
