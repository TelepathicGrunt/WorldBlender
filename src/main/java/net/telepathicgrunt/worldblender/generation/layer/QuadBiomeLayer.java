package net.telepathicgrunt.worldblender.generation.layer;

import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.layer.traits.IAreaTransformer0;
import net.telepathicgrunt.worldblender.biome.WBBiomes;


@SuppressWarnings("deprecation")
public enum QuadBiomeLayer implements IAreaTransformer0
{
	INSTANCE;

	private static final int BLENDED_BIOME_ID = Registry.BIOME.getId(WBBiomes.BLENDED_BIOME);
	private static final int MOUNTAINOUS_BLENDED_BIOME_ID = Registry.BIOME.getId(WBBiomes.MOUNTAINOUS_BLENDED_BIOME);
	private static final int OCEAN_BLENDED_BIOME_ID = Registry.BIOME.getId(WBBiomes.OCEAN_BLENDED_BIOME);
	private static final int FROZEN_OCEAN_BLENDED_BIOME_ID = Registry.BIOME.getId(WBBiomes.FROZEN_OCEAN_BLENDED_BIOME);
	private static PerlinNoiseGenerator perlinGen;
//	private double max = -100000;
//	private double min = 100000;

	public int apply(INoiseRandom noise, int x, int z)
	{
		double perlinNoise = perlinGen.noiseAt((double) x * 0.1D, (double)z * 0.1D, false);
		
//		max = Math.max(max, perlinNoise);
//		min = Math.min(min, perlinNoise);
//		AllTheFeatures.LOGGER.log(Level.DEBUG, "Max: " + max +", Min: "+min + ", perlin: "+perlinNoise);
		
		
		if(perlinNoise > 0.53) {	
			return MOUNTAINOUS_BLENDED_BIOME_ID;
		}
		else if(perlinNoise > -0.58) {	
			return BLENDED_BIOME_ID;
		}
		else {	
			return noise.random(100)/800D + perlinNoise%0.4D > -0.2D ? OCEAN_BLENDED_BIOME_ID : FROZEN_OCEAN_BLENDED_BIOME_ID;
		}
	
	}


	public static void setSeed(long seed)
	{
		if (perlinGen == null)
		{
			SharedSeedRandom sharedseedrandom = new SharedSeedRandom(seed);
			perlinGen = new PerlinNoiseGenerator(sharedseedrandom, 0, 0);
		}
	}
}