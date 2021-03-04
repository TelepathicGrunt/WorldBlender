package com.telepathicgrunt.worldblender.theblender;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.*;


public class FeatureGrouping {
	private static final List<String> BAMBOO_FEATURE_KEYWORDS = ImmutableList.of("bamboo");
	private static final List<String> LAGGY_STATE_KEYWORDS = ImmutableList.of("lava", "fire", "bamboo", "sugar_cane");
	private static final List<String> LAGGY_FEATURE_KEYWORDS = ImmutableList.of("basalt_columns", "basalt_pillar", "delta_feature");
	private static final List<String> SMALL_PLANT_KEYWORDS = ImmutableList.of("grass", "flower", "rose", "plant", "bush", "fern");
	private static final List<String> LARGE_PLANT_KEYWORDS = ImmutableList.of("tree", "huge_mushroom", "big_mushroom", "poplar", "twiglet", "mangrove", "bramble");
	
	public final Map<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>> smallPlants = Maps.newHashMap();
	//while we are storing large plants into this map, we don't use it at the moment as we just
	//need to identify what's a large plant and move it to the front of the feature list.
	public final Map<GenerationStage.Decoration, List<ConfiguredFeature<?, ?>>> largePlants = Maps.newHashMap();
	public boolean bambooFound = false;
	
	public FeatureGrouping() {
		for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
			smallPlants.put(stage, new ArrayList<>());
			largePlants.put(stage, new ArrayList<>());
		}
	}
	
	/**
	 Tries to find if the feature is bamboo, sugar cane, lava, or
	 fire and return true if it is due to them being laggy.
	 */
	public boolean isLaggy(ConfiguredFeature<?, ?> configuredFeature) {
		Optional<JsonElement> _configuredFeatureJSON = encode(configuredFeature);
		if (!_configuredFeatureJSON.isPresent()) return false;
		JsonElement configuredFeatureJSON = _configuredFeatureJSON.get();
		
		if (containsBannedFeatureName(configuredFeatureJSON, BAMBOO_FEATURE_KEYWORDS) ||
			containsBannedState(configuredFeatureJSON, BAMBOO_FEATURE_KEYWORDS)) {
			bambooFound = true;
			return true;
		}
		
		return containsBannedFeatureName(configuredFeatureJSON, LAGGY_FEATURE_KEYWORDS)
			|| containsBannedState(configuredFeatureJSON, LAGGY_STATE_KEYWORDS);
	}
	
	/**
	 Will check if incoming configuredfeature is a small plant and add it to the small plant map if it is so
	 we can have a list of them for specific feature manipulation later
	 */
	public boolean checkAndAddSmallPlantFeatures(GenerationStage.Decoration stage, ConfiguredFeature<?, ?> configuredFeature) {
		boolean alreadyPresent = smallPlants.get(stage).stream().anyMatch(existing ->
			serializeAndCompareFeature(existing, configuredFeature, true)
		);
		if (alreadyPresent) return false; // TODO: should this actually return true?
		
		Optional<JsonElement> _configuredFeatureJSON = encode(configuredFeature);
		if (!_configuredFeatureJSON.isPresent()) return false;
		JsonElement configuredFeatureJSON = _configuredFeatureJSON.get();
		
		boolean isSmallPlant = containsBannedFeatureName(configuredFeatureJSON, SMALL_PLANT_KEYWORDS)
			|| containsBannedState(configuredFeatureJSON, SMALL_PLANT_KEYWORDS);
		if (!isSmallPlant) return false;
		
		smallPlants.get(stage).add(configuredFeature);
		return true;
	}
	
	/**
	 Will check if incoming configuredfeature is a large plant and add it to the Large plant map if it is so
	 we can have a list of them for specific feature manipulation later
	 */
	public boolean checkAndAddLargePlantFeatures(GenerationStage.Decoration stage, ConfiguredFeature<?, ?> configuredFeature) {
		//if large plant is already added, skip it
		boolean alreadyPresent = largePlants.get(stage).stream().anyMatch(existing ->
			serializeAndCompareFeature(existing, configuredFeature, true)
		);
		if (alreadyPresent) return false; // TODO: should this actually return true?
		
		Optional<JsonElement> _configuredFeatureJSON = encode(configuredFeature);
		if (!_configuredFeatureJSON.isPresent()) return false;
		JsonElement configuredFeatureJSON = _configuredFeatureJSON.get();
		
		boolean isLargePlant = containsBannedFeatureName(configuredFeatureJSON, LARGE_PLANT_KEYWORDS)
			|| containsBannedState(configuredFeatureJSON, LARGE_PLANT_KEYWORDS);
		if (!isLargePlant) return false;
		
		largePlants.get(stage).add(configuredFeature);
		return true;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// UTILS
	
	/**
	 Look to see if any of the banned words are in the json state object
	 
	 If you get crossed-eye, that normal.
	 Checks if the state's name block contains a banned word.
	 */
	private static boolean containsBannedState(JsonElement jsonElement, List<String> keywordList) {
		JsonObject jsonStartObject = jsonElement.getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonStartObject.entrySet()) {
			if (entry.getKey().equals("state")) {
				JsonObject state = entry.getValue().getAsJsonObject();
				
				JsonElement name = state.get("Name");
				if (name == null) continue;
				
				String blockPath = name.getAsString().split(":")[1];
				for (String keyword : keywordList) {
					if (blockPath.contains(keyword)) return true;
				}
			} else if (entry.getValue().isJsonObject()) {
				// TODO: is this correct? would return false before checking other entries
				return containsBannedState(entry.getValue().getAsJsonObject(), keywordList);
			}
		}
		
		return false;
	}
	
	/**
	 Look to see if any of the banned words are in the json feature object
	 This is gonna check if the bottommost type or default contains a banned word
	 */
	private static boolean containsBannedFeatureName(JsonElement jsonElement, List<String> keywordList) {
		String stringToCheck = getFeatureName(jsonElement);
		return keywordList.stream().anyMatch(stringToCheck::contains);
	}
	
	/**
	 Gets the Feature's name being used
	 
	 If you get crossed-eye, that normal. I blame mojang's json format being so cursed and random.
	 */
	private static String getFeatureName(JsonElement jsonElement) {
		JsonObject root = jsonElement.getAsJsonObject();
		
		// TODO: I feel like this logic could be simplified a _lot_
		
		JsonElement _config = root.get("config");
		if (_config != null) {
			JsonObject config = _config.getAsJsonObject();
			
			JsonElement features = config.get("features");
			if (features != null) {
				// Handles vanilla's one freaking feature that holds MULTIPLE features for no reason! (trees usually)
				if (features.isJsonArray()) {
					StringBuilder allFeatures = new StringBuilder();
					
					for (JsonElement entry : features.getAsJsonArray()) {
						allFeatures.append(entry.toString()).append(" ");
					}
					
					JsonElement defaultValue = config.get("default");
					if (defaultValue != null) {
						allFeatures.append(defaultValue.toString()).append(" ");
					}
					
					JsonElement typeValue = config.get("type");
					if (typeValue != null) {
						allFeatures.append(typeValue.toString()).append(" ");
					}
					
					return allFeatures.toString();
				}
			} else if (config.has("feature")) {
				JsonElement jsonFeatureElement = config.get("feature");
				
				if (jsonFeatureElement.isJsonObject()) {
					return getFeatureName(jsonFeatureElement);
				}
			} else if (root.has("type")) {
				return root.toString();
			}
		}
		
		JsonElement type = root.get("type");
		if (type != null) {
			return type.toString();
		}
		
		return root.toString();
	}
	
	/**
	 Will serialize (if possible) both features and check if they are the same feature.
	 If cannot serialize, compare the feature itself to see if it is the same.
	 */
	public static boolean serializeAndCompareFeature(ConfiguredFeature<?, ?> configuredFeature1, ConfiguredFeature<?, ?> configuredFeature2, boolean doDeepJSONCheck) {
		Optional<JsonElement> optionalJsonElement1 = encode(configuredFeature1);
		Optional<JsonElement> optionalJsonElement2 = encode(configuredFeature2);
		
		// Compare the JSON to see if it's the exact same ConfiguredFeature.
		if (optionalJsonElement1.isPresent() &&
			optionalJsonElement2.isPresent()) {
			JsonElement configuredFeatureJSON1 = optionalJsonElement1.get();
			JsonElement configuredFeatureJSON2 = optionalJsonElement2.get();
			
			return configuredFeatureJSON1.toString().equals(configuredFeatureJSON2.toString()) ||
				(doDeepJSONCheck && getFeatureName(configuredFeatureJSON1).equals(getFeatureName(configuredFeatureJSON2)));
		}
		
		return configuredFeature1.equals(configuredFeature2);
	}
	
	// TODO: this seems to be unused
	/**
	 Will serialize (if possible) both features and check if they are the same feature.
	 If cannot serialize, compare the feature itself to see if it is the same.
	 */
	public static boolean serializeAndCompareStructureJSONOnly(StructureFeature<?, ?> configuredStructure1, StructureFeature<?, ?> configuredStructure2) {
		Optional<JsonElement> _json1 = StructureFeature.field_236267_a_.encode(configuredStructure1, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();
		Optional<JsonElement> _json2 = StructureFeature.field_236267_a_.encode(configuredStructure2, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();
		
		// Compare the JSON to see if it's the exact same ConfiguredFeature.
		if (!_json1.isPresent() || !_json2.isPresent()) {
			return configuredStructure1.equals(configuredStructure2);
		}
		
		JsonElement json1 = _json1.get();
		JsonElement json2 = _json2.get();
		return json1.toString().equals(json2.toString());
	}
	
	private static Optional<JsonElement> encode(ConfiguredFeature<?, ?> feature) {
		return ConfiguredFeature.field_242763_a.encode(feature, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).get().left();
	}
}
