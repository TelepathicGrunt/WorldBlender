package net.telepathicgrunt.worldblender.blocks;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.telepathicgrunt.worldblender.WorldBlender;


public class WBBlocks
{
	public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, WorldBlender.MODID);
	public static final RegistryObject<Block> WORLD_BLENDER_PORTAL = BLOCKS.register("world_blender_portal", WBPortalBlock::new);
	
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, WorldBlender.MODID);
	public static final RegistryObject<TileEntityType<WBPortalTileEntity>> WORLD_BLENDER_PORTAL_TILE = TILE_ENTITIES.register("world_blender_portal", () -> TileEntityType.Builder.create(WBPortalTileEntity::new, WORLD_BLENDER_PORTAL.get()).build(null));

	/**
	 * Adds the stuff to the bus so they fire when the registration begins for Forge
	 * 
	 * @param event - registry to add blocks to
	 */
	public static void registerAll(IEventBus modEventBus)
	{
		BLOCKS.register(modEventBus);
		TILE_ENTITIES.register(modEventBus);
	}

}