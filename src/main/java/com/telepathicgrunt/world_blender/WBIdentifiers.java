package com.telepathicgrunt.world_blender;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class WBIdentifiers {
    public static final ResourceLocation MOD_DIMENSION_ID = new ResourceLocation(WorldBlender.MODID, WorldBlender.MODID);
    public static final RegistryKey<World> WB_WORLD_KEY = RegistryKey.func_240903_a_(Registry.WORLD_KEY, WBIdentifiers.MOD_DIMENSION_ID);
    public static final ResourceLocation WB_BIOME_PROVIDER_ID = new ResourceLocation(WorldBlender.MODID, "biome_source");

    public static final ResourceLocation PORTAL_COOLDOWN_PACKET_ID = new ResourceLocation(WorldBlender.MODID, "portal_cooldown");

    public static final ResourceLocation GENERAL_BLENDED_BIOME_ID = new ResourceLocation(WorldBlender.MODID, "general_blended");
    public static final ResourceLocation COLD_HILLS_BLENDED_BIOME_ID = new ResourceLocation(WorldBlender.MODID, "cold_hills_blended");
    public static final ResourceLocation MOUNTAINOUS_BLENDED_BIOME_ID = new ResourceLocation(WorldBlender.MODID, "mountainous_blended");
    public static final ResourceLocation OCEAN_BLENDED_BIOME_ID = new ResourceLocation(WorldBlender.MODID, "ocean_blended");
    public static final ResourceLocation FROZEN_OCEAN_BLENDED_BIOME_ID = new ResourceLocation(WorldBlender.MODID, "frozen_ocean_blended");

    public static final ResourceLocation ALTAR_ID = new ResourceLocation(WorldBlender.MODID, "portal_altar");
}
