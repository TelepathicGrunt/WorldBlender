package net.telepathicgrunt.worldblender.biome;


import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.telepathicgrunt.worldblender.WorldBlender;
import net.telepathicgrunt.worldblender.biome.biomes.BlendedBiome;
import net.telepathicgrunt.worldblender.biome.biomes.FrozenOceanBlendedBiome;
import net.telepathicgrunt.worldblender.biome.biomes.MountainousBlendedBiome;
import net.telepathicgrunt.worldblender.biome.biomes.OceanBlendedBiome;
import net.telepathicgrunt.worldblender.biome.biomes.surfacebuilder.BlendedSurfaceBuilder;

public class BiomeInit {


	//list of all biomes we registered
	public static Set<Biome> biomes = new HashSet<Biome>();
	
	public static final SurfaceBuilder<SurfaceBuilderConfig> FEATURE_SURFACE_BUILDER = new BlendedSurfaceBuilder(SurfaceBuilderConfig::deserialize);

	//biome instances
	public static Biome BLENDED_BIOME = new BlendedBiome();
	public static Biome MOUNTAINOUS_BLENDED_BIOME = new MountainousBlendedBiome();
	public static Biome OCEAN_BLENDED_BIOME = new OceanBlendedBiome();
	public static Biome FROZEN_OCEAN_BLENDED_BIOME = new FrozenOceanBlendedBiome();
	
	//registers the biomes so they now exist in the registry along with their types
	public static void registerBiomes(RegistryEvent.Register<Biome> event) {

   	    IForgeRegistry<Biome> registry = event.getRegistry();

		initBiome(registry, BLENDED_BIOME, "Blended Biome", BiomeType.DESERT, Type.RARE);
		initBiome(registry, MOUNTAINOUS_BLENDED_BIOME, "Mountain Blended Biome", BiomeType.WARM, Type.RARE);
		initBiome(registry, OCEAN_BLENDED_BIOME, "Ocean Blended Biome", BiomeType.COOL, Type.RARE);
		initBiome(registry, FROZEN_OCEAN_BLENDED_BIOME, "Frozen Ocean Blended Biome", BiomeType.ICY, Type.RARE);
	}


	//adds biome to registry with their type to the registry and to the biome dictionary
	private static Biome initBiome(IForgeRegistry<Biome> registry, Biome biome, String name, BiomeType biomeType, Type... types) {
		WorldBlender.register(registry, biome, name);
		//BiomeDictionary.addTypes(biome, types);
		biomes.add(biome);
		return biome;
	}
}
