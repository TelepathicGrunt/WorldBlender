package com.telepathicgrunt.worldblender.surfacebuilder;

import com.telepathicgrunt.worldblender.WorldBlender;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.surfacebuilders.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.IntStream;

@ParametersAreNonnullByDefault
public class BlendedSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderConfig> {
	static SurfaceBlender blender;
	
	public static final SurfaceBuilderConfig SAND_SAND_UNDERWATER_CONFIG =
		new SurfaceBuilderConfig(Blocks.SAND.getDefaultState(), Blocks.SAND.getDefaultState(), Blocks.SANDSTONE.getDefaultState());
	
	private PerlinNoiseGenerator perlinGen;
	private long perlinSeed;
	
	private void setPerlinSeed(long seed) {
		if (perlinGen == null || perlinSeed != seed) {
			perlinGen = new PerlinNoiseGenerator(new SharedSeedRandom(seed), IntStream.rangeClosed(-1, 0));
			perlinSeed = seed;
		}
	}
	
	public BlendedSurfaceBuilder() {
		super(SurfaceBuilderConfig.field_237203_a_);
	}
	
	/**
	 Passes the chosen surface blocks at this coordinate to the Surface Builder.
	 */
	@Override
	public void buildSurface(
		Random random, IChunk chunk, Biome biome,
		int x, int z, int startHeight,
		double noise,
		BlockState defaultBlock, BlockState defaultFluid,
		int seaLevel,
		long seed,
		SurfaceBuilderConfig config
	) {
		if (blender == null) {
			// blender is set after blending all biomes (to collect their surfaces) with and calling save() on a SurfaceBlender
			WorldBlender.LOGGER.fatal("BlendedSurfaceBuilder not properly initialized!");
			return;
		}
		
		setPerlinSeed(seed);
		
		ISurfaceBuilderConfig chosenConfig = weightedRandomSurface(x, z);
		BlockState bottom = chosenConfig instanceof SurfaceBuilderConfig
			? ((SurfaceBuilderConfig) chosenConfig).getUnderWaterMaterial()
			: chosenConfig.getUnder();
		
		// creates surface using a surface builder similar to vanilla's default but using a random config and makes end, nether, and certain modded surfaces fill entire column
		BlockState top = chosenConfig.getTop();
		//noinspection ConstantConditions — BYG has this as null sometimes
		if (top == null) {
			top = Blocks.AIR.getDefaultState();
		}
		
		this.buildSurface(
			random, chunk, biome,
			x, z, startHeight,
			noise,
			defaultBlock, defaultFluid,
			top,
			chosenConfig.getUnder(),
			bottom,
			seaLevel
		);
	}
	
	/**
	 Returns a random index within the range of allSurfaceList.size(). The index picked is noise based and when visualized, it creates thin bands of areas for the indices chosen.
	 */
	private ISurfaceBuilderConfig weightedRandomSurface(int x, int z) {
		// list checking
		//		for(int i = 0; i<configList.size(); i++) {
		//			WorldBlender.LOGGER.log(Level.INFO, i+": top "+configList.get(i).getTop().getBlock().getRegistryName().getPath()+": middle "+configList.get(i).getUnder().getBlock().getRegistryName().getPath()+": bottom "+configList.get(i).getUnderWaterMaterial().getBlock().getRegistryName().getPath());
		//		}
		
		int chosenConfigIndex = 2; // Grass surface
		double noiseScale = WorldBlender.WBDimensionConfig.surfaceScale.get();
		
		for (int configIndex = 0; configIndex < blender.surfaces.size(); configIndex++) {
			if (configIndex == 0) {
				if (Math.abs(perlinGen.noiseAt(x / noiseScale, z / noiseScale, true)) < 0.035D) {
					chosenConfigIndex = 0; // nether pathway
					break;
				}
			} else if (configIndex == 1) {
				if (Math.abs(perlinGen.noiseAt(x / noiseScale, z / noiseScale, true)) < 0.06D) {
					chosenConfigIndex = 1; // end border on nether path. Uses same scale as nether path.
					break;
				}
			} else {
				double offset = 200D * configIndex;
				double scaling = 200D + configIndex * 4D;
				double threshold = blender.baseScale + Math.min(configIndex / 150D, 0.125D);
				if (Math.abs(perlinGen.noiseAt((x + offset) / scaling, (z + offset) / scaling, true)) < threshold) {
					chosenConfigIndex = configIndex; // all other surfaces with scale offset and threshold decreasing as index gets closer to 0.
					break;
				}
			}
		}
		
		int index = Math.min(chosenConfigIndex, blender.surfaces.size() - 1); // no index out of bounds errors by locking to last config in list
		return blender.surfaces.get(index);
	}
	
	private void buildSurface(
		Random random, IChunk chunk, Biome biome,
		int x, int z, int startHeight,
		double noise,
		BlockState defaultBlock, BlockState defaultFluid,
		BlockState top, BlockState middle, BlockState bottom,
		int seaLevel
	) {
		boolean replaceEntireColumn = bottom.getBlock() == Blocks.END_STONE
			|| bottom.getBlock() == Blocks.NETHERRACK
			|| !Registry.BLOCK.getKey(bottom.getBlock()).getNamespace().equals("minecraft");
		
		// randomly generate a maxDepth from noise
		final int maxDepth = (int) (noise / 3.0D + 3.0D + random.nextDouble() * 0.25D);
		// WorldBlender.LOGGER.log(Level.DEBUG, "Max Noise depth: "+maxDepth);
		
		final int xInChunk = x & 15;
		final int zInChunk = z & 15;
		
		BlockState activeBlock = middle;
		int depth = -1;
		// reused to avoid allocations
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int y = startHeight; y >= 0; --y) {
			pos.setPos(xInChunk, y, zInChunk);
			BlockState currentBlock = chunk.getBlockState(pos);
			if (currentBlock.getMaterial() == Material.AIR) {
				// reset depth so next non-air block is treated as new top surface
				depth = -1;
				continue;
			}
			
			if (currentBlock.getBlock() != defaultBlock.getBlock()) continue;
			
			final BlockState toPlace; // this way we can ensure we always place a block or explicitly continue
			if (depth == -1) {
				// at top of surface: place top block.
				// mostly about setting activeBlock for lower gen though
				
				// begin creating the actual solid surface with depth set
				// to max depth for how far down to replace blocks
				depth = maxDepth;
				
				BlockState topLayer = top;
				if (maxDepth <= 0) {
					// dunno what this part is for
					topLayer = Blocks.AIR.getDefaultState();
					activeBlock = defaultBlock;
				} else if (y >= seaLevel - 4 && y <= seaLevel + 1) {
					// sets the solid blocks to use within a band around sea level
					activeBlock = middle;
				}
				
				// add the sea with frozen top if needed
				if (y < seaLevel && topLayer.getMaterial() == Material.AIR) {
					float temperature = biome.getTemperature(pos.setPos(x, y, z));
					pos.setPos(xInChunk, y, zInChunk);
					
					topLayer = temperature < 0.15F
						? Blocks.ICE.getDefaultState()
						: defaultFluid;
				}
				
				// sets the top block and since depth is now set greater than 1,
				// it'll enter the else if part for if (depth == -1) when going below
				if (y >= seaLevel - 1) {
					toPlace = topLayer;
				} else if (y < seaLevel - 7 - maxDepth) {
					// create the thin seafloor
					activeBlock = defaultBlock;
					toPlace = bottom;
				} else {
					// use middle block when between sea level and threshold for ocean floor.
					toPlace = activeBlock;
				}
			} else if (depth > 0) {
				// replaces the blocks under the surface
				--depth;
				toPlace = activeBlock;
				
				// creates thick band of sandstone if middle block is sand.
				if (depth == 0 && activeBlock.getMaterial() == Material.SAND) {
					depth = random.nextInt(4) + Math.max(0, y - 63);
					activeBlock = activeBlock.getBlock() == Blocks.RED_SAND
						? Blocks.RED_SANDSTONE.getDefaultState()
						: Blocks.SANDSTONE.getDefaultState();
				}
			} else if (replaceEntireColumn) {
				toPlace = bottom;
			} else continue;
			
			chunk.setBlockState(pos, toPlace, false);
		}
	}
}
