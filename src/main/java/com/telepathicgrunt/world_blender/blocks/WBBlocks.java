package com.telepathicgrunt.world_blender.blocks;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;


public class WBBlocks
{
	public static final Block WORLD_BLENDER_PORTAL = new WBPortalBlock();
	public static final BlockEntityType<WBPortalBlockEntity> WORLD_BLENDER_PORTAL_BE = BlockEntityType.Builder.create(WBPortalBlockEntity::new, WORLD_BLENDER_PORTAL).build(null);

	public static void register()
	{
		Registry.register(Registry.BLOCK, new Identifier(WorldBlender.MODID, "world_blender_portal"), WORLD_BLENDER_PORTAL);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(WorldBlender.MODID, "world_blender_portal"), WORLD_BLENDER_PORTAL_BE);
	}

}