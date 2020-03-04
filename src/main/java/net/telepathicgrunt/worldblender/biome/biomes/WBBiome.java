package net.telepathicgrunt.worldblender.biome.biomes;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;


public class WBBiome extends Biome
{

	protected WBBiome(Builder builder)
	{
		super(builder);
	}


	@SuppressWarnings("deprecation")
	public void decorate(GenerationStage.Decoration generationStage, ChunkGenerator<? extends GenerationSettings> generatorSettings, IWorld world, long seed, SharedSeedRandom random, BlockPos blockPos)
	{
		//skip feature gen for center 6 chunks in world except for snow and altar
		//if(blockPos.getX() < 17 && blockPos.getZ() < 17 && blockPos.getX() > -33 && blockPos.getZ() > -33 && generationStage != GenerationStage.Decoration.TOP_LAYER_MODIFICATION) return; 

		int i = 0;
		for (ConfiguredFeature<?, ?> configuredfeature : this.features.get(generationStage))
		{
			random.setFeatureSeed(seed, i, generationStage.ordinal());

			try
			{
				configuredfeature.place(world, generatorSettings, random, blockPos);
			}
			catch (Exception exception)
			{
				CrashReport crashreport = CrashReport.makeCrashReport(exception, "Feature placement");
				crashreport.makeCategory("Feature").addDetail("Id", Registry.FEATURE.getKey(configuredfeature.feature)).addDetail("Description", () ->
				{
					return configuredfeature.feature.toString();
				});
				throw new ReportedException(crashreport);
			}

			++i;
		}
	}
}
