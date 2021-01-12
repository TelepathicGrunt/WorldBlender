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
	
    public static final RegistryObject<Feature<NoFeatureConfig>> WB_PORTAL_ALTAR = createFeature("portal_altar", WBPortalAltar::new);
    public static final RegistryObject<Feature<NoFeatureConfig>> ANTI_FLOATING_BLOCKS_AND_SEPARATE_LIQUIDS = createFeature("anti_floating_blocks_and_separate_liquids", AntiFloatingBlocksAndSeparateLiquids::new);

    private static <F extends Feature<?>> RegistryObject<F> createFeature(String name, Supplier<F> feature)
    {
		return FEATURES.register(name, feature);
	}
}
