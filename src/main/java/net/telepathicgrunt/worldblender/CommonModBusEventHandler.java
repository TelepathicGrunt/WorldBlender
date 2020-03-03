package net.telepathicgrunt.worldblender;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.telepathicgrunt.worldblender.the_blender.ConfigBlacklisting;
import net.telepathicgrunt.worldblender.the_blender.IntermodCompatibility;
import net.telepathicgrunt.worldblender.the_blender.PerformBiomeBlending;

@Mod.EventBusSubscriber(modid = WorldBlender.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModBusEventHandler
{
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void latestCommonSetup(FMLLoadCompleteEvent event)
	{
		ConfigBlacklisting.setupBlackLists();
		PerformBiomeBlending.setupBiomes();
		
		if(ModList.get().isLoaded("dimdungeons"))
		{
			IntermodCompatibility.addDDDungeons();
		}
	}

}
