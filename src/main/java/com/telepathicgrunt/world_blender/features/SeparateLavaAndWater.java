package com.telepathicgrunt.world_blender.features;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;


public class SeparateLavaAndWater extends Feature<DefaultFeatureConfig>
{

    public SeparateLavaAndWater() {
		super(DefaultFeatureConfig.CODEC);
    }


    @Override
    public boolean generate(StructureWorldAccess world, ChunkGenerator chunkgenerator, Random rand, BlockPos position, DefaultFeatureConfig config) {
		// this feature is completely turned off.
		if (!WorldBlender.WB_CONFIG.WBDimensionConfig.preventLavaTouchingWater) return false;

		BlockPos.Mutable mutable;
		BlockState currentBlockstate;
		BlockState neighboringBlockstate;

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				mutable = new BlockPos.Mutable(position.getX() + x, 0, position.getZ() + z);
				mutable.move(Direction.UP, Math.max(world.getTopY(Heightmap.Type.WORLD_SURFACE, mutable.getX(), mutable.getZ()), chunkgenerator.getSeaLevel()));

				// checks the column downward
				for (; mutable.getY() >= 0; mutable.move(Direction.DOWN)) {
					currentBlockstate = world.getBlockState(mutable);

					// current block is a water-tagged fluid
					if (currentBlockstate.getFluidState().isIn(FluidTags.LAVA)) {
						for (Direction face : Direction.values()) {
							neighboringBlockstate = world.getBlockState(mutable.offset(face));
							if (neighboringBlockstate.getFluidState().isIn(FluidTags.WATER)) {
								world.setBlockState(mutable, Blocks.OBSIDIAN.getDefaultState(), 2);
								break;
							}
						}
					}
				}
			}
		}

		return true;
    }
}
