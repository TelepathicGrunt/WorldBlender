package net.telepathicgrunt.worldblender.dimension;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.telepathicgrunt.worldblender.features.WBFeatures;


public class AltarManager
{

	private boolean altarMade = false;
	private final ServerWorld world;


	public AltarManager(ServerWorld serverWorld)
	{
		this.world = serverWorld;
		this.altarMade = WBWorldSavedData.get(serverWorld).getWBAltarState();
	}


	public boolean getState()
	{
		return this.altarMade;
	}


	public void tick()
	{
		boolean flag = this.isWorldOriginTicking();
		if (!this.altarMade && flag)
		{
			WBFeatures.WB_PORTAL_ALTAR.place(this.world, this.world.getChunkProvider().generator, this.world.rand, new BlockPos(0, 255, 0), IFeatureConfig.NO_FEATURE_CONFIG);
			this.altarMade = true;
			this.saveWBAltarData(this.world);
		}
	}


	private boolean isWorldOriginTicking()
	{
		for (int x = -1; x <= 1; ++x)
		{
			for (int z = -1; z <= 1; ++z)
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
