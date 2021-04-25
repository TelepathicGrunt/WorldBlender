package com.telepathicgrunt.worldblender.dimension;

import com.telepathicgrunt.worldblender.features.WBFeatures;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;


public class AltarManager
{
	private boolean altarMade;
	private final ServerWorld world;

	public AltarManager(ServerWorld serverWorld)
	{
		this.world = serverWorld;
		this.altarMade = WBWorldSavedData.get(serverWorld).getWBAltarState();
	}

	public boolean isAltarMade(){
		return altarMade;
	}

	@SuppressWarnings("resource")
	public void tick()
	{
		if (!this.altarMade)
		{
			boolean flag = this.isWorldOriginTicking();
			if(flag)
			{
				WBFeatures.WB_PORTAL_ALTAR.get().generate(this.world, this.world.getChunkProvider().getChunkGenerator(), this.world.rand, new BlockPos(0, 255, 0), IFeatureConfig.NO_FEATURE_CONFIG);
				this.altarMade = true;
				this.saveWBAltarData(this.world);
			}
		}
	}


	private boolean isWorldOriginTicking()
	{
		for (int x = -1; x <= 0; ++x)
		{
			for (int z = -1; z <= 0; ++z)
			{
				IChunk ichunk = this.world.getChunk(x, z, ChunkStatus.FULL, false);
				if (!(ichunk instanceof Chunk))
				{
					return false;
				}

				ChunkHolder.LocationType chunkholder$locationtype = ((Chunk) ichunk).getLocationType();
				if (!chunkholder$locationtype.isAtLeast(ChunkHolder.LocationType.TICKING))
				{
					return false;
				}
			}
		}

		return true;
	}
	
	public void saveWBAltarData(World world) 
	{
		WBWorldSavedData.get(world).setWBAltarState(this.altarMade);
		WBWorldSavedData.get(world).markDirty();
	}
}
