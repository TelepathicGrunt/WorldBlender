package com.telepathicgrunt.world_blender.blocks;

import com.telepathicgrunt.world_blender.WBIdentifiers;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;


public class WBPortalBlock extends ContainerBlock
{
	protected static final VoxelShape COLLISION_BOX = Block.makeCuboidShape(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D);
	
	protected WBPortalBlock()
	{
		super(AbstractBlock.Properties.create(Material.PORTAL, MaterialColor.BLACK).doesNotBlockMovement().setLightLevel((blockState) -> 6).hardnessAndResistance(-1.0F, 3600000.0F).noDrops());
	}


	@Override
	public TileEntity createNewTileEntity(IBlockReader blockReader)
	{
		return new WBPortalBlockEntity();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return COLLISION_BOX;
	}

	@SuppressWarnings("resource")
	@Override
	public void onEntityCollision(BlockState blockState, World world, BlockPos position, Entity entity)
	{
		TileEntity blockEntityOriginal = world.getTileEntity(position);
		if (blockEntityOriginal instanceof WBPortalBlockEntity)
		{
			WBPortalBlockEntity wbBlockEntity = (WBPortalBlockEntity) blockEntityOriginal;

			if (!world.isRemote() &&
					!wbBlockEntity.isCoolingDown() &&
					!entity.isPassenger() &&
					!entity.isBeingRidden() &&
					entity.isNonBoss() &&
					VoxelShapes.compare(
							VoxelShapes.create(entity.getBoundingBox().offset(
									(-position.getX()),
									(-position.getY()),
									(-position.getZ()))),
							COLLISION_BOX,
							IBooleanFunction.AND))
			{
				//gets the world in the destination dimension
				MinecraftServer minecraftServer = entity.getServer(); // the server itself

				assert minecraftServer != null;
				ServerWorld destinationWorld = minecraftServer.getWorld(world.getDimensionKey().equals(WBIdentifiers.WB_WORLD_KEY) ? World.OVERWORLD : WBIdentifiers.WB_WORLD_KEY);
				ServerWorld originalWorld = minecraftServer.getWorld(entity.world.getDimensionKey());

				if(destinationWorld == null) return;
				BlockPos destPos = null;

				//looks for portal blocks in other dimension
				//within a 9x256x9 area
				boolean portalOrChestFound = false;
				for (BlockPos blockpos : BlockPos.getAllInBoxMutable(position.add(-4, -position.getY(), -4), position.add(4, 255 - position.getY(), 4)))
				{
					Block blockNearTeleport = destinationWorld.getBlockState(blockpos).getBlock();

					if (blockNearTeleport == WBBlocks.WORLD_BLENDER_PORTAL.get())
					{
						//gets portal block closest to players original xz coordinate
						if (destPos == null || (Math.abs(blockpos.getX() - position.getX()) < Math.abs(destPos.getX() - position.getX()) && Math.abs(blockpos.getZ() - position.getZ()) < Math.abs(destPos.getZ() - position.getZ())))
							destPos = blockpos.toImmutable();

						portalOrChestFound = true;

						//make portals have a cooldown after being teleported to
						TileEntity blockEntity = destinationWorld.getTileEntity(blockpos);
						if(blockEntity instanceof WBPortalBlockEntity){
							((WBPortalBlockEntity)blockEntity).triggerCooldown();
						}

						continue;
					}


					// We check if the block entity class itself has 'chest in the name.
					// Cache the result and only count the block entity if it is a chest.
					TileEntity blockEntity = destinationWorld.getTileEntity(blockpos);
					if(blockEntity == null || blockNearTeleport instanceof IInventory) continue;

					if (WBPortalSpawning.VALID_CHEST_BLOCKS_ENTITY_TYPES.getOrDefault(blockEntity.getType(), false))
					{
						//only set position to chest if no portal block is found
						if (destPos == null)
							destPos = blockpos.toImmutable();
						portalOrChestFound = true;
					}
				}

				//no portal or chest was found around destination. just teleport to top land
				if (!portalOrChestFound)
				{
					destPos = destinationWorld.getHeight(Heightmap.Type.WORLD_SURFACE, position);

					//places a portal block in World Blender so player can escape if
					//there is no portal block and then makes it be in cooldown
					if (destinationWorld.getDimensionKey().equals(WBIdentifiers.WB_WORLD_KEY))
					{
						destinationWorld.setBlockState(destPos, Blocks.AIR.getDefaultState());
						destinationWorld.setBlockState(destPos.up(), Blocks.AIR.getDefaultState());
						
						destinationWorld.setBlockState(destPos, WBBlocks.WORLD_BLENDER_PORTAL.get().getDefaultState());
						TileEntity blockEntity = destinationWorld.getTileEntity(destPos);
						if(blockEntity instanceof WBPortalBlockEntity){
							((WBPortalBlockEntity)blockEntity).triggerCooldown();
						}
					}
				}

				wbBlockEntity.teleportEntity(entity, destPos, destinationWorld, originalWorld);
			}
		}
	}


	/**
	 * Turns this portal blocks to air when right clicked while crouching
	 */
	@Override
	public ActionResultType onBlockActivated(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult rayTrace)
	{
		TileEntity blockEntity = world.getTileEntity(blockPos);
		if(playerEntity.isCrouching() &&
				blockEntity instanceof WBPortalBlockEntity &&
				((WBPortalBlockEntity)blockEntity).isRemoveable())
		{
			if (world.isRemote()) {
				//show lots of particles when portal is removed on client
				createLotsOfParticles(blockState, world, blockPos, world.rand);
			}
			else {
				//remove this portal on server side
				world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
			}
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.FAIL;
	}


	/**
	 * Shows particles around this block
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState blockState, World world, BlockPos position, Random random)
	{
		TileEntity TileEntity = world.getTileEntity(position);
		if (TileEntity instanceof WBPortalBlockEntity)
		{
			if (random.nextFloat() < 0.09f)
			{
				spawnParticle(world, position, random);
			}

		}
	}

	@OnlyIn(Dist.CLIENT)
	public void createLotsOfParticles(BlockState blockState, World world, BlockPos position, Random random)
	{
		TileEntity TileEntity = world.getTileEntity(position);
		if (TileEntity instanceof WBPortalBlockEntity)
		{
			for(int i = 0; i < 50; i++) 
			{
				spawnParticle(world, position, random);
			}
		}
	}

	private void spawnParticle(World world, BlockPos position, Random random) {
		double xPos = (double) position.getX() + (double) random.nextFloat();
		double yPos = (double) position.getY() + (double) random.nextFloat();
		double zPos = (double) position.getZ() + (double) random.nextFloat();
		double xVelocity = (random.nextFloat() - 0.5D) * 0.08D;
		double yVelocity = (random.nextFloat() - 0.5D) * 0.13D;
		double zVelocity = (random.nextFloat() - 0.5D) * 0.08D;

		world.addParticle(ParticleTypes.END_ROD, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity);
	}

	@Override
	public ItemStack getItem(IBlockReader p_185473_1_, BlockPos p_185473_2_, BlockState p_185473_3_)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isReplaceable(BlockState p_225541_1_, Fluid p_225541_2_)
	{
		return false;
	}
}
