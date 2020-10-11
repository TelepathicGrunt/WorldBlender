package com.telepathicgrunt.world_blender.generation;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.mixin.BiomeLayerSamplerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.layer.ScaleLayer;
import net.minecraft.world.biome.layer.util.*;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.BiomeSource;

import java.util.List;
import java.util.function.LongFunction;


public class WBBiomeProvider extends BiomeSource
{
	public static void registerBiomeProvider() {
		Registry.register(Registry.BIOME_SOURCE, WBIdentifiers.WB_BIOME_PROVIDER_ID, WBBiomeProvider.CODEC);
	}

	public static final Codec<WBBiomeProvider> CODEC =
			RecordCodecBuilder.create((instance) -> instance.group(
					RegistryLookupCodec.of(Registry.BIOME_KEY).forGetter((biomeSource) -> biomeSource.BIOME_REGISTRY))
					.apply(instance, instance.stable(WBBiomeProvider::new)));

	private final BiomeLayerSampler BIOME_SAMPLER;
	private final Registry<Biome> BIOME_REGISTRY;
	protected static Registry<Biome> LAYERS_BIOME_REGISTRY;
	private static final List<RegistryKey<Biome>> BIOMES = ImmutableList.of(
			RegistryKey.of(Registry.BIOME_KEY, WBIdentifiers.GENERAL_BLENDED_BIOME_ID),
			RegistryKey.of(Registry.BIOME_KEY, WBIdentifiers.MOUNTAINOUS_BLENDED_BIOME_ID),
			RegistryKey.of(Registry.BIOME_KEY, WBIdentifiers.COLD_HILLS_BLENDED_BIOME_ID),
			RegistryKey.of(Registry.BIOME_KEY, WBIdentifiers.OCEAN_BLENDED_BIOME_ID),
			RegistryKey.of(Registry.BIOME_KEY, WBIdentifiers.FROZEN_OCEAN_BLENDED_BIOME_ID));

	public WBBiomeProvider(Registry<Biome> biomeRegistry) {
		// find a way to pass in world seed here
		this(0, biomeRegistry);
	}

	public WBBiomeProvider(long seed, Registry<Biome> biomeRegistry) {
		super(BIOMES.stream().map((registryKey) -> () -> (Biome)biomeRegistry.get(registryKey)));
		MainBiomeLayer.setSeed(seed);
		this.BIOME_REGISTRY = biomeRegistry;
		WBBiomeProvider.LAYERS_BIOME_REGISTRY = biomeRegistry;
		this.BIOME_SAMPLER = buildWorldProcedure(seed);
	}


	public static BiomeLayerSampler buildWorldProcedure(long seed) {
		LayerFactory<CachingLayerSampler> layerFactory = build((salt) ->
				new CachingLayerContext(25, seed, salt));
		return new BiomeLayerSampler(layerFactory);
	}


	public static <T extends LayerSampler, C extends LayerSampleContext<T>> LayerFactory<T> build(LongFunction<C> contextFactory) {
		LayerFactory<T> layerFactory = MainBiomeLayer.INSTANCE.create(contextFactory.apply(200L));
		layerFactory = ScaleLayer.NORMAL.create(contextFactory.apply(2001L), layerFactory);
		layerFactory = ScaleLayer.FUZZY.create(contextFactory.apply(2000L), layerFactory);
		return layerFactory;
	}


	public Biome getBiomeForNoiseGen(int x, int y, int z) {
		return this.sample(WBBiomeProvider.LAYERS_BIOME_REGISTRY, x, z);
	}

	public Biome sample(Registry<Biome> registry, int i, int j) {
		int k = ((BiomeLayerSamplerAccessor)this.BIOME_SAMPLER).getSampler().sample(i, j);
		Biome biome = registry.get(k);
		if (biome == null) {
			//fallback to builtin registry if dynamic registry doesnt have biome
			if (SharedConstants.isDevelopment) {
				throw Util.throwOrPause(new IllegalStateException("Unknown biome id: " + k));
			}
			else {
				return registry.get(BuiltinBiomes.fromRawId(0));
			}
		}
		else {
			return biome;
		}
	}

	@Override
	protected Codec<? extends BiomeSource> getCodec() {
		return CODEC;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public BiomeSource withSeed(long seed) {
		return new WBBiomeProvider(seed, WBBiomeProvider.LAYERS_BIOME_REGISTRY);
	}
}
