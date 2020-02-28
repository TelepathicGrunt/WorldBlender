package net.telepathicgrunt.worldblender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.dedicated.ServerProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.telepathicgrunt.worldblender.biome.BiomeInit;
import net.telepathicgrunt.worldblender.blocks.WBBlocks;
import net.telepathicgrunt.worldblender.configs.WBConfig;
import net.telepathicgrunt.worldblender.networking.MessageHandler;
import net.telepathicgrunt.worldblender.worldtype.WBWorldType;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(WorldBlender.MODID)
public class WorldBlender
{
	public static final String MODID = "world_blender";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	//worldTypes
	public static WorldType WBWorldType;
	
	public WorldBlender()
	{
		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		MinecraftForge.EVENT_BUS.register(this);
		
		modEventBus.addListener(this::setup);

		//generates/handles config
		modEventBus.addListener(this::modConfig);
		modLoadingContext.registerConfig(ModConfig.Type.COMMON, WBConfig.SERVER_SPEC);
		
		//Add block, item, and tile entity to registration on mod bus
		WBBlocks.registerAll(modEventBus);
		
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientEvents.subscribeClientEvents(modEventBus, forgeBus));
	}


	/*
	 * Hacky workaround similar to what Biome O' Plenty did to get around server.properties being read and set too early
	 * before the mod even loads at all. It's so early that the mod's worldtypes aren't added to WORLD_TYPES array and so
	 * it'll change level-type to default regardless of what the user added.
	 * 
	 * My solution is to tell users to add a new entry called use-modded-worldtype=ultra-amplified and then read that
	 * property instead.
	 */
	public void dedicatedServerSetup(FMLDedicatedServerSetupEvent event)
	{
		ServerProperties serverProperties = event.getServerSupplier().get().getServerProperties();

		if (serverProperties != null)
		{
			//get entry if it exists or null if it doesn't
			String entryValue = serverProperties.serverProperties.getProperty("use-modded-worldtype");

			if (entryValue != null && entryValue.equals("world-blender"))
			{
				//make server use our worldtype
				serverProperties.worldType = WBWorldType;
			}
		}
		// Do nothing. server.properties file does not exist.
	}

	

	public void setup(final FMLCommonSetupEvent event)
	{
		//registers the worldtype used for this mod so we can select that worldtype
		WBWorldType = new WBWorldType();
		MessageHandler.init();
	}
	
	public void modConfig(final ModConfig.ModConfigEvent event)
	{
		ModConfig config = event.getConfig();
		if (config.getSpec() == WBConfig.SERVER_SPEC)
			WBConfig.refreshServer();
	}

	// You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents
	{
		@SubscribeEvent
		public static void registerBiomes(final RegistryEvent.Register<Biome> event)
		{
			//registers all my modified biomes
			BiomeInit.registerBiomes(event);
		}
	}


	/*
	 * Helper method to quickly register features, blocks, items, structures, biomes, anything that can be registered.
	 */
	public static <T extends IForgeRegistryEntry<T>> T register(IForgeRegistry<T> registry, T entry, String registryKey)
	{
		entry.setRegistryName(new ResourceLocation(MODID, registryKey.toLowerCase().replace(' ', '_')));
		registry.register(entry);
		return entry;
	}
}
