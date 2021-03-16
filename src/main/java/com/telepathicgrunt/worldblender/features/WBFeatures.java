package com.telepathicgrunt.worldblender.features;

import com.google.common.base.Supplier;
import com.telepathicgrunt.worldblender.WorldBlender;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class WBFeatures
{
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, WorldBlender.MODID);
	
    public static final RegistryObject<Feature<NoFeatureConfig>> WB_PORTAL_ALTAR = FEATURES.register("portal_altar", WBPortalAltar::new);
    public static final RegistryObject<Feature<NoFeatureConfig>> ANTI_FLOATING_BLOCKS_AND_SEPARATE_LIQUIDS = FEATURES.register("anti_floating_blocks_and_separate_liquids", AntiFloatingBlocksAndSeparateLiquids::new);
    public static final RegistryObject<Feature<NoFeatureConfig>> ITEM_CLEARING = FEATURES.register("item_clearing", ItemClearingFeature::new);
}
