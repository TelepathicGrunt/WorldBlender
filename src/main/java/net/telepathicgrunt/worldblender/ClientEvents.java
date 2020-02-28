package net.telepathicgrunt.worldblender;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.telepathicgrunt.worldblender.biome.BiomeInit;
import net.telepathicgrunt.worldblender.blocks.WBBlocks;
import net.telepathicgrunt.worldblender.blocks.WBPortalTileEntity;
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