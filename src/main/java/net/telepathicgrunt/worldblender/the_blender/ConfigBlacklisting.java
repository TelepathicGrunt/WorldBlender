package net.telepathicgrunt.worldblender.the_blender;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.telepathicgrunt.worldblender.configs.WBConfig;

public class ConfigBlacklisting
{
	private static List<String> blanketBL;
	private static List<String> featureBL;
	private static List<String> structureBL;
	private static List<String> carverBL;
	private static List<String> spawnBL;
	private static List<String> surfaceBL;
	
	public static void setupBlackLists() 
	{
		blanketBL = parseConfigAndAssignEntries(WBConfig.blanketBlacklist);
		featureBL = parseConfigAndAssignEntries(WBConfig.blacklistedFeatures);
		structureBL = parseConfigAndAssignEntries(WBConfig.blacklistedStructures);
		carverBL = parseConfigAndAssignEntries(WBConfig.blacklistedCarvers);
		spawnBL = parseConfigAndAssignEntries(WBConfig.blacklistedSpawns);
		surfaceBL = parseConfigAndAssignEntries(WBConfig.blacklistedBiomeSurfaces);
	}
	
	/**
	 * Takes config string and chops it up into individual entries and returns the array of the entries.
	 * Splits the incoming string on commas, trims white spaces on end, turns inside whitespace to _, and lowercases entry.
	 */
	private static List<String> parseConfigAndAssignEntries(String configEntry) {
		List<String> entriesArray = Arrays.asList(configEntry.split(","));
		entriesArray.forEach(string -> string.trim().toLowerCase().replace(' ', '_'));
		return entriesArray;
	}
	
	/**
	 * Helper method that will perform the actual RL match, mod specific match, 
	 * and term matching based on the format of the blacklisted entry string
	 */
	private static boolean matchFound(String blacklistedEntry, ResourceLocation biomeRL) 
	{
		//full resource location specific ban
		if(blacklistedEntry.contains(":") && blacklistedEntry.equals(biomeRL.toString())) 
		{
			return true;
		}
		//mod specific ban
		else if(blacklistedEntry.contains("*") && blacklistedEntry.substring(0, blacklistedEntry.length() - 2).equals(biomeRL.getNamespace().toString())) 
		{
			return true;
		}
		//term specific ban
		else if(biomeRL.toString().contains(blacklistedEntry))
		{
			return true;
		}
		
		return false;
	}
	
	
	
	
	public static boolean allowedBiome(ResourceLocation biomeRL) 
	{
		boolean allowedBiome = !blanketBL.stream().anyMatch(banEntry -> matchFound(banEntry, biomeRL));
		return allowedBiome;
	}

	public static boolean allowedFeature(ResourceLocation featureRL) 
	{
		boolean allowedFeature = !featureBL.stream().anyMatch(banEntry -> matchFound(banEntry, featureRL));
		return allowedFeature;
	}
	
	public static boolean allowedStructure(ResourceLocation structureRL) 
	{
		boolean allowedStructure = !structureBL.stream().anyMatch(banEntry -> matchFound(banEntry, structureRL));
		return allowedStructure;
	}
	
	public static boolean allowedcarver(ResourceLocation carverRL) 
	{
		boolean allowedcarver = !carverBL.stream().anyMatch(banEntry -> matchFound(banEntry, carverRL));
		return allowedcarver;
	}
	
	public static boolean allowedSpawn(ResourceLocation spawnRL) 
	{
		boolean allowedSpawn = !spawnBL.stream().anyMatch(banEntry -> matchFound(banEntry, spawnRL));
		return allowedSpawn;
	}
	
	public static boolean allowedSurface(ResourceLocation blockRL) 
	{
		boolean allowedSurface = !surfaceBL.stream().anyMatch(banEntry -> matchFound(banEntry, blockRL));
		return allowedSurface;
	}
}
