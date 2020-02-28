package net.telepathicgrunt.worldblender.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.telepathicgrunt.worldblender.WorldBlender;
import net.telepathicgrunt.worldblender.configs.WBConfig;

@Mod.EventBusSubscriber(modid = WorldBlender.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WBPortalSpawning
{
	@SubscribeEvent
	public static void BlockRightClickEvent(PlayerInteractEvent.RightClickBlock event)
	{
		World world = event.getWorld();
		BlockPos position = event.getPos();
		
		//checks to make sure the activation item is a real item before doing the rest of the checks
		ForgeRegistry<Item> registry = ((ForgeRegistry<Item>) ForgeRegistries.ITEMS);
		ResourceLocation activationItem = new ResourceLocation(WBConfig.activationItem);
		if(!registry.containsKey(activationItem)){
			WorldBlender.LOGGER.log(Level.INFO, "World Blender: Warning, the activation item set in the config does not exist. Please make sure it is a valid resource location to a real item as the portal cannot be created now.");
			ITextComponent message = new StringTextComponent("§eWorld Blender: §fWarning, the activation item set in the config does not exist. Please make sure it is a valid resource location to a real item as the portal cannot be created now.");
			event.getPlayer().sendMessage(message);
			return;
		}

		// Checks to see if player uses right click on a chest while crouching while holding nether star
		if (event.getPlayer().isCrouching() &&
			event.getPlayer().getHeldItemMainhand().getItem() == registry.getRaw(activationItem) &&	
			world.getBlockState(position).getBlock().getTags().contains(Tags.Blocks.CHESTS.getId()))
		{
			BlockPos.Mutable cornerOffset = new BlockPos.Mutable(1,1,1);
			boolean eightChestsFound = checkForValidChests(world, position, cornerOffset);
			
			//8 chests found, time to check their inventory.
			if(eightChestsFound) 
			{
				Set<Item> uniqueBlocksSet = new HashSet<Item>();
				Set<Item> invalidItemSet = new HashSet<Item>();
				
				for(BlockPos blockpos : BlockPos.getAllInBoxMutable(position, position.add(cornerOffset))) 
				{
					ChestTileEntity chestTileEntity = (ChestTileEntity)world.getTileEntity(blockpos);
					for(int index = 0; index < chestTileEntity.getSizeInventory(); index++)
					{
						Item item = chestTileEntity.getStackInSlot(index).getItem();
						
						//if it is a valid block, it would not return air
						if(Block.getBlockFromItem(item) != Blocks.AIR) 
						{
							uniqueBlocksSet.add(item);
						}
						//not a valid block item.
						else 
						{
							invalidItemSet.add(item);
						}
					}
				}
				
				//enough unique blocks were found. Make portal now
				if(uniqueBlocksSet.size() >= WBConfig.uniqueBlocksNeeded)
				{
					for(BlockPos blockpos : BlockPos.getAllInBoxMutable(position, position.add(cornerOffset))) 
					{
						//drop chest and contents if config says so
						if(!WBConfig.consumeChests) {
							world.breakBlock(blockpos, true, event.getPlayer());
						}
						
						world.setBlockState(blockpos, WBBlocks.WORLD_BLENDER_PORTAL.get().getDefaultState());
					}
				}
				//throw error and list all the invalid items in the chests
				else
				{
					//collect the items names into a list of strings
					List<String> invalidItemString = new ArrayList<String>();
					invalidItemSet.stream().forEach(item -> invalidItemString.add(", " + item.getName()));
					
					WorldBlender.LOGGER.log(Level.INFO, "World Blender: There are not enough unique block items in the chests. (stacks or duplicates are ignored) You need "+WBConfig.uniqueBlocksNeeded+" block items to make the portal. Also, here is a list of non-block items if any are found: "+String.join(", ", invalidItemString));
					ITextComponent message = new StringTextComponent("§eWorld Blender: §fThere are not enough unique block items in the chests. (stacks or duplicates are ignored) You need §e"+WBConfig.uniqueBlocksNeeded+"§f block items to make the portal. Also, here is a list of non-block items if any are found: §c"+String.join(", ", invalidItemString));
					event.getPlayer().sendMessage(message);
				}
			}
		}
	}
	
	
	/**
	 * Checks all 8 configurations that a 2x2 area of chests could be around incoming position.
	 * If 2x2 is all chests, returns true and the offset blockpos will be set to that configeration's corner.
	 */
	private static boolean checkForValidChests(World world, BlockPos position, BlockPos.Mutable offset) {
		boolean eightChestsFound = true;
		for(; offset.getX() >= -1; offset.move(Direction.WEST, 2)) {
			for(; offset.getY() >= -1; offset.move(Direction.DOWN, 2)) {
				for(; offset.getZ() >= -1; offset.move(Direction.NORTH, 2)) {
					//checks if this 2x2 has 8 chests
					for(BlockPos blockpos : BlockPos.getAllInBoxMutable(position, position.add(offset))) {
						if(!world.getBlockState(blockpos).getBlock().getTags().contains(Tags.Blocks.CHESTS.getId())) {
							eightChestsFound = false;
							break;
						}
					}
					
					//is only true if no spot was not a chest
					if(eightChestsFound) {
						return true;
					}
					
					//reset to true for next 2x2 to be checked
					eightChestsFound = true;
				}
			}
		}
		
		return false;
	}
}
