package net.telepathicgrunt.allthefeatures.biome.biomes.surfacebuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;

public class FeatureSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderConfig>
{ 
	public FeatureSurfaceBuilder(Function<Dynamic<?>, ? extends SurfaceBuilderConfig> config)
	{
		super(config);
		configList = new ArrayList<SurfaceBuilderConfig>();
		
		//default order of surface builders I want to start with
		configList.add(NETHERRACK_CONFIG);
		configList.add(END_STONE_CONFIG);
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

	@Override
	public void buildSurface(Random random, IChunk chunk, Biome biome, int x, int z, int startHeight, double noise, BlockState defaultBlock, BlockState defaultFluid, int seaLevel, long seed, SurfaceBuilderConfig config)
	{
//		max = Math.max(max, noise);
//		min = Math.min(min, noise);
//		AllTheFeatures.LOGGER.log(Level.DEBUG, "Max: " + max +", Min: "+min + ", perlin: "+noise);
		
		//creates surface using normal surface builder but using a random config
		SurfaceBuilder.DEFAULT.buildSurface(random, chunk, biome, x, z, startHeight, noise, defaultBlock, defaultFluid, seaLevel, seed, configList.get(weightedIndex(x, z)));

	}

	
	private static List<SurfaceBuilderConfig> configList;
	private static PerlinNoiseGenerator perlinGen;
//	private double max = -100000;
//	private double min = 100000;
	
	public boolean containsConfig(SurfaceBuilderConfig configIn) {
		return configList.stream().anyMatch(configEntry -> configEntry.getTop() == configIn.getTop() && configEntry.getUnder() == configIn.getUnder() && configEntry.getUnderWaterMaterial() == configIn.getUnderWaterMaterial());
	}
	
	public void addConfig(SurfaceBuilderConfig configIn) {
		configList.add(configIn);
	}

	private int weightedIndex(int x, int z) {
		int size = configList.size();
		
		//list checking
//		for(int i = 0; i<configList.size(); i++) {
//			AllTheFeatures.LOGGER.log(Level.INFO, i+": top "+configList.get(i).getTop().getBlock().getRegistryName().getPath()+": middle "+configList.get(i).getUnder().getBlock().getRegistryName().getPath()+": bottom "+configList.get(i).getUnderWaterMaterial().getBlock().getRegistryName().getPath());
//		}
		
		int chosenIndex = 2; // Grass surface
		
		for(int i = 0; i < size; i++) {
			if(i == 0) {
				if(Math.abs(perlinGen.noiseAt(x/240D, z/240D, true)) < 0.035D) {
					chosenIndex = 0; // nether path
					break;
				}
			}
			else if(i == 1) {
				if(Math.abs(perlinGen.noiseAt(x/240D, z/240D, true)) < 0.06D) {
					chosenIndex = 1; // end border on nether path. Uses same scale as nether path.
					break;
				}
			}
			else {
				if(Math.abs(perlinGen.noiseAt((x+200*i)/(260D+size*3), (z+200*i)/(260D+size*3), true)) < 0.6D/size+Math.min(i/150D, 0.13D)) {
					chosenIndex = i; // all other surfaces with scale offset and threshold decreasing as index gets closer to 0.
					break;
				}
			}
		}
		
		
		return chosenIndex;
	}
	
	public void setPerlinSeed(long seed)
	{
		if(perlinGen == null) {
			perlinGen = new PerlinNoiseGenerator(new SharedSeedRandom(seed), 0, 1);
		}
	}
}
