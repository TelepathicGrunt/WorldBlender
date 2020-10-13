package com.telepathicgrunt.world_blender.surfacebuilder;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.event.RegistryEvent;

public class WBSurfaceBuilders {
    public static final SurfaceBuilder<SurfaceBuilderConfig> BLENDED_SURFACE_BUILDER = new BlendedSurfaceBuilder();

    public static void registerSurfaceBuilders(final RegistryEvent.Register<SurfaceBuilder<?>> event) {
        WorldBlender.register(event.getRegistry(), BLENDED_SURFACE_BUILDER, "blended_surface_builder");
    }
}
