package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.block.Block;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(DimensionStructuresSettings.class)
public interface DimensionStructureSettingsAccessor {

    @Accessor("field_236193_d_")
    void setStructureConfigMap(Map<Structure<?>, StructureSeparationSettings> structureStructureSeparationSettingsMap);
}