package net.telepathicgrunt.worldblender;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WorldBlender.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeBusEventHandler
{
	@SubscribeEvent
	public static void Load(WorldEvent.Load event)
	{
		BiomeBlending.setupPerlinSeed(event.getWorld().getSeed());
	}
}
