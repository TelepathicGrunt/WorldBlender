package net.telepathicgrunt.worldblender.biome.biomes.surfacebuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.apache.logging.log4j.Level;

import com.mojang.datafixers.Dynamic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.telepathicgrunt.worldblender.WorldBlender;
import net.telepathicgrunt.worldblender.configs.WBConfig;
import net.telepathicgrunt.worldblender.the_blender.ConfigBlacklisting;

public class BlendedSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderConfig>
{ 
	public BlendedSurfaceBuilder(Function<Dynamic<?>, ? extends SurfaceBuilderConfig> config){
		super(config);
	}

	@Override
	public void buildSurface(Random random, IChunk chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, long seed, SurfaceBuilderConfig config){
//		max = Math.max(max, noise);
//		min = Math.min(min, noise);
//		AllTheFeatures.LOGGER.log(Level.DEBUG, "Max: " + max +", Min: "+min + ", perlin: "+noise);
		
		//creates surface using a surface builder similar to vanilla's default but using a random config and makes end, nether, and certain modded surfaces fill entire column
		SurfaceBuilderConfig chosenConfig = configList.get(weightedIndex(x, z));
		this.buildSurface(random, chunk, biome, x, z, startHeight, noise, defaultBlock, defaultFluid, chosenConfig.getTop(), chosenConfig.getUnder(), chosenConfig.getUnderWaterMaterial(), seaLevel);
	}

	
	protected void buildSurface(Random random, IChunk chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState defaultBlock, BlockState defaultFluid, BlockState top, BlockState middle, BlockState bottom, int sealevel)
	{
		boolean replaceEntireColumn = false;
		
		//makes the entire column be replaced with the bottom block
		if(bottom.getBlock() == Blocks.END_STONE || 
			bottom.getBlock() == Blocks.NETHERRACK || 
			!bottom.getBlock().getRegistryName().getNamespace().equals("minecraft"))
		{
			replaceEntireColumn = true;
		}
		
		BlockState topBlockstate = top;
		BlockState middleBlockstate = middle;
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
		int depth = -1;
		int maxNoiseDepth = (int) (noise / 3.0D + 3.0D + random.nextDouble() * 0.25D);
		WorldBlender.LOGGER.log(Level.DEBUG, "Max Noise depth: "+maxNoiseDepth);
		int xInChunk = x & 15;
		int zInChunk = z & 15;

		for (int y = startHeight; y >= 0; --y)
		{
			blockpos$mutable.setPos(xInChunk, y, zInChunk);
			BlockState currentBlockstate = chunk.getBlockState(blockpos$mutable);
			if (currentBlockstate.getMaterial() == Material.AIR)
			{
				//reset depth so next non-air block is treated as new top surface
				depth = -1;
			}
			else if (currentBlockstate.getBlock() == defaultBlock.getBlock())
			{
				//at top of surface. Place top block
				if (depth == -1)
				{
					//dunno what this part is for
					if (maxNoiseDepth <= 0)
					{
						topBlockstate = Blocks.AIR.getDefaultState();
						middleBlockstate = defaultBlock;
					}
					
					//sets the solid blocks to use within a band around sealevel
					else if (y >= sealevel - 4 && y <= sealevel + 1)
					{
						topBlockstate = top;
						middleBlockstate = middle;
					}

					//adds the sea with frozen top if needed
					if (y < sealevel && (topBlockstate == null || topBlockstate.getMaterial() == Material.AIR))
					{
						if (biome.getTemperature(blockpos$mutable.setPos(x, y, z)) < 0.15F)
						{
							topBlockstate = Blocks.ICE.getDefaultState();
						}
						else
						{
							topBlockstate = defaultFluid;
						}

						blockpos$mutable.setPos(xInChunk, y, zInChunk);
					}

					//begin creating the actual solid surface with depth set 
					//to max depth for how far down to replace blocks
					depth = maxNoiseDepth;
					
					//sets the top block and since depth is now set greater than 1, 
					//it'll enter the else if part for if (depth == -1) when going below
					if (y >= sealevel - 1)
					{
						chunk.setBlockState(blockpos$mutable, topBlockstate, false);
					}
					
					//creates the thin seafloor
					else if (y < sealevel - 7 - maxNoiseDepth)
					{
						topBlockstate = Blocks.AIR.getDefaultState();
						middleBlockstate = defaultBlock;
						chunk.setBlockState(blockpos$mutable, bottom, false);
					}
					
					//uses middle block when between sealevel and threshold for ocean floor.
					else
					{
						chunk.setBlockState(blockpos$mutable, middleBlockstate, false);
					}
				}
				
				//replaces the blocks under the surface
				else if (depth > 0)
				{
					--depth;
					chunk.setBlockState(blockpos$mutable, middleBlockstate, false);
					
					//creates thick band of sandstone if middle block is sand.
					if (depth == 0 && middleBlockstate.getBlock() == Blocks.SAND && maxNoiseDepth > 1)
					{
						depth = random.nextInt(4) + Math.max(0, y - 63);
						middleBlockstate = middleBlockstate.getBlock() == Blocks.RED_SAND ? Blocks.RED_SANDSTONE.getDefaultState() : Blocks.SANDSTONE.getDefaultState();
					}
				}
				
				else if(replaceEntireColumn)
				{
					chunk.setBlockState(blockpos$mutable, bottom, false);
				}
			}
		}

	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//config picking
	
	private static List<SurfaceBuilderConfig> configList;
	private static PerlinNoiseGenerator perlinGen;
	public static long perlinSeed;
//	private double max = -100000;
//	private double min = 100000;
	
	public static void setPerlinSeed(long seed){
		if(perlinGen == null) {
			perlinGen = new PerlinNoiseGenerator(new SharedSeedRandom(seed), 0, 1);
			perlinSeed = seed;
		}
	}
	
	public boolean containsConfig(SurfaceBuilderConfig configIn) {
		return configList.stream().anyMatch(configEntry -> configEntry.getTop() == configIn.getTop() && configEntry.getUnder() == configIn.getUnder() && configEntry.getUnderWaterMaterial() == configIn.getUnderWaterMaterial());
	}
	
	public void addConfig(SurfaceBuilderConfig configIn) {
		configList.add(configIn);
	}

	private int weightedIndex(int x, int z) {
		int listSize = configList.size();
		
		//list checking
//		for(int i = 0; i<configList.size(); i++) {
//			WorldBlender.LOGGER.log(Level.INFO, i+": top "+configList.get(i).getTop().getBlock().getRegistryName().getPath()+": middle "+configList.get(i).getUnder().getBlock().getRegistryName().getPath()+": bottom "+configList.get(i).getUnderWaterMaterial().getBlock().getRegistryName().getPath());
//		}
		
		int chosenConfigIndex = 2; // Grass surface
		
		for(int configIndex = 0; configIndex < listSize; configIndex++) {
			if(configIndex == 0) {
				if(Math.abs(perlinGen.noiseAt(x/240D, z/240D, true)) < 0.035D) {
					chosenConfigIndex = 0; // nether pathway
					break;
				}
			}
			else if(configIndex == 1) {
				if(Math.abs(perlinGen.noiseAt(x/240D, z/240D, true)) < 0.06D) {
					chosenConfigIndex = 1; // end border on nether path. Uses same scale as nether path.
					break;
				}
			}
			else {
				double offset = 200D*configIndex;
				double scaling = 200D+configIndex*4D;
				double threshold = 0.6D/listSize+Math.min(configIndex/150D, 0.125D);
				if(Math.abs(perlinGen.noiseAt((x+offset)/scaling, (z+offset)/scaling, true)) < threshold) {
					chosenConfigIndex = configIndex; // all other surfaces with scale offset and threshold decreasing as index gets closer to 0.
					break;
				}
			}
		}
		
		return Math.min(chosenConfigIndex, listSize-1); //no index out of bounds
	}

	public static void resetSurfaceList() {
		configList = new ArrayList<SurfaceBuilderConfig>();
		
		//default order of surface builders I want to start with always
		configList.add(NETHERRACK_CONFIG);
		configList.add(END_STONE_CONFIG);
		
		if (WBConfig.SERVER.allowVanillaSurfaces.get() && WBConfig.SERVER.allowVanillaBiomeImport.get()) {
			configList.add(GRASS_DIRT_GRAVEL_CONFIG);
			configList.add(PODZOL_DIRT_GRAVEL_CONFIG);
			configList.add(RED_SAND_WHITE_TERRACOTTA_GRAVEL_CONFIG);
			configList.add(SAND_CONFIG);
			configList.add(MYCELIUM_DIRT_GRAVEL_CONFIG);
			configList.add(new SurfaceBuilderConfig(Blocks.SNOW_BLOCK.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.GRAVEL.getDefaultState()));
			configList.add(CORASE_DIRT_DIRT_GRAVEL_CONFIG);
			configList.add(AIR_CONFIG);
			configList.add(GRAVEL_CONFIG);
		}
		
		//remove the surfaces that we disallow through blacklist but keep nether/end road
		for(int i = configList.size()-1; i > 1; i--) {
			if(ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, configList.get(i).getTop().getBlock().getRegistryName()))
			{
				configList.remove(i);
			}
		}
	}

	
	//setup what vanilla carvers can carve through so they dont get cut off by unique blocks added to surfacebuilder config
	public static Set<Block> blocksToCarve()
	{
		Set<Block> carvableBlocks = new HashSet<Block>();

		carvableBlocks.addAll(WorldCarver.CANYON.carvableBlocks);
		carvableBlocks.add(Blocks.NETHERRACK);
		carvableBlocks.add(Blocks.END_STONE);
		
		//adds underground modded blocks to carve through
		for(int i = configList.size()-1; i > 1; i--) {
			if(!configList.get(i).getUnderWaterMaterial().getBlock().getRegistryName().getNamespace().equals("minecraft"))
			{
				carvableBlocks.add(configList.get(i).getUnderWaterMaterial().getBlock());
			}
		}
		
		return carvableBlocks;
	}
}
