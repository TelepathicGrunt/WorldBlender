package net.telepathicgrunt.worldblender.dimension;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.telepathicgrunt.worldblender.WorldBlender;


public class WBWorldSavedData extends WorldSavedData
{
	private static final String ALTAR_DATA = WorldBlender.MODID + "AltarMade";
	private static final WBWorldSavedData CLIENT_DUMMY = new WBWorldSavedData();
	private static boolean altarMade;
	private static boolean dragonKilled;

	public WBWorldSavedData()
	{
		super(ALTAR_DATA);
	}

	public WBWorldSavedData(String s)
	{
		super(s);
	}


	public static WBWorldSavedData get(World world)
	{
		if (!(world instanceof ServerWorld))
		{
			return CLIENT_DUMMY;
		}
		
		ServerWorld wbWorld = ((ServerWorld)world).getServer().getWorld(WBDimension.worldblender());
		DimensionSavedDataManager storage = wbWorld.getSavedData();
		return storage.getOrCreate(WBWorldSavedData::new, ALTAR_DATA);
	}
	
	

	@Override
	public void read(CompoundNBT data)
	{
		altarMade = data.getBoolean("altarMade");
	}

	@Override
	public CompoundNBT write(CompoundNBT data)
	{
		data.putBoolean("altarMade", altarMade);
		return data;
	}

	public void setAltarState(boolean state) 
	{
		altarMade = state;
	}
	
	public boolean getAltarState() 
	{
		return WBWorldSavedData.altarMade;
	}
	
	public static boolean isDragonKilled()
	{
		return dragonKilled;
	}

	public static void setDragonKilled(boolean dragonKilled)
	{
		WBWorldSavedData.dragonKilled = dragonKilled;
	}

}