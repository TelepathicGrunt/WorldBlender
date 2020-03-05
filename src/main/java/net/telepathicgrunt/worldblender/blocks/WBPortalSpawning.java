package net.telepathicgrunt.worldblender.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
		
		//cannot create portals in WB world type
		if(world.getWorldType() == WorldBlender.WBWorldType) {
			return;
		}

		//checks to make sure the activation item is a real item before doing the rest of the checks
		ForgeRegistry<Item> registry = ((ForgeRegistry<Item>) ForgeRegistries.ITEMS);
		ResourceLocation activationItem = new ResourceLocation(WBConfig.activationItem);
		if (!registry.containsKey(activationItem))
		{
			WorldBlender.LOGGER.log(Level.INFO, "World Blender: Warning, the activation item set in the config does not exist. Please make sure it is a valid resource location to a real item as the portal cannot be created now.");
			ITextComponent message = new StringTextComponent("§eWorld Blender: §fWarning, the activation item set in the config does not exist. Please make sure it is a valid resource location to a real item as the portal cannot be created now.");
			event.getPlayer().sendMessage(message);
			return;
		}

		// Checks to see if player uses right click on a chest while crouching while holding nether star
		if (event.getPlayer().isSneaking() && event.getPlayer().getHeldItemMainhand().getItem() == registry.getRaw(activationItem) && world.getBlockState(position).getBlock().getTags().contains(Tags.Blocks.CHESTS.getId()))
		{
			BlockPos.MutableBlockPos cornerOffset = new BlockPos.MutableBlockPos(1, 1, 1);
			boolean eightChestsFound = checkForValidChests(world, position, cornerOffset);

			//8 chests found, time to check their inventory.
			if (eightChestsFound)
			{
				Set<Item> uniqueBlocksSet = new HashSet<Item>();
				Set<Item> invalidItemSet = new HashSet<Item>();
				Set<Item> duplicateBlockSlotSet = new HashSet<Item>();

				for (BlockPos blockpos : BlockPos.getAllInBoxMutable(position, position.add(cornerOffset)))
				{
					ChestTileEntity chestTileEntity = (ChestTileEntity) world.getTileEntity(blockpos);
					for (int index = 0; index < chestTileEntity.getSizeInventory(); index++)
					{
						Item item = chestTileEntity.getStackInSlot(index).getItem();

						//if it is a valid block, it would not return air
						if (Block.getBlockFromItem(item) != Blocks.AIR)
						{
							if(uniqueBlocksSet.contains(item)) 
							{
								duplicateBlockSlotSet.add(item); // save what block is taking up multiple slots
							}
							else 
							{
								uniqueBlocksSet.add(item); 
							}
						}
						//not a valid block item.
						else
						{
							invalidItemSet.add(item);
						}
					}
				}

				//enough unique blocks were found. Make portal now
				if (uniqueBlocksSet.size() >= WBConfig.uniqueBlocksNeeded)
				{
					for (BlockPos blockpos : BlockPos.getAllInBoxMutable(position, position.add(cornerOffset)))
					{
						//not need actually. We dont check the inventory of the chest outside portal area anyway.
						//hole onto this in case we change our mind to read from neighboring chests

						//						//breaks chest the doublechest is attached to so players cant cheese the 
						//						//portal creation with blocks outside the portal area
						//						BlockState chest = world.getBlockState(blockpos);
						//						ChestType type = chest.get(BlockStateProperties.CHEST_TYPE);
						//						if(type != ChestType.SINGLE) {
						//							Direction facing = chest.get(HorizontalBlock.HORIZONTAL_FACING);
						//							if(type == ChestType.LEFT) {
						//								facing = facing.rotateY(); //move right
						//							}
						//							else {
						//								facing = facing.rotateY().rotateY().rotateY(); //move left
						//							}
						//							
						//							//drop chest and contents if config says so
						//							if(!WBConfig.consumeChests) {
						//								world.breakBlock(blockpos.offset(facing), true, event.getPlayer());
						//							}
						//						}

						
						//consume chest and contents if config says so
						if (WBConfig.consumeChests)
						{
							ChestTileEntity chestTileEntity = (ChestTileEntity) world.getTileEntity(blockpos);
							for (int index = chestTileEntity.getSizeInventory(); index >= 0; index--)
							{
								chestTileEntity.removeStackFromSlot(index);
							}
						}
						else
						{
							world.destroyBlock(blockpos, true);
						}

						//create portal but with cooldown so players can grab items before they get teleported
						world.setBlockState(blockpos, WBBlocks.WORLD_BLENDER_PORTAL.get().getDefaultState(), 3);
						WBPortalTileEntity wbtile = (WBPortalTileEntity) world.getTileEntity(blockpos);
						wbtile.triggerCooldown();
						
						event.getPlayer().getActiveItemStack().shrink(1); //consume item in hand
					}
				}
				//throw error and list all the invalid items in the chests
				else
				{

					if (!event.getWorld().isRemote && event.getPlayer().getActiveHand() == event.getHand())
					{
						String msg = "§eWorld Blender: §fThere are not enough unique block items in the chests. (stacks or duplicates are ignored) You need §c" + WBConfig.uniqueBlocksNeeded + "§f block items to make the portal but there is only §a" + uniqueBlocksSet.size() + "§f unique block items right now.";
						
						if(invalidItemSet.size() != 0) 
						{
							//collect the items names into a list of strings
							List<String> invalidItemString = new ArrayList<String>();
							invalidItemSet.remove(Items.AIR); //We dont need to list air
							invalidItemSet.stream().forEach(item -> invalidItemString.add(item.getName().getString()));
							msg += "§f Also, here is a list of non-block items that were found and should be removed: §6" + String.join(", ", invalidItemString);
						}

						if(duplicateBlockSlotSet.size() != 0) 
						{
							//collect the items names into a list of strings
							List<String> duplicateSlotString = new ArrayList<String>();
							duplicateBlockSlotSet.remove(Items.AIR); //We dont need to list air
							duplicateBlockSlotSet.stream().forEach(blockitem -> duplicateSlotString.add(blockitem.getName().getString()));
							msg += "§f There are some slots that contains the same blocks and should be removed. These blocks are: §6" + String.join(", ", duplicateSlotString);
						}
						
						WorldBlender.LOGGER.log(Level.INFO, msg);
						ITextComponent message = new StringTextComponent(msg);
						event.getPlayer().sendMessage(message);
					}
				}
			}
		}
	}


	/**
	 * Checks all 8 configurations that a 2x2 area of chests could be around incoming position. If 2x2 is all chests,
	 * returns true and the offset blockpos will be set to that configeration's corner.
	 */
	private static boolean checkForValidChests(World world, BlockPos position, BlockPos.MutableBlockPos offset)
	{
		boolean eightChestsFound = true;
		for (; offset.getX() >= -1; offset.move(Direction.WEST, 2))
		{
			for (; offset.getY() >= -1; offset.move(Direction.DOWN, 2))
			{
				for (; offset.getZ() >= -1; offset.move(Direction.NORTH, 2))
				{
					//checks if this 2x2 has 8 chests
					for (BlockPos blockpos : BlockPos.getAllInBoxMutable(position, position.add(offset)))
					{
						if (!world.getBlockState(blockpos).getBlock().getTags().contains(Tags.Blocks.CHESTS.getId()))
						{
							eightChestsFound = false;
							break;
						}
					}

					//is only true if no spot was not a chest
					if (eightChestsFound)
					{
						return true;
					}

					//reset to true for next 2x2 to be checked
					eightChestsFound = true;
				}
				offset.move(Direction.SOUTH, 4); //move back. have to do 4 because the loop's move will fire when exiting loop too
			}
			offset.move(Direction.UP, 4); //move back. have to do 4 because the loop's move will fire when exiting loop too
		}

		return false;
	}
}
