package com.telepathicgrunt.world_blender.the_blender;

import com.telepathicgrunt.world_blender.WorldBlender;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

public class IdentifierPrinting
{

	/**
	 * Prints out all the resource location that the blacklists for World Blender config uses.
	 * Will create 6 sections: Biomes, Features, Structures, Carvers, Entities, and Blocks.
	 * 
	 * The resource location will be printed into a file call identifierDump.txt
	 * and can be found below the world's save folder in the Minecraft folder.
	 */
	public static void printAllResourceLocations(DynamicRegistries registryManager)
	{
		try(PrintStream printStream = new PrintStream("identifierDump.txt"))
		{ 
			printOutSection(printStream, registryManager.getRegistry(Registry.BIOME_KEY), "BIOMES");

			printStream.println();
			printOutSection(printStream, registryManager.getRegistry(Registry.CONFIGURED_FEATURE_KEY), "CONFIGURED FEATURES");
			
			printStream.println();
			printOutSection(printStream, registryManager.getRegistry(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY), "CONFIGURED STRUCTURES");

			printStream.println();
			printOutSection(printStream, registryManager.getRegistry(Registry.CONFIGURED_CARVER_KEY), "CARVERS");
			
			printStream.println();
			printOutSection(printStream, Registry.ENTITY_TYPE, "ENTITIES");
			
			printStream.println();
			printOutSection(printStream, Registry.BLOCK, "BLOCKS");
			
		}
		catch (FileNotFoundException e)
		{
			WorldBlender.LOGGER.warn("FAILED TO CREATE AND WRITE TO identifierDump.txt. SEE STACKTRACE AND SHOW IT TO MOD MAKER.");
			e.printStackTrace();
		} 
	}


	/**
	 * Will go through that registry passed in and print out all the resource locations of every entry inside of it.
	 *
	 * @param printStream - the place we are printing the resource locations to
	 * @param registry - the registry to go through and get all entries
	 * @param section - name of this section. Will be put into the header and printed into the printStream
	 */
	private static <T> void printOutSection(PrintStream printStream, Registry<T> registry, String section)
	{
		AtomicReference<String> previous_namespace = new AtomicReference<>("minecraft");
		
		//title of the section
		printStream.println("######################################################################"); 
		printStream.println("######      "+section+" RESOURCE LOCATION (IDs)        ######"); 
		printStream.println();

		registry.getEntries().stream().sorted(Comparator.comparing(p -> p.getKey().getLocation().toString()))
				.forEach(entry -> writeEntry(printStream, entry.getKey(), previous_namespace));
	}

	private static void writeEntry(PrintStream printStream, RegistryKey<?> entry, AtomicReference<String> previous_namespace){
		ResourceLocation entryID = entry.getLocation();

		// extra check to just make sure. Probably never possible to be null
		if(entryID == null) return;

		//prints a space between different Mod IDs
		previous_namespace.set(printSpacingBetweenMods(printStream, previous_namespace.get(), entryID.getNamespace()));

		//prints the actual entry's resource location
		printStream.println(entryID.toString());

	}
	
	/**
	 * helper method to print spacing between different mod's resource location section
	 */
	private static String printSpacingBetweenMods(PrintStream printStream, String previousModID, String currentModID) 
	{
		if(!currentModID.isEmpty() && !previousModID.equals(currentModID))
		{
			printStream.println();
			return currentModID;
		}
		
		return previousModID;
	}
}
