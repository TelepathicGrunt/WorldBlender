package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.biome.SpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(SpawnSettings.class)
public interface SpawnSettingsAccessor {

    @Accessor("spawners")
    Map<SpawnGroup, List<SpawnSettings.SpawnEntry>> getSpawners();

    @Accessor("spawners")
    void setSpawners(Map<SpawnGroup, List<SpawnSettings.SpawnEntry>> features);


    @Accessor("spawnCosts")
    Map<EntityType<?>, SpawnSettings.SpawnDensity> getSpawnCosts();

    @Accessor("spawnCosts")
    void setSpawnCosts(Map<EntityType<?>, SpawnSettings.SpawnDensity>  structureFeatures);

}