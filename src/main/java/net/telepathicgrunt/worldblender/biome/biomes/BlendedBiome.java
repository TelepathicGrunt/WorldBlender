package net.telepathicgrunt.worldblender.biome.biomes;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.telepathicgrunt.worldblender.biome.WBBiomes;

public final class BlendedBiome extends WBBiome
{
	public BlendedBiome()
	{
		super((new Builder()).surfaceBuilder(new ConfiguredSurfaceBuilder<>(WBBiomes.BLENDED_SURFACE_BUILDER, SurfaceBuilder.AIR_CONFIG)).precipitation(Biome.RainType.RAIN).category(Biome.Category.NONE).depth(0.122F).scale(0.1F).temperature(1.0F).downfall(0.4F).waterColor(4159204).waterFogColor(329011).parent((String) null));
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
	public int getSkyColor() {
		return 44525;
	}
}
