package com.telepathicgrunt.worldblender.theblender;

import com.telepathicgrunt.worldblender.WorldBlender;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class ConfigBlacklisting
{
	private static Map<BlacklistType, List<String>> TYPE_TO_BLACKLIST;
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
		List<String> blanketBL = parseConfigAndAssignEntries(WorldBlender.WBBlendingConfig.blanketBlacklist.get());
		List<String> featureBL = parseConfigAndAssignEntries(WorldBlender.WBBlendingConfig.blacklistedFeatures.get());
		List<String> structureBL = parseConfigAndAssignEntries(WorldBlender.WBBlendingConfig.blacklistedStructures.get());
		List<String> carverBL = parseConfigAndAssignEntries(WorldBlender.WBBlendingConfig.blacklistedCarvers.get());
		List<String> spawnBL = parseConfigAndAssignEntries(WorldBlender.WBBlendingConfig.blacklistedSpawns.get());
		List<String> surfaceBL = parseConfigAndAssignEntries(WorldBlender.WBBlendingConfig.blacklistedBiomeSurfaces.get());

		TYPE_TO_BLACKLIST = new HashMap<>();
		TYPE_TO_BLACKLIST.put(BlacklistType.BLANKET, blanketBL);
		TYPE_TO_BLACKLIST.put(BlacklistType.FEATURE, featureBL);
		TYPE_TO_BLACKLIST.put(BlacklistType.STRUCTURE, structureBL);
		TYPE_TO_BLACKLIST.put(BlacklistType.CARVER, carverBL);
		TYPE_TO_BLACKLIST.put(BlacklistType.SPAWN, spawnBL);
		TYPE_TO_BLACKLIST.put(BlacklistType.SURFACE_BLOCK, surfaceBL);
	}
	
	/**
	 * Takes config string and chops it up into individual entries and returns the array of the entries.
	 * Splits the incoming string on commas, trims white spaces on end, turns inside whitespace to _, and lowercases entry.
	 */
	private static List<String> parseConfigAndAssignEntries(String configEntry) {
		String[] entriesArray = configEntry.split(",");
		Arrays.parallelSetAll(entriesArray, (i) -> entriesArray[i].trim().toLowerCase(Locale.ROOT).replace(' ', '_'));
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
			return blacklistedEntry.substring(0, blacklistedEntry.length() - 1).equals(resourceLocationToCheck.getNamespace());
		}
		//term specific ban
		return resourceLocationToCheck.getPath().contains(blacklistedEntry);
	}
	

	public static boolean isResourceLocationBlacklisted(BlacklistType type, ResourceLocation incomingRL)
	{
		List<String> listToUse = TYPE_TO_BLACKLIST.get(type);
		return listToUse.stream().anyMatch(banEntry -> matchFound(banEntry, incomingRL));
	}
}
