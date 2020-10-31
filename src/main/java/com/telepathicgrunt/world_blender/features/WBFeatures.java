package com.telepathicgrunt.world_blender.features;

import com.google.common.base.Supplier;
import com.telepathicgrunt.world_blender.WorldBlender;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class WBFeatures
{
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, WorldBlender.MODID);
	
    public static final RegistryObject<Feature<NoFeatureConfig>> WB_PORTAL_ALTAR = createFeature("portal_altar", () -> new WBPortalAltar());
    public static final RegistryObject<Feature<NoFeatureConfig>> NO_FLOATING_LIQUIDS_OR_FALLING_BLOCKS = createFeature("no_floating_liquids_or_falling_blocks", () -> new NoFloatingLiquidsOrFallingBlocks());
    public static final RegistryObject<Feature<NoFeatureConfig>> SEPARATE_LAVA_AND_WATER = createFeature("separate_lava_and_water", () -> new SeparateLavaAndWater());
    
    private static <F extends Feature<?>> RegistryObject<F> createFeature(String name, Supplier<F> feature)
    {
		return FEATURES.register(name, feature);
	}
}
