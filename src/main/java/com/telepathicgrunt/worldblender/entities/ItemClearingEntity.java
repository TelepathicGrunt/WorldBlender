package com.telepathicgrunt.worldblender.entities;

import com.telepathicgrunt.worldblender.mixin.blocks.AbstractRailBlockInvoker;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

public class ItemClearingEntity extends Entity {
   private int ticksTillDetonation;
   private final int tickCountdownStart = 50;

   public ItemClearingEntity(World worldIn) {
      this(WBEntities.ITEM_CLEARING_ENTITY.get(), worldIn);
   }

   public ItemClearingEntity(EntityType<? extends ItemClearingEntity> type, World worldIn) {
      super(type, worldIn);
      ticksTillDetonation = tickCountdownStart;
   }

   @Override
   @SuppressWarnings("unchecked")
   public EntityType<? extends ItemClearingEntity> getType() {
      return (EntityType<? extends ItemClearingEntity>) super.getType();
   }

   @Override
   protected void registerData() {}

   @Override
   public IPacket<?> createSpawnPacket() {
      return new SSpawnObjectPacket(this);
   }

   @Override
   public void writeAdditional(CompoundNBT compound) {
      compound.putInt("ticksTillDetonation", this.ticksTillDetonation);
   }

   @Override
   public void readAdditional(CompoundNBT compound) {
      this.ticksTillDetonation = compound.getInt("ticksTillDetonation");
      if(this.ticksTillDetonation == 0) this.ticksTillDetonation = tickCountdownStart;
   }

   @Override
   public void tick() {
      if(ticksTillDetonation > 0){
         // Force blocks to update themselves and tick so they break
         if(ticksTillDetonation == tickCountdownStart - 2){
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            Chunk chunk = this.world.getChunk(this.chunkCoordX, this.chunkCoordZ);
            BlockPos chunkBlockPos = chunk.getPos().asBlockPos();

            for(int x = 0; x < 16; x++){
               for(int z = 0; z < 16; z++){
                  for(int y = 0; y < this.world.getHeight(); y++){
                     BlockState currentState = chunk.getBlockState(mutable.setAndOffset(chunkBlockPos, x, y, z));

                     // Special case as rails return themselves from getValidBlockForPosition when they shouldn't. Rails bad
                     if(currentState.getBlock() instanceof AbstractRailBlock){
                        RailShape railShape = ((AbstractRailBlock)currentState.getBlock()).getRailDirection(currentState, this.world, mutable, null);
                        boolean invalidSpot = AbstractRailBlockInvoker.wb_callIsValidRailDirection(mutable, this.world, railShape);
                        if(invalidSpot){
                           this.world.removeBlock(mutable, false);
                        }
                        continue;
                     }

                     // Skip air, full solid cubes, and liquid blocks as those typically do not break themselves.
                     if(!currentState.isAir() &&
                       !(currentState.getBlock() instanceof FlowingFluidBlock) &&
                       !(currentState.getMaterial().isOpaque() && currentState.isOpaqueCube(this.world, mutable)))
                     {
                        BlockState newState = Block.getValidBlockForPosition(currentState, this.world, mutable);
                        if(currentState != newState){
                           // removes all invalid placed blocks like floating grass or rails
                           this.world.setBlockState(mutable, newState, 3);
                        }
                        else{
                           // forces blocks like leaves or twisting vines to self-destruct
                           currentState.tick((ServerWorld) this.world, mutable, this.rand);
                           currentState.randomTick((ServerWorld) this.world, mutable, this.rand);
                        }
                     }
                  }
               }
            }
         }

         // count down
         ticksTillDetonation--;
      }

      // NUKE ALL THE ITEMS NOW
      else{
         Chunk chunk = this.world.getChunk(this.chunkCoordX, this.chunkCoordZ);
         ClassInheritanceMultiMap<Entity>[] entityList = chunk.getEntityLists();

         // Clear the chunk of all ItemEntities
         for (ClassInheritanceMultiMap<Entity> entities : entityList) {
            entities.forEach(entity -> {
               if (entity.getType().equals(EntityType.ITEM)) {
                  entity.remove(); // Will be removed automatically on next world tick
               }
            });
         }

         this.remove(); // remove self as task is done
      }
   }
}