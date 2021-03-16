package com.telepathicgrunt.worldblender.features;

import com.telepathicgrunt.worldblender.WorldBlender;
import com.telepathicgrunt.worldblender.dimension.WBWorldSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.*;


public class ItemClearingFeature extends Feature<NoFeatureConfig>
{

	public ItemClearingFeature()
	{
		super(NoFeatureConfig.field_236558_a_);
	}

	@Override
	public boolean generate(ISeedReader world, ChunkGenerator chunkgenerator, Random rand, BlockPos position, NoFeatureConfig config)
	{
		WBWorldSavedData.get(world.getWorld()).addTask(new WBWorldSavedData.ScheduledTask(new ChunkPos(position), 20));
		return true;
	}
}
