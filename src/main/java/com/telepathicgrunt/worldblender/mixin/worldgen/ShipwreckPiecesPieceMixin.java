package com.telepathicgrunt.worldblender.mixin.worldgen;

import com.telepathicgrunt.worldblender.configs.WBDimensionConfigs;
import com.telepathicgrunt.worldblender.dimension.WBBiomeProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.ShipwreckPieces;
import net.minecraft.world.gen.feature.structure.StructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Random;


@Mixin(ShipwreckPieces.Piece.class)
public class ShipwreckPiecesPieceMixin {

    /**
     * @author TelepathicGrunt
     * @reason Prevent structures from being placed at world bottom if disallowed in World Blender's config
     */
    @Inject(
            method = "func_230383_a_(Lnet/minecraft/world/ISeedReader;Lnet/minecraft/world/gen/feature/structure/StructureManager;Lnet/minecraft/world/gen/ChunkGenerator;Ljava/util/Random;Lnet/minecraft/util/math/MutableBoundingBox;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/ISeedReader;getHeight(Lnet/minecraft/world/gen/Heightmap$Type;II)I", ordinal = 0),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void worldblender_disableHeightmapSnap(ISeedReader world, StructureManager structureAccessor,
                                                   ChunkGenerator chunkGenerator, Random random, MutableBoundingBox boundingBox,
                                                   ChunkPos chunkPos, BlockPos pos, CallbackInfoReturnable<Boolean> cir,
                                                   int i, int j)
    {
        if(WBDimensionConfigs.removeWorldBottomStructures.get() &&
                world.getWorld().getChunkProvider().getChunkGenerator().getBiomeProvider() instanceof WBBiomeProvider &&
                j <= 0)
        {
            cir.setReturnValue(false);
        }
    }

    /**
     * @author TelepathicGrunt
     * @reason Prevent structures from being placed at world bottom if disallowed in World Blender's config
     */
    @Inject(
            method = "func_230383_a_(Lnet/minecraft/world/ISeedReader;Lnet/minecraft/world/gen/feature/structure/StructureManager;Lnet/minecraft/world/gen/ChunkGenerator;Ljava/util/Random;Lnet/minecraft/util/math/MutableBoundingBox;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/ISeedReader;getHeight(Lnet/minecraft/world/gen/Heightmap$Type;II)I", ordinal = 1),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void worldblender_disableHeightmapSnap2(ISeedReader world, StructureManager structureAccessor,
                                                    ChunkGenerator chunkGenerator, Random random, MutableBoundingBox boundingBox,
                                                    ChunkPos chunkPos, BlockPos pos, CallbackInfoReturnable<Boolean> cir,
                                                    int i, int j, BlockPos pos2, Heightmap.Type type, int k, BlockPos blockPos,
                                                    Iterator<BlockPos> blockPosIterator, BlockPos blockPos2, int l)
    {
        if(WBDimensionConfigs.removeWorldBottomStructures.get() &&
                world.getWorld().getChunkProvider().getChunkGenerator().getBiomeProvider() instanceof WBBiomeProvider &&
                l <= 0)
        {
            cir.setReturnValue(false);
        }
    }
}
