package net.telepathicgrunt.allthefeatures.biome.biomes;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.telepathicgrunt.allthefeatures.biome.BiomeInit;

public final class OceanFeatureBiome extends Biome
{
	public OceanFeatureBiome()
	{
		super((new Builder()).surfaceBuilder(new ConfiguredSurfaceBuilder<>(BiomeInit.FEATURE_SURFACE_BUILDER, SurfaceBuilder.AIR_CONFIG)).precipitation(Biome.RainType.RAIN).category(Biome.Category.NONE).depth(-1.0F).scale(0.2F).temperature(0.05F).downfall(0.4F).waterColor(4159204).waterFogColor(329011).parent((String) null));
	}

	/**
	 * returns the chance a creature has to spawn.
	 */
	public float getSpawningChance()
	{
		return 0.35F;
	}


	/*
	 * set grass color
	 */
	@OnlyIn(Dist.CLIENT)
	public int func_225528_a_(double p_225528_1_, double p_225528_3_)
	{
		return 3730080;
	}


	/*
	 * set foliage/plant color
	 */
	@OnlyIn(Dist.CLIENT)
	public int func_225527_a_()
	{
		return 3397255;
	}
}
