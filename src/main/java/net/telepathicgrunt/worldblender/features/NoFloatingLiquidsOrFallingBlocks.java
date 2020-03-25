package net.telepathicgrunt.worldblender.features;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.telepathicgrunt.worldblender.configs.WBConfig;


public class NoFloatingLiquidsOrFallingBlocks extends Feature<NoFeatureConfig>
{

	public NoFloatingLiquidsOrFallingBlocks(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory)
	{
		super(configFactory);
	}

	private static final Map<MaterialColor, Block> COLOR_MAP;
	static {
		COLOR_MAP = new HashMap<MaterialColor, Block>();
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
		COLOR_MAP.put(MaterialColor.ORANGE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.PINK_TERRACOTTA, Blocks.PINK_TERRACOTTA);
		COLOR_MAP.put(MaterialColor.PURPLE_TERRACOTTA, Blocks.PURPLE_TERRACOTTA);
	}
	
	private static final Set<Material> REPLACEABLE_MATERIALS;
	static {
		REPLACEABLE_MATERIALS = new HashSet<Material>();
		REPLACEABLE_MATERIALS.add(Material.AIR);
		REPLACEABLE_MATERIALS.add(Material.STRUCTURE_VOID);
		REPLACEABLE_MATERIALS.add(Material.TALL_PLANTS);
		REPLACEABLE_MATERIALS.add(Material.CARPET);
		REPLACEABLE_MATERIALS.add(Material.CACTUS);
	}

	@Override
	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> changedBlock, Random rand, BlockPos position, NoFeatureConfig config)
	{
		//this feature is turned off.
		if(!WBConfig.preventFallingBlocks && !WBConfig.containFloatingLiquids)
			return false;
		
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		BlockState currentBlockstate = Blocks.STONE.getDefaultState();
		BlockState lastBlockstate = Blocks.STONE.getDefaultState();
		
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{
				mutable = new BlockPos.Mutable(position.getX() + x, 0, position.getZ() + z);
				mutable.move(Direction.UP, world.getHeight(Heightmap.Type.WORLD_SURFACE_WG, mutable.getX(), mutable.getZ()));
				
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

	private static void preventfalling(IWorld world, BlockPos.Mutable mutable, BlockState lastBlockstate)
	{
		if(!WBConfig.preventFallingBlocks) return;
		
		if(lastBlockstate.getBlock() instanceof FallingBlock)
		{
			@SuppressWarnings("deprecation")
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
	
	private static void liquidContaining(IWorld world, BlockPos.Mutable mutable, BlockState lastBlockstate)
	{
		if(!WBConfig.containFloatingLiquids) return;
		
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
			@SuppressWarnings("deprecation")
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
