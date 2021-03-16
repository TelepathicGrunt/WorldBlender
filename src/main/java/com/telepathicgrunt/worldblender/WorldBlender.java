package com.telepathicgrunt.worldblender;

import com.telepathicgrunt.worldblender.biomes.WBBiomes;
import com.telepathicgrunt.worldblender.blocks.WBBlocks;
import com.telepathicgrunt.worldblender.blocks.WBPortalSpawning;
import com.telepathicgrunt.worldblender.configs.WBBlendingConfigs;
import com.telepathicgrunt.worldblender.configs.WBDimensionConfigs;
import com.telepathicgrunt.worldblender.configs.WBPortalConfigs;
import com.telepathicgrunt.worldblender.dimension.WBBiomeProvider;
import com.telepathicgrunt.worldblender.dimension.WBWorldSavedData;
import com.telepathicgrunt.worldblender.features.WBConfiguredFeatures;
import com.telepathicgrunt.worldblender.features.WBFeatures;
import com.telepathicgrunt.worldblender.surfacebuilder.WBSurfaceBuilders;
import com.telepathicgrunt.worldblender.theblender.TheBlender;
import com.telepathicgrunt.worldblender.utils.ConfigHelper;
import com.telepathicgrunt.worldblender.utils.MessageHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(WorldBlender.MODID)
public class WorldBlender{
	public static final String MODID = "world_blender";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static WBBlendingConfigs.WBConfigValues WBBlendingConfig = null;
	public static WBDimensionConfigs.WBConfigValues WBDimensionConfig = null;
	public static WBPortalConfigs.WBConfigValues WBPortalConfig = null;

	private static boolean chestListGenerated = false;

	public WorldBlender() {

		//Set up config
		WBBlendingConfig = ConfigHelper.register(ModConfig.Type.COMMON, WBBlendingConfigs.WBConfigValues::new, "world_blender-blending.toml");
		WBDimensionConfig = ConfigHelper.register(ModConfig.Type.COMMON, WBDimensionConfigs.WBConfigValues::new, "world_blender-dimension.toml");
		WBPortalConfig = ConfigHelper.register(ModConfig.Type.COMMON, WBPortalConfigs.WBConfigValues::new, "world_blender-portal.toml");

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(this::setup);
		WBBiomes.BIOMES.register(modEventBus);
		WBFeatures.FEATURES.register(modEventBus);
		WBBlocks.BLOCKS.register(modEventBus);
		WBBlocks.TILE_ENTITY_TYPES.register(modEventBus);
		WBSurfaceBuilders.SURFACE_BUILDERS.register(modEventBus);

		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.addListener(EventPriority.NORMAL, this::setupChestList);
		forgeBus.addListener(EventPriority.NORMAL, WBWorldSavedData::worldTick);
		forgeBus.addListener(EventPriority.LOWEST, TheBlender::addDimensionalSpacing);
		forgeBus.addListener(EventPriority.NORMAL, WBPortalSpawning::BlockRightClickEvent);
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> WorldBlenderClient::subscribeClientEvents);
	}

	public void setup(final FMLCommonSetupEvent event)
	{
		event.enqueueWork(() ->
		{
			WBConfiguredFeatures.registerConfiguredFeatures();
			WBBiomeProvider.registerBiomeProvider();
		});
		MessageHandler.init();
	}

	public void setupChestList(final WorldEvent.Load event)
	{
		// Do it at any world startup so tile-entities using tags like Vampirism does not crash.
		// We do not need to re-make like when entering other worlds as blocks/tile-entities are
		// not dynamic registries like worldgen registries are.
		if(!chestListGenerated){
			WBPortalSpawning.generateRequiredBlockList(event.getWorld(), WBPortalConfig.requiredBlocksInChests.get());
			chestListGenerated = true;
		}
	}

	/*
	 * Helper method to quickly register features, blocks, items, structures, biomes, anything that can be registered.
	 */
	public static <T extends IForgeRegistryEntry<T>> T register(IForgeRegistry<T> registry, T entry, String registryKey)
	{
		entry.setRegistryName(new ResourceLocation(MODID, registryKey));
		registry.register(entry);
		return entry;
	}
}
