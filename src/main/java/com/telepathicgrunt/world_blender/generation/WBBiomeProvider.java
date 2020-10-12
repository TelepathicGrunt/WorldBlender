package com.telepathicgrunt.world_blender.generation;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.mixin.BiomeLayerSamplerAccessor;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.IExtendedNoiseRandom;
import net.minecraft.world.gen.LazyAreaLayerContext;
import net.minecraft.world.gen.area.IArea;
import net.minecraft.world.gen.area.IAreaFactory;
import net.minecraft.world.gen.area.LazyArea;
import net.minecraft.world.gen.layer.Layer;
import net.minecraft.world.gen.layer.ZoomLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.LongFunction;


public class WBBiomeProvider extends BiomeProvider
{
	public static void registerBiomeProvider() {
		Registry.register(Registry.BIOME_PROVIDER_CODEC, WBIdentifiers.WB_BIOME_PROVIDER_ID, WBBiomeProvider.CODEC);
	}

	public static final Codec<WBBiomeProvider> CODEC =
			RecordCodecBuilder.create((instance) -> instance.group(
					RegistryLookupCodec.func_244331_a(Registry.BIOME_KEY).forGetter((biomeProvider) -> biomeProvider.BIOME_REGISTRY))
					.apply(instance, instance.stable(WBBiomeProvider::new)));

	private final Layer BIOME_SAMPLER;
	protected final Registry<Biome> BIOME_REGISTRY;
	protected static Registry<Biome> LAYERS_BIOME_REGISTRY;
	private static final List<RegistryKey<Biome>> BIOMES = ImmutableList.of(
			RegistryKey.func_240903_a_(Registry.BIOME_KEY, WBIdentifiers.GENERAL_BLENDED_BIOME_ID),
			RegistryKey.func_240903_a_(Registry.BIOME_KEY, WBIdentifiers.MOUNTAINOUS_BLENDED_BIOME_ID),
			RegistryKey.func_240903_a_(Registry.BIOME_KEY, WBIdentifiers.COLD_HILLS_BLENDED_BIOME_ID),
			RegistryKey.func_240903_a_(Registry.BIOME_KEY, WBIdentifiers.OCEAN_BLENDED_BIOME_ID),
			RegistryKey.func_240903_a_(Registry.BIOME_KEY, WBIdentifiers.FROZEN_OCEAN_BLENDED_BIOME_ID));

	public WBBiomeProvider(Registry<Biome> biomeRegistry) {
		// find a way to pass in world seed here
		this(0, biomeRegistry);
	}

	public WBBiomeProvider(long seed, Registry<Biome> biomeRegistry) {
		super(BIOMES.stream().map((registryKey) -> () -> (Biome)biomeRegistry.func_243576_d(registryKey)));
		MainBiomeLayer.setSeed(seed);
		this.BIOME_REGISTRY = biomeRegistry;
		WBBiomeProvider.LAYERS_BIOME_REGISTRY = biomeRegistry;
		this.BIOME_SAMPLER = buildWorldProcedure(seed);
	}


	public static Layer buildWorldProcedure(long seed) {
		IAreaFactory<LazyArea> layerFactory = build((salt) ->
				new LazyAreaLayerContext(25, seed, salt));
		return new Layer(layerFactory);
	}

	public static <T extends IArea, C extends IExtendedNoiseRandom<T>> IAreaFactory<T> build(LongFunction<C> contextFactory) {
		IAreaFactory<T> layerFactory = MainBiomeLayer.INSTANCE.apply(contextFactory.apply(200L));
		layerFactory = ZoomLayer.NORMAL.apply(contextFactory.apply(2001L), layerFactory);
		layerFactory = ZoomLayer.FUZZY.apply(contextFactory.apply(2000L), layerFactory);
		return layerFactory;
	}


	public Biome getNoiseBiome(int x, int y, int z) {
		return this.sample(WBBiomeProvider.LAYERS_BIOME_REGISTRY, x, z);
	}

	public Biome sample(Registry<Biome> registry, int x, int z) {
		int k = ((BiomeLayerSamplerAccessor)this.BIOME_SAMPLER).getSampler().getValue(x, z);
		Biome biome = registry.getByValue(k);
		if (biome == null) {
			//fallback to builtin registry if dynamic registry doesnt have biome
			if (SharedConstants.developmentMode) {
				throw Util.pauseDevMode(new IllegalStateException("Unknown biome id: " + k));
			}
			else {
				return registry.getValueForKey(BiomeRegistry.func_244203_a(0));
			}
		}
		else {
			return biome;
		}
	}

	@Override
	protected Codec<? extends BiomeProvider> getBiomeProviderCodec() {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public BiomeProvider getBiomeProvider(long seed) {
		return new WBBiomeProvider(seed, WBBiomeProvider.LAYERS_BIOME_REGISTRY);
	}
}
