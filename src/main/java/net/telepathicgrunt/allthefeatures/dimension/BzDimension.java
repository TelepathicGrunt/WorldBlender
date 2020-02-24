package net.telepathicgrunt.allthefeatures.dimension;

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
import net.minecraftforge.fml.common.Mod;
import net.telepathicgrunt.allthefeatures.AllTheFeatures;


@Mod.EventBusSubscriber(modid = AllTheFeatures.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BzDimension {

	public static final ModDimension ALL_THE_FEATURES = new ModDimension() {
        @Override
        public BiFunction<World, DimensionType, ? extends Dimension> getFactory() {
            return FeatureDimensionProvider::new;
        }
    };

    private static final ResourceLocation ALL_THE_FEATURES_ID = new ResourceLocation(AllTheFeatures.MODID, "all_the_features");
	
    
    //registers the dimension
    @Mod.EventBusSubscriber(modid = AllTheFeatures.MODID)
    private static class ForgeEvents {
        @SubscribeEvent
        public static void registerDimensions(RegisterDimensionsEvent event) {
            if (DimensionType.byName(ALL_THE_FEATURES_ID) == null) {
                DimensionManager.registerDimension(ALL_THE_FEATURES_ID, ALL_THE_FEATURES, null, true);
            }
        }
    }

    @SubscribeEvent
    public static void registerModDimensions(RegistryEvent.Register<ModDimension> event) {
        RegUtil.generic(event.getRegistry()).add("ultraamplified", ALL_THE_FEATURES);
    }

    public static DimensionType bumblezone() {
        return DimensionType.byName(ALL_THE_FEATURES_ID);
    }
    
}
