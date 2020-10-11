package com.telepathicgrunt.world_blender.the_blender;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;

import java.util.*;


public class FeatureGrouping
{
	public static void setupFeatureMaps() 
	{
		for(GenerationStep.Feature stage : GenerationStep.Feature.values())
		{
			SMALL_PLANT_MAP.put(stage, new ArrayList<>());
			LARGE_PLANT_MAP.put(stage, new ArrayList<>());
			bambooFound = false;
		}
	}
	
	public static void clearFeatureMaps() 
	{
		SMALL_PLANT_MAP.clear();
		LARGE_PLANT_MAP.clear();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////

	private static final List<String> BAMBOO_FEATURE_KEYWORDS = Arrays.asList("bamboo");
	private static final List<String> LAGGY_STATE_KEYWORDS = Arrays.asList("lava", "fire", "bamboo", "sugar_cane");
	private static final List<String> LAGGY_FEATURE_KEYWORDS = Arrays.asList("basalt_columns", "basalt_pillar", "delta_feature");
	public static boolean bambooFound = false;
	
	/**
	 * tries to find if the feature is bamboo, sugar cane, lava, or 
	 * fire and return true if it is due to them being laggy
	 */
	public static boolean isLaggyFeature(ConfiguredFeature<?, ?> configuredFeature)
	{
		Optional<JsonElement> optionalConfiguredFeatureJSON = ConfiguredFeature.CODEC.encode(() -> configuredFeature, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();

		if(optionalConfiguredFeatureJSON.isPresent()){
			JsonElement configuredFeatureJSON = optionalConfiguredFeatureJSON.get();

			if(containsBannedFeatureName(configuredFeatureJSON, BAMBOO_FEATURE_KEYWORDS))
				bambooFound = true;

			if(containsBannedFeatureName(configuredFeatureJSON, LAGGY_FEATURE_KEYWORDS))
				return true;

			if(containsBannedState(configuredFeatureJSON, LAGGY_STATE_KEYWORDS))
				return true;

		}

		return false;
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final Map<GenerationStep.Feature, List<ConfiguredFeature<?, ?>>> SMALL_PLANT_MAP = Maps.newHashMap();
	private static final List<String> SMALL_PLANT_KEYWORDS = Arrays.asList("grass", "flower", "rose", "plant", "bush", "fern");

	/**
	 * Will check if incoming configuredfeature is a small plant and add it to the small plant map if it is so 
	 * we can have a list of them for specific feature manipulation later
	 */
	public static boolean checksAndAddSmallPlantFeatures(GenerationStep.Feature stage, ConfiguredFeature<?, ?> configuredFeature) 
	{
		//if small plant is already added, skip it
		if(SMALL_PLANT_MAP.get(stage).stream().anyMatch(vanillaConfigFeature -> serializeAndCompareFeature(vanillaConfigFeature, configuredFeature, true)))
		{
			return false;
		}


		Optional<JsonElement> optionalConfiguredFeatureJSON = ConfiguredFeature.CODEC.encode(() -> configuredFeature, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();

		if(optionalConfiguredFeatureJSON.isPresent()) {
			JsonElement configuredFeatureJSON = optionalConfiguredFeatureJSON.get();

			if (containsBannedFeatureName(configuredFeatureJSON, SMALL_PLANT_KEYWORDS) ||
					containsBannedState(configuredFeatureJSON, SMALL_PLANT_KEYWORDS)) {

				SMALL_PLANT_MAP.get(stage).add(configuredFeature);
				return true;
			}
		}

		
		return false;
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//while we are storing large plants into this map, we don't use it at the moment as we just
	//need to identify what's a large plant and move it to the front of the feature list.
	public static final Map<GenerationStep.Feature, List<ConfiguredFeature<?, ?>>> LARGE_PLANT_MAP = Maps.newHashMap();
	private static final List<String> LARGE_PLANT_KEYWORDS = Arrays.asList("tree", "huge_mushroom", "big_mushroom", "poplar", "twiglet", "mangrove", "bramble");
	
	/**
	 * Will check if incoming configuredfeature is a large plant and add it to the Large plant map if it is so 
	 * we can have a list of them for specific feature manipulation later
	 */
	public static boolean checksAndAddLargePlantFeatures(GenerationStep.Feature stage, ConfiguredFeature<?, ?> configuredFeature) 
	{
		//if large plant is already added, skip it
		if(LARGE_PLANT_MAP.get(stage).stream().anyMatch(vanillaConfigFeature -> serializeAndCompareFeature(vanillaConfigFeature, configuredFeature, true)))
		{
			return false;
		}


		Optional<JsonElement> optionalConfiguredFeatureJSON = ConfiguredFeature.CODEC.encode(() -> configuredFeature, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();

		if(optionalConfiguredFeatureJSON.isPresent()) {
			JsonElement configuredFeatureJSON = optionalConfiguredFeatureJSON.get();

			if (containsBannedFeatureName(configuredFeatureJSON, LARGE_PLANT_KEYWORDS) ||
					containsBannedState(configuredFeatureJSON, LARGE_PLANT_KEYWORDS)) {

				LARGE_PLANT_MAP.get(stage).add(configuredFeature);
				return true;
			}
		}

		return false;
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//UTILS
	

	/**
	 * Look to see if any of the banned words are in the json state object
	 *
	 * If you get crossed-eye, that normal.
	 * Checks if the state's name block contains a banned word.
	 */
	private static boolean containsBannedState(JsonElement jsonElement, List<String> keywordList)
	{
		JsonObject jsonStartObject = jsonElement.getAsJsonObject();
		for(Map.Entry<String, JsonElement> entry : jsonStartObject.entrySet()){
			if(entry.getKey().equals("state")){
				JsonObject jsonStateObject = entry.getValue().getAsJsonObject();
				if(jsonStateObject.has("Name")){
					String blockPath = jsonStateObject.get("Name").getAsString().split(":")[1];
					for(String keyword : keywordList) {
						if(blockPath.contains(keyword)) return true;
					}
				}
			}
			else if(entry.getValue().isJsonObject()){
				return containsBannedState(entry.getValue().getAsJsonObject(), keywordList);
			}
		}

		return false;
	}


	/**
	 * Look to see if any of the banned words are in the json feature object
	 * This is gonna check if the bottommost type or default contains a banned word
	 */
	private static boolean containsBannedFeatureName(JsonElement jsonElement, List<String> keywordList)
	{
		String stringToCheck = getsFeatureName(jsonElement);
		return keywordList.stream().anyMatch(stringToCheck::contains);
	}


	/**
	 * Gets the Feature's name being used
	 *
	 * If you get crossed-eye, that normal. I blame mojang's json format being so cursed and random.
	 */
	private static String getsFeatureName(JsonElement jsonElement)
	{
		JsonObject jsonStartObject = jsonElement.getAsJsonObject();

		if(jsonStartObject.has("config")){

			JsonObject jsonConfigObject = jsonStartObject.get("config").getAsJsonObject();

			if(jsonConfigObject.has("features")){
				JsonElement jsonFeatureElement = jsonConfigObject.get("features");

				// Handles vanilla's one freaking feature that holds MULTIPLE features for no reason! (trees usually)
				if(jsonFeatureElement.isJsonArray()){
					StringBuilder allFeatures = new StringBuilder();

					for(JsonElement entry : jsonFeatureElement.getAsJsonArray()){
						allFeatures.append(entry.toString()).append(" ");
					}

					if(jsonConfigObject.has("default")){
						allFeatures.append(jsonConfigObject.get("default").toString()).append(" ");
						return allFeatures.toString();
					}
					else if(jsonConfigObject.has("type")){
						allFeatures.append(jsonConfigObject.get("type").toString()).append(" ");
						return allFeatures.toString();
					}
				}
			}
			else if(jsonConfigObject.has("feature")){

				JsonElement jsonFeatureElement = jsonConfigObject.get("feature");

				if(jsonFeatureElement.isJsonObject()){
					return getsFeatureName(jsonFeatureElement);
				}
			}
			else if(jsonStartObject.has("type")){
				return jsonStartObject.toString();
			}

		}
		else if(jsonStartObject.has("type")){
			return jsonStartObject.get("type").toString();
		}

		return "";
	}


	/**
	 * Will serialize (if possible) both features and check if they are the same feature.
	 * If cannot serialize, compare the feature itself to see if it is the same.
	 */
	public static boolean serializeAndCompareFeature(ConfiguredFeature<?, ?> configuredFeature1, ConfiguredFeature<?, ?> configuredFeature2, boolean doDeepJSONCheck) {

		Optional<JsonElement> optionalJsonElement1 = ConfiguredFeature.CODEC.encode(() -> configuredFeature1, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();
		Optional<JsonElement> optionalJsonElement2 = ConfiguredFeature.CODEC.encode(() -> configuredFeature2, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();

		// Compare the JSON to see if it's the exact same ConfiguredFeature.
		if(optionalJsonElement1.isPresent() &&
			optionalJsonElement2.isPresent())
		{
			JsonElement configuredFeatureJSON1 = optionalJsonElement1.get();
			JsonElement configuredFeatureJSON2 = optionalJsonElement2.get();

			return configuredFeatureJSON1.toString().equals(configuredFeatureJSON2.toString()) ||
					(doDeepJSONCheck && getsFeatureName(configuredFeatureJSON1).equals(getsFeatureName(configuredFeatureJSON2)));
		}

		return configuredFeature1.equals(configuredFeature2);
	}


	/**
	 * Will serialize (if possible) both features and check if they are the same feature.
	 * If cannot serialize, compare the feature itself to see if it is the same.
	 */

	public static boolean serializeAndCompareStructureJSONOnly(ConfiguredStructureFeature<?, ?> configuredStructure1, ConfiguredStructureFeature<?, ?> configuredStructure2) {

		Optional<JsonElement> optionalJsonElement1 = ConfiguredStructureFeature.CODEC.encode(configuredStructure1, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();
		Optional<JsonElement> optionalJsonElement2 = ConfiguredStructureFeature.CODEC.encode(configuredStructure2, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();

		// Compare the JSON to see if it's the exact same ConfiguredFeature.
		if(optionalJsonElement1.isPresent() &&
				optionalJsonElement2.isPresent())
		{
			JsonElement configuredFeatureJSON1 = optionalJsonElement1.get();
			JsonElement configuredFeatureJSON2 = optionalJsonElement2.get();

			return configuredFeatureJSON1.toString().equals(configuredFeatureJSON2.toString());
		}

		return configuredStructure1.equals(configuredStructure2);
	}
}
