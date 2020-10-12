package com.telepathicgrunt.world_blender.blocks;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.block.Block;
import net.minecraft.block.entity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;


public class WBBlocks
{
	public static final Block WORLD_BLENDER_PORTAL = new WBPortalBlock();
	public static final TileEntityType<WBPortalBlockEntity> WORLD_BLENDER_PORTAL_BE = TileEntityType.Builder.create(WBPortalBlockEntity::new, WORLD_BLENDER_PORTAL).build(null);

	public static void registerBlocks(final RegistryEvent.Register<Block> event){
		WorldBlender.register(event.getRegistry(), WORLD_BLENDER_PORTAL, "world_blender_portal");
	}

	public static void registerBlockEntities(final RegistryEvent.Register<TileEntityType<?>>event)
	{
		WorldBlender.register(event.getRegistry(), WORLD_BLENDER_PORTAL_BE, "world_blender_portal");
	}

}