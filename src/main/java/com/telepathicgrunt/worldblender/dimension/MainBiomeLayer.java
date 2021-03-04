package com.telepathicgrunt.worldblender.dimension;

import com.telepathicgrunt.worldblender.WBIdentifiers;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.layer.traits.IAreaTransformer0;

import java.util.stream.IntStream;


public class MainBiomeLayer implements IAreaTransformer0
{
	private final Registry<Biome> dynamicRegistry;
	private static PerlinNoiseGenerator perlinGen;
	
	public MainBiomeLayer(long seed, Registry<Biome> dynamicRegistry){
		this.dynamicRegistry = dynamicRegistry;

		if (perlinGen == null)
		{
			SharedSeedRandom sharedseedrandom = new SharedSeedRandom(seed);
			perlinGen = new PerlinNoiseGenerator(sharedseedrandom, IntStream.rangeClosed(0, 0));
		}
	}
	
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
			return this.dynamicRegistry.getId(this.dynamicRegistry.getOrDefault(WBIdentifiers.MOUNTAINOUS_BLENDED_BIOME_ID));
		}
		else if(perlinNoise > -0.58) {	
			if(perlinNoise2 < -0.75) {	
				return this.dynamicRegistry.getId(this.dynamicRegistry.getOrDefault(WBIdentifiers.COLD_HILLS_BLENDED_BIOME_ID));
			}
			else {
				return this.dynamicRegistry.getId(this.dynamicRegistry.getOrDefault(WBIdentifiers.GENERAL_BLENDED_BIOME_ID));
			}
		}
		else {	
			return noise.random(100)/800D + perlinNoise%0.4D > -0.2D ?
					this.dynamicRegistry.getId(this.dynamicRegistry.getOrDefault(WBIdentifiers.OCEAN_BLENDED_BIOME_ID)) :
					this.dynamicRegistry.getId(this.dynamicRegistry.getOrDefault(WBIdentifiers.FROZEN_OCEAN_BLENDED_BIOME_ID));
		}
	
	}
}