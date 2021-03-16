package com.telepathicgrunt.worldblender.blocks;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.telepathicgrunt.worldblender.WorldBlender;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class WBBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WorldBlender.MODID);
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, WorldBlender.MODID);

	public static final RegistryObject<Block> WORLD_BLENDER_PORTAL = BLOCKS.register("world_blender_portal", WBPortalBlock::new);
	public static final RegistryObject<TileEntityType<WBPortalBlockEntity>> WORLD_BLENDER_PORTAL_BE = TILE_ENTITY_TYPES.register("world_blender_portal", () -> new TileEntityType<>(WBPortalBlockEntity::new, Sets.newHashSet(WORLD_BLENDER_PORTAL.get()), null));
}