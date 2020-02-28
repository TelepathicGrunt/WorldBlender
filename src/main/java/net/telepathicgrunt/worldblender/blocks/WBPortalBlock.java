package net.telepathicgrunt.worldblender.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.telepathicgrunt.worldblender.dimension.WBDimension;


public class WBPortalBlock extends ContainerBlock
{
	protected WBPortalBlock()
	{
		super(Block.Properties.create(Material.PORTAL, MaterialColor.BLACK).doesNotBlockMovement().lightValue(6).hardnessAndResistance(-1.0F, 3600000.0F).noDrops());
	}


	public TileEntity createNewTileEntity(IBlockReader blockReader)
	{
		return new WBPortalTileEntity();
	}


	public void onEntityCollision(BlockState blockState, World world, BlockPos position, Entity entity)
	{
		TileEntity tileentity = world.getTileEntity(position);
		if (tileentity instanceof WBPortalTileEntity)
		{
			WBPortalTileEntity wbtile = (WBPortalTileEntity) tileentity;

			if (!world.isRemote && !wbtile.isCoolingDown() && !entity.isPassenger() && !entity.isBeingRidden() && entity.isNonBoss() && VoxelShapes.compare(VoxelShapes.create(entity.getBoundingBox().offset((double) (-position.getX()), (double) (-position.getY()), (double) (-position.getZ()))), blockState.getShape(world, position), IBooleanFunction.AND))
			{
				//gets the world in the destination dimension
				MinecraftServer minecraftServer = entity.getServer(); // the server itself
				ServerWorld destinationWorld = minecraftServer.getWorld(world.dimension.getType() == WBDimension.worldblender() ? DimensionType.OVERWORLD : WBDimension.worldblender());
				ServerWorld originalWorld = minecraftServer.getWorld(entity.dimension);

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
						WBPortalTileEntity wbtile2 = (WBPortalTileEntity) destinationWorld.getTileEntity(blockpos);
						wbtile2.triggerCooldown();
					}
					else if (blockNearTeleport.getTags().contains(net.minecraftforge.common.Tags.Blocks.CHESTS.getId()))
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
					destPos = destinationWorld.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, position);

					//places a portal block in World Blender so player can escape if
					//there is no portal block and then makes it be in cooldown
					if (destinationWorld.getWorld().dimension.getType() == WBDimension.worldblender())
					{
						destinationWorld.setBlockState(destPos, WBBlocks.WORLD_BLENDER_PORTAL.get().getDefaultState());
						WBPortalTileEntity wbtile2 = (WBPortalTileEntity) destinationWorld.getTileEntity(destPos);
						wbtile2.triggerCooldown();
					}
				}

				wbtile.teleportEntity(entity, destPos, destinationWorld, originalWorld);
			}
		}
	}


	/**
	 * Turns this portal and all neightboring portal blocks to air when right clicked while crouching
	 */
	public ActionResultType onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult rayTrace)
	{
		if(playerEntity.isCrouching()) 
		{
			if (world.isRemote)
			{
				//show lots of particles when portal is removed
				createLotsOfParticles(blockState, world, blockPos, world.rand);
				return ActionResultType.SUCCESS;
			}
			else
			{
				//remove this portal
				world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
				
				return ActionResultType.SUCCESS;
			}
		}
		
		return ActionResultType.FAIL;
	}


	/**
	 * Shows particles around this block
	 */
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState blockState, World world, BlockPos position, Random random)
	{
		TileEntity tileentity = world.getTileEntity(position);
		if (tileentity instanceof WBPortalTileEntity)
		{
			if (random.nextFloat() < 0.09f)
			{
				double xPos = (double) position.getX() + (double) random.nextFloat();
				double yPos = (double) position.getY() + (double) random.nextFloat();
				double zPos = (double) position.getZ() + (double) random.nextFloat();
				double xVelocity = ((double) random.nextFloat() - 0.5D) * 0.08D;
				double yVelocity = ((double) random.nextFloat() - 0.5D) * 0.13D;
				double zVelocity = ((double) random.nextFloat() - 0.5D) * 0.08D;

				world.addParticle(ParticleTypes.END_ROD, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity);
			}

		}
	}

	@OnlyIn(Dist.CLIENT)
	public void createLotsOfParticles(BlockState blockState, World world, BlockPos position, Random random)
	{
		TileEntity tileentity = world.getTileEntity(position);
		if (tileentity instanceof WBPortalTileEntity)
		{
			for(int i = 0; i < 50; i++) 
			{
				double xPos = (double) position.getX() + (double) random.nextFloat();
				double yPos = (double) position.getY() + (double) random.nextFloat();
				double zPos = (double) position.getZ() + (double) random.nextFloat();
				double xVelocity = ((double) random.nextFloat() - 0.5D) * 0.08D;
				double yVelocity = ((double) random.nextFloat() - 0.5D) * 0.13D;
				double zVelocity = ((double) random.nextFloat() - 0.5D) * 0.08D;

				world.addParticle(ParticleTypes.END_ROD, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity);
			}
		}
	}

	public ItemStack getItem(IBlockReader p_185473_1_, BlockPos p_185473_2_, BlockState p_185473_3_)
	{
		return ItemStack.EMPTY;
	}


	public boolean canBucketPlace(BlockState p_225541_1_, Fluid p_225541_2_)
	{
		return false;
	}
}
