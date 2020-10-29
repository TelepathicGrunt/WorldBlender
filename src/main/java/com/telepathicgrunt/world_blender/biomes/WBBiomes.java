package com.telepathicgrunt.world_blender.biomes;

import com.google.common.base.Supplier;
import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.WorldBlender;
import com.telepathicgrunt.world_blender.generation.WBBiomeProvider;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMaker;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
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
		createBiome("general_blended", () -> BiomeMaker.func_244234_c(false));
		createBiome("cold_hills_blended", () -> BiomeMaker.func_244234_c(false));
		createBiome("mountainous_blended", () -> BiomeMaker.func_244234_c(false));
		createBiome("ocean_blended", () -> BiomeMaker.func_244234_c(false));
		createBiome("frozen_ocean_blended", () -> BiomeMaker.func_244234_c(false));
	}
	
	private static RegistryObject<Biome> createBiome(String name, Supplier<Biome> biome)
    {
		return BIOMES.register(name, biome);
	}
}