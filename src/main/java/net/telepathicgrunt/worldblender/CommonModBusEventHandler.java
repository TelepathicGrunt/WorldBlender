package net.telepathicgrunt.worldblender;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.Level;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.telepathicgrunt.worldblender.configs.WBConfig;
import net.telepathicgrunt.worldblender.the_blender.ConfigBlacklisting;
import net.telepathicgrunt.worldblender.the_blender.PerformBiomeBlending;
import net.telepathicgrunt.worldblender.the_blender.ResourceLocationPrinting;
import net.telepathicgrunt.worldblender.the_blender.dedicated_mod_support.DimDungeonsCompatibility;
import net.telepathicgrunt.worldblender.the_blender.dedicated_mod_support.TerraForgedCompatibility;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(modid = WorldBlender.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModBusEventHandler
{

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void latestCommonSetup(FMLLoadCompleteEvent event) {
	DeferredWorkQueue.runLater(CommonModBusEventHandler::beginSetup);
    }


    public static void beginSetup() {
	ConfigBlacklisting.setupBlackLists();
	PerformBiomeBlending.setupBiomes();


	try {
	    runIfModIsLoaded("dimdungeons", () -> () -> DimDungeonsCompatibility.addDDDungeons());
	}
	catch (Exception e) {
	    WorldBlender.LOGGER.log(Level.INFO, "ERROR: Failed to setup compatibility with Dimensional Dungeons. Their dungeons will not spawn in World Blender's dimension now. Please let the developer of World Blender know about this!");
	    e.printStackTrace();
	}
	
	try {
	    runIfModIsLoaded("terraforged", () -> () -> TerraForgedCompatibility.addTerraForgedtrees());
	}
	catch (Exception e) {
	    WorldBlender.LOGGER.log(Level.INFO, "ERROR: Failed to setup compatibility with Terraforged. Their trees and stuff will not spawn in World Blender's dimension now. Please let the developer of World Blender know about this!");
	    e.printStackTrace();
	}

	if (WBConfig.resourceLocationDump) {
	    ResourceLocationPrinting.printAllResourceLocations();
	}
    }


    /**
     * Hack to make Java not load the class beforehand when we don't have the mod installed. Basically:
     * 
     * "java only loads the method body in 2 cases when it runs or when it needs to run the class verifier"
     * 
     * So by double wrapping, we prevent Java from loading a class with calls to a mod that isn't present
     */
    public static void runIfModIsLoaded(String modid, Callable<Runnable> toRun) throws Exception {
	if (ModList.get().isLoaded(modid)) toRun.call().run();
    }
}
