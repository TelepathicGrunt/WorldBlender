package com.telepathicgrunt.world_blender.dimension;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;


public class WBWorldSavedData extends PersistentState
{
	private static final String ALTAR_DATA = WorldBlender.MODID + "AltarMade";
	private static final WBWorldSavedData CLIENT_DUMMY = new WBWorldSavedData();
	private boolean wbAltarMade;

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

		PersistentStateManager storage = ((ServerWorld)world).getPersistentStateManager();
		return storage.getOrCreate(WBWorldSavedData::new, ALTAR_DATA);
	}
	
	

	@Override
	public void fromTag(CompoundTag data)
	{
		wbAltarMade = data.getBoolean("WBAltarMade");
	}

	@Override
	public CompoundTag toTag(CompoundTag data)
	{
		data.putBoolean("WBAltarMade", wbAltarMade);
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
}