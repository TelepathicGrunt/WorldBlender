package net.telepathicgrunt.worldblender.features;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.telepathicgrunt.worldblender.configs.WBConfig;


public class SeparateLavaAndWater extends Feature<NoFeatureConfig>
{

    public SeparateLavaAndWater(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory) {
	super(configFactory);
    }


    @Override
    public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> changedBlock, Random rand, BlockPos position, NoFeatureConfig config) {
	// this feature is completely turned off.
	if (!WBConfig.preventLavaTouchingWater) return false;

	BlockPos.Mutable mutable = new BlockPos.Mutable();
	BlockState currentBlockstate = Blocks.STONE.getDefaultState();
	BlockState neighboringBlockstate;

	for (int x = 0; x < 16; x++) {
	    for (int z = 0; z < 16; z++) {
		mutable = new BlockPos.Mutable(position.getX() + x, 0, position.getZ() + z);
		mutable.move(Direction.UP, Math.max(world.getHeight(Heightmap.Type.WORLD_SURFACE, mutable.getX(), mutable.getZ()), world.getSeaLevel()));

		// checks the column downward
		for (; mutable.getY() >= 0; mutable.move(Direction.DOWN)) {
		    currentBlockstate = world.getBlockState(mutable);

		    // current block is a water-tagged fluid
		    if (currentBlockstate.getFluidState().isTagged(FluidTags.LAVA)) {

			for (Direction face : Direction.values()) {
			    neighboringBlockstate = world.getBlockState(mutable.offset(face));
			    if (neighboringBlockstate.getFluidState().isTagged(FluidTags.WATER)) {
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
