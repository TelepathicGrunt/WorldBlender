package net.telepathicgrunt.worldblender.dimension;

import java.util.function.BiFunction;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.telepathicgrunt.worldblender.WorldBlender;


@EventBusSubscriber(modid = WorldBlender.MODID, bus = Bus.MOD)
public class WBDimension {

	public static final ModDimension WORLD_BLENDER_DIMENSION = new ModDimension() {
        @Override
        public BiFunction<World, DimensionType, ? extends Dimension> getFactory() {
            return WBDimensionProvider::new;
        }
    };

    private static final ResourceLocation WORLD_BLENDER_DIMENSION_RL = new ResourceLocation(WorldBlender.MODID, "world_blender");
	
    
    public static void registerDimensions(RegisterDimensionsEvent event) {
        if (DimensionType.byName(WORLD_BLENDER_DIMENSION_RL) == null) {
            DimensionManager.registerDimension(WORLD_BLENDER_DIMENSION_RL, WORLD_BLENDER_DIMENSION, null, true);
        }
    }
    
    @SubscribeEvent
    public static void registerModDimensions(RegistryEvent.Register<ModDimension> event) {
        RegUtil.generic(event.getRegistry()).add(WorldBlender.MODID, WORLD_BLENDER_DIMENSION);
    }

    public static DimensionType worldblender() {
        return DimensionType.byName(WORLD_BLENDER_DIMENSION_RL);
    }
    
}
