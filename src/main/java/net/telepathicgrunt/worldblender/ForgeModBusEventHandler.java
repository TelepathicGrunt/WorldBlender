package net.telepathicgrunt.worldblender;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.telepathicgrunt.worldblender.features.DDDungeonFeature;
import net.telepathicgrunt.worldblender.features.WBFeatures;

@Mod.EventBusSubscriber(modid = WorldBlender.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeModBusEventHandler
{
	@SubscribeEvent
	public static void worldLoad(WorldEvent.Load event)
	{
		((DDDungeonFeature)WBFeatures.DD_DUNGEON_FEATURE).setSeed(event.getWorld().getSeed());
	}
}
