package net.telepathicgrunt.worldblender;

import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.telepathicgrunt.worldblender.dimension.WBDimension;
import net.telepathicgrunt.worldblender.features.DDDungeonFeature;
import net.telepathicgrunt.worldblender.features.WBFeatures;

@EventBusSubscriber(modid = WorldBlender.MODID, bus = Bus.FORGE)
public class ForgeModBusEventHandler
{
	@SubscribeEvent
	public static void worldLoad(WorldEvent.Load event)
	{
		((DDDungeonFeature)WBFeatures.DD_DUNGEON_FEATURE).setSeed(event.getWorld().getSeed());
	}    
	
    @SubscribeEvent
    public static void registerDimensions(RegisterDimensionsEvent event) {
    	WBDimension.registerDimensions(event);
    }
}
