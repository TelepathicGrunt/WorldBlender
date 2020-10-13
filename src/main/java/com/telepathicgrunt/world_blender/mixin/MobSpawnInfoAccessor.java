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

    @Accessor("field_242554_e")
    Map<EntityClassification, List<MobSpawnInfo.Spawners>> getSpawners();

    @Accessor("field_242554_e")
    void setSpawners(Map<EntityClassification, List<MobSpawnInfo.Spawners>> features);


    @Accessor("field_242555_f")
    Map<EntityType<?>, MobSpawnInfo.SpawnCosts> getSpawnCosts();

    @Accessor("field_242555_f")
    void setSpawnCosts(Map<EntityType<?>, MobSpawnInfo.SpawnCosts>  structureFeatures);

}