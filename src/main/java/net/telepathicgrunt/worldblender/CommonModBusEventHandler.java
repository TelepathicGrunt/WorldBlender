package net.telepathicgrunt.worldblender;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod.EventBusSubscriber(modid = WorldBlender.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModBusEventHandler
{
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void latestCommonSetup(FMLLoadCompleteEvent event)
	{
		PerformBiomeBlending.setupBiomes();
	}
}
