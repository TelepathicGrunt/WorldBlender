package net.telepathicgrunt.worldblender.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class WBPortalTileEntity extends TileEntity implements ITickableTileEntity
{
	private float teleportCooldown;


	public WBPortalTileEntity()
	{
		super(WBBlocks.WORLD_BLENDER_PORTAL_TILE.get());
	}


	@Override
	public void tick()
	{
		boolean isCoolingDown = this.isCoolingDown();
		if (isCoolingDown)
		{
			--this.teleportCooldown;
		}
		else if (!this.world.isRemote)
		{
			List<Entity> list = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.getPos()));
			if (!list.isEmpty())
			{
				this.teleportEntity(list.get(0).getLowestRidingEntity());
			}
		}

		if (isCoolingDown != this.isCoolingDown())
		{
			this.markDirty();
		}

	}


	public void teleportEntity(Entity p_195496_1_)
	{
		this.teleportCooldown = 100;
		//	         if (this.exitPortal == null && this.world.dimension instanceof EndDimension) {
		//	            this.createPortal((ServerWorld)this.world);
		//	         }
		//
		//	         if (this.exitPortal != null) {
		//	            BlockPos blockpos = this.exactTeleport ? this.exitPortal : this.findExitPosition();
		//	            p_195496_1_.teleportKeepLoaded((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D);
		//	         }

		this.triggerCooldown();
	}


	public boolean isCoolingDown()
	{
		return this.teleportCooldown > 0;
	}


	public void triggerCooldown()
	{
		this.teleportCooldown = 40;
		this.world.addBlockEvent(this.getPos(), this.getBlockState().getBlock(), 1, 0);
		this.markDirty();
	}


	@OnlyIn(Dist.CLIENT)
	public int getParticleAmount()
	{
		int visibleFaces = 0;

		for (Direction direction : Direction.values())
		{
			visibleFaces += this.shouldRenderFace(direction) ? 1 : 0;
		}

		return visibleFaces;
	}


	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderFace(Direction direction)
	{
		return Block.shouldSideBeRendered(this.getBlockState(), this.world, this.getPos(), direction);
	}
}
