package com.telepathicgrunt.worldblender.dimension;

import com.telepathicgrunt.worldblender.WorldBlender;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;


public class WBWorldSavedData extends WorldSavedData
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

		DimensionSavedDataManager storage = ((ServerWorld)world).getSavedData();
		return storage.getOrCreate(WBWorldSavedData::new, ALTAR_DATA);
	}
	
	

	@Override
	public void read(CompoundNBT data)
	{
		wbAltarMade = data.getBoolean("WBAltarMade");
	}

	@Override
	public CompoundNBT write(CompoundNBT data)
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