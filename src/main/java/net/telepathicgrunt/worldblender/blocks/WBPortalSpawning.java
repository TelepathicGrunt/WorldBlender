package net.telepathicgrunt.worldblender.blocks;

import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.telepathicgrunt.worldblender.WorldBlender;

@Mod.EventBusSubscriber(modid = WorldBlender.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WBPortalSpawning
{
	@SubscribeEvent
	public static void BlockRightClickEvent(PlayerInteractEvent.RightClickBlock event)
	{
		World world = event.getWorld();

		// Checks to see if player uses right click on a chest while crouching while holding nether star
		if (event.getPlayer().isCrouching() &&
			event.getPlayer().getHeldItemMainhand().getItem() == Items.NETHER_STAR &&	
			world.getBlockState(event.getPos()).getBlock().getTags().contains(Tags.Blocks.CHESTS.getId()))
		{
			int g = 5;
			g +=g;
		}
	}
}
