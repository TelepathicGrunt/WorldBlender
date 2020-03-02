package net.telepathicgrunt.worldblender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Blocks;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.feature.SeaGrassConfig;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidWithNoiseConfig;


public class VanillaFeatureGrouping
{
	public static Map<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>> bamboofeatures = Maps.newHashMap();
	static
	{
		Map<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>> result = new HashMap<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>>();
		for (GenerationStage.Decoration generationstage$decoration : GenerationStage.Decoration.values())
		{
			result.put(generationstage$decoration, Lists.newArrayList());
		}
		
		//add all vanilla features here
		result.get(GenerationStage.Decoration.VEGETAL_DECORATION).add(Feature.BAMBOO.configure(new ProbabilityConfig(0.2F)).createDecoratedFeature(Placement.TOP_SOLID_HEIGHTMAP_NOISE_BIASED.configure(new TopSolidWithNoiseConfig(160, 80.0D, 0.3D, Heightmap.Type.WORLD_SURFACE_WG))));
		result.get(GenerationStage.Decoration.VEGETAL_DECORATION).add(Feature.BAMBOO.configure(new ProbabilityConfig(0.0F)).createDecoratedFeature(Placement.COUNT_HEIGHTMAP_DOUBLE.configure(new FrequencyConfig(16))));

		bamboofeatures = result;
	}
	
	public static Map<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>> lavaAndFirefeatures = Maps.newHashMap();
	static
	{
		Map<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>> result = new HashMap<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>>();
		for (GenerationStage.Decoration generationstage$decoration : GenerationStage.Decoration.values())
		{
			result.put(generationstage$decoration, Lists.newArrayList());
		}
		
		//add all vanilla features here
		result.get(GenerationStage.Decoration.VEGETAL_DECORATION).add(Feature.SPRING_FEATURE.configure(DefaultBiomeFeatures.LAVA_SPRING_CONFIG).createDecoratedFeature(Placement.COUNT_VERY_BIASED_RANGE.configure(new CountRangeConfig(20, 8, 16, 256))));
		result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(Feature.field_227248_z_.configure(DefaultBiomeFeatures.NETHER_FIRE_CONFIG).createDecoratedFeature(Placement.HELL_FIRE.configure(new FrequencyConfig(10))));
		result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(Feature.SPRING_FEATURE.configure(DefaultBiomeFeatures.NETHER_SPRING_CONFIG).createDecoratedFeature(Placement.COUNT_RANGE.configure(new CountRangeConfig(8, 4, 8, 128))));
		result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(Feature.field_227248_z_.configure(DefaultBiomeFeatures.NETHER_FIRE_CONFIG).createDecoratedFeature(Placement.HELL_FIRE.configure(new FrequencyConfig(10))));
		result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(Feature.SPRING_FEATURE.configure(DefaultBiomeFeatures.ENCLOSED_NETHER_SPRING_CONFIG).createDecoratedFeature(Placement.COUNT_RANGE.configure(new CountRangeConfig(16, 10, 20, 128))));
		result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(Feature.ORE.configure(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NETHERRACK, Blocks.MAGMA_BLOCK.getDefaultState(), 33)).createDecoratedFeature(Placement.MAGMA.configure(new FrequencyConfig(4))));
		result.get(GenerationStage.Decoration.UNDERGROUND_DECORATION).add(Feature.LAKE.configure(new BlockStateFeatureConfig(Blocks.LAVA.getDefaultState())).createDecoratedFeature(Placement.LAVA_LAKE.configure(new ChanceConfig(80))));

		lavaAndFirefeatures = result;
	}
	
	public static Map<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>> treefeatures = Maps.newHashMap();
	static
	{
		Map<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>> result = new HashMap<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>>();
		for (GenerationStage.Decoration generationstage$decoration : GenerationStage.Decoration.values())
		{
			result.put(generationstage$decoration, Lists.newArrayList());
		}
		
		//add all vanilla features here
//		result.get(GenerationStage.Decoration.VEGETAL_DECORATION).add(Feature.BAMBOO.configure(new ProbabilityConfig(0.2F)).createDecoratedFeature(Placement.TOP_SOLID_HEIGHTMAP_NOISE_BIASED.configure(new TopSolidWithNoiseConfig(160, 80.0D, 0.3D, Heightmap.Type.WORLD_SURFACE_WG))));
//		result.get(GenerationStage.Decoration.VEGETAL_DECORATION).add(Feature.BAMBOO.configure(new ProbabilityConfig(0.0F)).createDecoratedFeature(Placement.COUNT_HEIGHTMAP_DOUBLE.configure(new FrequencyConfig(16))));

		treefeatures = result;
	}
}
