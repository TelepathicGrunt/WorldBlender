package com.telepathicgrunt.worldblender.blocks;

import com.telepathicgrunt.worldblender.mixin.BlockAccessor;
import com.telepathicgrunt.worldblender.utils.MessageHandler;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;


public class WBPortalBlockEntity extends TileEntity implements ITickableTileEntity {
    private float teleportCooldown = 300;
    private boolean removeable = true;

    // Culling optimization by Comp500
    // https://github.com/comp500/PolyDungeons/blob/master/src/main/java/polydungeons/block/entity/DecorativeEndBlockEntity.java
    private final Direction[] FACINGS = Direction.values();
    private int cachedCullFaces = 0;
    private boolean hasCachedFaces = false;


    public WBPortalBlockEntity() {
        super(WBBlocks.WORLD_BLENDER_PORTAL_BE.get());
    }


    @Override
    public void tick() {
        boolean isCoolingDown = this.isCoolingDown();
        if (isCoolingDown) {
            --this.teleportCooldown;
        }

        if (isCoolingDown != this.isCoolingDown()) {
            this.markDirty();
        }
    }


    public void teleportEntity(Entity entity, BlockPos destPos, ServerWorld destinationWorld, ServerWorld originalWorld) {
        this.triggerCooldown();

        if (entity instanceof PlayerEntity) {
            ((ServerPlayerEntity) entity).teleport(
                    destinationWorld,
                    destPos.getX() + 0.5D,
                    destPos.getY() + 1D,
                    destPos.getZ() + 0.5D,
                    entity.rotationYaw,
                    entity.rotationPitch);
        }
        else {
            Entity entity2 = entity.getType().create(destinationWorld);
            if (entity2 != null) {
                entity2.copyDataFromOld(entity);
                entity2.moveToBlockPosAndAngles(destPos, entity.rotationYaw, entity.rotationPitch);
                entity2.setMotion(entity.getMotion());
                destinationWorld.addFromAnotherDimension(entity2);
            }
            entity.remove();
            assert this.world != null;
            this.world.getProfiler().endSection();
            originalWorld.resetUpdateEntityTick();
            destinationWorld.resetUpdateEntityTick();
            this.world.getProfiler().endSection();
        }
    }

    public boolean isCoolingDown() {
        return this.teleportCooldown > 0;
    }

    public float getCoolDown() {
        return this.teleportCooldown;
    }

    public void setCoolDown(float cooldown) {
        this.teleportCooldown = cooldown;
    }

    public void triggerCooldown() {
        if (this.world == null || this.world.isRemote()) return;

        this.teleportCooldown = 300;
        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);

        // Send cooldown to client to display visually
        MessageHandler.UpdateTECooldownPacket.sendToClient(this.pos, this.getCoolDown());
    }

    public boolean isRemoveable() {
        return this.removeable;
    }

    public void makeNotRemoveable() {
        this.removeable = false;
        this.markDirty();
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        super.write(data);
        data.putFloat("Cooldown", this.teleportCooldown);
        data.putBoolean("Removeable", this.removeable);
        return data;
    }


    @Override
    public void read(BlockState blockState, CompoundNBT data) {
        super.read(blockState, data);
        if (data.contains("Cooldown")) {
            this.teleportCooldown = data.getFloat("Cooldown");
        }
        else {
            this.teleportCooldown = 300; //if this is missing cooldown entry, have it start with a cooldown
        }

        this.removeable = data.getBoolean("Removeable");
    }

    /**
     * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For modded
     * TE's, this packet comes back to you clientside
     */
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }


    /**
     * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when many
     * blocks change at once. This compound comes back to you clientside
     */
    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    // CLIENT-SIDED
    public boolean shouldRenderFace(Direction direction) {
        return shouldDrawSide(direction);
    }

    @Deprecated
    // CLIENT-SIDED
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return false;
    }

    @Override
    // CLIENT-SIDED
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    // CLIENT-SIDED
    public void updateCullFaces() {
        assert world != null;
        hasCachedFaces = true;
        int mask;
        for (Direction dir : FACINGS) {
            mask = 1 << dir.getIndex();
            if (shouldDrawSideSpecialized(getBlockState(), world, getPos(), dir)) {
                cachedCullFaces |= mask;
            }
            else {
                cachedCullFaces &= ~mask;
            }
        }
    }

    public boolean shouldDrawSide(Direction direction) {
        // Cull faces that are not visible
        if (!hasCachedFaces) {
            updateCullFaces();
        }
        return (cachedCullFaces & (1 << direction.getIndex())) != 0;
    }

    public static void updateCullCache(BlockPos pos, World world) {
        updateCullCacheNeighbor(pos.up(), world);
        updateCullCacheNeighbor(pos.down(), world);
        updateCullCacheNeighbor(pos.north(), world);
        updateCullCacheNeighbor(pos.east(), world);
        updateCullCacheNeighbor(pos.south(), world);
        updateCullCacheNeighbor(pos.west(), world);
    }

    public static void updateCullCacheNeighbor(BlockPos pos, World world) {
        TileEntity be = world.getTileEntity(pos);
        if (be instanceof WBPortalBlockEntity) {
            ((WBPortalBlockEntity) be).updateCullFaces();
        }
    }

    /**
     * Need out own implementation because the original method uses getOutlineShape to extrude and cause
     * our block's sides to be culled for snow and slabs. We need getOutlineShape so we can right click the block.
     */
    public static boolean shouldDrawSideSpecialized(BlockState state, IBlockReader world, BlockPos pos, Direction facing) {
        BlockPos blockPos = pos.offset(facing);
        BlockState blockState = world.getBlockState(blockPos);
        // Do not draw side for our block if bordering our own block.
        if (state.isSideInvisible(blockState, facing) || blockState.getBlock() == WBBlocks.WORLD_BLENDER_PORTAL.get()) {
            return false;
        }
        else if (blockState.isSolid()) {
            Block.RenderSideCacheKey neighborGroup = new Block.RenderSideCacheKey(state, blockState, facing);
            Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey> object2ByteLinkedOpenHashMap = BlockAccessor.wb_getSHOULD_SIDE_RENDER_CACHE().get();
            byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(neighborGroup);
            if (b != 127) {
                return b != 0;
            }
            else {
                VoxelShape voxelShape = VoxelShapes.fullCube(); // No extrusions for our block.
                VoxelShape voxelShape2 = blockState.getFaceOcclusionShape(world, blockPos, facing.getOpposite());
                boolean bl = VoxelShapes.compare(voxelShape, voxelShape2, IBooleanFunction.ONLY_FIRST);
                if (object2ByteLinkedOpenHashMap.size() == 2048) {
                    object2ByteLinkedOpenHashMap.removeLastByte();
                }

                object2ByteLinkedOpenHashMap.putAndMoveToFirst(neighborGroup, (byte) (bl ? 1 : 0));
                return bl;
            }
        }
        else {
            return true;
        }
    }
}
