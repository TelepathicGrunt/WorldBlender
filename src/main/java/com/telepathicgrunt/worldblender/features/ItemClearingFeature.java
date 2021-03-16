package com.telepathicgrunt.worldblender.features;

import com.telepathicgrunt.worldblender.WorldBlender;
import com.telepathicgrunt.worldblender.blocks.WBBlocks;
import com.telepathicgrunt.worldblender.dimension.WBWorldSavedData;
import com.telepathicgrunt.worldblender.entities.ItemClearingEntity;
import com.telepathicgrunt.worldblender.entities.WBEntities;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
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
		ItemClearingEntity itemClearingEntity = WBEntities.ITEM_CLEARING_ENTITY.get().create(world.getWorld());
		if(itemClearingEntity == null){
			WorldBlender.LOGGER.warn("Error with spawning clearing item entity at: ({}, {}, {})", position.getX(), position.getY(), position.getZ());
			return false;
		}
		itemClearingEntity.setLocationAndAngles((double)position.getX() + 0.5D, 255, (double)position.getZ() + 0.5D, 0.0F, 0.0F);
		world.func_242417_l(itemClearingEntity);
		return true;
	}
}
