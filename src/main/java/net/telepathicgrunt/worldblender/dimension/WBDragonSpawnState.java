package net.telepathicgrunt.worldblender.dimension;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ServerWorld;


public enum WBDragonSpawnState
{
	START
	{
		public void process(ServerWorld p_186079_1_, WBDragonManager p_186079_2_, List<EnderCrystalEntity> p_186079_3_, int p_186079_4_, BlockPos p_186079_5_)
		{
			BlockPos blockpos = new BlockPos(0, 128, 0);

			for (EnderCrystalEntity endercrystalentity : p_186079_3_)
			{
				endercrystalentity.setBeamTarget(blockpos);
			}

			p_186079_2_.setRespawnState(PREPARING_TO_SUMMON_PILLARS);
		}
	},
	PREPARING_TO_SUMMON_PILLARS
	{
		public void process(ServerWorld p_186079_1_, WBDragonManager p_186079_2_, List<EnderCrystalEntity> p_186079_3_, int p_186079_4_, BlockPos p_186079_5_)
		{
			if (p_186079_4_ < 100)
			{
				if (p_186079_4_ == 0 || p_186079_4_ == 50 || p_186079_4_ == 51 || p_186079_4_ == 52 || p_186079_4_ >= 95)
				{
					p_186079_1_.playEvent(3001, new BlockPos(0, 128, 0), 0);
				}
			}
			else
			{
				p_186079_2_.setRespawnState(SUMMONING_PILLARS);
			}

		}
	},
	SUMMONING_PILLARS
	{
		public void process(ServerWorld p_186079_1_, WBDragonManager p_186079_2_, List<EnderCrystalEntity> p_186079_3_, int p_186079_4_, BlockPos p_186079_5_)
		{
			boolean flag = p_186079_4_ % 40 == 0;
			boolean flag1 = p_186079_4_ % 40 == 39;
			if (flag || flag1)
			{
				List<EndSpikeFeature.EndSpike> list = EndSpikeFeature.generateSpikes(p_186079_1_);
				int j = p_186079_4_ / 40;
				if (j < list.size())
				{
					EndSpikeFeature.EndSpike endspikefeature$endspike = list.get(j);
					if (flag)
					{
						for (EnderCrystalEntity endercrystalentity : p_186079_3_)
						{
							endercrystalentity.setBeamTarget(new BlockPos(endspikefeature$endspike.getCenterX(), endspikefeature$endspike.getHeight() + 1, endspikefeature$endspike.getCenterZ()));
						}
					}
					else
					{
						for (BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(endspikefeature$endspike.getCenterX() - 10, endspikefeature$endspike.getHeight() - 10, endspikefeature$endspike.getCenterZ() - 10), new BlockPos(endspikefeature$endspike.getCenterX() + 10, endspikefeature$endspike.getHeight() + 10, endspikefeature$endspike.getCenterZ() + 10)))
						{
							p_186079_1_.removeBlock(blockpos, false);
						}

						p_186079_1_.createExplosion((Entity) null, (double) ((float) endspikefeature$endspike.getCenterX() + 0.5F), (double) endspikefeature$endspike.getHeight(), (double) ((float) endspikefeature$endspike.getCenterZ() + 0.5F), 5.0F, Explosion.Mode.DESTROY);
						EndSpikeFeatureConfig endspikefeatureconfig = new EndSpikeFeatureConfig(true, ImmutableList.of(endspikefeature$endspike), new BlockPos(0, 128, 0));
						Feature.END_SPIKE.configure(endspikefeatureconfig).place(p_186079_1_, p_186079_1_.getChunkProvider().getChunkGenerator(), new Random(), new BlockPos(endspikefeature$endspike.getCenterX(), 45, endspikefeature$endspike.getCenterZ()));
					}
				}
				else if (flag)
				{
					p_186079_2_.setRespawnState(SUMMONING_DRAGON);
				}
			}

		}
	},
	SUMMONING_DRAGON
	{
		public void process(ServerWorld p_186079_1_, WBDragonManager p_186079_2_, List<EnderCrystalEntity> p_186079_3_, int p_186079_4_, BlockPos p_186079_5_)
		{
			if (p_186079_4_ >= 100)
			{
				p_186079_2_.setRespawnState(END);
				p_186079_2_.resetSpikeCrystals();

				for (EnderCrystalEntity endercrystalentity : p_186079_3_)
				{
					endercrystalentity.setBeamTarget((BlockPos) null);
					p_186079_1_.createExplosion(endercrystalentity, endercrystalentity.getX(), endercrystalentity.getY(), endercrystalentity.getZ(), 6.0F, Explosion.Mode.NONE);
					endercrystalentity.remove();
				}
			}
			else if (p_186079_4_ >= 80)
			{
				p_186079_1_.playEvent(3001, new BlockPos(0, 128, 0), 0);
			}
			else if (p_186079_4_ == 0)
			{
				for (EnderCrystalEntity endercrystalentity1 : p_186079_3_)
				{
					endercrystalentity1.setBeamTarget(new BlockPos(0, 128, 0));
				}
			}
			else if (p_186079_4_ < 5)
			{
				p_186079_1_.playEvent(3001, new BlockPos(0, 128, 0), 0);
			}

		}
	},
	END
	{
		public void process(ServerWorld p_186079_1_, WBDragonManager p_186079_2_, List<EnderCrystalEntity> p_186079_3_, int p_186079_4_, BlockPos p_186079_5_)
		{
		}
	};

	private WBDragonSpawnState() 
	{
	}

	public abstract void process(ServerWorld p_186079_1_, WBDragonManager p_186079_2_, List<EnderCrystalEntity> p_186079_3_, int p_186079_4_, BlockPos p_186079_5_);
}