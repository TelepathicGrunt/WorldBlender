package com.telepathicgrunt.worldblender.dimension;

import com.telepathicgrunt.worldblender.blocks.WBPortalBlockEntity;
import com.telepathicgrunt.worldblender.mixin.dimensions.EnderDragonFightAccessor;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraft.world.server.ServerWorld;

public class EnderDragonFightModification {

    /*
     * Needed so that the portal check does not recognize a pattern in the
     * Bedrock floor of World Blender's dimension as if it is an End Podium.
     *
     * This was the cause of End Podium and Altar not spawning in WB dimension randomly.
     */
    public static BlockPattern.PatternHelper findEndPortal(DragonFightManager enderDragonFight, BlockPattern.PatternHelper blockPattern) {
        ServerWorld world = ((EnderDragonFightAccessor)enderDragonFight).wb_getworld();
        Chunk worldChunk = world.getChunk(0, 0);

        for(TileEntity blockEntity : worldChunk.getTileEntityMap().values()) {
            if (blockEntity instanceof WBPortalBlockEntity) {
                if(!((WBPortalBlockEntity) blockEntity).isRemoveable()){
                    BlockPattern.PatternHelper blockpattern = ((EnderDragonFightAccessor)enderDragonFight).wb_getendPortalPattern().match(world, blockEntity.getPos().add(-3, 3, -3));
                    if (blockpattern != null) {
                        BlockPos blockpos = blockpattern.translateOffset(3, 7, 3).getPos();
                        if (((EnderDragonFightAccessor)enderDragonFight).wb_getexitPortalLocation() == null && blockpos.getX() == 0 && blockpos.getZ() == 0) {
                            ((EnderDragonFightAccessor)enderDragonFight).wb_setexitPortalLocation(blockpos);
                        }

                        return blockpattern;
                    }
                }
            }
        }

        int maxY = world.getHeight(Heightmap.Type.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION).getY();
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        // skip checking bedrock layer
        for(int currentY = maxY; currentY >= 10; --currentY) {
            BlockPattern.PatternHelper result2 = ((EnderDragonFightAccessor)enderDragonFight).wb_getendPortalPattern().match(world, mutable.setPos(EndPodiumFeature.END_PODIUM_LOCATION.getX(), currentY, EndPodiumFeature.END_PODIUM_LOCATION.getZ()));
            if (result2 != null) {
                if (((EnderDragonFightAccessor)enderDragonFight).wb_getexitPortalLocation() == null) {
                    ((EnderDragonFightAccessor)enderDragonFight).wb_setexitPortalLocation(result2.translateOffset(3, 3, 3).getPos());
                }

                return result2;
            }
        }

        return null;
    }
}
