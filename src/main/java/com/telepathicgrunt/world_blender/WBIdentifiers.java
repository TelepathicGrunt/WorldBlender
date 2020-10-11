package com.telepathicgrunt.world_blender;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class WBIdentifiers {
    public static final Identifier MOD_DIMENSION_ID = new Identifier(WorldBlender.MODID, WorldBlender.MODID);
    public static final RegistryKey<World> WB_WORLD_KEY = RegistryKey.of(Registry.DIMENSION, WBIdentifiers.MOD_DIMENSION_ID);
    public static final Identifier WB_BIOME_PROVIDER_ID = new Identifier(WorldBlender.MODID, "biome_source");

    public static final Identifier PORTAL_COOLDOWN_PACKET_ID = new Identifier(WorldBlender.MODID, "portal_cooldown");

    public static final Identifier GENERAL_BLENDED_BIOME_ID = new Identifier(WorldBlender.MODID, "general_blended");
    public static final Identifier COLD_HILLS_BLENDED_BIOME_ID = new Identifier(WorldBlender.MODID, "cold_hills_blended");
    public static final Identifier MOUNTAINOUS_BLENDED_BIOME_ID = new Identifier(WorldBlender.MODID, "mountainous_blended");
    public static final Identifier OCEAN_BLENDED_BIOME_ID = new Identifier(WorldBlender.MODID, "ocean_blended");
    public static final Identifier FROZEN_OCEAN_BLENDED_BIOME_ID = new Identifier(WorldBlender.MODID, "frozen_ocean_blended");

    public static final Identifier ALTAR_ID = new Identifier(WorldBlender.MODID, "portal_altar");
}
