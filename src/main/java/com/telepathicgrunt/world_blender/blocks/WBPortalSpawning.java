package com.telepathicgrunt.world_blender.blocks;

import com.telepathicgrunt.world_blender.WorldBlender;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.stream.Collectors;

public class WBPortalSpawning
{
	protected static final Object2BooleanMap<TileEntityType<?>> VALID_CHEST_BLOCKS_ENTITY_TYPES = new Object2BooleanArrayMap<>();
	private static final List<Block> REQUIRED_PORTAL_BLOCKS = new ArrayList<>();
	private static final List<String> INVALID_IDS = new ArrayList<>();

	/**
	 * Takes config string and chops it up into individual entries and returns the array of the entries.
	 * Splits the incoming string on commas, trims white spaces on end, turns inside whitespace to _, and lowercases entry.
	 */
	public static void generateRequiredBlockList(String configEntry) {
		String[] entriesArray = configEntry.split(",");
		Arrays.parallelSetAll(entriesArray, (i) -> entriesArray[i].trim().toLowerCase(Locale.ROOT).replace(' ', '_'));
		
		//test and make sure the entries exists
		//if not, add it to an invalid rl list so we can warn user later 
		for(String rlString : entriesArray)
		{
			if(rlString.isEmpty()) continue;
			
			if(Registry.BLOCK.containsKey(new ResourceLocation(rlString)))
			{
				REQUIRED_PORTAL_BLOCKS.add(Registry.BLOCK.getOrDefault(new ResourceLocation(rlString)));
			}
			else
			{
				INVALID_IDS.add(rlString);
			}
		}

		//find all entity types that are most likely chests
		for(TileEntityType<?> blockEntityType : Registry.BLOCK_ENTITY_TYPE)
		{
			try{
				TileEntity tileEntity = blockEntityType.create();
				if(tileEntity != null){
					boolean containsChestName = tileEntity.getClass().getSimpleName().toLowerCase().contains("chest");
					boolean isIInventory = tileEntity instanceof IInventory;
					WBPortalSpawning.VALID_CHEST_BLOCKS_ENTITY_TYPES.put(
							blockEntityType,
							containsChestName && isIInventory);
				}
			}
			catch(Throwable e){
				WorldBlender.LOGGER.log(Level.WARN, "Failed to check if "+blockEntityType.getRegistryName()+" is a chest. If is not a chest, ignore this message. If it is, let telepathicGrunt (World Blender dev) know this.");
			}
		}
	}

	public static void BlockRightClickEvent(PlayerInteractEvent.RightClickBlock event)
	{
		ActionResultType result = blockRightClick(event.getPlayer(), event.getWorld(), event.getHand(), event.getPos());
		if(!result.equals(ActionResultType.PASS)){
			event.setResult(Event.Result.DENY);
		}
	}

	public static ActionResultType blockRightClick(PlayerEntity player, World world, Hand hand, BlockPos position)
	{
		if(world.isRemote() || player.isSpectator()) return ActionResultType.PASS;

		// Checks to see if player uses right click on a chest while crouching while holding nether star
		TileEntity blockEntity = world.getTileEntity(position);
		if (player.isCrouching() &&
				blockEntity != null &&
				WBPortalSpawning.VALID_CHEST_BLOCKS_ENTITY_TYPES.getOrDefault(blockEntity.getType(), false))
		{
			//checks to make sure the activation item is a real item before doing the rest of the checks
			ResourceLocation activationItem = new ResourceLocation(WorldBlender.WBPortalConfig.activationItem.get());
			if (Registry.ITEM.func_241873_b(activationItem).isPresent())
			{
				WorldBlender.LOGGER.log(Level.INFO, "World Blender: Warning, the activation item set in the config does not exist. Please make sure it is a valid resource location to a real item as the portal cannot be created now.");
				StringTextComponent message = new StringTextComponent(TextFormatting.YELLOW + "World Blender: " + TextFormatting.WHITE + "Warning, the activation item set in the config does not exist. Please make sure it is a valid resource location to a real item as the portal cannot be created now.");
				player.sendStatusMessage(message, false);
				return ActionResultType.FAIL;
			}
			else if((player.getHeldItemMainhand().getItem() != Registry.ITEM.getOrDefault(activationItem) && hand == Hand.MAIN_HAND) ||
					(player.getHeldItemOffhand().getItem() != Registry.ITEM.getOrDefault(activationItem) && hand == Hand.OFF_HAND))
			{
				return ActionResultType.PASS;
			}


			BlockPos.Mutable cornerOffset = new BlockPos.Mutable(1, 1, 1);
			boolean eightChestsFound = checkForValidChests(world, position, cornerOffset);

			//8 chests found, time to check their inventory.
			if (eightChestsFound)
			{
				Set<Item> uniqueBlocksSet = new HashSet<>();
				Set<Item> invalidItemSet = new HashSet<>();
				Set<Item> duplicateBlockSlotSet = new HashSet<>();

				for (BlockPos blockpos : BlockPos.getAllInBoxMutable(position, position.add(cornerOffset)))
				{
					TileEntity chestTileEntity = world.getTileEntity(blockpos);
					if(chestTileEntity != null &&
							WBPortalSpawning.VALID_CHEST_BLOCKS_ENTITY_TYPES.getOrDefault(blockEntity.getType(), false))
					{
						for (int index = 0; index < ((IInventory) chestTileEntity).getSizeInventory(); index++)
						{
							Item item = ((IInventory) chestTileEntity).getStackInSlot(index).getItem();

							//if it is a valid block, it would not return air
							if (Block.getBlockFromItem(item) != Blocks.AIR)
							{
								if(uniqueBlocksSet.contains(item))
								{
									duplicateBlockSlotSet.add(item); // save what block is stacked
								}

								uniqueBlocksSet.add(item);
							}
							//not a valid block item.
							else
							{
								invalidItemSet.add(item);
							}
						}
					}
				}

				if(!INVALID_IDS.isEmpty())
				{
					WorldBlender.LOGGER.log(Level.INFO, "World Blender: Warning, error reading the required blocks config entry. Please make sure the blocks specified in that config are valid resource locations and points to real blocks as the portal cannot be created now. The problematic entries are: " + String.join(", ", INVALID_IDS));
					StringTextComponent message = new StringTextComponent(TextFormatting.YELLOW + "World Blender: " + TextFormatting.WHITE + "Warning, error reading the required blocks config entry. Please make sure the blocks specified in that config are valid resource locations and points to real blocks as the portal cannot be created now. The problematic entries are: " + TextFormatting.GOLD + String.join(", ", INVALID_IDS));
					player.sendStatusMessage(message, false);
					return ActionResultType.FAIL;
				}

				List<Block> listOfRequireBlocksNotFound = new ArrayList<>(REQUIRED_PORTAL_BLOCKS);
				boolean isMissingRequiredBlocks = false;

				//all unique blocks in chests must be a part of the require blocks list
				if(WorldBlender.WBPortalConfig.uniqueBlocksNeeded.get() <= REQUIRED_PORTAL_BLOCKS.size())
				{
					for(Item blockItem : uniqueBlocksSet)
					{
						listOfRequireBlocksNotFound.remove(Block.getBlockFromItem(blockItem));
					}

					if(WorldBlender.WBPortalConfig.uniqueBlocksNeeded.get() > REQUIRED_PORTAL_BLOCKS.size() - listOfRequireBlocksNotFound.size())
					{
						isMissingRequiredBlocks = true;
					}
				}
				//all blocks in the required blocks list must be present
				else
				{
					for(Item blockItem : uniqueBlocksSet)
					{
						listOfRequireBlocksNotFound.remove(Block.getBlockFromItem(blockItem));
					}

					if(listOfRequireBlocksNotFound.size() != 0)
					{
						isMissingRequiredBlocks = true;
					}
				}

				//warn player that they do not have enough required blocks for the portal
				if(isMissingRequiredBlocks)
				{
					WorldBlender.LOGGER.log(Level.INFO, "World Blender: There are not enough required blocks in the chests. Please add the needed required blocks and then add any other unique blocks until you have "+WorldBlender.WBPortalConfig.uniqueBlocksNeeded.get()+" unique blocks. The require blocks specified in the config are " + REQUIRED_PORTAL_BLOCKS.stream().map(entry -> Registry.BLOCK.getKey(entry).toString()).collect(Collectors.joining(", ")));
					StringTextComponent message = new StringTextComponent(TextFormatting.YELLOW + "World Blender: " + TextFormatting.WHITE + "There are not enough required blocks in the chests. Please add the needed required blocks and then add any other unique blocks until you have " + TextFormatting.RED+WorldBlender.WBPortalConfig.uniqueBlocksNeeded.get()+TextFormatting.WHITE + " unique blocks. The require blocks specified in the config are " + TextFormatting.GOLD + REQUIRED_PORTAL_BLOCKS.stream().map(entry -> Registry.BLOCK.getKey(entry).toString()).collect(Collectors.joining(", ")));
					player.sendStatusMessage(message, false);
					return ActionResultType.FAIL;
				}



				invalidItemSet.remove(Items.AIR); //We don't need to list air
				if (invalidItemSet.size() == 0 &&
						uniqueBlocksSet.size() >= WorldBlender.WBPortalConfig.uniqueBlocksNeeded.get())
				{
					//enough unique blocks were found and no items are in chest. Make portal now
					for (BlockPos blockpos : BlockPos.getAllInBoxMutable(position, position.add(cornerOffset)))
					{
						//consume chest and contents if config says so
						if (WorldBlender.WBPortalConfig.consumeChests.get())
						{
							TileEntity chestTileEntity = world.getTileEntity(blockpos);
							if(chestTileEntity != null &&
									WBPortalSpawning.VALID_CHEST_BLOCKS_ENTITY_TYPES.getOrDefault(blockEntity.getType(), false))
							{
								for (int index = ((IInventory) chestTileEntity).getSizeInventory(); index >= 0; index--) {
									((IInventory) chestTileEntity).removeStackFromSlot(index);
								}
							}
						}
						else
						{
							world.destroyBlock(blockpos, true, player);
						}

						//create portal but with cooldown so players can grab items before they get teleported
						world.setBlockState(blockpos, WBBlocks.WORLD_BLENDER_PORTAL.getDefaultState(), 3);
						WBPortalBlockEntity wbtile = (WBPortalBlockEntity) world.getTileEntity(blockpos);

						if(wbtile != null)
							wbtile.triggerCooldown();

						player.getActiveItemStack().shrink(1); //consume item in hand
					}

					return ActionResultType.SUCCESS;
				}
				//throw error and list all the invalid items in the chests
				else
				{
					String msg = TextFormatting.YELLOW + "World Blender: " + TextFormatting.WHITE + "There are not enough unique block items in the chests. (stacks or duplicates are ignored) You need " + TextFormatting.RED + WorldBlender.WBPortalConfig.uniqueBlocksNeeded.get() + TextFormatting.WHITE + " block items to make the portal but there is only " + TextFormatting.GREEN + uniqueBlocksSet.size() + TextFormatting.WHITE + " unique block items right now.";

					if(invalidItemSet.size() > 0)
					{
						//collect the items names into a list of strings
						List<String> invalidItemString = new ArrayList<>();
						invalidItemSet.forEach(item -> invalidItemString.add(item.getDisplayName(new ItemStack(item)).getString()));
						msg += TextFormatting.WHITE + "\n Also, here is a list of non-block items that were found and should be removed: " + TextFormatting.GOLD + String.join(", ", invalidItemString);
					}

					if(duplicateBlockSlotSet.size() != 0)
					{
						//collect the items names into a list of strings
						List<String> duplicateSlotString = new ArrayList<>();
						duplicateBlockSlotSet.remove(Items.AIR); //We dont need to list air
						duplicateBlockSlotSet.forEach(blockitem -> duplicateSlotString.add(blockitem.getDisplayName(new ItemStack(blockitem)).getString()));
						msg += TextFormatting.WHITE + "\n There are some slots that contains the same blocks and should be removed. These blocks are: " + TextFormatting.GOLD + String.join(", ", duplicateSlotString);
					}

					WorldBlender.LOGGER.log(Level.INFO, msg);
					player.sendStatusMessage(new StringTextComponent(msg), false);

					return ActionResultType.FAIL;
				}
			}
		}

		return ActionResultType.PASS;
	}


	/**
	 * Checks all 8 configurations that a 2x2 area of chests could be around incoming position. If 2x2 is all chests,
	 * returns true and the offset blockpos will be set to that configeration's corner.
	 */
	private static boolean checkForValidChests(World world, BlockPos position, BlockPos.Mutable offset)
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
						// We check if the block entity class itself has 'chest in the name.
						TileEntity blockEntity = world.getTileEntity(blockpos);
						if (blockEntity == null ||
								!WBPortalSpawning.VALID_CHEST_BLOCKS_ENTITY_TYPES.getOrDefault(blockEntity.getType(), false))
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
