package com.telepathicgrunt.world_blender.features;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.*;


public class NoFloatingLiquidsOrFallingBlocks extends Feature<NoFeatureConfig>
{

	public NoFloatingLiquidsOrFallingBlocks()
	{
		super(NoFeatureConfig.field_236558_a_);
	}

	private static final Map<MaterialColor, Block> COLOR_MAP;
	static {
		COLOR_MAP = new HashMap<>();
		COLOR_MAP.put(MaterialColor.AIR, Blocks.TERRACOTTA);
		COLOR_MAP.put(MaterialColor.ADOBE, Blocks.ORANGE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.BLACK, Blocks.BLACK_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.BLUE, Blocks.BLUE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.BROWN, Blocks.BROWN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.CLAY, Blocks.CYAN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.CYAN, Blocks.CYAN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.DIAMOND, Blocks.LIGHT_BLUE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.DIRT, Blocks.BROWN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.EMERALD, Blocks.GREEN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.FOLIAGE, Blocks.GREEN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.GOLD, Blocks.YELLOW_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.GRASS, Blocks.GREEN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.GRAY, Blocks.GRAY_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.GREEN, Blocks.GREEN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.ICE, Blocks.LIGHT_BLUE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.IRON, Blocks.WHITE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.LAPIS, Blocks.BLUE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.LIME, Blocks.LIME_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.MAGENTA, Blocks.MAGENTA_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.NETHERRACK, Blocks.RED_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.OBSIDIAN, Blocks.BLACK_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.PINK, Blocks.PINK_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.PURPLE, Blocks.PURPLE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.QUARTZ, Blocks.WHITE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.RED, Blocks.RED_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.SAND, Blocks.WHITE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.SNOW, Blocks.WHITE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.STONE, Blocks.CYAN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.TNT, Blocks.RED_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.WATER, Blocks.LIGHT_BLUE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.WOOD, Blocks.TERRACOTTA);
		COLOR_MAP.put(MaterialColor.WOOL, Blocks.WHITE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.YELLOW, Blocks.YELLOW_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.ORANGE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.BLACK_TERRACOTTA, Blocks.BLACK_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.BLUE_TERRACOTTA, Blocks.BLUE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.BROWN_TERRACOTTA, Blocks.BROWN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.CYAN_TERRACOTTA, Blocks.CYAN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.LIGHT_BLUE_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.GREEN_TERRACOTTA, Blocks.GREEN_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.GRAY_TERRACOTTA, Blocks.GRAY_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.LIGHT_GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.LIME_TERRACOTTA, Blocks.LIME_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.MAGENTA_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.RED_TERRACOTTA, Blocks.RED_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.YELLOW_TERRACOTTA, Blocks.YELLOW_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.PINK_TERRACOTTA, Blocks.PINK_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.PURPLE_TERRACOTTA, Blocks.PURPLE_TERRACOTTA);
	}
	
	private static final Set<Material> REPLACEABLE_MATERIALS;
	static {
		REPLACEABLE_MATERIALS = new HashSet<>();
		REPLACEABLE_MATERIALS.add(Material.AIR);
		REPLACEABLE_MATERIALS.add(Material.STRUCTURE_VOID);
		REPLACEABLE_MATERIALS.add(Material.TALL_PLANTS);
		REPLACEABLE_MATERIALS.add(Material.CARPET);
		REPLACEABLE_MATERIALS.add(Material.CACTUS);
	}

	@Override
	public boolean func_241855_a(ISeedReader world, ChunkGenerator chunkgenerator, Random rand, BlockPos position, NoFeatureConfig config)
	{
		//this feature is completely turned off.
		if(!WorldBlender.WBDimensionConfig.preventFallingBlocks.get() &&
				!WorldBlender.WBDimensionConfig.containFloatingLiquids.get())
			return false;
		
		BlockPos.Mutable mutable;
		BlockState currentBlockstate;
		BlockState lastBlockstate = Blocks.STONE.getDefaultState();
		
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{
				mutable = new BlockPos.Mutable(position.getX() + x, 0, position.getZ() + z);
				mutable.move(Direction.UP, Math.max(world.getHeight(Heightmap.Type.WORLD_SURFACE, mutable.getX(), mutable.getZ()), chunkgenerator.func_230356_f_()));
				
				//checks the column downward
				for(; mutable.getY() >= 0; mutable.move(Direction.DOWN))
				{
					currentBlockstate = world.getBlockState(mutable);
					
					//current block is a block that liquids can break. time to check if we need to replace this block
					if(REPLACEABLE_MATERIALS.contains(currentBlockstate.getMaterial()))
					{
						//if above block was a fallible block, place a solid block below
						preventfalling(world, mutable, lastBlockstate);
						
						//if neighboring block is a liquid block, place a solid block next to it
						liquidContaining(world, mutable, lastBlockstate);
					}
					else if(currentBlockstate.getMaterial() == Material.WATER || currentBlockstate.getMaterial() == Material.LAVA)
					{
						//if above block was a fallible block, place a solid block below
						preventfalling(world, mutable, lastBlockstate);
					}
					
					//saves our current block to the last blockstate before we move down one.
					lastBlockstate = currentBlockstate;
				}
			}
		}
		
		return true;

	}

	/**
	 * Will place Terracotta block at mutable position if above block is a FallingBlock
	 * @param world - world we are in
	 * @param mutable - current position
	 * @param lastBlockstate - must be the above blockstate when passed in
	 */
	private static void preventfalling(ISeedReader world, BlockPos.Mutable mutable, BlockState lastBlockstate)
	{
		if(!WorldBlender.WBDimensionConfig.preventFallingBlocks.get()) return;
		
		if(lastBlockstate.getBlock() instanceof FallingBlock)
		{
			MaterialColor targetMaterial = lastBlockstate.getMaterialColor(world, mutable);
			if(targetMaterial == null || !COLOR_MAP.containsKey(targetMaterial))
			{
				world.setBlockState(mutable, Blocks.CYAN_TERRACOTTA.getDefaultState(), 2);
			}
			else
			{
				world.setBlockState(mutable, COLOR_MAP.get(targetMaterial).getDefaultState(), 2);
			}
		}
	}
	
	
	/**
	 * Will place terracotta block at mutable position if above, north, west, east, or south is a liquid block
	 * @param world - world we are in
	 * @param mutable - current position
	 * @param lastBlockstate - must be the above blockstate when passed in
	 */
	private static void liquidContaining(ISeedReader world, BlockPos.Mutable mutable, BlockState lastBlockstate)
	{
		if(!WorldBlender.WBDimensionConfig.containFloatingLiquids.get()) return;
		
		boolean touchingLiquid = false;
		BlockState neighboringBlockstate = null;
		

		//if above is liquid, we need to contain it
		if(!lastBlockstate.getFluidState().isEmpty())
		{
			touchingLiquid = true;
			neighboringBlockstate = lastBlockstate;
		}
		//if side is liquid, we need to contain it
		else
		{
			for(Direction face : Direction.Plane.HORIZONTAL)
			{
				neighboringBlockstate = world.getBlockState(mutable.offset(face));
				if(!neighboringBlockstate.getFluidState().isEmpty())
				{
					touchingLiquid = true;
					break;
				}
			}
		}
		
		
		if(touchingLiquid)
		{
			MaterialColor targetMaterial = neighboringBlockstate.getMaterialColor(world, mutable);
			if(targetMaterial == null || !COLOR_MAP.containsKey(targetMaterial))
			{
				world.setBlockState(mutable, Blocks.CYAN_TERRACOTTA.getDefaultState(), 2);
			}
			else
			{
				world.setBlockState(mutable, COLOR_MAP.get(targetMaterial).getDefaultState(), 2);
			}
		}
	}


}
