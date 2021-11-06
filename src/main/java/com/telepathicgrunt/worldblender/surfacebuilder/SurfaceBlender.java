package com.telepathicgrunt.worldblender.surfacebuilder;

import com.telepathicgrunt.worldblender.configs.WBBlendingConfigs;
import com.telepathicgrunt.worldblender.theblender.ConfigBlacklisting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SurfaceBlender {
	final List<ISurfaceBuilderConfig> surfaces = new ArrayList<>();
	final double baseScale;
	
	public SurfaceBlender() {
		// default order of surface builders I want to start with always
		surfaces.add(SurfaceBuilder.NETHERRACK_CONFIG);
		surfaces.add(SurfaceBuilder.END_STONE_CONFIG);
		
		if (WBBlendingConfigs.allowVanillaSurfaces.get() &&
			WBBlendingConfigs.allowVanillaBiomeImport.get()) {
			surfaces.add(SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG);
			surfaces.add(SurfaceBuilder.PODZOL_DIRT_GRAVEL_CONFIG);
			surfaces.add(SurfaceBuilder.RED_SAND_WHITE_TERRACOTTA_GRAVEL_CONFIG);
			surfaces.add(BlendedSurfaceBuilder.SAND_SAND_UNDERWATER_CONFIG);
			surfaces.add(SurfaceBuilder.MYCELIUM_DIRT_GRAVEL_CONFIG);
			surfaces.add(new SurfaceBuilderConfig(Blocks.SNOW_BLOCK.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.GRAVEL.getDefaultState()));
			surfaces.add(SurfaceBuilder.field_237185_P_);
			surfaces.add(SurfaceBuilder.field_237186_Q_);
			surfaces.add(SurfaceBuilder.field_237187_R_);
			surfaces.add(SurfaceBuilder.CORASE_DIRT_DIRT_GRAVEL_CONFIG);
			surfaces.add(SurfaceBuilder.GRAVEL_CONFIG);
		}
		
		// remove the surfaces that we disallow through blacklist but keep nether/end road
		for (int i = surfaces.size() - 1; i > 1; i--) {
			Block topBlock = surfaces.get(i).getTop().getBlock();
			boolean isBlacklisted = ConfigBlacklisting.isResourceLocationBlacklisted(
				ConfigBlacklisting.BlacklistType.SURFACE_BLOCK,
				Registry.BLOCK.getKey(topBlock)
			);
			if (isBlacklisted) {
				surfaces.remove(i);
			}
		}
		
		baseScale = 0.6D / surfaces.size();
	}
	
	public void save() {
		BlendedSurfaceBuilder.blender = this;
	}
	
	/**
	 Adds the surface to allSurfaceList for surface gen later
	 */
	public void addIfMissing(ISurfaceBuilderConfig config) {
		boolean alreadyPresent = surfaces.stream().anyMatch(existing -> areEquivalent(existing, config));
		if (alreadyPresent) return;
		
		surfaces.add(config);
	}
	
	// Returns what vanilla carvers should carve through so they don't get cut off by unique blocks added to surfacebuilder config
	public Set<Block> blocksToCarve() {
		Set<Block> carvableBlocks = new HashSet<>();
		carvableBlocks.add(Blocks.NETHERRACK);
		carvableBlocks.add(Blocks.END_STONE);
		
		// adds underground modded blocks to carve through
		for (ISurfaceBuilderConfig surface : surfaces) {
			BlockState underwaterMaterial = getUnderwaterMaterial(surface);
			if (underwaterMaterial == null) continue;
			Block underwaterBlock = underwaterMaterial.getBlock();
			boolean isVanilla = Registry.BLOCK.getKey(underwaterBlock).getNamespace().equals("minecraft");
			if (isVanilla) continue;
			carvableBlocks.add(underwaterBlock);
		}
		
		return carvableBlocks;
	}
	
	@Nullable
	private static BlockState getUnderwaterMaterial(ISurfaceBuilderConfig surface) {
		if (!(surface instanceof SurfaceBuilderConfig)) return null;
		return ((SurfaceBuilderConfig) surface).getUnderWaterMaterial();
	}
	
	private static boolean areEquivalent(ISurfaceBuilderConfig config1, ISurfaceBuilderConfig config2) {
		if (config1.getTop() != config2.getTop()) return false;
		if (config1.getUnder() != config2.getUnder()) return false;
		return getUnderwaterMaterial(config1) == getUnderwaterMaterial(config2);
	}
}
