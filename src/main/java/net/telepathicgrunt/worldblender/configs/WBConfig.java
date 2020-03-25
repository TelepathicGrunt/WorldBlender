package net.telepathicgrunt.worldblender.configs;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class WBConfig
{
	  /*
	   * Config to control all sorts of settings.
	   */

	    public static final ServerConfig SERVER;
	    public static final ForgeConfigSpec SERVER_SPEC;
	    static {
	        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
	        SERVER_SPEC = specPair.getRight();
	        SERVER = specPair.getLeft();
	    }
	    public static boolean allowVanillaBiomeImport = true;
	    public static boolean allowModdedBiomeImport = true;
	    public static boolean disallowLaggyFeatures = true;

	    public static boolean allowVanillaFeatures = true;
	    public static boolean allowVanillaStructures = true;
	    public static boolean allowVanillaCarvers = true;
	    public static boolean allowVanillaSpawns = true;
	    public static boolean allowVanillaSurfaces = true;
	    
	    public static boolean allowModdedFeatures = true;
	    public static boolean allowModdedStructures = true;
	    public static boolean allowModdedCarvers = true;
	    public static boolean allowModdedSpawns = true;
	    public static boolean allowModdedSurfaces = true;
	    
	    public static String blanketBlacklist = "ultra_amplified_dimension*";
	    public static String blacklistedFeatures = "";
	    public static String blacklistedStructures = "";
	    public static String blacklistedCarvers = "";
	    public static String blacklistedSpawns = "";
	    public static String blacklistedBiomeSurfaces = "";
	    public static boolean resourceLocationDump = false;

	    public static int uniqueBlocksNeeded = 216;
	    public static String activationItem = "minecraft:nether_star";
	    public static boolean consumeChests = true;

	    public static boolean spawnEnderDragon = false;
	    public static boolean vanillaCarversCanCarveMoreBlocks = true;
	    
	    public static class ServerConfig
	    {

		    public final BooleanValue allowVanillaBiomeImport;
		    public final BooleanValue allowModdedBiomeImport;
		    public final BooleanValue disallowLaggyFeatures;
	    	
		    public final BooleanValue allowVanillaFeatures;
		    public final BooleanValue allowVanillaStructures;
		    public final BooleanValue allowVanillaCarvers;
		    public final BooleanValue allowVanillaSpawns;
		    public final BooleanValue allowVanillaSurfaces;
		    
		    public final BooleanValue allowModdedFeatures;
		    public final BooleanValue allowModdedStructures;
		    public final BooleanValue allowModdedCarvers;
		    public final BooleanValue allowModdedSpawns;
		    public final BooleanValue allowModdedSurfaces;

		    public final ConfigValue<String> blanketBlacklist;
		    public final ConfigValue<String> blacklistedFeatures;
		    public final ConfigValue<String> blacklistedStructures;
		    public final ConfigValue<String> blacklistedCarvers;
		    public final ConfigValue<String> blacklistedSpawns;
		    public final ConfigValue<String> blacklistedBiomeSurfaces;
		    public final BooleanValue resourceLocationDump;

		    public final IntValue uniqueBlocksNeeded;
		    public final ConfigValue<String> activationItem;
		    public final BooleanValue consumeChests;
		    
		    public final BooleanValue spawnEnderDragon;
		    public final BooleanValue vanillaCarversCanCarveMoreBlocks;
		    
	        ServerConfig(ForgeConfigSpec.Builder builder) 
	        {

	            builder.push("Optimization Options");

	            disallowLaggyFeatures = builder
	                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
	                    		+" Will make vanilla bamboo, lava, and fire features and will try to \r\n"
	                    		+" make modded bamboo, lava, and fire features not spawn at all\r\n"
	                    		+" in order to help reduce lag in the world due to bamboo\r\n"
	                    		+" breaking like crazy or fire spreading rapidly.\r\n"
	                    		+" \r\n"
	                    		+" If all else fail, do /gamerule doFireTick false to reduce fire lag.\r\n")
	                    .translation("world_blender.config.vanilla.disallowlaggyvanillafeatures")
	                    .define("disallowLaggyVanillaFeatures", true);
	            
	            builder.pop();
	            
	            builder.push("Vanilla Options");
	        	
	            	allowVanillaBiomeImport = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension can import anything from vanilla biomes.\r\n"
		                    		+" Note: If the other vanilla stuff options are set to true and you \r\n"
		                    		+" have the import from modded biome option set to true as well, then\r\n"
		                    		+" vanilla stuff can still get imported if a modded biome has them.\r\n")
		                    .translation("world_blender.config.vanilla.allowvanillabiomeimport")
		                    .define("allowVanillaBiomeImport", true);
	
	            
		            allowVanillaFeatures = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension imports features like trees, plants, ores, etc.\r\n")
		                    .translation("world_blender.config.vanilla.allowvanillafeatures")
		                    .define("allowVanillaFeatures", true);
	
		            allowVanillaStructures = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension imports structures like temples, villages, etc.\r\n")
		                    .translation("world_blender.config.vanilla.allowvanillastructures")
		                    .define("allowVanillaStructures", true);
	
		            allowVanillaCarvers = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension imports carvers like caves, ravines, etc.\r\n")
		                    .translation("world_blender.config.vanilla.allowvanillacarvers")
		                    .define("allowVanillaCarvers", true);
	
		            allowVanillaSpawns = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension imports natural mob spawns like zombies, cows, etc.\r\n")
		                    .translation("world_blender.config.vanilla.allowvanillaspawns")
		                    .define("allowVanillaSpawns", true);
	
		            allowVanillaSurfaces = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension imports surfaces like desert's sand, giant tree taiga's podzol, etc.\r\n")
		                    .translation("world_blender.config.vanilla.allowvanillasurfaces")
		                    .define("allowVanillaSurfaces", true);
	            
	            builder.pop();
	            
	            builder.push("Modded Options");
	        	
            		allowModdedBiomeImport = builder
	                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
	                    		+" Decides if the dimension can import anything from modded biomes.\r\n"
	                    		+" Note: If the other vanilla stuff options are set to true and you\r\n"
	                    		+" this option set to true as well, then vanilla stuff can still\r\n"
	                    		+" get imported if a modded biome has vanilla stuff in it.\r\n")
	                    .translation("world_blender.config.vanilla.allowmoddedbiomeimport")
	                    .define("allowModdedBiomeImport", true);

	            	allowModdedFeatures = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension imports features like trees, plants, ores, etc.\r\n")
		                    .translation("world_blender.config.vanilla.allowmoddedfeatures")
		                    .define("allowModdedFeatures", true);
	
	            	allowModdedStructures = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension imports structures like temples, villages, etc.\r\n")
		                    .translation("world_blender.config.vanilla.allowmoddedstructures")
		                    .define("allowModdedStructures", true);
	
		            allowModdedCarvers = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension imports carvers like caves, ravines, etc.\r\n")
		                    .translation("world_blender.config.vanilla.allowmoddedcarvers")
		                    .define("allowModdedCarvers", true);
	
		            allowModdedSpawns = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension imports natural mob spawns like zombies, cows, etc.\r\n")
		                    .translation("world_blender.config.vanilla.allowmoddedspawns")
		                    .define("allowModdedSpawns", true);
	
		            allowModdedSurfaces = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Decides if the dimension imports surfaces like desert's sand, giant tree taiga's podzol, etc.\r\n")
		                    .translation("world_blender.config.vanilla.allowmoddedsurfaces")
		                    .define("allowModdedSurfaces", true);
		            
	            builder.pop();
	            
	            builder.push("Portal Options");
	            
		            uniqueBlocksNeeded = builder
			                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
			                    		+" At least many unique block items are needed to be in the eight\r\n"
			                    		+" chests (or other blocks with chest tag) to create the portal. \r\n"
			                    		+" \r\n"
			                    		+" Items with no block form will be ignored and not counted but still be consumed.\r\n"
			                    		+" \r\n"
			                    		+" If you set this to beyond 216 (maximum slots four 8 chests), make\r\n"
			                    		+" sure you have a mod that has a chest that has much more inventory "
			                    		+" slots to fill or else you cannot ")
			                    .translation("world_blender.config.portal.uniqueblocksneeded")
			                    .defineInRange("uniqueBlocksNeeded", 216, 0, 1000);
			            
		            activationItem = builder
			                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
			                    		+" Item that you need in your hand when you're crouching and right\r\n"
			                    		+" clicking a chest block to begin the portal creation process.\r\n"
			                    		+" This activation item will then be consumed.\r\n"
			                    		+" \r\n"
			                    		+" NOTE: the 8 chests needs to be in a 2x2 pattern before this mod "
			                    		+" starts checking the contents of the chests and then create the"
			                    		+" portal if there are enough unique blocks in the chests."
			                    		+" \r\n"
			                    		+" You can remove a portal by crouch right clicking execpt for the\r\n"
			                    		+" portal block at world origin in World Blender dimension.\r\n")
			                    .translation("world_blender.config.portal.activationitem")
			                    .define("activationItem", "minecraft:nether_star");
			            
		            consumeChests = builder
			                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
			                    		+" If true, portal creation will destroy the chests and all contents in it\r\n"
			                    		+" Non-block items and stacks of items will still be consumed.\r\n"
			                    		+" \r\n"
			                    		+" If set to false, the chests and contents will be dropped when portal is made.\r\n")
			                    .translation("world_blender.config.portal.consumechests")
			                    .define("consumeChests", true);
		            
	            builder.pop();

	            builder.push("Resource Location Dump Option");
	            
				    resourceLocationDump = builder
			                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
			                    		+" Dumps all resource locations (IDs) for all mods into a new file\r\n"
			                    		+" called resourceLocationDump.txt and can be found by looking below\r\n"
			                    		+" the saves and mods folder in Minecraft's folder. The file is made if"
			                    		+" you set this option to true and then run modded Minecraft until you"
			                    		+" reach the title menu.\r\n"
			                    		+"\r\n"
			                    		+" Use this option to look up the resource location or name of features,"
			                    		+" biomes, blocks, carvers, structures, or entities that you want to blacklist.\r\n")
			                    .translation("world_blender.config.resourcelocation.resourcelocationdump")
			                    .define("resourceLocationDump", false);
			            
	            builder.pop();
	            
	            builder.push("Blacklist Options");
	            
	            
	            	blanketBlacklist = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" This option can let you blacklist entire biomes or mods to\r\n"
		                    		+" prevent any importing of any kind from them. You can also use\r\n"
		                    		+" terms to ban any biome that contains the terms too.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist a mod's biome, you would enter the resourcelocation\r\n"
		                    		+" for the biome. That means you need to enter the mod's ID first,\r\n"
		                    		+" then put a : (semicolon), and then the ID of the biome.\r\n"
		                    		+" For example, to blacklist just vanilla's Ice Spike biome, you\r\n"
		                    		+" would put in minecraft:ice_spike and nothing will be imported\r\n"
		                    		+" from that specific biome.\r\n"
		                    		+" \r\n"
		                    		+" If you want to blacklist an entire mod itself so no importing\r\n"
		                    		+" will happen for any of its biome, just enter the mod's ID and thenr\n"
		                    		+" put an * at the end.r\n"
		                    		+" As default, Ultra Amplified Dimension is blacklisted because\r\n"
		                    		+" its features are not setup for normal worldgen and will completely\r\n"
		                    		+" destroy this dimension.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by key terms, just enter the term alone such as \"ocean\"\r\n"
		                    		+" and all biomes with ocean in their name will not be imported.\r\n"
		                    		+" \r\n"
		                    		+" NOTE: You can blacklist multiple things at a time. Just separate\r\n"
		                    		+" each entry with a , (comma). Here's an example blacklisting a mod\r\n"
		                    		+" and a vanilla biome at the same time: \r\n"
		                    		+" \"ultra_amplified_dimension*, minecraft:ice_spike\"\r\n")
		                    .translation("world_blender.config.blacklist.blanketblacklist")
		                    .define("blanketBlacklist", "ultra_amplified_dimension*");

	            
			    	blacklistedFeatures = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Blacklist features by key terms, mod ID, or their resourcelocation\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by key terms, just enter the term alone such as \"tree\"\r\n"
		                    		+" and all features with tree in their name will not be imported.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by mod ID, just enter the mod ID with an * on the end such as\r\n"
		                    		+" \"ultra_amplified_dimension*\" and all features from that mod will not be imported.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist a single feature, enter the mod ID, then :, and then the \r\n"
		                    		+" feature's name. For example, \"minecraft:icebergs\" will prevent vanilla's\r\n"
		                    		+" icebergs from being imported but allow other mod's icebergs to be imported.\r\n"
		                    		+" \r\n"
		                    		+" NOTE: You can blacklist multiple things at a time. Just separate\r\n"
		                    		+" each entry with a , (comma). Here's an example blacklisting all trees\r\n"
		                    		+" and vanilla's icebergs: \r\n"
		                    		+" \"tree, minecraft:iceberg\"\r\n")
		                    .translation("world_blender.config.blacklist.blacklistedfeatures")
		                    .define("blacklistedFeatures", "");

	            
			    	blacklistedStructures = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Blacklist structures by key terms, mod ID, or their resourcelocation\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by key terms, just enter the term alone such as \"temple\"\r\n"
		                    		+" and all features with temple in their name will not be imported.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by mod ID, just enter the mod ID with an * on the end such as\r\n"
		                    		+" \"ultra_amplified_dimension*\" and all structures from that mod will not be imported.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist a single feature, enter the mod ID, then :, and then the \r\n"
		                    		+" feature's name. For example, \"minecraft:igloo\" will prevent vanilla's\r\n"
		                    		+" igloos from being imported but allow other mod's igloos to be imported.\r\n"
		                    		+" \r\n"
		                    		+" NOTE: You can blacklist multiple things at a time. Just separate\r\n"
		                    		+" each entry with a , (comma). Here's an example blacklisting all temples\r\n"
		                    		+" and vanilla's igloos: \r\n"
		                    		+" \"temple, minecraft:igloo\"\r\n")
		                    .translation("world_blender.config.blacklist.blacklistedstructures")
		                    .define("blacklistedStructures", "");

	            
			    	blacklistedCarvers = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Blacklist carvers by key terms, mod ID, or their resourcelocation\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by key terms, just enter the term alone such as \"cave\"\r\n"
		                    		+" and all carvers with cave in their name will not be imported if they\r\n"
		                    		+" are registered with a name. Not many mods register their carvers sadly.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by mod ID, just enter the mod ID with an * on the end such as\r\n"
		                    		+" \"ultra_amplified_dimension*\" and all carvers from that mod will not be imported.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist a single feature, enter the mod ID, then :, and then the \r\n"
		                    		+" feature's name. For example, \"minecraft:underwater_canyon\" will prevent \r\n"
		                    		+" vanilla's underwater canyons (ravines) from being imported. For underwater\r\n"
				                    +" caves, use \"minecraft:underwater_cave\" to stop them from being imported.\r\n"
		                    		+" \r\n"
		                    		+" NOTE: You can blacklist multiple things at a time. Just separate\r\n"
		                    		+" each entry with a , (comma). Here's an example blacklisting all caves\r\n"
		                    		+" and vanilla's underwater canyons: \r\n"
		                    		+" \"cave, minecraft:underwater_canyon\"\r\n")
		                    .translation("world_blender.config.blacklist.blacklistedcarvers")
		                    .define("blacklistedCarvers", "");

	            
			    	blacklistedSpawns = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Blacklist natural spawning mobs by key terms,\r\n" 
		                    		+" mod ID, or their resourcelocation\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by key terms, just enter the term alone such as \"zombie\"\r\n"
		                    		+" and all mobs with zombie in their name will not be imported.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by mod ID, just enter the mod ID with an * on the end such as\r\n"
		                    		+" \"super_duper_mob_mod*\" and all mobs from that mod will not be imported.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist a single mob, enter the mod ID, then :, and then the \r\n"
		                    		+" mob's name. For example, \"minecraft:ghast\" will prevent \r\n"
		                    		+" vanilla's ghast from being imported.\r\n"
		                    		+" \r\n"
		                    		+" As default, The Midnight's Night Stag is blacklisted because\r\n"
		                    		+" trying to spawn that mob outside The Mindnight's dimension will\r\n"
		                    		+" cause the world to hang forever.\r\n"
		                    		+" \r\n"
		                    		+" NOTE: You can blacklist multiple things at a time. Just separate\r\n"
		                    		+" each entry with a , (comma). Here's an example blacklisting all zombies\r\n"
		                    		+" and vanilla's ghasts: \r\n"
		                    		+" \"zombie, minecraft:ghast\"\r\n")
		                    .translation("world_blender.config.blacklist.blacklistedspawns")
		                    .define("blacklistedSpawns", "midnight:nightstag");
	            
			    
			    	blacklistedBiomeSurfaces = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Blacklist surfaces by key terms, mod ID, or by block's resourcelocation\r\n"
		                    		+" This will blacklist based on the very top most block that the surface uses.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by key terms, just enter the term alone such as \"sand\"\r\n"
		                    		+" and all biome surfaces that uses blocks with sand in its name will\r\n"
		                    		+" not be imported. After all, sand is coarse and rough and gets everywhere!\r\n"
		                    		+" \r\n"
		                    		+" To blacklist by mod ID, just enter the mod ID with an * on the end such as\r\n"
		                    		+" \"weird_biome_mod*\" and all biome surfaces from that mod will not be imported.\r\n"
		                    		+" \r\n"
		                    		+" To blacklist a block from being a surface, enter the mod ID, then :, and then the \r\n"
		                    		+" block's name. For example, \"minecraft:mycelium\" will prevent any surfaces that uses\r\n"
		                    		+" Mycelium blocks from being imported.\r\n"
		                    		+" \r\n"
		                    		+" Also, some biomes might add Air block as a surface block which will create pits in\r\n"
		                    		+" the surface that looks like it is missing the top layer of land. Add minecraft:air to \r\n"
		                    		+" this config to prevent these kinds of surfaces from being added.\r\n"
		                    		+" \r\n"
		                    		+" NOTE: You can blacklist multiple things at a time. Just separate\r\n"
		                    		+" each entry with a , (comma). Here's an example blacklisting all sand\r\n"
		                    		+" surfaces and vanilla Mushroom Biome's Mycelium surface: \r\n"
		                    		+" \"sand, minecraft:mycelium\"\r\n")
		                    .translation("world_blender.config.blacklist.blacklistedbiomesurfaces")
		                    .define("blacklistedBiomeSurfaces", "");


	            builder.pop();
	            
	            builder.push("Misc Options");
		            spawnEnderDragon = builder
				                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
				                    		+" If true, the Enderdragon will spawn at world origin in the\r\n"
				                    		+" World Blender dimension and can respawn if you put back the\r\n"
				                    		+" End Crystals on the podiums. Once killed, the podium's portal \r\n"
				                    		+" will take you to the End where you can battle the End's Enderdragon. \r\n"
				                    		+" \r\n"
				                    		+" And yes, you can respawn the EnderDragon by placing 4 End Crystals \r\n"
				                    		+" on the edges of the Bedrock Podium. \r\n"
				                    		+" \r\n"
				                    		+" If set to false, the Enderdragon will not spawn.\r\n"
				                    		+" NOTE: Once the Enderdragon is spawned, changing this to false"
				                    		+" will not despawn the Enderdragon. Also, this option will not\r\n"
				                    		+" work in the World Blender Worldtype due to how fight managers are \r\n"
				                    		+" set up. It will only work for the dimension. \r\n")
				                    .translation("world_blender.config.misc.spawnenderdragon")
				                    .define("spawnEnderDragon", false);
		            
		            vanillaCarversCanCarveMoreBlocks = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" If true, Vanilla Carvers (caves and ravines) can now carve out\r\n"
		                    		+" Netherrack, End Stone, and some modded blocks as well.\r\n"
		                    		+" \r\n"
		                    		+" If turned off, you might see Vanilla caves and stuff gets cutoff \r\n"
		                    		+" by a wall of End Stone, Netherrack, or modded blocks. \r\n")
		                    .translation("world_blender.config.misc.vanillaCarversCanCarveMoreBlocks")
		                    .define("vanillaCarversCanCarveMoreBlocks", true);
	            builder.pop();
	        }
	            		
	    } 
	    
	    public static void refreshServer()
	    {
	    	allowVanillaBiomeImport = SERVER.allowVanillaBiomeImport.get();
	    	allowModdedBiomeImport = SERVER.allowModdedBiomeImport.get();
	    	
	    	allowVanillaFeatures = SERVER.allowVanillaFeatures.get();
	    	allowVanillaStructures = SERVER.allowVanillaStructures.get();
	    	allowVanillaCarvers = SERVER.allowVanillaCarvers.get();
	    	allowVanillaSpawns = SERVER.allowVanillaSpawns.get();
	    	allowVanillaSurfaces = SERVER.allowVanillaSurfaces.get();
	    	disallowLaggyFeatures = SERVER.disallowLaggyFeatures.get();
	    	
	    	allowModdedFeatures = SERVER.allowModdedFeatures.get();
	    	allowModdedStructures = SERVER.allowModdedStructures.get();
	    	allowModdedCarvers = SERVER.allowModdedCarvers.get();
	    	allowModdedSpawns = SERVER.allowModdedSpawns.get();
	    	allowModdedSurfaces = SERVER.allowModdedSurfaces.get();
	    
	    	blanketBlacklist = SERVER.blanketBlacklist.get();
	    	blacklistedFeatures = SERVER.blacklistedFeatures.get();
	    	blacklistedStructures = SERVER.blacklistedStructures.get();
	    	blacklistedCarvers = SERVER.blacklistedCarvers.get();
	    	blacklistedSpawns = SERVER.blacklistedSpawns.get();
	    	blacklistedBiomeSurfaces = SERVER.blacklistedBiomeSurfaces.get();
	    	resourceLocationDump = SERVER.resourceLocationDump.get();
	    	
		    uniqueBlocksNeeded = SERVER.uniqueBlocksNeeded.get();
	    	activationItem = SERVER.activationItem.get();
	    	consumeChests = SERVER.consumeChests.get();

	    	spawnEnderDragon = SERVER.spawnEnderDragon.get();
	    	vanillaCarversCanCarveMoreBlocks = SERVER.vanillaCarversCanCarveMoreBlocks.get();
	    }
}
