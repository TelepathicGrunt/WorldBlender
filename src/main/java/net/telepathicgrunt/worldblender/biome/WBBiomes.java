package net.telepathicgrunt.worldblender.biome;


import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.telepathicgrunt.worldblender.WorldBlender;
import net.telepathicgrunt.worldblender.biome.biomes.BlendedBiome;
import net.telepathicgrunt.worldblender.biome.biomes.FrozenOceanBlendedBiome;
import net.telepathicgrunt.worldblender.biome.biomes.MountainousBlendedBiome;
import net.telepathicgrunt.worldblender.biome.biomes.OceanBlendedBiome;
import net.telepathicgrunt.worldblender.biome.biomes.VanillaEntriesHolderBiome;
import net.telepathicgrunt.worldblender.biome.biomes.surfacebuilder.BlendedSurfaceBuilder;

public class WBBiomes {


	//list of all biomes we registered
	public static Set<Biome> biomes = new HashSet<Biome>();
	
	public static final SurfaceBuilder<SurfaceBuilderConfig> BLENDED_SURFACE_BUILDER = new BlendedSurfaceBuilder(SurfaceBuilderConfig::deserialize);

	//biome instances
	public static Biome BLENDED_BIOME = new BlendedBiome();
	public static Biome MOUNTAINOUS_BLENDED_BIOME = new MountainousBlendedBiome();
	public static Biome OCEAN_BLENDED_BIOME = new OceanBlendedBiome();
	public static Biome FROZEN_OCEAN_BLENDED_BIOME = new FrozenOceanBlendedBiome();
	public static Biome VANILLA_TEMP_BIOME = new VanillaEntriesHolderBiome(); //do not register it. We aint ever spawning it
	
	//registers the biomes so they now exist in the registry along with their types
	public static void registerBiomes(RegistryEvent.Register<Biome> event) {

   	    IForgeRegistry<Biome> registry = event.getRegistry();

		initBiome(registry, BLENDED_BIOME, "blended_biome", BiomeType.DESERT, Type.RARE);
		initBiome(registry, MOUNTAINOUS_BLENDED_BIOME, "mountain_blended_biome", BiomeType.WARM, Type.RARE);
		initBiome(registry, OCEAN_BLENDED_BIOME, "ocean_blended_biome", BiomeType.COOL, Type.RARE);
		initBiome(registry, FROZEN_OCEAN_BLENDED_BIOME, "frozen_ocean_blended_biome", BiomeType.ICY, Type.RARE);
	}


	//adds biome to registry with their type to the registry and to the biome dictionary
	private static Biome initBiome(IForgeRegistry<Biome> registry, Biome biome, String name, BiomeType biomeType, Type... types) {
		WorldBlender.register(registry, biome, name);
		BiomeDictionary.addTypes(biome, types);
		biomes.add(biome);
		return biome;
	}
}
