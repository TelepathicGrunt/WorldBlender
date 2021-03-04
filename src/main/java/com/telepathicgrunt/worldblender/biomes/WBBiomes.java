package com.telepathicgrunt.worldblender.biomes;

import com.google.common.base.Supplier;
import com.telepathicgrunt.worldblender.WorldBlender;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMaker;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class WBBiomes
{
	public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, WorldBlender.MODID);
	
	// Dummy biomes to reserve the numeric ID safely for the json biomes to overwrite.
	// No static variable to hold as these dummy biomes should NOT be held and referenced elsewhere.
	static
	{
		createBiome("general_blended", BiomeMaker::makeVoidBiome);
		createBiome("cold_hills_blended", BiomeMaker::makeVoidBiome);
		createBiome("mountainous_blended", BiomeMaker::makeVoidBiome);
		createBiome("ocean_blended", BiomeMaker::makeVoidBiome);
		createBiome("frozen_ocean_blended", BiomeMaker::makeVoidBiome);
	}
	
	private static RegistryObject<Biome> createBiome(String name, Supplier<Biome> biome)
    {
		return BIOMES.register(name, biome);
	}
}