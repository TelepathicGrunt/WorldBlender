package net.telepathicgrunt.worldblender;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.telepathicgrunt.worldblender.blocks.WBBlocks;
import net.telepathicgrunt.worldblender.blocks.WBPortalTileEntityRenderer;


public class ClientEvents
{
	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientEvents::onClientSetup);
	}


	public static void onClientSetup(FMLClientSetupEvent event)
	{
		ClientRegistry.bindTileEntityRenderer(WBBlocks.WORLD_BLENDER_PORTAL_TILE.get(), WBPortalTileEntityRenderer::new);
	}
}