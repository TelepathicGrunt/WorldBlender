package net.telepathicgrunt.worldblender.dimension;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.EndPortalTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Unit;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;


public class WBDragonManager
{

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Predicate<Entity> VALID_PLAYER = EntityPredicates.IS_ALIVE.and(EntityPredicates.withinRange(0.0D, 128.0D, 0.0D, 192.0D));
	private final ServerBossInfo bossInfo = (ServerBossInfo) (new ServerBossInfo(new TranslationTextComponent("entity.minecraft.ender_dragon"), BossInfo.Color.PINK, BossInfo.Overlay.PROGRESS)).setPlayEndBossMusic(true).setCreateFog(true);
	private final ServerWorld world;
	private final BlockPattern portalPattern;
	private int ticksSinceDragonSeen;
	private int aliveCrystals;
	private int ticksSinceCrystalsScanned;
	private int ticksSinceLastPlayerScan;
	private boolean dragonKilled;
	private boolean previouslyKilled;
	private UUID dragonUniqueId;
	private BlockPos exitPortalLocation;
	private WBDragonSpawnState respawnState;
	private boolean scanForLegacyFight = true;
	private int respawnStateTicks;
	private EnderDragonEntity enderDragon;
	private List<EnderCrystalEntity> crystals;
	private boolean noCrystalAlive = false;


	public WBDragonManager(ServerWorld serverWorld)
	{
		this.world = serverWorld;
		WBWorldSavedData savedData = WBWorldSavedData.get(serverWorld);

		if (savedData.isDragonDataSaved())
		{
			this.dragonUniqueId = savedData.getDragonUUID();
			this.dragonKilled = savedData.isDragonKilled();
			
			this.previouslyKilled = savedData.isDragonPreviouslyKilled();
			this.scanForLegacyFight = savedData.isScanForLegacyFight();
			if (savedData.isDragonRespawning())
			{
				this.respawnState = WBDragonSpawnState.START;
			}
			
			this.exitPortalLocation = savedData.getEndAltarPosition();
		}
		else
		{
			this.dragonKilled = true;
			this.previouslyKilled = true;
		}

		this.portalPattern = BlockPatternBuilder.start()
				.aisle( "       ", 
						"       ", 
						"       ", 
						"   #   ", 
						"       ", 
						"       ", 
						"       ")
				
				.aisle( "       ", 
						"       ", 
						"       ", 
						"   #   ", 
						"       ", 
						"       ", 
						"       ")
				
				.aisle( "       ", 
						"       ", 
						"       ", 
						"   #   ", 
						"       ", 
						"       ", 
						"       ")
				
				.aisle( "  ###  ", 
						" #   # ", 
						"#     #", 
						"#  #  #", 
						"#     #", 
						" #   # ", 
						"  ###  ")
				
				.aisle( "       ", 
						"  ###  ", 
						" ##### ", 
						" ##### ", 
						" ##### ", 
						"  ###  ", 
						"       ")
				.where('#', CachedBlockInfo.hasState(BlockMatcher.forBlock(Blocks.BEDROCK))).build();

	}


	public void saveWBDragonData(World world) 
	{
		WBWorldSavedData.get(world).setDragonKilled(this.dragonKilled);
		WBWorldSavedData.get(world).setDragonPreviouslyKilled(this.previouslyKilled);
		WBWorldSavedData.get(world).setDragonRespawning(this.respawnState != null);
		WBWorldSavedData.get(world).setScanForLegacyFight(this.scanForLegacyFight);
		WBWorldSavedData.get(world).setDragonUUID(this.dragonUniqueId);
		WBWorldSavedData.get(world).setEndAltarPosition(this.exitPortalLocation);
		WBWorldSavedData.get(world).setDragonDataSaved(true);
		WBWorldSavedData.get(world).markDirty();
	}


	@SuppressWarnings("deprecation")
	public void tick()
	{
		this.bossInfo.setVisible(!this.dragonKilled);
		if (++this.ticksSinceLastPlayerScan >= 20)
		{
			this.updatePlayers();
			this.ticksSinceLastPlayerScan = 0;
		}

		if (!this.bossInfo.getPlayers().isEmpty())
		{
			this.world.getChunkProvider().registerTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
			boolean flag = this.isWorldOriginTicking();
			if(flag)
			{
				//make sure the ID matches a real entity and grabs that dragon.
				if(!this.dragonKilled && this.dragonUniqueId != null) 
				{
					this.enderDragon = (EnderDragonEntity) this.world.getEntityByUuid(this.dragonUniqueId);
					if(this.enderDragon == null)
					{
						this.dragonUniqueId = null;
						this.dragonKilled = true;
					}
				}
				
				
				if (this.scanForLegacyFight)
				{
					this.generatePortal();
					this.scanForLegacyFight = false;
				}
				
	
				if (this.respawnState != null)
				{
					if (this.crystals == null)
					{
						this.respawnState = null;
						this.tryRespawnDragon();
					}

					if (this.crystals != null)
					{
						this.respawnState.process(this.world, this, this.crystals, this.respawnStateTicks++, this.exitPortalLocation);
					}
				}
				else 
				{
					this.tryRespawnDragon();
				}

				if (!this.dragonKilled && this.enderDragon != null && !this.enderDragon.removed)
				{
					if (++this.ticksSinceDragonSeen >= 1200)
					{
						this.findOrCreateDragon();
						this.ticksSinceDragonSeen = 0;
					}
					else if(this.enderDragon != null)
					{
						dragonUpdate(this.enderDragon);
					}

					if (++this.ticksSinceCrystalsScanned >= 100 && !this.noCrystalAlive)
					{
						this.findAliveCrystals();
						this.ticksSinceCrystalsScanned = 0;
					}
					
					if(this.enderDragon.getHealth() <= 0)
					{
						this.enderDragon.dropExperience(250);
					}
				}
				else
				{
					if(this.enderDragon != null && this.enderDragon.removed) 
					{
						processDragonDeath(this.enderDragon);
					}
				}
			}
		}
		else
		{
			this.world.getChunkProvider().releaseTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
		}

	}


	private void generatePortal()
	{
		LOGGER.info("Scanning for legacy world dragon fight...");
		boolean flag = this.worldContainsEndPortal();
		if (flag)
		{
			LOGGER.info("Found that the dragon has been killed in this world already.");
			this.previouslyKilled = true;
		}
		else
		{
			LOGGER.info("Found that the dragon has not yet been killed in this world.");
			this.previouslyKilled = false;
			if (this.findExitPortal() == null)
			{
				this.generatePortal(false);
			}
		}

		List<EnderDragonEntity> list = this.world.getDragons();
		if (list.isEmpty())
		{
			this.dragonKilled = true;
		}
		else
		{
			EnderDragonEntity enderdragonentity = list.get(0);
			this.dragonUniqueId = enderdragonentity.getUniqueID();
			this.enderDragon = enderdragonentity;
			LOGGER.info("Found that there's a dragon still alive ({})", (Object) enderdragonentity);
			this.dragonKilled = false;
			if (!flag)
			{
				LOGGER.info("But we didn't have a portal, let's remove it.");
				enderdragonentity.remove();
				this.dragonUniqueId = null;
				this.enderDragon = null;
			}
		}

		if (!this.previouslyKilled && this.dragonKilled)
		{
			this.dragonKilled = false;
		}

	}


	private void findOrCreateDragon()
	{
		List<EnderDragonEntity> list = this.world.getDragons();
		if (list.isEmpty())
		{
			LOGGER.debug("Haven't seen the dragon, respawning it");
			this.createNewDragon();
		}
		else
		{
			LOGGER.debug("Haven't seen our dragon, but found another one to use.");
			this.dragonUniqueId = list.get(0).getUniqueID();
			this.enderDragon = list.get(0);
		}

	}


	protected void setRespawnState(WBDragonSpawnState preparingToSummonPillars)
	{
		if (this.respawnState == null)
		{
			throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");
		}
		else
		{
			this.respawnStateTicks = 0;
			if (preparingToSummonPillars == WBDragonSpawnState.END)
			{
				this.respawnState = null;
				this.dragonKilled = false;
				EnderDragonEntity enderdragonentity = this.createNewDragon();

				for (ServerPlayerEntity serverplayerentity : this.bossInfo.getPlayers())
				{
					CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayerentity, enderdragonentity);
				}
			}
			else if(doesRespawnCrystalExist() != null)
			{
				this.respawnState = preparingToSummonPillars;
			}

		}
	}


	private boolean worldContainsEndPortal()
	{
		for (int x = -1; x <= 1; ++x)
		{
			for (int z = -1; z <= 1; ++z)
			{
				Chunk chunk = this.world.getChunk(x, z);

				for (TileEntity tileentity : chunk.getTileEntityMap().values())
				{
					if (tileentity instanceof EndPortalTileEntity)
					{
						return true;
					}
				}
			}
		}

		return false;
	}


	@Nullable
	private BlockPattern.PatternHelper findExitPortal()
	{
		for (int x = -1; x <= 1; ++x)
		{
			for (int z = -1; z <= 1; ++z)
			{
				Chunk chunk = this.world.getChunk(x, z);

				for (TileEntity tileentity : chunk.getTileEntityMap().values())
				{
					if (tileentity instanceof EndPortalTileEntity)
					{
						BlockPattern.PatternHelper blockpattern$patternhelper = this.portalPattern.match(this.world, tileentity.getPos());
						if (blockpattern$patternhelper != null)
						{
							BlockPos blockpos = blockpattern$patternhelper.translateOffset(3, 3, 3).getPos();
							if (this.exitPortalLocation == null && blockpos.getX() == 0 && blockpos.getZ() == 0)
							{
								this.exitPortalLocation = blockpos;
							}

							return blockpattern$patternhelper;
						}
					}
				}
			}
		}

		int maxHeight = this.world.getHeight(Heightmap.Type.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION).getY() + 1;

		for (int currentHeight = maxHeight; currentHeight >= 0; --currentHeight)
		{
			BlockPattern.PatternHelper blockpattern$patternhelper1 = this.portalPattern.match(this.world, new BlockPos(EndPodiumFeature.END_PODIUM_LOCATION.getX(), currentHeight, EndPodiumFeature.END_PODIUM_LOCATION.getZ()));
			if (blockpattern$patternhelper1 != null)
			{
				if (this.exitPortalLocation == null)
				{
					this.exitPortalLocation = blockpattern$patternhelper1.translateOffset(3, 3, 3).getPos();
				}

				return blockpattern$patternhelper1;
			}
		}

		return null;
	}


	private boolean isWorldOriginTicking()
	{
		for (int x = -4; x <= 4; ++x)
		{
			for (int z = -4; z <= 4; ++z)
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



	private void updatePlayers()
	{
		Set<ServerPlayerEntity> set = Sets.newHashSet();

		for (ServerPlayerEntity serverplayerentity : this.world.getPlayers(VALID_PLAYER))
		{
			this.bossInfo.addPlayer(serverplayerentity);
			set.add(serverplayerentity);
		}

		Set<ServerPlayerEntity> set1 = Sets.newHashSet(this.bossInfo.getPlayers());
		set1.removeAll(set);

		for (ServerPlayerEntity serverplayerentity1 : set1)
		{
			this.bossInfo.removePlayer(serverplayerentity1);
		}

	}


	private void findAliveCrystals()
	{
		this.ticksSinceCrystalsScanned = 0;
		this.aliveCrystals = 0;

		for (EndSpikeFeature.EndSpike endspikefeature$endspike : EndSpikeFeature.generateSpikes(this.world))
		{
			this.aliveCrystals += this.world.getEntitiesWithinAABB(EnderCrystalEntity.class, endspikefeature$endspike.getTopBoundingBox()).size();
		}

		if(this.aliveCrystals == 0) this.noCrystalAlive = true;
		
		LOGGER.debug("Found {} end crystals still alive", (int) this.aliveCrystals);
	}


	public void processDragonDeath(EnderDragonEntity dragonEntity)
	{
		if (dragonEntity == null || dragonEntity.getUniqueID().equals(this.dragonUniqueId))
		{
			this.bossInfo.setPercent(0.0F);
			this.bossInfo.setVisible(false);
			this.generatePortal(true);
			if (!this.previouslyKilled)
			{
				this.world.setBlockState(this.world.getHeight(Heightmap.Type.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION), Blocks.DRAGON_EGG.getDefaultState());
			}

			this.previouslyKilled = true;
			this.dragonKilled = true;
			this.enderDragon = null;
		}

	}

	private void generatePortal(boolean p_186094_1_)
	{
		EndPodiumFeature endpodiumfeature = new EndPodiumFeature(p_186094_1_);
		if (this.exitPortalLocation == null)
		{
			for (this.exitPortalLocation = this.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION); 
					this.world.getBlockState(this.exitPortalLocation).getBlock() == Blocks.BEDROCK && this.exitPortalLocation.getY() > this.world.getSeaLevel(); 
					this.exitPortalLocation = this.exitPortalLocation.down())
			{
				;
			}
		}
		endpodiumfeature.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).place(this.world, this.world.getChunkProvider().getChunkGenerator(), new Random(), this.exitPortalLocation);
	}


	private EnderDragonEntity createNewDragon()
	{
		this.world.getChunkAt(new BlockPos(0, 128, 0));
		EnderDragonEntity enderdragonentity = EntityType.ENDER_DRAGON.create(this.world);
		enderdragonentity.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
		enderdragonentity.setLocationAndAngles(0.0D, 128.0D, 0.0D, this.world.rand.nextFloat() * 360.0F, 0.0F);
		this.world.addEntity(enderdragonentity);
		this.dragonUniqueId = enderdragonentity.getUniqueID();
		this.enderDragon = enderdragonentity;
		return enderdragonentity;
	}


	public void dragonUpdate(EnderDragonEntity dragon)
	{
		if (dragon.getUniqueID().equals(this.dragonUniqueId))
		{
			this.bossInfo.setPercent(dragon.getHealth() / dragon.getMaxHealth());
			this.ticksSinceDragonSeen = 0;
			if (dragon.hasCustomName())
			{
				this.bossInfo.setName(dragon.getDisplayName());
			}
		}

	}

	public void tryRespawnDragon()
	{
		if (this.dragonKilled && this.respawnState == null)
		{
			BlockPos blockpos = this.exitPortalLocation;
			if (blockpos == null)
			{
				LOGGER.debug("Tried to respawn, but need to find the portal first.");
				BlockPattern.PatternHelper blockpattern$patternhelper = this.findExitPortal();
				if (blockpattern$patternhelper == null)
				{
					LOGGER.debug("Couldn't find a portal, so we made one.");
					this.generatePortal(true);
				}
				else
				{
					LOGGER.debug("Found the exit portal & temporarily using it.");
				}

				blockpos = this.exitPortalLocation;
			}

			List<EnderCrystalEntity> list1 = doesRespawnCrystalExist();
			if(list1 == null) return;
			
			LOGGER.debug("Found all crystals, respawning dragon.");
			this.respawnDragon(list1);
		}

	}
	
	private List<EnderCrystalEntity> doesRespawnCrystalExist() {
		BlockPos blockpos1 = this.exitPortalLocation.up(1);
		List<EnderCrystalEntity> list1 = Lists.newArrayList();

		for (Direction direction : Direction.Plane.HORIZONTAL)
		{
			List<EnderCrystalEntity> list = this.world.getEntitiesWithinAABB(EnderCrystalEntity.class, new AxisAlignedBB(blockpos1.offset(direction, 2)));
			if (list.isEmpty())
			{
				return null;
			}

			list1.addAll(list);
		}
		return list1;
	}


	private void respawnDragon(List<EnderCrystalEntity> p_186093_1_)
	{
		if (this.dragonKilled && this.respawnState == null)
		{
			for (BlockPattern.PatternHelper blockpattern$patternhelper = this.findExitPortal(); blockpattern$patternhelper != null; blockpattern$patternhelper = this.findExitPortal())
			{
				for (int i = 0; i < this.portalPattern.getPalmLength(); ++i)
				{
					for (int j = 0; j < this.portalPattern.getThumbLength(); ++j)
					{
						for (int k = 0; k < this.portalPattern.getFingerLength(); ++k)
						{
							CachedBlockInfo cachedblockinfo = blockpattern$patternhelper.translateOffset(i, j, k);
							if (cachedblockinfo.getBlockState().getBlock() == Blocks.BEDROCK || cachedblockinfo.getBlockState().getBlock() == Blocks.END_PORTAL)
							{
								this.world.setBlockState(cachedblockinfo.getPos(), Blocks.END_STONE.getDefaultState());
							}
						}
					}
				}
			}

			this.respawnState = WBDragonSpawnState.START;
			this.respawnStateTicks = 0;
			this.generatePortal(false);
			this.crystals = p_186093_1_;
		}

	}


	public void resetSpikeCrystals()
	{
		for (EndSpikeFeature.EndSpike endspikefeature$endspike : EndSpikeFeature.generateSpikes(this.world))
		{
			for (EnderCrystalEntity endercrystalentity : this.world.getEntitiesWithinAABB(EnderCrystalEntity.class, endspikefeature$endspike.getTopBoundingBox()))
			{
				endercrystalentity.setInvulnerable(false);
				endercrystalentity.setBeamTarget((BlockPos) null);
			}
		}
	}

}
