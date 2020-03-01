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

	    public static boolean allowVanillaFeatures = true;
	    public static boolean allowVanillaStructures = true;
	    public static boolean allowVanillaCarvers = true;
	    public static boolean allowVanillaSpawns = true;
	    public static boolean allowVanillaSurfaces = true;
	    public static boolean disallowLaggyVanillaFeatures = false;
	    
	    public static boolean allowModdedFeatures = true;
	    public static boolean allowModdedStructures = true;
	    public static boolean allowModdedCarvers = true;
	    public static boolean allowModdedSpawns = true;
	    public static boolean allowModdedSurfaces = true;

	    public static int uniqueBlocksNeeded = 216;
	    public static String activationItem = "minecraft:nether_star";
	    public static boolean consumeChests = true;

	    public static boolean spawnEnderDragon = true;
	    
	    public static class ServerConfig
	    {

		    public final BooleanValue allowVanillaBiomeImport;
		    public final BooleanValue allowModdedBiomeImport;
	    	
		    public final BooleanValue allowVanillaFeatures;
		    public final BooleanValue allowVanillaStructures;
		    public final BooleanValue allowVanillaCarvers;
		    public final BooleanValue allowVanillaSpawns;
		    public final BooleanValue allowVanillaSurfaces;
		    public final BooleanValue disallowLaggyVanillaFeatures;
		    
		    public final BooleanValue allowModdedFeatures;
		    public final BooleanValue allowModdedStructures;
		    public final BooleanValue allowModdedCarvers;
		    public final BooleanValue allowModdedSpawns;
		    public final BooleanValue allowModdedSurfaces;

		    public final IntValue uniqueBlocksNeeded;
		    public final ConfigValue<String> activationItem;
		    public final BooleanValue consumeChests;
		    
		    public final BooleanValue spawnEnderDragon;
		    
	        ServerConfig(ForgeConfigSpec.Builder builder) 
	        {

	            
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
	
		            disallowLaggyVanillaFeatures = builder
		                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
		                    		+" Will make vanilla bamboo, lava, and fire features not spawn\r\n"
		                    		+" in order to help reduce lag in the world due to bamboo\r\n"
		                    		+" breaking like crazy or fire spreading rapidly.\r\n")
		                    .translation("world_blender.config.vanilla.disallowlaggyvanillafeatures")
		                    .define("disallowLaggyVanillaFeatures", true);
	            
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
	            builder.push("Misc Options");

	            spawnEnderDragon = builder
			                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
			                    		+" If true, the Enderdragon will spawn at world origin in the\r\n"
			                    		+" World Blender dimension and can respawn if you put back the\r\n"
			                    		+" End Crystals on the podiums. Once killed, the podium's portal \r\n"
			                    		+" will take you to the End where you can battle the End's Enderdragon \r\n"
			                    		+" \r\n"
			                    		+" If set to false, the Enderdragon will not spawn.\r\n."
			                    		+" NOTE: Once the Enderdragon is spawned, changing this to false"
			                    		+" will not despawn the Enderdragon.\r\n")
			                    .translation("world_blender.config.portal.spawnenderdragon")
			                    .define("spawnEnderDragon", true);
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
	    	disallowLaggyVanillaFeatures = SERVER.disallowLaggyVanillaFeatures.get();
	    	
	    	allowModdedFeatures = SERVER.allowModdedFeatures.get();
	    	allowModdedStructures = SERVER.allowModdedStructures.get();
	    	allowModdedCarvers = SERVER.allowModdedCarvers.get();
	    	allowModdedSpawns = SERVER.allowModdedSpawns.get();
	    	allowModdedSurfaces = SERVER.allowModdedSurfaces.get();
	    	
		    uniqueBlocksNeeded = SERVER.uniqueBlocksNeeded.get();
	    	activationItem = SERVER.activationItem.get();
	    	consumeChests = SERVER.consumeChests.get();

	    	spawnEnderDragon = SERVER.spawnEnderDragon.get();
	    }
}
