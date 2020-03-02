package net.telepathicgrunt.worldblender.features;

import java.util.Random;
import java.util.function.Function;

import org.apache.logging.log4j.Level;

import com.mojang.datafixers.Dynamic;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.BarrelTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.OctavesNoiseGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.telepathicgrunt.worldblender.WorldBlender;


// Source is Dimension Dungeon's features :
// https://github.com/AllenSeitz/DimDungeons/blob/4be22b877fb358a632adc1cf61eaabed66e12cad/src/main/java/com/catastrophe573/dimdungeons/structure/DungeonBuilderLogic.java
public class DDDungeonFeature extends Feature<NoFeatureConfig>
{
	private String entrance[] = { "entrance_1", "entrance_2", "entrance_3", "entrance_4", "entrance_5", "entrance_6", "entrance_7", "entrance_8" };
	private String end[] = { "deadend_1", "deadend_2", "deadend_3", "deadend_4", "deadend_5", "deadend_6", "deadend_7", "deadend_8", "coffin_1", "advice_room_1", "restroom_1", "shoutout_1", "spawner_1", "redspuzzle_1", "deathtrap_1", "keyroom_1" };
	private String corner[] = { "corner_1", "corner_2", "corner_3", "corner_4", "corner_5", "corner_6", "corner_7", "corner_8", "redstrap_3", "longcorner_1", "longcorner_2", "longcorner_3", "longcorner_4", "longcorner_5", "skullcorner", "mazenotfound_1" };
	private String hallway[] = { "hallway_1", "hallway_2", "hallway_3", "hallway_4", "hallway_5", "hallway_6", "advice_room_3", "tempt_1", "redstrap_2", "extrahall_1", "extrahall_2", "extrahall_3", "coalhall_1", "moohall", "mazenotfound_3" };
	private String threeway[] = { "threeway_1", "threeway_2", "threeway_3", "threeway_4", "threeway_5", "advice_room_2", "redstrap_4", "morethree_1", "morethree_2", "morethree_3", "tetris_1", "mazenotfound_2" };
	private String fourway[] = { "fourway_1", "fourway_2", "fourway_3", "fourway_4", "fourway_5", "fourway_6", "fourway_7", "fourway_8", "fourway_9", "combat_1", "combat_1", "redstrap_1", "disco_1" };
	private String hardrooms[] = { 
			"swimmaze_1", 
			"combat_2", "combat_3", "combat_4", "combat_5", 
			"disco_2", "disco_3", "disco_4", 
			"tetris_2", "tetris_3" , 
			"tempt_2", "tempt_3", "tempt_4", 
			"coalhall_3", "coalhall_4", "coalhall_5" , 
			"extrahall_3", "extrahall_4", "extrahall_5", 
			"coffin_2", "coffin_3", "coffin_4", "coffin_5",
			"restroom_2", "restroom_3", "restroom_4", "restroom_5", 
			"shoutout_2",
			"redspuzzle_2", "redspuzzle_3", "redspuzzle_4",
			"deathtrap_2", "deathtrap_3", "deathtrap_4",
			"keytrap_1", "keytrap_2", "keytrap_3", "keytrap_4", "keytrap_5",
			"keyroom_2", "keyroom_3", "keyroom_4",
			"spawner_2", "spawner_3", "spawner_4", "spawner_5", "spawner_6"};
	private String[] roomTypes[] = { entrance, end, corner, hallway, threeway, fourway, hardrooms};

	
	protected long seed;
	protected OctavesNoiseGenerator noiseGen;
	public void setSeed(long seed)
	{
		if (this.noiseGen == null)
		{
			this.noiseGen = new OctavesNoiseGenerator(new SharedSeedRandom(seed), 1, 0);
		}

		this.seed = seed;
	}


	
	public DDDungeonFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory)
	{
		super(configFactory);
	}


	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> changedBlock, Random rand, BlockPos position, NoFeatureConfig config)
	{
		//low chance of spawning
		double noise = noiseGen.noiseAt((double)position.getX() * 0.0125D, (double)position.getZ() * 0.0125D, 0.0625D, 0.0625D);
		WorldBlender.LOGGER.warn(noise);
		if (noise < 0.4f)
		{
			return false;
		}

		int xChunk = position.getX() >> 4;
		int zChunk = position.getZ() >> 4;
		String roomName;

		//Creates checker pattern of 4 ways rooms with a random room in the other spots
		if(xChunk % 2 == zChunk % 2)
		{
			//picks only a 4 way room
			roomName = fourway[rand.nextInt(fourway.length)];
		}
		else {
			//picks a random room that can be any room
			String[] roomGroup = roomTypes[rand.nextInt(roomTypes.length)];
			roomName = roomGroup[rand.nextInt(roomGroup.length)];
		}
		
		
		TemplateManager templatemanager = ((ServerWorld) world.getWorld()).getSaveHandler().getStructureTemplateManager();
		Template template = templatemanager.getTemplate(new ResourceLocation("dimdungeons:" + roomName));

		if (template == null)
		{
			WorldBlender.LOGGER.warn("dimdungeons's "+roomName+" NTB does not exist!");
			return false;
		}

		//sets the dungeon at 20 and then as the noise increase towards the center, the dungeons raise in height slightly
		BlockPos finalPosition = new BlockPos(xChunk << 4, 20 + noise*8, zChunk << 4);
		PlacementSettings placementsettings = (new PlacementSettings()).setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(false).setChunk((ChunkPos) null);
		placementsettings.setBoundingBox(placementsettings.getBoundingBox());
		
		Rotation rot = Rotation.randomRotation(rand);
		if (rot == Rotation.COUNTERCLOCKWISE_90)
		{
		    // west: rotate CCW and push +Z
		    placementsettings.setRotation(Rotation.COUNTERCLOCKWISE_90);
		    position = position.add(0, 0, template.getSize().getZ() - 1);
		}
		else if (rot == Rotation.CLOCKWISE_90)
		{
		    // east rotate CW and push +X
		    placementsettings.setRotation(Rotation.CLOCKWISE_90);
		    position = position.add(template.getSize().getX() - 1, 0, 0);
		}
		else if (rot == Rotation.CLOCKWISE_180)
		{
		    // south: rotate 180 and push both +X and +Z
		    placementsettings.setRotation(Rotation.CLOCKWISE_180);
		    position = position.add(template.getSize().getX() - 1, 0, template.getSize().getZ() - 1);
		}
		else
		{
		    // north: no rotation
		    placementsettings.setRotation(Rotation.NONE);
		}
		
		try 
		{
			template.addBlocksToWorld(world, finalPosition, placementsettings);
		}
		catch(Exception e) 
		{
			WorldBlender.LOGGER.log(Level.WARN, "CRASHED WHILE MAKING "+ "dimdungeons:" + roomName+" AND ERROR IS: " +e.getCause()+e.getStackTrace());
			return false;
		}
		
		for (Template.BlockInfo template$blockinfo : template.func_215381_a(finalPosition, placementsettings, Blocks.STRUCTURE_BLOCK))
		{
			if (template$blockinfo.nbt != null)
			{
				StructureMode structuremode = StructureMode.valueOf(template$blockinfo.nbt.getString("mode"));
				if (structuremode == StructureMode.DATA)
				{
					handleDataBlock(template$blockinfo.nbt.getString("metadata"), template$blockinfo.pos, world, world.getRandom(), placementsettings.getBoundingBox());
				}
			}
		}

		WorldBlender.LOGGER.log(Level.WARN, "dimdungeons's "+roomName+" built at " + finalPosition.getX() + ", " + finalPosition.getY() + ", " + finalPosition.getZ());
		return true;
	}

	
	
	//////////////////////////////////////////////////////////////////////////////////////
	// Mainly from DimDungeons's code to make the dungeons be as close to the real deal //
	// https://github.com/AllenSeitz/DimDungeons/blob/4be22b877fb358a632adc1cf61eaabed66e12cad/src/main/java/com/catastrophe573/dimdungeons/structure/DungeonBuilderLogic.java


	// resembles TemplateStructurePiece.handleDataMarker()
	protected static void handleDataBlock(String name, BlockPos pos, IWorld world, Random rand, MutableBoundingBox bb)
	{
		//DimDungeons.LOGGER.info("DATA BLOCK NAME: " + name);

		if ("LockIt".equals(name))
		{
			LockDispensersAround(world, pos);
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block 
		}
		else if ("LockItStoneBrick".equals(name))
		{
			LockDispensersAround(world, pos);
			world.setBlockState(pos, Blocks.STONE_BRICKS.getDefaultState(), 2); // erase this data block 
		}
		else if ("ReturnPortal".equals(name))
		{
			LockDispensersAround(world, pos);
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block 
		}
		else if ("FortuneTeller".equals(name))
		{
			world.setBlockState(pos, Blocks.STONE_BRICKS.getDefaultState(), 2); // erase this data block 
			faceContainerTowardsAir(world, pos.down());
			LockDispensersAround(world, pos.down());
		}
		else if ("ChestLoot1".equals(name))
		{
			// 80% loot_1, 20% loot_2
			int lucky = rand.nextInt(100);
			if (lucky < 80)
			{
				fillChestBelow(pos, new ResourceLocation("dimdungeons:chests/chestloot_1"), world, rand);
			}
			else
			{
				fillChestBelow(pos, new ResourceLocation("dimdungeons:chests/chestloot_2"), world, rand);
			}
		}
		else if ("ChestLoot2".equals(name))
		{
			fillChestBelow(pos, new ResourceLocation("dimdungeons:chests/chestloot_2"), world, rand);
		}
		else if ("ChestLootLucky".equals(name))
		{
			// 70% nothing, 30% random minecraft loot table that isn't an end city
			int lucky = rand.nextInt(100);
			if (lucky < 30)
			{
				fillChestBelow(pos, new ResourceLocation("dimdungeons:chests/chestloot_lucky"), world, rand);
			}
			else
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block 
				world.setBlockState(pos.down(), Blocks.AIR.getDefaultState(), 2); // and erase the chest below it
			}
		}
		else if ("SetTrappedLoot".equals(name))
		{
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block
			ChestTileEntity te = (ChestTileEntity) world.getTileEntity(pos.down());
			if (te != null)
			{
				te.clear();
				te.setLootTable(new ResourceLocation("dimdungeons:chests/chestloot_1"), rand.nextLong());
			}
		}
		else if ("BarrelLoot1".equals(name))
		{
			// 80% loot_1, 20% loot_2
			int lucky = rand.nextInt(100);
			if (lucky < 80)
			{
				fillBarrelBelow(pos, new ResourceLocation("dimdungeons:chests/chestloot_1"), world, rand);
			}
			else
			{
				fillBarrelBelow(pos, new ResourceLocation("dimdungeons:chests/chestloot_2"), world, rand);
			}
		}
		else if ("PlaceL2Key".equals(name))
		{
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block
		}
		else if ("SummonWitch".equals(name))
		{
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block
			spawnEnemyHere(pos, "witch", world);
		}
		else if ("SummonWaterEnemy".equals(name))
		{
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block
			int chance = rand.nextInt(100);
			if (chance < 80)
			{
				spawnEnemyHere(pos, "guardian", world);
			}
			else
			{
				spawnEnemyHere(pos, "drowned", world);
			}
		}
		else if ("SummonEnderman".equals(name))
		{
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block
			spawnEnemyHere(pos, "enderman", world);
		}
		else if ("SummonEnemy1".equals(name))
		{
			// 50% chance of a weak enemy
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block
			int chance = rand.nextInt(100);
			if (chance < 16)
			{
				spawnEnemyHere(pos, "zombie", world);
			}
			else if (chance < 32)
			{
				spawnEnemyHere(pos, "husk", world);
			}
			else if (chance < 48)
			{
				spawnEnemyHere(pos, "drowned", world);
			}
			else if (chance < 64)
			{
				spawnEnemyHere(pos, "spider", world);
			}
		}
		else if ("SummonEnemy2".equals(name))
		{
			// 80% chance of a strong enemy
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block
			int chance = rand.nextInt(100);
			if (chance < 20)
			{
				spawnEnemyHere(pos, "wither_skeleton", world);
			}
			else if (chance < 40)
			{
				spawnEnemyHere(pos, "stray", world);
			}
			else if (chance < 60)
			{
				spawnEnemyHere(pos, "skeleton", world);
			}
			else if (chance < 80)
			{
				spawnEnemyHere(pos, "pillager", world);
			}
		}
		else
		{
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block
		}
	}


	private static void spawnEnemyHere(BlockPos pos, String casualName, IWorld world)
	{
		MobEntity mob = null;

		if ("witch".contentEquals(casualName))
		{
			mob = EntityType.WITCH.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		else if ("enderman".contentEquals(casualName))
		{
			mob = EntityType.ENDERMAN.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 2, pos.getZ());
		}
		else if ("guardian".contentEquals(casualName))
		{
			mob = EntityType.GUARDIAN.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		else if ("zombie".contentEquals(casualName))
		{
			mob = EntityType.ZOMBIE.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		else if ("husk".contentEquals(casualName))
		{
			mob = EntityType.HUSK.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		else if ("drowned".contentEquals(casualName))
		{
			mob = EntityType.DROWNED.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		else if ("skeleton".contentEquals(casualName))
		{
			mob = EntityType.SKELETON.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		else if ("wither_skeleton".contentEquals(casualName))
		{
			mob = EntityType.WITHER_SKELETON.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		else if ("stray".contentEquals(casualName))
		{
			mob = EntityType.STRAY.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		else if ("spider".contentEquals(casualName))
		{
			mob = EntityType.SPIDER.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		else if ("pillager".contentEquals(casualName))
		{
			mob = EntityType.PILLAGER.create(world.getWorld());
			mob.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		}
		else
		{
			System.out.println("DungeonChunkGenerator: Attempting to spawn unrecognized enemy: " + casualName);
			return;
		}

		mob.setCanPickUpLoot(false);
		//mob.setCustomName(new StringTextComponent(I18n.format("enemy.dimdungeons." + casualName)));
		mob.setCustomName(new TranslationTextComponent("enemy.dimdungeons." + casualName));
		mob.setHomePosAndDistance(pos, 8);
		mob.moveToBlockPosAndAngles(pos, 0.0F, 0.0F);
		mob.enablePersistence();

		mob.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.STRUCTURE, (ILivingEntityData) null, (CompoundNBT) null);
		world.addEntity(mob);
	}


	private static void fillChestBelow(BlockPos pos, ResourceLocation lootTable, IWorld world, Random rand)
	{
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block
		faceContainerTowardsAir(world, pos.down());

		// set the loot table
		TileEntity te = world.getTileEntity(pos.down());
		if (te instanceof ChestTileEntity)
		{
			((ChestTileEntity) te).clear();
			((ChestTileEntity) te).setLootTable(lootTable, rand.nextLong());
		}
	}


	private static void fillBarrelBelow(BlockPos pos, ResourceLocation lootTable, IWorld world, Random rand)
	{
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2); // erase this data block

		// set the loot table
		TileEntity te = world.getTileEntity(pos.down());
		if (te instanceof BarrelTileEntity)
		{
			((BarrelTileEntity) te).clear();
			((BarrelTileEntity) te).setLootTable(lootTable, rand.nextLong());
		}
	}


	// this function might not be needed unless the DungeonDimension::canMineBlock() returns true for Dispensers
	private static void LockDispensersAround(IWorld world, BlockPos pos)
	{
		//	Random r = new Random((world.getSeed() + (long) (pos.getX() * pos.getX() * 4987142) + (long) (pos.getX() * 5947611) + (long) (pos.getZ() * pos.getZ()) * 4392871L + (long) (pos.getZ() * 389711) ^ world.getSeed()));
		//
		//	// make sure the player cannot be holding an item with this name
		//	LockCode code = new LockCode("ThisIsIntentionallyLongerThanCanNormallyBePossiblePlus" + r.nextLong());
		//
		//	if (world.getBlockState(pos.up()).getBlock() == Blocks.DISPENSER)
		//	{
		//	    //((DispenserTileEntity) world.getTileEntity(pos.up())).setLockCode(code);
		//	}
		//	if (world.getBlockState(pos.down()).getBlock() == Blocks.DISPENSER)
		//	{
		//	    //((DispenserTileEntity) world.getTileEntity(pos.down())).setLockCode(code);
		//	}
		//	if (world.getBlockState(pos.north()).getBlock() == Blocks.DISPENSER)
		//	{
		//	    //((DispenserTileEntity) world.getTileEntity(pos.north())).setLockCode(code);
		//	}
		//	if (world.getBlockState(pos.south()).getBlock() == Blocks.DISPENSER)
		//	{
		//	    //((DispenserTileEntity) world.getTileEntity(pos.south())).setLockCode(code);
		//	}
		//	if (world.getBlockState(pos.west()).getBlock() == Blocks.DISPENSER)
		//	{
		//	    //((DispenserTileEntity) world.getTileEntity(pos.west())).setLockCode(code);
		//	}
		//	if (world.getBlockState(pos.east()).getBlock() == Blocks.DISPENSER)
		//	{
		//	    //((DispenserTileEntity) world.getTileEntity(pos.east())).setLockCode(code);
		//	}
	}


	// used on dispensers and chests, particularly ones created by data blocks
	// this function might not be needed in versions later thaN 1.13
	private static void faceContainerTowardsAir(IWorld world, BlockPos pos)
	{
		BlockState bs = world.getBlockState(pos);

		if (bs.getBlock() == Blocks.DISPENSER || bs.getBlock() == Blocks.CHEST)
		{
			if (world.getBlockState(pos.north()).getBlock() == Blocks.AIR)
			{
				//bs.with(DispenserBlock.FACING, Direction.NORTH);
			}
			if (world.getBlockState(pos.south()).getBlock() == Blocks.AIR)
			{
				//bs.with(DispenserBlock.FACING, Direction.SOUTH);
			}
			if (world.getBlockState(pos.west()).getBlock() == Blocks.AIR)
			{
				//bs.with(DispenserBlock.FACING, Direction.WEST);
			}
			if (world.getBlockState(pos.east()).getBlock() == Blocks.AIR)
			{
				//bs.with(DispenserBlock.FACING, Direction.EAST);
			}
			world.setBlockState(pos, bs, 2);
		}
	}
}
