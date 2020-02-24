package net.telepathicgrunt.allthefeatures.biome;


import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.telepathicgrunt.allthefeatures.AllTheFeatures;
import net.telepathicgrunt.allthefeatures.biome.biomes.FeatureBiome;
import net.telepathicgrunt.allthefeatures.biome.biomes.MountainFeatureBiome;
import net.telepathicgrunt.allthefeatures.biome.biomes.OceanFeatureBiome;
import net.telepathicgrunt.allthefeatures.biome.biomes.surfacebuilder.FeatureSurfaceBuilder;

public class BiomeInit {


	//list of all biomes we registered
	public static Set<Biome> biomes = new HashSet<Biome>();
	
	public static final SurfaceBuilder<SurfaceBuilderConfig> FEATURE_SURFACE_BUILDER = new FeatureSurfaceBuilder(SurfaceBuilderConfig::deserialize);

	//biome instances
	public static Biome FEATURE_BIOME = new FeatureBiome();
	public static Biome MOUNTAIN_FEATURE_BIOME = new MountainFeatureBiome();
	public static Biome OCEAN_FEATURE_BIOME = new OceanFeatureBiome();
	
	//registers the biomes so they now exist in the registry along with their types
	public static void registerBiomes(RegistryEvent.Register<Biome> event) {

   	    IForgeRegistry<Biome> registry = event.getRegistry();

		initBiome(registry, FEATURE_BIOME, "Feature Biome", BiomeType.WARM, Type.RARE);
		initBiome(registry, MOUNTAIN_FEATURE_BIOME, "Mountain Feature Biome", BiomeType.WARM, Type.RARE);
		initBiome(registry, OCEAN_FEATURE_BIOME, "Ocean Feature Biome", BiomeType.WARM, Type.RARE);
	}


	//adds biome to registry with their type to the registry and to the biome dictionary
	private static Biome initBiome(IForgeRegistry<Biome> registry, Biome biome, String name, BiomeType biomeType, Type... types) {
		AllTheFeatures.register(registry, biome, name);
		//BiomeDictionary.addTypes(biome, types);
		biomes.add(biome);
		return biome;
	}
}
