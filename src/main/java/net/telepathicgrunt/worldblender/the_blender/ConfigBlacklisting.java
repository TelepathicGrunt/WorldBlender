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
	
	public enum BlacklistType 
	{
		BLANKET,
		FEATURE,
		STRUCTURE,
		CARVER,
		SPAWN,
		SURFACE_BLOCK
	}
	
	
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
		String[] entriesArray = configEntry.split(",");
		Arrays.parallelSetAll(entriesArray, (i) -> entriesArray[i].trim().toLowerCase().replace(' ', '_'));
		return Arrays.asList(entriesArray);
	}
	
	/**
	 * Helper method that will perform the actual RL match, mod specific match, 
	 * and term matching based on the format of the blacklisted entry string
	 */
	private static boolean matchFound(String blacklistedEntry, ResourceLocation resourceLocationToCheck) 
	{
		//cannot do any matching. RIP
		if(resourceLocationToCheck == null || blacklistedEntry.isEmpty()) 
		{
			return false;
		}
		
		//full resource location specific ban
		if(blacklistedEntry.contains(":")) 
		{
			return blacklistedEntry.equals(resourceLocationToCheck.toString());
		}
		//mod specific ban
		else if(blacklistedEntry.contains("*")) 
		{
			return blacklistedEntry.substring(0, blacklistedEntry.length() - 1).equals(resourceLocationToCheck.getNamespace().toString());
		}
		//term specific ban
		return resourceLocationToCheck.getPath().toString().contains(blacklistedEntry);
	}
	
	
	
	
	public static boolean isResourceLocationBlacklisted(BlacklistType type, ResourceLocation biomeRL) 
	{
		List<String> listToUse;
		
		switch(type) 
		{
			case BLANKET:
				listToUse = blanketBL;
				break;
				
			case FEATURE:
				listToUse = featureBL;
				break;
				
			case STRUCTURE:
				listToUse = structureBL;
				break;
				
			case CARVER:
				listToUse = carverBL;
				break;
				
			case SPAWN:
				listToUse = spawnBL;
				break;
				
			case SURFACE_BLOCK:
				listToUse = surfaceBL;
				break;
				
			default:
				return false;
		}
				
		
		boolean isNotAllowed = listToUse.stream().anyMatch(banEntry -> matchFound(banEntry, biomeRL));
		return isNotAllowed;
	}
	
}
