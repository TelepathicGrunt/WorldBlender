package net.telepathicgrunt.worldblender.blocks;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
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

		if (isCoolingDown != this.isCoolingDown())
		{
			this.markDirty();
		}
	}


	public void teleportEntity(Entity entity, BlockPos destPos, ServerWorld destinationWorld)
	{
		this.triggerCooldown();

		if(entity instanceof PlayerEntity) {
			((ServerPlayerEntity) entity).teleport(
					destinationWorld, 
					destPos.getX() + 0.5D,
					destPos.getY() + 1D,
					destPos.getZ() + 0.5D, 
					entity.rotationYaw, 
					entity.rotationPitch);
		}
		else {
			entity.setWorld(destinationWorld);
			entity.dimension = destinationWorld.dimension.getType();
			entity.teleportKeepLoaded(
					destPos.getX() + 0.5D,
					destPos.getY() + 1D,
					destPos.getZ() + 0.5D);
		}
	}

	public boolean isCoolingDown()
	{
		return this.teleportCooldown > 0;
	}


	public float getCoolDown()
	{
		return this.teleportCooldown;
	}


	public void triggerCooldown()
	{
		this.teleportCooldown = 300;
		this.world.addBlockEvent(this.getPos(), this.getBlockState().getBlock(), 1, 0);
		this.markDirty();
	}


	@Override
	public CompoundNBT write(CompoundNBT data)
	{
		super.write(data);
		data.putFloat("Cooldown", this.teleportCooldown);
		return data;
	}


	@Override
	public void read(CompoundNBT data)
	{
		super.read(data);
		this.teleportCooldown = data.getFloat("Cooldown");
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


	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}


	/**
	 * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For modded
	 * TE's, this packet comes back to you clientside in {@link #onDataPacket}
	 */
	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(this.pos, 8, this.getUpdateTag());
	}


	/**
	 * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when many
	 * blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
	 */
	public CompoundNBT getUpdateTag()
	{
		return this.write(new CompoundNBT());
	}

}
