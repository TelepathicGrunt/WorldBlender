package net.telepathicgrunt.worldblender.features;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.telepathicgrunt.worldblender.WorldBlender;


public class DDBasicDungeonFeature extends Feature<NoFeatureConfig>
{

	public DDBasicDungeonFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactory)
	{
		super(configFactory);
	}
	

	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> changedBlock, Random rand, BlockPos position, NoFeatureConfig config)
	{
		if(rand.nextFloat() < 0.5f) 
		{
			return false;
		}
		
		TemplateManager templatemanager = ((ServerWorld) world.getWorld()).getSaveHandler().getStructureTemplateManager();
		Template template = templatemanager.getTemplate(new ResourceLocation("dimdungeons:fourway_1"));

		if (template == null)
		{
			WorldBlender.LOGGER.warn("dimdungeons's fourway_1 NTB does not exist!");
			return false;
		}
		
		//sets the dungeon anywhere between surface and y = 12.
		BlockPos finalPosition = world.getHeight(Heightmap.Type.WORLD_SURFACE_WG, position);
		PlacementSettings placementsettings = (new PlacementSettings()).setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(false).setChunk((ChunkPos) null);
		template.addBlocksToWorld(world, finalPosition.add(-8, -rand.nextInt(finalPosition.getY()-12), -8), placementsettings);

		return true;
	}
}
