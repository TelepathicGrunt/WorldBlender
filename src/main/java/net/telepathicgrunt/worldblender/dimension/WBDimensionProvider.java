package net.telepathicgrunt.worldblender.dimension;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.telepathicgrunt.worldblender.WorldBlender;
import net.telepathicgrunt.worldblender.biome.biomes.surfacebuilder.BlendedSurfaceBuilder;
import net.telepathicgrunt.worldblender.generation.WBBiomeProvider;


@Mod.EventBusSubscriber(modid = WorldBlender.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WBDimensionProvider extends Dimension
{
	private final float[] colorsSunriseSunset = new float[4];


	public WBDimensionProvider(World world, DimensionType typeIn)
	{
		super(world, typeIn, 15F);

		/**
		 * Creates the light to brightness table. It changes how light levels looks to the players but does not change the
		 * actual values of the light levels.
		 */
		for (int i = 0; i <= 15; ++i)
		{
			this.lightBrightnessTable[i] = (float) i / 15F;
		}

//		CompoundNBT compoundnbt = world.getWorldInfo().getDimensionData(typeIn);
//		this.dragonFightManager = world instanceof ServerWorld && WBConfig.spawnEnderDragon ? new DragonFightManager((ServerWorld) world, compoundnbt.getCompound("DragonFight"), this) : null;
	}


	/**
	 * Use our own biome provider and chunk generator for this dimension
	 */
	@Override
	public ChunkGenerator<?> createChunkGenerator()
	{
		//set seed here as WorldEvent.Load fires at a bad time sometimes. 
		BlendedSurfaceBuilder.setPerlinSeed(world.getSeed());

		return new OverworldChunkGenerator(world, new WBBiomeProvider(world), ChunkGeneratorType.SURFACE.createSettings());
	}


	@Override
	public SleepResult canSleepAt(net.minecraft.entity.player.PlayerEntity player, BlockPos pos)
	{
		return SleepResult.DENY; //NO EXPLODING BEDS! But no sleeping too.
	}


	@Override
	public boolean canRespawnHere()
	{
		return true;
	}


	@Override
	public BlockPos getSpawnCoordinate()
	{
		return null;
	}


	@Override
	public BlockPos findSpawn(ChunkPos chunkPosIn, boolean checkValid)
	{
		return null;
	}


	@Override
	public BlockPos findSpawn(int posX, int posZ, boolean checkValid)
	{
		return null;
	}


	@Override
	public boolean shouldMapSpin(String entity, double x, double z, double rotation)
	{
		return true; //SPINNY MAPS!
	}


	@Override
	public boolean isNether()
	{
		return false;
	}


	@Override
	public boolean isSurfaceWorld()
	{
		return true;
	}


	/**
	 * Returns array with sunrise/sunset colors
	 */
	@Nullable
	@OnlyIn(Dist.CLIENT)
	public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks)
	{
		float f1 = MathHelper.cos(celestialAngle * ((float) Math.PI * 2F)) - 0.0F;
		if (f1 >= -0.4F && f1 <= 0.4F)
		{
			float f3 = (f1 - -0.0F) / 0.4F * 0.5F + 0.5F;
			float f4 = 1.0F - (1.0F - MathHelper.sin(f3 * (float) Math.PI)) * 0.99F;
			f4 = f4 * f4;
			this.colorsSunriseSunset[0] = f3 * 0.3F + 0.7F;
			this.colorsSunriseSunset[1] = f3 * f3 * 0.7F + 0.2F;
			this.colorsSunriseSunset[2] = f3 * f3 * 0.0F + 0.2F;
			this.colorsSunriseSunset[3] = f4;
			return this.colorsSunriseSunset;
		}
		else
		{
			return null;
		}
	}


	@OnlyIn(Dist.CLIENT)
	public boolean isSkyColored()
	{
		return true;
	}


	/**
	 * the y level at which clouds are rendered.
	 */
	@OnlyIn(Dist.CLIENT)
	public float getCloudHeight()
	{
		return 155;
	}


	/**
	 * Returns fog color
	 * 
	 * What I done is made it be based on the day/night cycle so the fog will darken at night but brighten during day.
	 * calculateVanillaSkyPositioning returns a value which is between 0 and 1 for day/night and fogChangeSpeed is the range
	 * that the fog color will cycle between.
	 */
	@Override
	public Vec3d getFogColor(float celestialAngle, float partialTicks)
	{
		return new Vec3d(0.0D, 0.8D, 0.9D);
	}


	/**
	 * Returns a double value representing the Y value relative to the top of the map at which void fog is at its maximum.
	 */
	@OnlyIn(Dist.CLIENT)
	@Override
	public double getVoidFogYFactor()
	{
		return 0.011D;
	}


	/**
	 * Show fog at all?
	 */
	@Override
	public boolean doesXZShowFog(int x, int z)
	{
		return false;
	}


	/**
	 * mimics vanilla Overworld sky timer
	 * 
	 * Returns a value between 0 and 1. .25 is dusk and .75 is dawn 0 is noon. 0.5 is midnight
	 */
	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks)
	{
		double fractionComponent = MathHelper.frac((double) worldTime / 24000.0D - 0.25D);
		double d1 = 0.5D - Math.cos(fractionComponent * Math.PI) / 2.0D;
		return (float) (fractionComponent * 2.0D + d1) / 3.0F;
	}

}
