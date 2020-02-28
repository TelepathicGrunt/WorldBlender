package net.telepathicgrunt.worldblender.worldtype;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.telepathicgrunt.worldblender.biome.biomes.surfacebuilder.BlendedSurfaceBuilder;
import net.telepathicgrunt.worldblender.generation.WBBiomeProvider;


public class WBWorldType extends WorldType
{
	//displays our mod as a world type

	public WBWorldType()
	{
		/*
		 * Name of world type. Also had to add this to en_us.json file to display name and info properly:
		 * 
		 * "generator.world_blender":"World Blender"
		 * "generator.world_blender.info":"Blend your world together like a smoothie!"
		 */
		super("world_blender");
		this.enableInfoNotice();
	}


	@Override
	public ChunkGenerator<?> createChunkGenerator(World world)
	{
		if (world.dimension.getType() == DimensionType.OVERWORLD)
		{
			//set seed here as WorldEvent.Load fires too late
			BlendedSurfaceBuilder.setPerlinSeed(world.getSeed());
			
			//tells Minecraft to use this mod's ChunkGeneratorOverworld when running this world type in Overworld.
			return new OverworldChunkGenerator(world, new WBBiomeProvider(world), ChunkGeneratorType.SURFACE.createSettings());
		}

		// Run default chunkgenerator for each dimension
		return super.createChunkGenerator(world);
	}


	@Override
	public boolean hasCustomOptions()
	{
		//Not customizable since we use a config file instead to customize.
		return false;
	}
}
