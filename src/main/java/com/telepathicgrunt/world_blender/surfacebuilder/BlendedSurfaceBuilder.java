package com.telepathicgrunt.world_blender.surfacebuilder;

import com.telepathicgrunt.world_blender.WorldBlender;
import com.telepathicgrunt.world_blender.mixin.CarverAccessor;
import com.telepathicgrunt.world_blender.the_blender.ConfigBlacklisting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;

import java.util.*;
import java.util.stream.IntStream;

public class BlendedSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderConfig>
{
    public BlendedSurfaceBuilder() {
		super(SurfaceBuilderConfig.field_237203_a_);
    }


    /**
     * Passes the chosen surface blocks at this coordinate to the Surface Builder.
     */
    @Override
    public void buildSurface(Random random, IChunk chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, long seed, SurfaceBuilderConfig config) {
		BlendedSurfaceBuilder.setPerlinSeed(seed);

//		max = Math.max(max, noise);
//		min = Math.min(min, noise);
//		AllTheFeatures.LOGGER.log(Level.DEBUG, "Max: " + max +", Min: "+min + ", perlin: "+noise);

		// creates surface using a surface builder similar to vanilla's default but using a random config and makes end, nether, and certain modded surfaces fill entire column
		ISurfaceBuilderConfig chosenConfig = allSurfaceList.get(weightedIndex(x, z));
		if(chosenConfig instanceof SurfaceBuilderConfig) {
			this.buildSurface(random, chunk, biome, x, z, startHeight, noise, defaultBlock, defaultFluid, chosenConfig.getTop(), chosenConfig.getUnder(), ((SurfaceBuilderConfig)chosenConfig).getUnderWaterMaterial(), seaLevel);
		}
		else {
			this.buildSurface(random, chunk, biome, x, z, startHeight, noise, defaultBlock, defaultFluid, chosenConfig.getTop(), chosenConfig.getUnder(), chosenConfig.getUnder(), seaLevel);
		}
	   
    }


    protected void buildSurface(Random random, IChunk chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState defaultBlock, BlockState defaultFluid, BlockState top, BlockState middle, BlockState bottom, int sealevel) {
		boolean replaceEntireColumn = false;

		// makes the entire column be replaced with the bottom block
		if (bottom.getBlock() == Blocks.END_STONE || bottom.getBlock() == Blocks.NETHERRACK || !Registry.BLOCK.getKey(bottom.getBlock()).getNamespace().equals("minecraft")) {
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
			blockpos$mutable.setPos(xInChunk, y, zInChunk);
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
						if (biome.getTemperature(blockpos$mutable.setPos(x, y, z)) < 0.15F) {
							topBlockstate = Blocks.ICE.getDefaultState();
						}
						else {
							topBlockstate = defaultFluid;
						}

						blockpos$mutable.setPos(xInChunk, y, zInChunk);
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

    private static List<SurfaceBuilderConfig> threeLayerSurfaceList;
    private static List<ISurfaceBuilderConfig> allSurfaceList;
    private static PerlinNoiseGenerator perlinGen;
    public static long perlinSeed;
    private static double baseScale;
//	private double max = -100000;
//	private double min = 100000;

    public static void setPerlinSeed(long seed) {
		if (perlinGen == null || perlinSeed != seed) {
			perlinGen = new PerlinNoiseGenerator(new SharedSeedRandom(seed), IntStream.rangeClosed(-1, 0));
			perlinSeed = seed;
		}
    }


    /**
     * Helper method to see if we already have said surface
     */
    public boolean containsConfig(SurfaceBuilderConfig configIn) {
		return threeLayerSurfaceList.stream().anyMatch(configEntry -> configEntry.getTop() == configIn.getTop() && configEntry.getUnder() == configIn.getUnder() && configEntry.getUnderWaterMaterial() == configIn.getUnderWaterMaterial());
    }


    /**
     * Helper method to see if we already have said non-standard surface
     */
    public boolean containsConfig(ISurfaceBuilderConfig configIn) {
		return allSurfaceList.stream().anyMatch(configEntry -> configEntry.getTop() == configIn.getTop() && configEntry.getUnder() == configIn.getUnder());
    }


    /**
     * Adds the standard surface to both lists for surface gen later
     */
    public void addConfig(SurfaceBuilderConfig configIn) {
		threeLayerSurfaceList.add(configIn);
		allSurfaceList.add(configIn);
    }


    /**
     * Adds the non-standard surface to allSurfaceList for surface gen later
     */
    public void addConfig(ISurfaceBuilderConfig configIn) {
		allSurfaceList.add(configIn);
    }


	public static final SurfaceBuilderConfig SAND_SAND_UNDERWATER_CONFIG =
			new SurfaceBuilderConfig(Blocks.SAND.getDefaultState(), Blocks.SAND.getDefaultState(), Blocks.SANDSTONE.getDefaultState());

    /**
     * Reset the surfaces
     */
    public static void resetSurfaceList() {
		threeLayerSurfaceList = new ArrayList<>();
		allSurfaceList = new ArrayList<>();

		// default order of surface builders I want to start with always
		threeLayerSurfaceList.add(NETHERRACK_CONFIG);
		threeLayerSurfaceList.add(END_STONE_CONFIG);

		if (WorldBlender.WBBlendingConfig.allowVanillaSurfaces.get() &&
				WorldBlender.WBBlendingConfig.allowVanillaBiomeImport.get())
		{
			threeLayerSurfaceList.add(GRASS_DIRT_GRAVEL_CONFIG);
			threeLayerSurfaceList.add(PODZOL_DIRT_GRAVEL_CONFIG);
			threeLayerSurfaceList.add(RED_SAND_WHITE_TERRACOTTA_GRAVEL_CONFIG);
			threeLayerSurfaceList.add(SAND_SAND_UNDERWATER_CONFIG);
			threeLayerSurfaceList.add(MYCELIUM_DIRT_GRAVEL_CONFIG);
			threeLayerSurfaceList.add(new SurfaceBuilderConfig(Blocks.SNOW_BLOCK.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.GRAVEL.getDefaultState()));
			threeLayerSurfaceList.add(field_237185_P_);
			threeLayerSurfaceList.add(field_237186_Q_);
			threeLayerSurfaceList.add(field_237187_R_);
			threeLayerSurfaceList.add(CORASE_DIRT_DIRT_GRAVEL_CONFIG);
			threeLayerSurfaceList.add(GRAVEL_CONFIG);
		}

		// remove the surfaces that we disallow through blacklist but keep nether/end road
		for (int i = threeLayerSurfaceList.size() - 1; i > 1; i--) {
			if (ConfigBlacklisting.isResourceLocationBlacklisted(ConfigBlacklisting.BlacklistType.SURFACE_BLOCK, Registry.BLOCK.getKey(threeLayerSurfaceList.get(i).getTop().getBlock()))) {
				threeLayerSurfaceList.remove(i);
			}
		}

		baseScale = 0.6D / threeLayerSurfaceList.size();

		allSurfaceList.addAll(threeLayerSurfaceList);
    }


    // setup what vanilla carvers can carve through so they dont get cut off by unique blocks added to surfacebuilder config
    public static Set<Block> blocksToCarve() {

    	Set<Block> carvableBlocks = new HashSet<>(((CarverAccessor) WorldCarver.CANYON).getalwaysCarvableBlocks());
		carvableBlocks.add(Blocks.NETHERRACK);
		carvableBlocks.add(Blocks.END_STONE);

		// adds underground modded blocks to carve through
		for (int i = threeLayerSurfaceList.size() - 1; i > 1; i--) {
			if (!Registry.BLOCK.getKey(threeLayerSurfaceList.get(i).getUnderWaterMaterial().getBlock()).getNamespace().equals("minecraft")) {
			carvableBlocks.add(threeLayerSurfaceList.get(i).getUnderWaterMaterial().getBlock());
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
		double noiseScale = WorldBlender.WBDimensionConfig.surfaceScale.get();

		for (int configIndex = 0; configIndex < allSurfaceList.size(); configIndex++) {
			if (configIndex == 0) {
				if (Math.abs(perlinGen.noiseAt(x / noiseScale, z / noiseScale, true)) < 0.035D) {
					chosenConfigIndex = 0; // nether pathway
					break;
				}
			}
			else if (configIndex == 1) {
				if (Math.abs(perlinGen.noiseAt(x / noiseScale, z / noiseScale, true)) < 0.06D) {
					chosenConfigIndex = 1; // end border on nether path. Uses same scale as nether path.
					break;
				}
			}
			else {
				double offset = 200D * configIndex;
				double scaling = 200D + configIndex * 4D;
				double threshold = baseScale + Math.min(configIndex / 150D, 0.125D);
				if (Math.abs(perlinGen.noiseAt((x + offset) / scaling, (z + offset) / scaling, true)) < threshold) {
					chosenConfigIndex = configIndex; // all other surfaces with scale offset and threshold decreasing as index gets closer to 0.
					break;
				}
			}
		}

		return Math.min(chosenConfigIndex, allSurfaceList.size() - 1); // no index out of bounds errors by locking to last config in list
    }
}
