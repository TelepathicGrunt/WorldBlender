package com.telepathicgrunt.world_blender.surfacebuilder;

import com.telepathicgrunt.world_blender.WorldBlender;
import com.telepathicgrunt.world_blender.mixin.CarverAccessor;
import com.telepathicgrunt.world_blender.the_blender.ConfigBlacklisting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.SurfaceConfig;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

import java.util.*;
import java.util.stream.IntStream;

public class BlendedSurfaceBuilder extends SurfaceBuilder<TernarySurfaceConfig>
{
    public BlendedSurfaceBuilder() {
		super(TernarySurfaceConfig.CODEC);
    }


    /**
     * Passes the chosen surface blocks at this coordinate to the Surface Builder.
     */
    @Override
    public void generate(Random random, Chunk chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, long seed, TernarySurfaceConfig config) {
		BlendedSurfaceBuilder.setPerlinSeed(seed);

//		max = Math.max(max, noise);
//		min = Math.min(min, noise);
//		AllTheFeatures.LOGGER.log(Level.DEBUG, "Max: " + max +", Min: "+min + ", perlin: "+noise);

		// creates surface using a surface builder similar to vanilla's default but using a random config and makes end, nether, and certain modded surfaces fill entire column
		SurfaceConfig chosenConfig = allSurfaceList.get(weightedIndex(x, z));
		if(chosenConfig instanceof TernarySurfaceConfig) {
			this.buildSurface(random, chunk, biome, x, z, startHeight, noise, defaultBlock, defaultFluid, chosenConfig.getTopMaterial(), chosenConfig.getUnderMaterial(), ((TernarySurfaceConfig)chosenConfig).getUnderwaterMaterial(), seaLevel);
		}
		else {
			this.buildSurface(random, chunk, biome, x, z, startHeight, noise, defaultBlock, defaultFluid, chosenConfig.getTopMaterial(), chosenConfig.getUnderMaterial(), chosenConfig.getUnderMaterial(), seaLevel);
		}
	   
    }


    protected void buildSurface(Random random, Chunk chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState defaultBlock, BlockState defaultFluid, BlockState top, BlockState middle, BlockState bottom, int sealevel) {
		boolean replaceEntireColumn = false;

		// makes the entire column be replaced with the bottom block
		if (bottom.getBlock() == Blocks.END_STONE || bottom.getBlock() == Blocks.NETHERRACK || !Registry.BLOCK.getId(bottom.getBlock()).getNamespace().equals("minecraft")) {
			replaceEntireColumn = true;
		}

		BlockState topBlockstate = top;
		BlockState middleBlockstate = middle;
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
		int depth = -1;
		int maxNoiseDepth = (int) (noise / 3.0D + 3.0D + random.nextDouble() * 0.25D);
		// WorldBlender.LOGGER.log(Level.DEBUG, "Max Noise depth: "+maxNoiseDepth);
		int xInChunk = x & 15;
		int zInChunk = z & 15;

		for (int y = startHeight; y >= 0; --y) {
			blockpos$mutable.set(xInChunk, y, zInChunk);
			BlockState currentBlockstate = chunk.getBlockState(blockpos$mutable);
			if (currentBlockstate.getMaterial() == Material.AIR) {
				// reset depth so next non-air block is treated as new top surface
				depth = -1;
			}
			else if (currentBlockstate.getBlock() == defaultBlock.getBlock()) {
				// at top of surface. Place top block
				if (depth == -1) {
					// dunno what this part is for
					if (maxNoiseDepth <= 0) {
						topBlockstate = Blocks.AIR.getDefaultState();
						middleBlockstate = defaultBlock;
					}

					// sets the solid blocks to use within a band around sealevel
					else if (y >= sealevel - 4 && y <= sealevel + 1) {
						topBlockstate = top;
						middleBlockstate = middle;
					}

					// adds the sea with frozen top if needed
					if (y < sealevel && (topBlockstate == null || topBlockstate.getMaterial() == Material.AIR)) {
						if (biome.getTemperature(blockpos$mutable.set(x, y, z)) < 0.15F) {
							topBlockstate = Blocks.ICE.getDefaultState();
						}
						else {
							topBlockstate = defaultFluid;
						}

						blockpos$mutable.set(xInChunk, y, zInChunk);
					}

					// begin creating the actual solid surface with depth set
					// to max depth for how far down to replace blocks
					depth = maxNoiseDepth;

					// sets the top block and since depth is now set greater than 1,
					// it'll enter the else if part for if (depth == -1) when going below
					if (y >= sealevel - 1) {
						chunk.setBlockState(blockpos$mutable, topBlockstate, false);
					}

					// creates the thin seafloor
					else if (y < sealevel - 7 - maxNoiseDepth) {
						topBlockstate = Blocks.AIR.getDefaultState();
						middleBlockstate = defaultBlock;
						chunk.setBlockState(blockpos$mutable, bottom, false);
					}

					// uses middle block when between sealevel and threshold for ocean floor.
					else {
						chunk.setBlockState(blockpos$mutable, middleBlockstate, false);
					}
				}

				// replaces the blocks under the surface
				else if (depth > 0) {
					--depth;
					chunk.setBlockState(blockpos$mutable, middleBlockstate, false);

					// creates thick band of sandstone if middle block is sand.
					if (depth == 0 && middleBlockstate.getBlock() == Blocks.SAND && maxNoiseDepth > 1) {
						depth = random.nextInt(4) + Math.max(0, y - 63);
						middleBlockstate = middleBlockstate.getBlock() == Blocks.RED_SAND ? Blocks.RED_SANDSTONE.getDefaultState() : Blocks.SANDSTONE.getDefaultState();
					}
				}

				else if (replaceEntireColumn) {
					chunk.setBlockState(blockpos$mutable, bottom, false);
				}
			}
		}

    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // config picking

    private static List<TernarySurfaceConfig> threeLayerSurfaceList;
    private static List<SurfaceConfig> allSurfaceList;
    private static OctaveSimplexNoiseSampler perlinGen;
    public static long perlinSeed;
    private static double baseScale;
//	private double max = -100000;
//	private double min = 100000;

    public static void setPerlinSeed(long seed) {
		if (perlinGen == null || perlinSeed != seed) {
			perlinGen = new OctaveSimplexNoiseSampler(new ChunkRandom(seed), IntStream.rangeClosed(-1, 0));
			perlinSeed = seed;
		}
    }


    /**
     * Helper method to see if we already have said surface
     */
    public boolean containsConfig(TernarySurfaceConfig configIn) {
		return threeLayerSurfaceList.stream().anyMatch(configEntry -> configEntry.getTopMaterial() == configIn.getTopMaterial() && configEntry.getUnderMaterial() == configIn.getUnderMaterial() && configEntry.getUnderwaterMaterial() == configIn.getUnderwaterMaterial());
    }


    /**
     * Helper method to see if we already have said non-standard surface
     */
    public boolean containsConfig(SurfaceConfig configIn) {
		return allSurfaceList.stream().anyMatch(configEntry -> configEntry.getTopMaterial() == configIn.getTopMaterial() && configEntry.getUnderMaterial() == configIn.getUnderMaterial());
    }


    /**
     * Adds the standard surface to both lists for surface gen later
     */
    public void addConfig(TernarySurfaceConfig configIn) {
		threeLayerSurfaceList.add(configIn);
		allSurfaceList.add(configIn);
    }


    /**
     * Adds the non-standard surface to allSurfaceList for surface gen later
     */
    public void addConfig(SurfaceConfig configIn) {
		allSurfaceList.add(configIn);
    }


	public static final TernarySurfaceConfig SAND_SAND_UNDERWATER_CONFIG =
			new TernarySurfaceConfig(Blocks.SAND.getDefaultState(), Blocks.SAND.getDefaultState(), Blocks.SANDSTONE.getDefaultState());

    /**
     * Reset the surfaces
     */
    public static void resetSurfaceList() {
		threeLayerSurfaceList = new ArrayList<>();
		allSurfaceList = new ArrayList<>();

		// default order of surface builders I want to start with always
		threeLayerSurfaceList.add(NETHER_CONFIG);
		threeLayerSurfaceList.add(END_CONFIG);

		if (WorldBlender.WB_CONFIG.WBBlendingConfig.allowVanillaSurfaces &&
				WorldBlender.WB_CONFIG.WBBlendingConfig.allowVanillaBiomeImport)
		{
			threeLayerSurfaceList.add(GRASS_CONFIG);
			threeLayerSurfaceList.add(PODZOL_CONFIG);
			threeLayerSurfaceList.add(BADLANDS_CONFIG);
			threeLayerSurfaceList.add(SAND_SAND_UNDERWATER_CONFIG);
			threeLayerSurfaceList.add(MYCELIUM_CONFIG);
			threeLayerSurfaceList.add(new TernarySurfaceConfig(Blocks.SNOW_BLOCK.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.GRAVEL.getDefaultState()));
			threeLayerSurfaceList.add(CRIMSON_NYLIUM_CONFIG);
			threeLayerSurfaceList.add(WARPED_NYLIUM_CONFIG);
			threeLayerSurfaceList.add(BASALT_DELTA_CONFIG);
			threeLayerSurfaceList.add(COARSE_DIRT_CONFIG);
			threeLayerSurfaceList.add(GRAVEL_CONFIG);
		}

		// remove the surfaces that we disallow through blacklist but keep nether/end road
		for (int i = threeLayerSurfaceList.size() - 1; i > 1; i--) {
			if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, Registry.BLOCK.getId(threeLayerSurfaceList.get(i).getTopMaterial().getBlock()))) {
				threeLayerSurfaceList.remove(i);
			}
		}

		baseScale = 0.6D / threeLayerSurfaceList.size();

		allSurfaceList.addAll(threeLayerSurfaceList);
    }


    // setup what vanilla carvers can carve through so they dont get cut off by unique blocks added to surfacebuilder config
    public static Set<Block> blocksToCarve() {

    	Set<Block> carvableBlocks = new HashSet<>(((CarverAccessor) Carver.CANYON).getalwaysCarvableBlocks());
		carvableBlocks.add(Blocks.NETHERRACK);
		carvableBlocks.add(Blocks.END_STONE);

		// adds underground modded blocks to carve through
		for (int i = threeLayerSurfaceList.size() - 1; i > 1; i--) {
			if (!Registry.BLOCK.getId(threeLayerSurfaceList.get(i).getUnderwaterMaterial().getBlock()).getNamespace().equals("minecraft")) {
			carvableBlocks.add(threeLayerSurfaceList.get(i).getUnderwaterMaterial().getBlock());
			}
		}

		// It is now safe to clear out this list of surfaces (only needed for this method)
		threeLayerSurfaceList = new ArrayList<>();

		return carvableBlocks;
    }


    /**
     * Will return a random index within the range of allSurfaceList.size(). The index picked is noise based and when visualized, it creates thin bands of areas for the indices chosen.
     */
    private int weightedIndex(int x, int z) {

		// list checking
	//		for(int i = 0; i<configList.size(); i++) {
	//			WorldBlender.LOGGER.log(Level.INFO, i+": top "+configList.get(i).getTop().getBlock().getRegistryName().getPath()+": middle "+configList.get(i).getUnder().getBlock().getRegistryName().getPath()+": bottom "+configList.get(i).getUnderWaterMaterial().getBlock().getRegistryName().getPath());
	//		}

		int chosenConfigIndex = 2; // Grass surface
		double noiseScale = WorldBlender.WB_CONFIG.WBDimensionConfig.surfaceScale;

		for (int configIndex = 0; configIndex < allSurfaceList.size(); configIndex++) {
			if (configIndex == 0) {
				if (Math.abs(perlinGen.sample(x / noiseScale, z / noiseScale, true)) < 0.035D) {
					chosenConfigIndex = 0; // nether pathway
					break;
				}
			}
			else if (configIndex == 1) {
				if (Math.abs(perlinGen.sample(x / noiseScale, z / noiseScale, true)) < 0.06D) {
					chosenConfigIndex = 1; // end border on nether path. Uses same scale as nether path.
					break;
				}
			}
			else {
				double offset = 200D * configIndex;
				double scaling = 200D + configIndex * 4D;
				double threshold = baseScale + Math.min(configIndex / 150D, 0.125D);
				if (Math.abs(perlinGen.sample((x + offset) / scaling, (z + offset) / scaling, true)) < threshold) {
					chosenConfigIndex = configIndex; // all other surfaces with scale offset and threshold decreasing as index gets closer to 0.
					break;
				}
			}
		}

		return Math.min(chosenConfigIndex, allSurfaceList.size() - 1); // no index out of bounds errors by locking to last config in list
    }
}
