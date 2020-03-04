package net.telepathicgrunt.worldblender.dimension;

import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.telepathicgrunt.worldblender.WorldBlender;


public class WBWorldSavedData extends WorldSavedData
{
	private static final String ALTAR_DATA = WorldBlender.MODID + "AltarMade";
	private static final WBWorldSavedData CLIENT_DUMMY = new WBWorldSavedData();
	private boolean wbAltarMade;
	private boolean dragonDataSaved;
	private boolean dragonKilled;
	private boolean dragonPreviouslyKilled;
	private boolean dragonIsRespawning;
	private boolean scanForLegacyFight;
	private BlockPos endAltarPosition;
	private UUID dragonUUID;

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
		wbAltarMade = data.getBoolean("wbAltarMade");
		dragonDataSaved = data.getBoolean("dragonDataSaved");
		dragonKilled = data.getBoolean("dragonKilled");
		dragonPreviouslyKilled = data.getBoolean("dragonPreviouslyKilled");
		dragonIsRespawning = data.getBoolean("dragonIsRespawning");
		scanForLegacyFight = data.getBoolean("scanForLegacyFight");
		endAltarPosition = new BlockPos(data.getInt("endAltarPositionX"), data.getInt("endAltarPositionY"), data.getInt("endAltarPositionZ"));
		dragonUUID = data.getUniqueId("dragonUUID");
	}

	@Override
	public CompoundNBT write(CompoundNBT data)
	{
		data.putBoolean("wbAltarMade", wbAltarMade);
		data.putBoolean("dragonDataSaved", dragonDataSaved);
		data.putBoolean("dragonKilled", dragonKilled);
		data.putBoolean("dragonPreviouslyKilled", dragonPreviouslyKilled);
		data.putBoolean("dragonIsRespawning", dragonIsRespawning);
		data.putBoolean("scanForLegacyFight", scanForLegacyFight);
		data.putInt("endAltarPositionX", endAltarPosition.getX());
		data.putInt("endAltarPositionY", endAltarPosition.getY());
		data.putInt("endAltarPositionZ", endAltarPosition.getZ());
		data.putUniqueId("dragonUUID", dragonUUID);
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
	
	public boolean isDragonKilled()
	{
		return this.dragonKilled;
	}

	public void setDragonKilled(boolean dragonKilled)
	{
		this.dragonKilled = dragonKilled;
	}

	public UUID getDragonUUID()
	{
		return this.dragonUUID;
	}

	public void setDragonUUID(UUID dragonUUID)
	{
		this.dragonUUID = dragonUUID;
	}

	public boolean isDragonDataSaved()
	{
		return this.dragonDataSaved;
	}

	public void setDragonDataSaved(boolean dragonDataSaved)
	{
		this.dragonDataSaved = dragonDataSaved;
	}

	public boolean isDragonPreviouslyKilled()
	{
		return this.dragonPreviouslyKilled;
	}

	public void setDragonPreviouslyKilled(boolean dragonPreviouslyKilled)
	{
		this.dragonPreviouslyKilled = dragonPreviouslyKilled;
	}

	public boolean isDragonRespawning()
	{
		return this.dragonIsRespawning;
	}

	public void setDragonRespawning(boolean dragonIsRespawning)
	{
		this.dragonIsRespawning = dragonIsRespawning;
	}

	public BlockPos getEndAltarPosition()
	{
		return this.endAltarPosition;
	}

	public void setEndAltarPosition(BlockPos endAltarPosition)
	{
		this.endAltarPosition = endAltarPosition;
	}

	public boolean isScanForLegacyFight()
	{
		return scanForLegacyFight;
	}

	public void setScanForLegacyFight(boolean scanForLegacyFight)
	{
		this.scanForLegacyFight = scanForLegacyFight;
	}

}