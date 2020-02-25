package net.telepathicgrunt.allthefeatures.biome.biomes;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.telepathicgrunt.allthefeatures.biome.BiomeInit;

public final class FeatureBiome extends Biome
{
	public FeatureBiome()
	{
		super((new Builder()).surfaceBuilder(new ConfiguredSurfaceBuilder<>(BiomeInit.FEATURE_SURFACE_BUILDER, SurfaceBuilder.AIR_CONFIG)).precipitation(Biome.RainType.RAIN).category(Biome.Category.NONE).depth(0.120F).scale(0.1F).temperature(1.0F).downfall(0.4F).waterColor(4159204).waterFogColor(329011).parent((String) null));
	}

	/**
	 * returns the chance a creature has to spawn.
	 */
	public float getSpawningChance()
	{
		return 0.35F;
	}
}
