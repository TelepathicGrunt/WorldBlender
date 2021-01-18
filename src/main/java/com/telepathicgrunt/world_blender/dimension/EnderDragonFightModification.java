package com.telepathicgrunt.world_blender.dimension;

import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.blocks.WBPortalBlockEntity;
import com.telepathicgrunt.world_blender.mixin.dimensions.EnderDragonFightAccessor;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.end.DragonFightManager;

public class EnderDragonFightModification {

    /*
     * Needed so that the portal check does not recognize a pattern in the
     * Bedrock floor of World Blender's dimension as if it is an End Podium.
     *
     * This was the cause of End Podium and Altar not spawning in WB dimension randomly.
     */
    public static BlockPattern.PatternHelper findEndPortal(DragonFightManager enderDragonFight, BlockPattern.PatternHelper blockPattern) {
        if(((EnderDragonFightAccessor)enderDragonFight).wb_getworld().getDimensionKey().equals(WBIdentifiers.WB_WORLD_KEY)){
            Chunk worldChunk = ((EnderDragonFightAccessor)enderDragonFight).wb_getworld().getChunk(0, 0);

            for(TileEntity blockEntity : worldChunk.getTileEntityMap().values()) {
                if (blockEntity instanceof WBPortalBlockEntity) {
                    if(!((WBPortalBlockEntity) blockEntity).isRemoveable()){
                        BlockPattern.PatternHelper blockpattern = ((EnderDragonFightAccessor)enderDragonFight).wb_getendPortalPattern().match(((EnderDragonFightAccessor)enderDragonFight).wb_getworld(), blockEntity.getPos());
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

            return null; // Skip checking the bedrock layer
        }

        return blockPattern;
    }
}
