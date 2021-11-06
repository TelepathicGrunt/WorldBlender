package com.telepathicgrunt.worldblender.mixin.worldgen;

import com.telepathicgrunt.worldblender.configs.WBDimensionConfigs;
import com.telepathicgrunt.worldblender.dimension.WBBiomeProvider;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(StructurePiece.class)
public class StructurePieceMixin {

    @Shadow
    protected int getXWithOffset(int x, int z) {
        return 0;
    }

    @Shadow
    protected int getZWithOffset(int x, int z) {
        return 0;
    }

    /**
     * @author TelepathicGrunt
     * @reason Prevent some structures from placing pillars if disallowed in World Blender's config
     */
    @Inject(
            method = "replaceAirAndLiquidDownwards(Lnet/minecraft/world/ISeedReader;Lnet/minecraft/block/BlockState;IIILnet/minecraft/util/math/MutableBoundingBox;)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void worldblender_disablePillars(ISeedReader world, BlockState blockstate, int x, int y, int z, MutableBoundingBox boundingbox, CallbackInfo ci)
    {
        if(WBDimensionConfigs.removeStructurePillars.get() &&
                world.getWorld().getChunkProvider().getChunkGenerator().getBiomeProvider() instanceof WBBiomeProvider)
        {
            int heightmapY = world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, getXWithOffset(x, z), getZWithOffset(x, z));
            if(heightmapY <= 2){
                ci.cancel();
            }
        }
    }
}
