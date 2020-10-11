package com.telepathicgrunt.world_blender.blocks;

import com.telepathicgrunt.world_blender.WorldBlender;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.stream.Collectors;

public class WBPortalSpawning
{
	protected static final Object2BooleanMap<BlockEntityType<?>> VALID_CHEST_BLOCKS_ENTITY_TYPES = new Object2BooleanArrayMap<>();
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
			
			if(Registry.BLOCK.containsId(new Identifier(rlString)))
			{
				REQUIRED_PORTAL_BLOCKS.add(Registry.BLOCK.get(new Identifier(rlString)));
			}
			else
			{
				INVALID_IDS.add(rlString);
			}
		}

		//find all entity types that are most likely chests
		for(BlockEntityType<?> blockEntityType : Registry.BLOCK_ENTITY_TYPE)
		{
			WBPortalSpawning.VALID_CHEST_BLOCKS_ENTITY_TYPES.put(
					blockEntityType,
					Objects.requireNonNull(blockEntityType.instantiate()).getClass().getSimpleName().toLowerCase().contains("chest") &&
							blockEntityType.instantiate() instanceof Inventory);
		}
	}

	public static ActionResult blockRightClick(PlayerEntity player, World world, Hand hand, HitResult hitResult)
	{
		if(world.isClient() || player.isSpectator()) return ActionResult.PASS;

		BlockPos position = new BlockPos(hitResult.getPos());

		// Checks to see if player uses right click on a chest while crouching while holding nether star
		BlockEntity blockEntity = world.getBlockEntity(position);
		if (player.isInSneakingPose() &&
				blockEntity != null &&
				WBPortalSpawning.VALID_CHEST_BLOCKS_ENTITY_TYPES.getOrDefault(blockEntity.getType(), false))
		{
			//checks to make sure the activation item is a real item before doing the rest of the checks
			Identifier activationItem = new Identifier(WorldBlender.WB_CONFIG.WBPortalConfig.activationItem);
			if (!Registry.ITEM.containsId(activationItem))
			{
				WorldBlender.LOGGER.log(Level.INFO, "World Blender: Warning, the activation item set in the config does not exist. Please make sure it is a valid resource location to a real item as the portal cannot be created now.");
				Text message = new LiteralText("§eWorld Blender: §fWarning, the activation item set in the config does not exist. Please make sure it is a valid resource location to a real item as the portal cannot be created now.");
				player.sendMessage(message, false);
				return ActionResult.FAIL;
			}
			else if((player.getMainHandStack().getItem() != Registry.ITEM.get(activationItem) && hand == Hand.MAIN_HAND) ||
					(player.getOffHandStack().getItem() != Registry.ITEM.get(activationItem) && hand == Hand.OFF_HAND))
			{
				return ActionResult.FAIL;
			}


			BlockPos.Mutable cornerOffset = new BlockPos.Mutable(1, 1, 1);
			boolean eightChestsFound = checkForValidChests(world, position, cornerOffset);

			//8 chests found, time to check their inventory.
			if (eightChestsFound)
			{
				Set<Item> uniqueBlocksSet = new HashSet<>();
				Set<Item> invalidItemSet = new HashSet<>();
				Set<Item> duplicateBlockSlotSet = new HashSet<>();

				for (BlockPos blockpos : BlockPos.iterate(position, position.add(cornerOffset)))
				{
					BlockEntity chestTileEntity = world.getBlockEntity(blockpos);
					if(chestTileEntity != null &&
							WBPortalSpawning.VALID_CHEST_BLOCKS_ENTITY_TYPES.getOrDefault(blockEntity.getType(), false))
					{
						for (int index = 0; index < ((Inventory) chestTileEntity).size(); index++)
						{
							Item item = ((Inventory) chestTileEntity).getStack(index).getItem();

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
					Text message = new LiteralText("§eWorld Blender: §fWarning, error reading the required blocks config entry. Please make sure the blocks specified in that config are valid resource locations and points to real blocks as the portal cannot be created now. The problematic entries are: §6" + String.join(", ", INVALID_IDS));
					player.sendMessage(message, false);
					return ActionResult.FAIL;
				}

				List<Block> listOfRequireBlocksNotFound = new ArrayList<>(REQUIRED_PORTAL_BLOCKS);
				boolean isMissingRequiredBlocks = false;

				//all unique blocks in chests must be a part of the require blocks list
				if(WorldBlender.WB_CONFIG.WBPortalConfig.uniqueBlocksNeeded <= REQUIRED_PORTAL_BLOCKS.size())
				{
					for(Item blockItem : uniqueBlocksSet)
					{
						listOfRequireBlocksNotFound.remove(Block.getBlockFromItem(blockItem));
					}

					if(WorldBlender.WB_CONFIG.WBPortalConfig.uniqueBlocksNeeded > REQUIRED_PORTAL_BLOCKS.size() - listOfRequireBlocksNotFound.size())
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
					WorldBlender.LOGGER.log(Level.INFO, "World Blender: There are not enough required blocks in the chests. Please add the needed required blocks and then add any other unique blocks until you have "+WorldBlender.WB_CONFIG.WBPortalConfig.uniqueBlocksNeeded+" unique blocks. The require blocks specified in the config are " + REQUIRED_PORTAL_BLOCKS.stream().map(entry -> Registry.BLOCK.getId(entry).toString()).collect(Collectors.joining(", ")));
					Text message = new LiteralText("§eWorld Blender: §fThere are not enough required blocks in the chests. Please add the needed required blocks and then add any other unique blocks until you have §c"+WorldBlender.WB_CONFIG.WBPortalConfig.uniqueBlocksNeeded+"§f unique blocks. The require blocks specified in the config are §6" + REQUIRED_PORTAL_BLOCKS.stream().map(entry -> Registry.BLOCK.getId(entry).toString()).collect(Collectors.joining(", ")));
					player.sendMessage(message, false);
					return ActionResult.FAIL;
				}



				invalidItemSet.remove(Items.AIR); //We don't need to list air
				if (invalidItemSet.size() == 0 &&
						uniqueBlocksSet.size() >= WorldBlender.WB_CONFIG.WBPortalConfig.uniqueBlocksNeeded)
				{
					//enough unique blocks were found and no items are in chest. Make portal now
					for (BlockPos blockpos : BlockPos.iterate(position, position.add(cornerOffset)))
					{
						//consume chest and contents if config says so
						if (WorldBlender.WB_CONFIG.WBPortalConfig.consumeChests)
						{
							BlockEntity chestTileEntity = world.getBlockEntity(blockpos);
							if(chestTileEntity != null &&
									WBPortalSpawning.VALID_CHEST_BLOCKS_ENTITY_TYPES.getOrDefault(blockEntity.getType(), false))
							{
								for (int index = ((Inventory) chestTileEntity).size(); index >= 0; index--) {
									((Inventory) chestTileEntity).removeStack(index);
								}
							}
						}
						else
						{
							world.breakBlock(blockpos, true, player);
						}

						//create portal but with cooldown so players can grab items before they get teleported
						world.setBlockState(blockpos, WBBlocks.WORLD_BLENDER_PORTAL.getDefaultState(), 3);
						WBPortalBlockEntity wbtile = (WBPortalBlockEntity) world.getBlockEntity(blockpos);

						if(wbtile != null)
							wbtile.triggerCooldown();

						player.getActiveItem().decrement(1); //consume item in hand
					}

					return ActionResult.SUCCESS;
				}
				//throw error and list all the invalid items in the chests
				else
				{
					String msg = "§eWorld Blender: §fThere are not enough unique block items in the chests. (stacks or duplicates are ignored) You need §c" + WorldBlender.WB_CONFIG.WBPortalConfig.uniqueBlocksNeeded + "§f block items to make the portal but there is only §a" + uniqueBlocksSet.size() + "§f unique block items right now.";

					if(invalidItemSet.size() > 0)
					{
						//collect the items names into a list of strings
						List<String> invalidItemString = new ArrayList<>();
						invalidItemSet.forEach(item -> invalidItemString.add(item.getName(new ItemStack(item)).getString()));
						msg += "\n§f Also, here is a list of non-block items that were found and should be removed: §6" + String.join(", ", invalidItemString);
					}

					if(duplicateBlockSlotSet.size() != 0)
					{
						//collect the items names into a list of strings
						List<String> duplicateSlotString = new ArrayList<>();
						duplicateBlockSlotSet.remove(Items.AIR); //We dont need to list air
						duplicateBlockSlotSet.forEach(blockitem -> duplicateSlotString.add(blockitem.getName(new ItemStack(blockitem)).getString()));
						msg += "\n§f There are some slots that contains the same blocks and should be removed. These blocks are: §6" + String.join(", ", duplicateSlotString);
					}

					WorldBlender.LOGGER.log(Level.INFO, msg);
					player.sendMessage(new LiteralText(msg), false);

					return ActionResult.FAIL;
				}
			}
		}

		return ActionResult.PASS;
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
					for (BlockPos blockpos : BlockPos.iterate(position, position.add(offset)))
					{
						// We check if the block entity class itself has 'chest in the name.
						BlockEntity blockEntity = world.getBlockEntity(blockpos);
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
