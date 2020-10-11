package com.telepathicgrunt.world_blender.blocks;

import com.telepathicgrunt.world_blender.WBIdentifiers;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;


public class WBPortalBlockEntity extends BlockEntity implements Tickable
{
	private float teleportCooldown = 300;
	private boolean removeable = true;


	public WBPortalBlockEntity()
	{
		super(WBBlocks.WORLD_BLENDER_PORTAL_BE);
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


	public void teleportEntity(Entity entity, BlockPos destPos, ServerWorld destinationWorld, ServerWorld originalWorld)
	{
		this.triggerCooldown();

		if(entity instanceof PlayerEntity) {
			((ServerPlayerEntity) entity).teleport(
					destinationWorld, 
					destPos.getX() + 0.5D,
					destPos.getY() + 1D,
					destPos.getZ() + 0.5D, 
					entity.yaw, 
					entity.pitch);
		}
		else {
	         Entity entity2 = entity.getType().create(destinationWorld);
	         if (entity2 != null) {
	        	 entity2.copyFrom(entity);
	        	 entity2.refreshPositionAndAngles(destPos, entity.yaw, entity.pitch);
	        	 entity2.setVelocity(entity.getVelocity());
	        	 destinationWorld.onDimensionChanged(entity2);
	         }
	         entity.remove();
			 assert this.world != null;
			 this.world.getProfiler().pop();
	         originalWorld.resetIdleTimeout();
	         destinationWorld.resetIdleTimeout();
	         this.world.getProfiler().pop();
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

	public void setCoolDown(float cooldown)
	{
		this.teleportCooldown = cooldown;
	}

	public void triggerCooldown()
	{
		if(this.world == null || this.world.isClient()) return;

		this.teleportCooldown = 300;
		this.markDirty();
		this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);

		// Send cooldown to client to display visually
		PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
		passedData.writeBlockPos(this.pos);
		passedData.writeFloat(this.getCoolDown());

		PlayerStream.world(world).forEach(player ->
				ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, WBIdentifiers.PORTAL_COOLDOWN_PACKET_ID, passedData));
	}
	
	public boolean isRemoveable()
	{
		return this.removeable;
	}
	
	public void makeNotRemoveable()
	{
		this.removeable = false;
		this.markDirty();
	}

	@Override
	public CompoundTag toTag(CompoundTag data)
	{
		super.toTag(data);
		data.putFloat("Cooldown", this.teleportCooldown);
		data.putBoolean("Removeable", this.removeable);
		return data;
	}


	@Override
	public void fromTag(BlockState blockState, CompoundTag data)
	{
		super.fromTag(blockState, data);
		if(data.contains("Cooldown")) 
		{
			this.teleportCooldown = data.getFloat("Cooldown");
		}
		else 
		{
			this.teleportCooldown = 300; //if this is missing cooldown entry, have it start with a cooldown
		}
		
		this.removeable = data.getBoolean("Removeable");
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldRenderFace(Direction direction)
	{
		return true;
		//can't get this to work properly
		//return Block.shouldDrawSide(this.getCachedState(), this.world, this.getPos(), direction);
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return false;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public double getSquaredRenderDistance()
	{
		return 65536.0D;
	}


	/**
	 * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For modded
	 * TE's, this packet comes back to you clientside
	 */
	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket()
	{
		return new BlockEntityUpdateS2CPacket(this.pos, 0, this.toInitialChunkDataTag());
	}
	

	/**
	 * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when many
	 * blocks change at once. This compound comes back to you clientside
	 */
	@Override
	public CompoundTag toInitialChunkDataTag()
	{
		return this.toTag(new CompoundTag());
	}

}
