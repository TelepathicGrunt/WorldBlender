package com.telepathicgrunt.world_blender.surfacebuilder;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

public class WBSurfaceBuilders {
    public static final SurfaceBuilder<TernarySurfaceConfig> BLENDED_SURFACE_BUILDER = new BlendedSurfaceBuilder();

    public static void registerSurfaceBuilders() {
        Registry.register(Registry.SURFACE_BUILDER, new Identifier(WorldBlender.MODID, "blended_surface_builder"), BLENDED_SURFACE_BUILDER);
    }
}
