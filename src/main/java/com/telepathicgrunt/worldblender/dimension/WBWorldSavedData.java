package com.telepathicgrunt.worldblender.dimension;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.templates.CompoundList;
import com.telepathicgrunt.worldblender.WorldBlender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class WBWorldSavedData extends WorldSavedData
{
	private static final String ALTAR_DATA = WorldBlender.MODID + "AltarMade";
	private static final WBWorldSavedData CLIENT_DUMMY = new WBWorldSavedData();
	private boolean wbAltarMade;
	private final List<ScheduledTask> SCHEDULED_TASKS = Collections.synchronizedList(new ArrayList<>());

	public WBWorldSavedData()
	{
		super(ALTAR_DATA);
	}

	public static WBWorldSavedData get(World world)
	{
		if (!(world instanceof ServerWorld))
		{
			return CLIENT_DUMMY;
		}

		DimensionSavedDataManager storage = ((ServerWorld)world).getSavedData();
		return storage.getOrCreate(WBWorldSavedData::new, ALTAR_DATA);
	}

	@Override
	public void read(CompoundNBT data)
	{
		wbAltarMade = data.getBoolean("WBAltarMade");
		ListNBT list = data.getList("chunkList", 10);
		for (int index = 0; index < list.size(); index++) {
			CompoundNBT nbt = list.getCompound(index);
			ChunkPos chunkPos = new ChunkPos(nbt.getLong("chunkPos"));
			int tickCountdown = nbt.getInt("tickCountdown");
			SCHEDULED_TASKS.add(new ScheduledTask(chunkPos, tickCountdown));
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT data)
	{
		data.putBoolean("WBAltarMade", wbAltarMade);
		ArrayList<CompoundNBT> listOfScheduledChunks = new ArrayList<>();
		for(ScheduledTask st : SCHEDULED_TASKS){
			CompoundNBT nbt = new CompoundNBT();
			nbt.putLong("chunkPos", st.chunkPos.asLong());
			nbt.putInt("tickCountdown", st.tickCountdown);
			listOfScheduledChunks.add(nbt);
		}
		ListNBT list = new ListNBT();
		list.addAll(listOfScheduledChunks);
		data.put("chunkList", list);
		return data;
	}

	public void setWBAltarState(boolean state) 
	{
		this.wbAltarMade = state;
	}
	
	public boolean getWBAltarState() 
	{
		return this.wbAltarMade;
	}

	public void addTask(ScheduledTask taskToSchedule){

	}


	/**
	 * Does *not* create worlds that don't already exist
	 * So they should be created by the thing that schedules the tick, if possible
	 * @param world The world that is being ticked and contains a data instance
	 */
	public void tick(ServerWorld world) {
		SCHEDULED_TASKS.removeIf(task -> {
			if(world.chunkExists(task.chunkPos.x, task.chunkPos.z)){
				if(task.tickCountdown <= 0){
					Chunk chunk = world.getChunk(task.chunkPos.x, task.chunkPos.z);
					ClassInheritanceMultiMap<Entity>[] entityList = chunk.getEntityLists();

					// Clear the chunk of all ItemEntities
					for (ClassInheritanceMultiMap<Entity> entities : entityList) {
						entities.forEach(entity -> {
							if (entity instanceof ItemEntity) {
								entity.remove(); // Will be removed automatically on next world tick
							}
						});
					}
					return true; // Remove finished task
				}
				else{
					// Decrement countdown as it is not time yet
					task.tickCountdown--;
				}
			}
			return false; // Keep task
		});
	}

	public static void worldTick(TickEvent.WorldTickEvent event){
		if(event.phase == TickEvent.Phase.END && !event.world.isRemote()){
			get(event.world).tick((ServerWorld) event.world);
		}
	}

	public static class ScheduledTask {
		public final ChunkPos chunkPos;
		public int tickCountdown;

		public ScheduledTask(ChunkPos chunkPos, int tickCountdown){
			this.chunkPos = chunkPos;
			this.tickCountdown = tickCountdown;
		}
	}
}