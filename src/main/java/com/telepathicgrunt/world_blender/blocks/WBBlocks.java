package com.telepathicgrunt.world_blender.blocks;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class WBBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WorldBlender.MODID);
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, WorldBlender.MODID);
	
	public static final RegistryObject<Block> WORLD_BLENDER_PORTAL = createBlock("world_blender_portal", () -> new WBPortalBlock());
//	public static final RegistryObject<TileEntityType<WBPortalBlockEntity>> WORLD_BLENDER_PORTAL_BE = createTileEntity("world_blender_portal", () -> TileEntityType.Builder.create(WBPortalBlockEntity::new, WORLD_BLENDER_PORTAL).build(null));
	public static final RegistryObject<TileEntityType<WBPortalBlockEntity>> WORLD_BLENDER_PORTAL_BE = createTileEntity("world_blender_portal", () -> new TileEntityType<>(WBPortalBlockEntity::new, Sets.newHashSet(WORLD_BLENDER_PORTAL.get()), null));

	public static <B extends Block> RegistryObject<B> createBlock(String name, Supplier<? extends B> block)
	{
		return BLOCKS.register(name, block);
	}
	
	public static <T extends TileEntityType<?>> RegistryObject<T> createTileEntity(String name, Supplier<? extends T> tileEntity)
	{
		return TILE_ENTITY_TYPES.register(name, tileEntity);
	}
}