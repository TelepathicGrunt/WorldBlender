package net.telepathicgrunt.worldblender.the_blender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.HellLavaConfig;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.LakesConfig;
import net.minecraft.world.gen.feature.LiquidsConfig;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.LakeChanceConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidWithNoiseConfig;


public class VanillaFeatureGrouping
{
	public static Map<GenerationStage.Decoration, List<ConfiguredFeature<?>>> bamboofeatures = Maps.newHashMap();
	static
	{
		Map<GenerationStage.Decoration, List<ConfiguredFeature<?>>> result = new HashMap<GenerationStage.Decoration, List<ConfiguredFeature<?>>>();
		for (GenerationStage.Decoration generationstage$decoration : GenerationStage.Decoration.values())
		{
			result.put(generationstage$decoration, Lists.newArrayList());
		}
		
		//add all vanilla features here
		result.get(GenerationStage.Decoration.VEGETAL_DECORATION).add(new ConfiguredFeature<>(Feature.DECORATED, new DecoratedFeatureConfig(Feature.BAMBOO, new ProbabilityConfig(0.0F), Placement.COUNT_HEIGHTMAP_DOUBLE, new FrequencyConfig(16))));
		result.get(GenerationStage.Decoration.VEGETAL_DECORATION).add(new ConfiguredFeature<>(Feature.DECORATED, new DecoratedFeatureConfig(Feature.BAMBOO, new ProbabilityConfig(0.2F), Placement.TOP_SOLID_HEIGHTMAP_NOISE_BIASED, new TopSolidWithNoiseConfig(160, 80.0D, 0.3D, Heightmap.Type.WORLD_SURFACE_WG))));
	      
		bamboofeatures = result;
	}
	
	public static Map<GenerationStage.Decoration, List<ConfiguredFeature<?>>> lavaAndFirefeatures = Maps.newHashMap();
	static
	{
		Map<GenerationStage.Decoration, List<ConfiguredFeature<?>>> result = new HashMap<GenerationStage.Decoration, List<ConfiguredFeature<?>>>();
		for (GenerationStage.Decoration generationstage$decoration : GenerationStage.Decoration.values())
		{
			result.put(generationstage$decoration, Lists.newArrayList());
		}
		
		//add bad vanilla features here
		result.get(GenerationStage.Decoration.VEGETAL_DECORATION).add(new ConfiguredFeature<>(Feature.DECORATED, new DecoratedFeatureConfig(Feature.SPRING_FEATURE,  new LiquidsConfig(Fluids.LAVA.getDefaultState()), Placement.COUNT_VERY_BIASED_RANGE, new CountRangeConfig(20, 8, 16, 256))));
	    result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(new ConfiguredFeature<>(Feature.DECORATED, new DecoratedFeatureConfig(Feature.NETHER_SPRING, new HellLavaConfig(false), Placement.COUNT_RANGE, new CountRangeConfig(8, 4, 8, 128))));
	    result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(new ConfiguredFeature<>(Feature.DECORATED, new DecoratedFeatureConfig(Feature.HELL_FIRE, IFeatureConfig.NO_FEATURE_CONFIG, Placement.HELL_FIRE, new FrequencyConfig(10))));
	    result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(new ConfiguredFeature<>(Feature.DECORATED, new DecoratedFeatureConfig(Feature.NETHER_SPRING, new HellLavaConfig(true), Placement.COUNT_RANGE, new CountRangeConfig(16, 10, 20, 128))));
	    result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(new ConfiguredFeature<>(Feature.DECORATED, new DecoratedFeatureConfig(Feature.LAKE, new LakesConfig(Blocks.LAVA.getDefaultState()), Placement.LAVA_LAKE, new LakeChanceConfig(80))));
	    result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(new ConfiguredFeature<>(Feature.DECORATED, new DecoratedFeatureConfig(Feature.SPRING_FEATURE, new LiquidsConfig(Fluids.LAVA.getDefaultState()), Placement.COUNT_VERY_BIASED_RANGE, new CountRangeConfig(20, 8, 16, 256))));
	    
		lavaAndFirefeatures = result;
	}
	
}
