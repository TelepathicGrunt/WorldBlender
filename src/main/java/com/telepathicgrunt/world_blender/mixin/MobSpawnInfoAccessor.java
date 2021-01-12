package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.MobSpawnInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(MobSpawnInfo.class)
public interface MobSpawnInfoAccessor {

    @Accessor("spawners")
    Map<EntityClassification, List<MobSpawnInfo.Spawners>> wb_getSpawners();

    @Accessor("spawners")
    void wb_setSpawners(Map<EntityClassification, List<MobSpawnInfo.Spawners>> features);


    @Accessor("spawnCosts")
    Map<EntityType<?>, MobSpawnInfo.SpawnCosts> wb_getSpawnCosts();

    @Accessor("spawnCosts")
    void wb_setSpawnCosts(Map<EntityType<?>, MobSpawnInfo.SpawnCosts>  structureFeatures);
}