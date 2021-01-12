package com.telepathicgrunt.world_blender.generation;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.mixin.BiomeLayerSamplerAccessor;
import com.telepathicgrunt.world_blender.utils.WorldSeedHolder;
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
					Codec.LONG.fieldOf("seed").orElseGet(WorldSeedHolder::getSeed).forGetter((biomeSource) -> biomeSource.seed),
					RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY).forGetter((biomeProvider) -> biomeProvider.biomeRegistry),
					Codec.intRange(1, 20).fieldOf("biome_size").orElse(2).forGetter((biomeSource) -> biomeSource.biomeSize))
					.apply(instance, instance.stable(WBBiomeProvider::new)));

	private final long seed;
	private final int biomeSize;
	private final Layer biomeSampler;
	protected final Registry<Biome> biomeRegistry;
	private static final List<RegistryKey<Biome>> BIOMES = ImmutableList.of(
			RegistryKey.getOrCreateKey(Registry.BIOME_KEY, WBIdentifiers.GENERAL_BLENDED_BIOME_ID),
			RegistryKey.getOrCreateKey(Registry.BIOME_KEY, WBIdentifiers.MOUNTAINOUS_BLENDED_BIOME_ID),
			RegistryKey.getOrCreateKey(Registry.BIOME_KEY, WBIdentifiers.COLD_HILLS_BLENDED_BIOME_ID),
			RegistryKey.getOrCreateKey(Registry.BIOME_KEY, WBIdentifiers.OCEAN_BLENDED_BIOME_ID),
			RegistryKey.getOrCreateKey(Registry.BIOME_KEY, WBIdentifiers.FROZEN_OCEAN_BLENDED_BIOME_ID));


	public WBBiomeProvider(long seed, Registry<Biome> biomeRegistry, int biomeSize) {
		super(BIOMES.stream().map((registryKey) -> () -> (Biome)biomeRegistry.getValueForKey(registryKey)));

		this.biomeRegistry = biomeRegistry;
		this.biomeSize = biomeSize;
		this.seed = seed;
		this.biomeSampler = buildWorldProcedure(seed, biomeSize, biomeRegistry);
	}


	public static Layer buildWorldProcedure(long seed, int biomeSize, Registry<Biome> biomeRegistry) {
		IAreaFactory<LazyArea> layerFactory = build((salt) ->
				new LazyAreaLayerContext(25, seed, salt),
				biomeSize,
				seed,
				biomeRegistry);
		return new Layer(layerFactory);
	}

	public static <T extends IArea, C extends IExtendedNoiseRandom<T>> IAreaFactory<T> build(LongFunction<C> contextFactory, int biomeSize, long seed, Registry<Biome> biomeRegistry) {
		IAreaFactory<T> layerFactory = (new MainBiomeLayer(seed, biomeRegistry)).apply(contextFactory.apply(200L));

		for(int currentExtraZoom = 0; currentExtraZoom < biomeSize; currentExtraZoom++){
			if((currentExtraZoom + 2) % 3 != 0){
				layerFactory = ZoomLayer.NORMAL.apply(contextFactory.apply(2001L + currentExtraZoom), layerFactory);
			}
			else{
				layerFactory = ZoomLayer.FUZZY.apply(contextFactory.apply(2000L + (currentExtraZoom * 31)), layerFactory);
			}
		}

		return layerFactory;
	}


	@Override
	public Biome getNoiseBiome(int x, int y, int z) {
		int k = ((BiomeLayerSamplerAccessor)this.biomeSampler).wb_getSampler().getValue(x, z);
		Biome biome = this.biomeRegistry.getByValue(k);
		if (biome == null) {
			//fallback to builtin registry if dynamic registry doesnt have biome
			if (SharedConstants.developmentMode) {
				throw Util.pauseDevMode(new IllegalStateException("Unknown biome id: " + k));
			}
			else {
				return this.biomeRegistry.getValueForKey(BiomeRegistry.getKeyFromID(0));
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
		return new WBBiomeProvider(seed, this.biomeRegistry, this.biomeSize);
	}
}
