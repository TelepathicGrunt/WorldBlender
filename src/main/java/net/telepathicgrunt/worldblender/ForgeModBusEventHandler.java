package net.telepathicgrunt.worldblender;

import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.telepathicgrunt.worldblender.dimension.WBDimension;

@EventBusSubscriber(modid = WorldBlender.MODID, bus = Bus.FORGE)
public class ForgeModBusEventHandler
{
    @SubscribeEvent
    public static void registerDimensions(RegisterDimensionsEvent event) {
    	WBDimension.registerDimensions(event);
    }
}
