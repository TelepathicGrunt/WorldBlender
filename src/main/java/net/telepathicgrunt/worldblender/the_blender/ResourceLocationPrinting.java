package net.telepathicgrunt.worldblender.the_blender;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.telepathicgrunt.worldblender.WorldBlender;

public class ResourceLocationPrinting
{

	private enum registryEdgeCases 
	{
		FEATURES,
		STRUCTURES,
		OTHER
	}
	
	/**
	 * Prints out all the resource location that the blacklists for World Blender config uses.
	 * Will create 6 sections: Biomes, Features, Structures, Carvers, Entities, and Blocks.
	 * 
	 * The resource location will be printed into  a file call resourceLocationDump.txt
	 * and can be found below the world's save folder in the Minecraft folder.
	 */
	public static void printAllResourceLocations() 
	{
		try(PrintStream printStream = new PrintStream("resourceLocationDump.txt")) 
		{ 
			printOutSection(printStream, ForgeRegistries.BIOMES, "BIOMES", registryEdgeCases.OTHER);

			printStream.println(""); 
			printOutSection(printStream, ForgeRegistries.FEATURES, "FEATURES", registryEdgeCases.FEATURES);
			
			printStream.println(""); 
			printOutSection(printStream, ForgeRegistries.FEATURES, "STRUCTURES", registryEdgeCases.STRUCTURES);

			printStream.println(""); 
			printOutSection(printStream, ForgeRegistries.WORLD_CARVERS, "CARVERS", registryEdgeCases.OTHER);
			
			printStream.println(""); 
			printOutSection(printStream, ForgeRegistries.ENTITIES, "ENTITIES", registryEdgeCases.OTHER);
			
			printStream.println(""); 
			printOutSection(printStream, ForgeRegistries.BLOCKS, "BLOCKS", registryEdgeCases.OTHER);
			
		}
		catch (FileNotFoundException e)
		{
			WorldBlender.LOGGER.warn("FAILED TO CREATE AND WRITE TO resourceLocationDump.txt. SEE STACKTRACE AND SHOW IT TO MOD MAKER.");
			e.printStackTrace();
		} 
	}
	/**
	 * Will go through that registry passed in and print out all the resource locations of every entry inside of it.
	 * 
	 * @param <T> - generic type of the entries in the registry
	 * @param printStream - the place we are printing the resource locations to
	 * @param registry - the registry to go through and get all entries
	 * @param section - name of this section. Will be put into the header and printed into the printStream
	 * @param registryCase - the kind of section we are doing. This is to handle edge cases such as Features and Structures existing in the same Forge registry.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends IForgeRegistryEntry<T>> void printOutSection(PrintStream printStream, IForgeRegistry<T> registry, String section, registryEdgeCases registryCase) 
	{
		String previousNameSpace = "minecraft";
		ResourceLocation forgeRL;
		
		//title of the section
		printStream.println("######################################################################"); 
		printStream.println("######      "+section+" RESOURCE LOCATION (IDs)        ######"); 
		printStream.println(""); 

		for (T forgeEntry : registry.getValues())
		{
			if(registryCase == registryEdgeCases.FEATURES)
			{
				//structures are a subset of features. Filter them out as structures have their own section
				if(forgeEntry instanceof Structure) 
					continue;
			}
			else if(registryCase == registryEdgeCases.STRUCTURES)
			{
				//structures are a subset of features. Filter out features as features have their own section
				if(!(forgeEntry instanceof Structure)) 
					continue;
			}
			
			forgeRL = ((ForgeRegistryEntry<T>) forgeEntry).getRegistryName();
			
			// extra check to just make sure. Probably never possible to be null
			if(forgeRL == null) continue; 
			
			//prints a space between different Mod IDs
			previousNameSpace = printSpacingBetweenMods(printStream, previousNameSpace, forgeRL.getNamespace());
			
			//prints the actual entry's resource location
			printStream.println(forgeRL.toString()); 
		}
	}
	
	/**
	 * helper method to print spacing between different mod's resource location section
	 */
	private static String printSpacingBetweenMods(PrintStream printStream, String previousModID, String currentModID) 
	{
		if(!currentModID.isEmpty() && !previousModID.equals(currentModID))
		{
			printStream.println(""); 
			return currentModID;
		}
		
		return previousModID;
	}
}
