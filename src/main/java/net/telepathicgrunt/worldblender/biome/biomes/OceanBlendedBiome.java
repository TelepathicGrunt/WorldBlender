package net.telepathicgrunt.worldblender.biome.biomes;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.telepathicgrunt.worldblender.biome.WBBiomes;

public final class OceanBlendedBiome extends WBBiome
{
	public OceanBlendedBiome()
	{
		super((new Builder()).surfaceBuilder(new ConfiguredSurfaceBuilder<>(WBBiomes.FEATURE_SURFACE_BUILDER, SurfaceBuilder.AIR_CONFIG)).precipitation(Biome.RainType.RAIN).category(Biome.Category.OCEAN).depth(-1.0F).scale(0.2F).temperature(0.7F).downfall(0.4F).waterColor(4237029).waterFogColor(335155).parent((String) null));
	}

	/**
	 * returns the chance a creature has to spawn.
	 */
	public float getSpawningChance()
	{
		return 0.35F;
	}


	@OnlyIn(Dist.CLIENT)
	public int getSkyColor() {
		return 44525;
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
