package com.telepathicgrunt.world_blender.mixin;

import com.telepathicgrunt.world_blender.dimension.ChunkGeneratorBehavior;
import com.telepathicgrunt.world_blender.dimension.WBBiomeProvider;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    /**
     * Picks a random ConfiguredStructure if WB biome has a Structure with multiple forms in it.
     * @author TelepathicGrunt
     * @reason Prevents multiple ConfiguredStructures with same Structure base in a biome spawning only 1 as a result
     */
    @Inject(
            method = "func_242705_a(Lnet/minecraft/world/gen/feature/StructureFeature;Lnet/minecraft/util/registry/DynamicRegistries;Lnet/minecraft/world/gen/feature/structure/StructureManager;Lnet/minecraft/world/chunk/IChunk;Lnet/minecraft/world/gen/feature/template/TemplateManager;JLnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/biome/Biome;)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void generateAllConfiguredStructures(StructureFeature<?, ?> configuredStructureFeature,
                                                 DynamicRegistries dynamicRegistryManager,
                                                 StructureManager structureAccessor, IChunk chunk,
                                                 TemplateManager structureManager, long worldSeed,
                                                 ChunkPos chunkPos, Biome biome, CallbackInfo ci)
    {
        if (((ChunkGeneratorAccessor) this).getBiomeSource() instanceof WBBiomeProvider) {
            if (ChunkGeneratorBehavior.placeAllConfiguredStructures(
            		((ChunkGenerator) (Object) this), configuredStructureFeature, dynamicRegistryManager,
                    structureAccessor, chunk, structureManager, worldSeed, chunkPos, biome))
            {
                ci.cancel();
            }
        }
    }
}
