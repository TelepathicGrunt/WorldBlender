package net.telepathicgrunt.worldblender.the_blender.dedicated_mod_support;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.terraforged.fm.FeatureSerializer;
import com.terraforged.fm.data.DataManager;
import com.terraforged.fm.template.TemplateManager;
import com.terraforged.fm.template.feature.TemplateFeature;
import com.terraforged.mod.chunk.settings.SettingsHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.telepathicgrunt.worldblender.biome.WBBiomes;
import net.telepathicgrunt.worldblender.configs.WBConfig;
import net.telepathicgrunt.worldblender.the_blender.ConfigBlacklisting;
import net.telepathicgrunt.worldblender.the_blender.ConfigBlacklisting.BlacklistType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TerraForgedCompatibility {
	public static void addTerraForgedtrees() {
		if (!WBConfig.allowModdedFeatures || ConfigBlacklisting.isResourceLocationBlacklisted(BlacklistType.BLANKET, new ResourceLocation("terraforged:_"))) {
			return;
		}

		// find terraforge trees/features and adds them
		for (Biome blendedBiome : WBBiomes.biomes) {
			for (ConfiguredFeature<?, ?> configuredFeature : getAllFeatures()) {
				blendedBiome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, configuredFeature);
			}
		}
	}



	// Special thanks to Dags for the below code to grab his features! //
	// https://gist.github.com/dags-/a0b4783ed63421821f0b56a9591cf5fc

	private static final String[] INJECTIONS = {"replace", "head", "tail", "before", "after"};

	public static List<ConfiguredFeature<?, ?>> getAllFeatures() {
		List<ConfiguredFeature<?, ?>> features = new ArrayList<>(32);

		// DataManager is essentially a wrapper around IResourceManager
		try (DataManager dataManager = DataManager.of(new File(SettingsHelper.SETTINGS_DIR, "datapacks"))) {
			// The template manager loads/caches the nbt template/schematics so that they exist
			// when deserializing the FeatureConfigs
			TemplateManager.getInstance().load(dataManager);

			// Iterate all json files under /data/terraforged/features
			// You can do sub-dirs if you only want to load specific things like trees "features/trees"
			forEach(dataManager, "features", features::add);

			// Tidy up cached data
			TemplateManager.getInstance().clear();
		}

		return features;
	}

	public static void forEach(DataManager dataManager, String path, Consumer<ConfiguredFeature<?, ?>> consumer) {
		// Iterate all json entries under /data/terraforged/<path>
		dataManager.forEachJson(path, (name, injectorJson) -> {
			// The injector is config file that TF uses when replacing/adding features in/to biomes.
			// It should be an object
			if (!injectorJson.isJsonObject()) {
				return;
			}

			// The serialized ConfiguredFeature is stored as a child element keyed
			// by one of the above 'injection' keywords
			JsonElement featureConfig = getFeatureConfig(injectorJson.getAsJsonObject());
			if (!featureConfig.isJsonObject()) {
				return;
			}

			// Attempt to deserialize and pass to the consumer. Fails silently if an error is thrown
			FeatureSerializer.deserialize(featureConfig).ifPresent(consumer);
		});
	}

	private static JsonElement getFeatureConfig(JsonObject root) {
		for (String injection : INJECTIONS) {
			if (root.has(injection)) {
				JsonElement element = root.get(injection);
				if (element != null) {
					return element;
				}
			}
		}
		return JsonNull.INSTANCE;
	}
}