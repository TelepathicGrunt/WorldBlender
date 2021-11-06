package com.telepathicgrunt.worldblender.mixin.worldgen;

import com.telepathicgrunt.worldblender.configs.WBDimensionConfigs;
import com.telepathicgrunt.worldblender.dimension.WBBiomeProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.ScatteredStructurePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(ScatteredStructurePiece.class)
public abstract class ScatteredStructurePieceMixin extends StructurePiece {

    protected ScatteredStructurePieceMixin(IStructurePieceType structurePieceTypeIn, CompoundNBT nbt) {
        super(structurePieceTypeIn, nbt);
    }

    /**
     * @author TelepathicGrunt
     * @reason Prevent structures from being placed at world bottom if disallowed in World Blender's config
     */
    @Inject(
            method = "isInsideBounds(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/MutableBoundingBox;I)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IWorld;getHeight(Lnet/minecraft/world/gen/Heightmap$Type;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void worldblender_disableHeightmapSnap(IWorld world, MutableBoundingBox boundingBox, int height, CallbackInfoReturnable<Boolean> cir,
                                                   int j, int k, BlockPos.Mutable mutable)
    {
        if(WBDimensionConfigs.removeWorldBottomStructures.get() &&
                world instanceof WorldGenRegion &&
                ((WorldGenRegion)world).getWorld().getChunkProvider().getChunkGenerator().getBiomeProvider() instanceof WBBiomeProvider &&
                world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, mutable).getY() <= 0)
        {
            cir.setReturnValue(false);
        }
    }
}
