package com.telepathicgrunt.world_blender.features;

import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.WorldBlender;
import com.telepathicgrunt.world_blender.blocks.WBBlocks;
import com.telepathicgrunt.world_blender.blocks.WBPortalBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;


public class WBPortalAltar extends Feature<DefaultFeatureConfig>
{
	public static Structure ALTAR_TEMPLATE;
	private static final StructurePlacementData placementSettings = (new StructurePlacementData())
																			.setMirror(BlockMirror.NONE)
																			.setRotation(BlockRotation.NONE)
																			.setIgnoreEntities(false)
																			.setChunkPosition(null);
	
	public WBPortalAltar()
	{
		super(DefaultFeatureConfig.CODEC);
	}
	

	@Override
	public boolean generate(StructureWorldAccess world, ChunkGenerator chunkgenerator, Random rand, BlockPos position, DefaultFeatureConfig config)
	{
		//only world origin chunk allows generation
		if (world.toServerWorld().getRegistryKey() != WBIdentifiers.WB_WORLD_KEY ||
				position.getX() >> 4 != 0 ||
				position.getZ() >> 4 != 0)
		{
			return false;
		}

		if (ALTAR_TEMPLATE == null)
		{
			WorldBlender.LOGGER.warn("world blender portal altar NTB does not exist!");
			return false;
		}
		
		BlockPos.Mutable finalPosition = new BlockPos.Mutable().set(world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, position));
		
		//go past trees to world surface
		BlockState blockState = world.getBlockState(finalPosition);
		while(finalPosition.getY() > 12 && (!blockState.isOpaque() || blockState.getMaterial() == Material.WOOD)) {
			finalPosition.move(Direction.DOWN);
			blockState = world.getBlockState(finalPosition);
		}

		finalPosition.move(Direction.UP);
		world.setBlockState(finalPosition.down(), Blocks.AIR.getDefaultState(), 3);
		ALTAR_TEMPLATE.placeAndNotifyListeners(world, finalPosition.add(-5, -2, -5), placementSettings, rand);
		finalPosition.move(Direction.DOWN);
		world.setBlockState(finalPosition, WBBlocks.WORLD_BLENDER_PORTAL.getDefaultState(), 3); //extra check to make sure portal is placed

		//make portal block unremoveable in altar
		BlockEntity blockEntity = world.getBlockEntity(finalPosition);
		if(blockEntity instanceof WBPortalBlockEntity)
			((WBPortalBlockEntity)blockEntity).makeNotRemoveable();
		
		return true;

	}

}
