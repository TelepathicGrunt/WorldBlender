package net.telepathicgrunt.allthefeatures.biome.biomes.surfacebuilder;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.telepathicgrunt.allthefeatures.AllTheFeatures;
import net.telepathicgrunt.allthefeatures.biome.BiomeInit;

@Mod.EventBusSubscriber(modid = AllTheFeatures.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WorldLoadEvent
{
	@Mod.EventBusSubscriber(modid = AllTheFeatures.MODID)
	private static class ForgeEvents
	{

		@SubscribeEvent
		public static void Load(WorldEvent.Load event)
		{
			((FeatureSurfaceBuilder) BiomeInit.FEATURE_SURFACE_BUILDER).setPerlinSeed(event.getWorld().getSeed());
		}
	}
}
