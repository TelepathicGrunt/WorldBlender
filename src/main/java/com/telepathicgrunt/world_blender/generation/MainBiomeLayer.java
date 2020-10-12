package com.telepathicgrunt.world_blender.generation;

import com.telepathicgrunt.world_blender.WBIdentifiers;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.layer.traits.IAreaTransformer0;

import java.util.stream.IntStream;


public enum MainBiomeLayer implements IAreaTransformer0
{
	INSTANCE;

	private static PerlinNoiseGenerator perlinGen;
//	private double max = -100000;
//	private double min = 100000;

	@Override
	public int apply(INoiseRandom noise, int x, int z)
	{
		double perlinNoise = perlinGen.noiseAt(x * 0.1D, z * 0.1D, false);
		double perlinNoise2 = perlinGen.noiseAt(x * 0.08D + 1000, z * 0.08D + 1000, false);
		
//		max = Math.max(max, perlinNoise);
//		min = Math.min(min, perlinNoise);
//		AllTheFeatures.LOGGER.log(Level.DEBUG, "Max: " + max +", Min: "+min + ", perlin: "+perlinNoise);
		
		
		if(perlinNoise > 0.53) {	
			return WBBiomeProvider.LAYERS_BIOME_REGISTRY.getId(WBBiomeProvider.LAYERS_BIOME_REGISTRY.getOrDefault(WBIdentifiers.MOUNTAINOUS_BLENDED_BIOME_ID));
		}
		else if(perlinNoise > -0.58) {	
			if(perlinNoise2 < -0.75) {	
				return WBBiomeProvider.LAYERS_BIOME_REGISTRY.getId(WBBiomeProvider.LAYERS_BIOME_REGISTRY.getOrDefault(WBIdentifiers.COLD_HILLS_BLENDED_BIOME_ID));
			}
			else {
				return WBBiomeProvider.LAYERS_BIOME_REGISTRY.getId(WBBiomeProvider.LAYERS_BIOME_REGISTRY.getOrDefault(WBIdentifiers.GENERAL_BLENDED_BIOME_ID));
			}
		}
		else {	
			return noise.random(100)/800D + perlinNoise%0.4D > -0.2D ?
					WBBiomeProvider.LAYERS_BIOME_REGISTRY.getId(WBBiomeProvider.LAYERS_BIOME_REGISTRY.getOrDefault(WBIdentifiers.OCEAN_BLENDED_BIOME_ID)) :
					WBBiomeProvider.LAYERS_BIOME_REGISTRY.getId(WBBiomeProvider.LAYERS_BIOME_REGISTRY.getOrDefault(WBIdentifiers.FROZEN_OCEAN_BLENDED_BIOME_ID));
		}
	
	}


	public static void setSeed(long seed)
	{
		if (perlinGen == null)
		{
			SharedSeedRandom sharedseedrandom = new SharedSeedRandom(seed);
			perlinGen = new PerlinNoiseGenerator(sharedseedrandom, IntStream.rangeClosed(0, 0));
		}
	}
}