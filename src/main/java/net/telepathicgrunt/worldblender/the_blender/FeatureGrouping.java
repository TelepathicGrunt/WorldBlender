package net.telepathicgrunt.worldblender.the_blender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Maps;
import com.mojang.datafixers.Dynamic;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.BlockWithContextConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredRandomFeatureList;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.DecoratedFlowerFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.LiquidsConfig;
import net.minecraft.world.gen.feature.MultipleRandomFeatureConfig;
import net.minecraft.world.gen.feature.MultipleWithChanceRandomFeatureConfig;
import net.minecraft.world.gen.feature.SingleRandomFeature;
import net.minecraft.world.gen.feature.TwoFeatureChoiceConfig;
import net.telepathicgrunt.worldblender.WorldBlender;


public class FeatureGrouping
{
	public static void setupFeatureMaps() 
	{
		for(GenerationStage.Decoration stage : GenerationStage.Decoration.values())
		{
			SMALL_PLANT_MAP.put(stage, new ArrayList<ConfiguredFeature<?,?>>());
			LARGE_PLANT_MAP.put(stage, new ArrayList<ConfiguredFeature<?,?>>());
		}
	}
	
	public static void clearFeatureMaps() 
	{
		SMALL_PLANT_MAP.clear();
		LARGE_PLANT_MAP.clear();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	private static final List<String> LAGGY_FEATURE_KEYWORDS = Arrays.asList("lava","fire","bamboo","sugar_cane");
	
	/**
	 * tries to find if the feature is bamboo, sugar cane, lava, or 
	 * fire and return true if it is due to them being laggy
	 */
	public static boolean isLaggyFeature(GenerationStage.Decoration stage, ConfiguredFeature<?, ?> configuredFeature) 
	{
		if(configuredFeature.config instanceof DecoratedFeatureConfig)
		{
			DecoratedFeatureConfig decoratedConfig = (DecoratedFeatureConfig)configuredFeature.config;
			ResourceLocation rl = null;
			
			//A bunch of edge cases that have to handled because features can hold other features.
			//If a mod makes a custom feature to hold features, welp, we are screwed. Nothing we can do about it. 
			if(decoratedConfig.feature.feature == Feature.RANDOM_RANDOM_SELECTOR)
			{
				for(ConfiguredFeature<?, ?> nestedConfiguredFeature : ((MultipleWithChanceRandomFeatureConfig)decoratedConfig.feature.config).features)
				{
					rl = nestedConfiguredFeature.feature.getRegistryName();
					if(keywordFound(rl.getPath(), LAGGY_FEATURE_KEYWORDS))
						return true;
				}
			}
			else if(decoratedConfig.feature.feature == Feature.RANDOM_SELECTOR)
			{
				for(ConfiguredRandomFeatureList<?> nestedConfiguredFeature : ((MultipleRandomFeatureConfig)decoratedConfig.feature.config).features)
				{
					rl = nestedConfiguredFeature.feature.feature.getRegistryName();
					if(keywordFound(rl.getPath(), LAGGY_FEATURE_KEYWORDS))
						return true;
				}
			}
			else if(decoratedConfig.feature.feature == Feature.SIMPLE_RANDOM_SELECTOR)
			{
				for(ConfiguredFeature<?, ?> nestedConfiguredFeature : ((SingleRandomFeature)decoratedConfig.feature.config).features)
				{
					rl = nestedConfiguredFeature.feature.getRegistryName();
					if(keywordFound(rl.getPath(), LAGGY_FEATURE_KEYWORDS))
						return true;
				}
			}
			else if(decoratedConfig.feature.feature == Feature.RANDOM_BOOLEAN_SELECTOR)
			{
				rl = ((TwoFeatureChoiceConfig)decoratedConfig.feature.config).field_227285_a_.feature.getRegistryName();
				if(keywordFound(rl.getPath(), LAGGY_FEATURE_KEYWORDS))
					return true;
				
				rl = ((TwoFeatureChoiceConfig)decoratedConfig.feature.config).field_227286_b_.feature.getRegistryName();
				if(keywordFound(rl.getPath(), LAGGY_FEATURE_KEYWORDS))
					return true;
			}
			//end of edge cases with nested features
			else if(decoratedConfig.feature.feature == Feature.LAKE)
			{
				rl = ((BlockStateFeatureConfig)decoratedConfig.feature.config).field_227270_a_.getBlock().getRegistryName();
			}
			else if(decoratedConfig.feature.feature == Feature.SIMPLE_BLOCK)
			{
				rl = ((BlockWithContextConfig)decoratedConfig.feature.config).toPlace.getBlock().getRegistryName();
			}
			else if(decoratedConfig.feature.feature == Feature.SPRING_FEATURE)
			{
				rl = ((LiquidsConfig)decoratedConfig.feature.config).state.getBlockState().getBlock().getBlock().getRegistryName();
			}
			else
			{
				rl = decoratedConfig.feature.feature.getRegistryName();
			}

			//checks rl of non-nested feature's block or itself
			if(rl != null && keywordFound(rl.getPath(), LAGGY_FEATURE_KEYWORDS))
				return true;
			
		}
		else if(!(configuredFeature.feature instanceof DecoratedFlowerFeature))
		{
			WorldBlender.LOGGER.log(Level.INFO, "Error with trying to detect laggy features: "+ configuredFeature.feature.getRegistryName().toString());
		}
		
		return false;
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Map<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>> SMALL_PLANT_MAP = Maps.newHashMap();
	private static final List<String> SMALL_PLANT_KEYWORDS = Arrays.asList("grass","flower","rose","plant","bush","fern");

	/**
	 * Will check if incoming configuredfeature is a small plant and add it to the small plant map if it is so 
	 * we can have a list of them for specific feature manipulation later
	 */
	public static boolean checksAndAddSmallPlantFeatures(GenerationStage.Decoration stage, ConfiguredFeature<?, ?> configuredFeature) 
	{
		//if small plant is already added, skip it
		if(SMALL_PLANT_MAP.get(stage).stream().anyMatch(vanillaConfigFeature -> serializeAndCompareFeature(vanillaConfigFeature, configuredFeature)))
		{
			return false;
		}
		
		
		if(configuredFeature.feature instanceof DecoratedFlowerFeature)
		{
			//is flower already, add it to map
			SMALL_PLANT_MAP.get(stage).add(configuredFeature);
			return true;
		}
		else if(configuredFeature.config instanceof DecoratedFeatureConfig)
		{
			DecoratedFeatureConfig decoratedConfig = (DecoratedFeatureConfig)configuredFeature.config;
			ResourceLocation rl;
			
			//A bunch of edge cases that have to handled because features can hold other features.
			//If a mod makes a custom feature to hold features, welp, we are screwed. Nothing we can do about it. 
			if(decoratedConfig.feature.feature == Feature.FLOWER)
			{
				//is flower already, add it to map
				SMALL_PLANT_MAP.get(stage).add(configuredFeature);
				return true;
			}
			else if(decoratedConfig.feature.feature == Feature.RANDOM_PATCH)
			{
				rl = ((BlockClusterFeatureConfig)decoratedConfig.feature.config).stateProvider.getBlockState(new Random(0), BlockPos.ZERO).getBlock().getRegistryName();
				if(rl != null && keywordFound(rl.getPath(), SMALL_PLANT_KEYWORDS))
				{
					SMALL_PLANT_MAP.get(stage).add(configuredFeature);
					return true;
				}
			}
			
		}
		else
		{
			WorldBlender.LOGGER.log(Level.INFO, "Error with trying to detect plant: "+ configuredFeature.feature.getRegistryName().toString());
		}
		
		
		return false;
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//while we are storing large plants into this map, we don't use it at the moment as we just
	//need to identify what's a large plant and move it to the front of the feature list.
	public static final Map<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>> LARGE_PLANT_MAP = Maps.newHashMap();
	private static final List<String> LARGE_PLANT_KEYWORDS = Arrays.asList("tree","huge_mushroom","big_mushroom","poplar","twiglet","mangrove","bramble");
	
	/**
	 * Will check if incoming configuredfeature is a large plant and add it to the Large plant map if it is so 
	 * we can have a list of them for specific feature manipulation later
	 */
	public static boolean checksAndAddLargePlantFeatures(GenerationStage.Decoration stage, ConfiguredFeature<?, ?> configuredFeature) 
	{
		//if large plant is already added, skip it
		if(LARGE_PLANT_MAP.get(stage).stream().anyMatch(vanillaConfigFeature -> serializeAndCompareFeature(vanillaConfigFeature, configuredFeature)))
		{
			return false;
		}
		
		if(configuredFeature.config instanceof DecoratedFeatureConfig)
		{
			DecoratedFeatureConfig decoratedConfig = (DecoratedFeatureConfig)configuredFeature.config;
			ResourceLocation rl = null;
			
			//A bunch of edge cases that have to handled because features can hold other features.
			//If a mod makes a custom feature to hold features, welp, we are screwed. Nothing we can do about it. 
			if(decoratedConfig.feature.feature == Feature.RANDOM_RANDOM_SELECTOR)
			{
				for(ConfiguredFeature<?, ?> nestedConfiguredFeature : ((MultipleWithChanceRandomFeatureConfig)decoratedConfig.feature.config).features)
				{
					rl = nestedConfiguredFeature.feature.getRegistryName();
					if(addFeatureToLargePlantMap(rl, configuredFeature,stage))
						return true;
				}
			}
			else if(decoratedConfig.feature.feature == Feature.RANDOM_SELECTOR)
			{
				for(ConfiguredRandomFeatureList<?> nestedConfiguredFeature : ((MultipleRandomFeatureConfig)decoratedConfig.feature.config).features)
				{
					rl = nestedConfiguredFeature.feature.feature.getRegistryName();
					if(addFeatureToLargePlantMap(rl, configuredFeature,stage))
						return true;
				}
			}
			else if(decoratedConfig.feature.feature == Feature.SIMPLE_RANDOM_SELECTOR)
			{
				for(ConfiguredFeature<?, ?> nestedConfiguredFeature : ((SingleRandomFeature)decoratedConfig.feature.config).features)
				{
					rl = nestedConfiguredFeature.feature.getRegistryName();
					if(addFeatureToLargePlantMap(rl, configuredFeature,stage))
						return true;
				}
			}
			else if(decoratedConfig.feature.feature == Feature.RANDOM_BOOLEAN_SELECTOR)
			{
				rl = ((TwoFeatureChoiceConfig)decoratedConfig.feature.config).field_227285_a_.feature.getRegistryName();
				if(addFeatureToLargePlantMap(rl, configuredFeature,stage))
					return true;
				
				rl = ((TwoFeatureChoiceConfig)decoratedConfig.feature.config).field_227286_b_.feature.getRegistryName();
				if(addFeatureToLargePlantMap(rl, configuredFeature,stage))
					return true;
			}
			else
			{
				rl = decoratedConfig.feature.feature.getRegistryName();
				if(addFeatureToLargePlantMap(rl, configuredFeature,stage))
					return true;
			}
		}
		else
		{
			WorldBlender.LOGGER.log(Level.INFO, "Error with trying to detect trees: "+ configuredFeature.feature.getRegistryName().toString());
		}
		
		return false;
	}
	
	/*
	 * Adds large plant found to the large plant map if rl isn't null
	 */
	private static boolean addFeatureToLargePlantMap(ResourceLocation rl, ConfiguredFeature<?,?> configuredFeature, GenerationStage.Decoration stage) 
	{
		if(rl != null) 
		{
			if(keywordFound(rl.getPath(), LARGE_PLANT_KEYWORDS))
			{
				LARGE_PLANT_MAP.get(stage).add(configuredFeature);
				return true;
			}
		}
		
		return false;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//UTILS
	
	
	/**
	 * Takes the feature's path/name and checks if it contains a keyword from a list anywhere in it.
	 */
	private static boolean keywordFound(String featurePath, List<String> keywordList) 
	{
		for(String keyword : keywordList)
		{
			if(featurePath.contains(keyword))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Will serialize (if possible) both features and check if they are the same feature.
	 * If cannot serialize, compare the feature itself to see if it is the same
	 */
	public static boolean serializeAndCompareFeature(ConfiguredFeature<?, ?> feature1, ConfiguredFeature<?, ?> feature2)
	{
		try
		{
			Map<Dynamic<INBT>, Dynamic<INBT>> feature1Map = feature1.serialize(NBTDynamicOps.INSTANCE).getMapValues().get();
			Map<Dynamic<INBT>, Dynamic<INBT>> feature2Map = feature2.serialize(NBTDynamicOps.INSTANCE).getMapValues().get();

			if (feature1Map != null && feature2Map != null)
			{
				return feature1Map.equals(feature2Map);
			}
		}
		catch (Exception e)
		{
			//One of the features cannot be serialized which can only happen with custom modded features
			//Check if the features are the same feature even though the placement or config for the feature might be different. 
			//This is the best way we can remove duplicate modded features as best as we can. (I think)
			if ((feature1.config instanceof DecoratedFeatureConfig && feature2.config instanceof DecoratedFeatureConfig) && 
				((DecoratedFeatureConfig) feature1.config).feature.feature == ((DecoratedFeatureConfig) feature2.config).feature.feature)
			{
				return true;
			}
		}

		return false;
	}
}
