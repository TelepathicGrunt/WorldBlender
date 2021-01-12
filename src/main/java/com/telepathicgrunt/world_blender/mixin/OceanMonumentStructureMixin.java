package com.telepathicgrunt.world_blender.mixin;

import com.telepathicgrunt.world_blender.generation.WBBiomeProvider;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.OceanMonumentStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(OceanMonumentStructure.class)
public class OceanMonumentStructureMixin {

    /**
     * @author TelepathicGrunt
     * @reason make Ocean Monuments skip their RIVER/OCEAN category checks if in World Blender's biome provider. Otherwise, Monuments don't spawn. Mojank lmao
     */
    @Inject(
            method = "func_230363_a_(Lnet/minecraft/world/gen/ChunkGenerator;Lnet/minecraft/world/biome/provider/BiomeProvider;JLnet/minecraft/util/SharedSeedRandom;IILnet/minecraft/world/biome/Biome;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/gen/feature/NoFeatureConfig;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/provider/BiomeProvider;getBiomes(IIII)Ljava/util/Set;", ordinal = 1),
            cancellable = true
    )
    private void modifyBiomeRegistry(ChunkGenerator chunkGenerator, BiomeProvider biomeProvider, 
                                     long seed, SharedSeedRandom random,
                                     int chunkX, int chunkZ, Biome biome,
                                     ChunkPos chunkPos, NoFeatureConfig config,
                                     CallbackInfoReturnable<Boolean> cir) 
    {
        if(biomeProvider instanceof WBBiomeProvider){
            cir.setReturnValue(true);
        }
    }
}
