package com.telepathicgrunt.worldblender.features;

import com.telepathicgrunt.worldblender.WBIdentifiers;
import com.telepathicgrunt.worldblender.WorldBlender;
import com.telepathicgrunt.worldblender.blocks.WBBlocks;
import com.telepathicgrunt.worldblender.blocks.WBPortalBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

import java.util.Random;


public class WBPortalAltar extends Feature<NoFeatureConfig>
{
	public static Template ALTAR_TEMPLATE;
	private static final PlacementSettings placementSettings = (new PlacementSettings())
																			.setMirror(Mirror.NONE)
																			.setRotation(Rotation.NONE)
																			.setIgnoreEntities(false);
	
	public WBPortalAltar()
	{
		super(NoFeatureConfig.field_236558_a_);
	}
	

	@Override
	public boolean generate(ISeedReader world, ChunkGenerator chunkgenerator, Random rand, BlockPos position, NoFeatureConfig config)
	{
		//only world origin chunk allows generation
		if (world.getWorld().getDimensionKey().equals(WBIdentifiers.WB_WORLD_KEY) &&
				position.getX() >> 4 == 0 &&
				position.getZ() >> 4 == 0)
		{

			if (ALTAR_TEMPLATE == null) {
				WBPortalAltar.ALTAR_TEMPLATE = world.getWorld().getStructureTemplateManager().getTemplate(WBIdentifiers.ALTAR_ID);

				if (ALTAR_TEMPLATE == null) {
					WorldBlender.LOGGER.warn("world blender portal altar NTB does not exist!");
					return false;
				}
			}

			BlockPos.Mutable finalPosition = new BlockPos.Mutable().setPos(world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, position));

			//go past trees to world surface
			BlockState blockState = world.getBlockState(finalPosition);
			while(finalPosition.getY() > 12 && (!blockState.isSolid() || blockState.getMaterial() == Material.WOOD)) {
				finalPosition.move(Direction.DOWN);
				blockState = world.getBlockState(finalPosition);
			}

			finalPosition.move(Direction.UP);
			world.setBlockState(finalPosition.down(), Blocks.AIR.getDefaultState(), 3);
			ALTAR_TEMPLATE.func_237152_b_(world, finalPosition.add(-5, -2, -5), placementSettings, rand);
			finalPosition.move(Direction.DOWN);
			world.setBlockState(finalPosition, WBBlocks.WORLD_BLENDER_PORTAL.get().getDefaultState(), 3); //extra check to make sure portal is placed

			//make portal block unremoveable in altar
			TileEntity blockEntity = world.getTileEntity(finalPosition);
			if(blockEntity instanceof WBPortalBlockEntity)
				((WBPortalBlockEntity)blockEntity).makeNotRemoveable();

			return true;
		}

		return false;
	}

}
