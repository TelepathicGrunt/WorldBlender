package net.telepathicgrunt.worldblender.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.telepathicgrunt.worldblender.dimension.WBDimension;


public class WBPortalBlock extends ContainerBlock
{
	protected WBPortalBlock()
	{
		super(Block.Properties.create(Material.PORTAL, MaterialColor.BLACK).doesNotBlockMovement().lightValue(5).hardnessAndResistance(-1.0F, 3600000.0F).noDrops());
	}


	public TileEntity createNewTileEntity(IBlockReader blockReader)
	{
		return new WBPortalTileEntity();
	}


	public void onEntityCollision(BlockState blockState, World world, BlockPos position, Entity entity)
	{
//		if (!world.isRemote && 
//			!entity.isPassenger() && 
//			!entity.isBeingRidden() && 
//			entity.isNonBoss() && 
//			VoxelShapes.compare(VoxelShapes.create(entity.getBoundingBox().offset((double) (-position.getX()), (double) (-position.getY()), (double) (-position.getZ()))), blockState.getShape(world, position), IBooleanFunction.AND))
//		{
//			entity.changeDimension(world.dimension.getType() == WBDimension.worldblender() ? DimensionType.OVERWORLD : WBDimension.worldblender());
//		}
	}


	/**
	 * Shows particles around this block
	 */
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState blockState, World world, BlockPos position, Random random)
	{
		TileEntity tileentity = world.getTileEntity(position);
		if (tileentity instanceof EndGatewayTileEntity)
		{
			int maxParticleCount = ((EndGatewayTileEntity) tileentity).getParticleAmount() * 3; //LOTS OF PARTICLES

			for (int currentParticleCount = 0; currentParticleCount < maxParticleCount; ++currentParticleCount)
			{
				double xPos = (double) position.getX() + (double) random.nextFloat();
				double yPos = (double) position.getY() + (double) random.nextFloat();
				double zPos = (double) position.getZ() + (double) random.nextFloat();
				double xVelocity = ((double) random.nextFloat() - 0.5D) * 0.5D;
				double yVelocity = ((double) random.nextFloat() - 0.5D) * 0.5D;
				double zVelocity = ((double) random.nextFloat() - 0.5D) * 0.5D;
				int speedScale = random.nextInt(2) * 2 - 1;
				if (random.nextBoolean())
				{
					zPos = (double) position.getZ() + 0.5D + 0.25D * (double) speedScale;
					zVelocity = (double) (random.nextFloat() * 2.0F * (float) speedScale);
				}
				else
				{
					xPos = (double) position.getX() + 0.5D + 0.25D * (double) speedScale;
					xVelocity = (double) (random.nextFloat() * 2.0F * (float) speedScale);
				}

				world.addParticle(ParticleTypes.DRAGON_BREATH, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity);
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
