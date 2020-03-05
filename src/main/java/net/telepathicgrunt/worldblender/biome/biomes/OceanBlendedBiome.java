package net.telepathicgrunt.worldblender.biome.biomes;

import net.minecraft.util.math.BlockPos;
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
		super((new Builder()).surfaceBuilder(new ConfiguredSurfaceBuilder<>(WBBiomes.FEATURE_SURFACE_BUILDER, SurfaceBuilder.AIR_CONFIG)).precipitation(Biome.RainType.RAIN).category(Biome.Category.NONE).depth(-1.0F).scale(0.2F).temperature(0.7F).downfall(0.4F).waterColor(4237029).waterFogColor(335155).parent((String) null));
	}

	/**
	 * returns the chance a creature has to spawn.
	 */
	@Override
	public float getSpawningChance()
	{
		return 0.35F;
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	public int getSkyColorByTemp(float currentTemperature) {
		return 44525;
	}
	

	/*
	 * set grass color
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public int getGrassColor(BlockPos pos) 
	{
		return 3730080;
	}


	/*
	 * set foliage/plant color
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public int getFoliageColor(BlockPos pos)
	{
		return 3397255;
	}
}
