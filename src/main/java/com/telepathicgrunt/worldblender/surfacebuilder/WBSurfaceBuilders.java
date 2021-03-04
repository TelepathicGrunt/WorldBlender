package com.telepathicgrunt.worldblender.surfacebuilder;

import com.google.common.base.Supplier;
import com.telepathicgrunt.worldblender.WorldBlender;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class WBSurfaceBuilders
{
	public static final DeferredRegister<SurfaceBuilder<?>> SURFACE_BUILDERS = DeferredRegister.create(ForgeRegistries.SURFACE_BUILDERS, WorldBlender.MODID);
	
    public static final RegistryObject<SurfaceBuilder<SurfaceBuilderConfig>> BLENDED_SURFACE_BUILDER = createSurfaceBuilder("blended_surface_builder", () -> new BlendedSurfaceBuilder());
    
    public static <S extends SurfaceBuilder<?>> RegistryObject<S> createSurfaceBuilder(String name, Supplier<? extends S> surfaceBuilder)
	{
		return SURFACE_BUILDERS.register(name, surfaceBuilder);
	}
}
