package com.telepathicgrunt.world_blender;

import com.telepathicgrunt.world_blender.blocks.WBBlocks;
import com.telepathicgrunt.world_blender.blocks.WBPortalBlockEntityRenderer;
import com.telepathicgrunt.world_blender.dimension.WBSkyProperty;
import com.telepathicgrunt.world_blender.mixin.SkyPropertiesAccessor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class WorldBlenderClient{
	public static void subscribeClientEvents()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(WorldBlenderClient::onClientSetup);
	}


	public static void onClientSetup(FMLClientSetupEvent event)
	{
		event.enqueueWork(() ->
		{
			SkyPropertiesAccessor.getfield_239208_a_().put(new ResourceLocation(WorldBlender.MODID, "sky_property"), new WBSkyProperty());

			//Put this into enqueue because its not thread safe - andrew
			ClientRegistry.bindTileEntityRenderer(WBBlocks.WORLD_BLENDER_PORTAL_BE.get(), WBPortalBlockEntityRenderer::new);
		});
	}
}
