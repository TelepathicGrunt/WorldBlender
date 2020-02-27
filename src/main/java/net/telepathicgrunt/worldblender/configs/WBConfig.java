package net.telepathicgrunt.worldblender.configs;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
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
	    
	    public static boolean allowModdedFeatures = true;
	    public static boolean allowModdedStructures = true;
	    public static boolean allowModdedCarvers = true;
	    public static boolean allowModdedSpawns = true;
	    public static boolean allowModdedSurfaces = true;

	    
	    public static class ServerConfig
	    {

		    public final BooleanValue allowVanillaBiomeImport;
		    public final BooleanValue allowModdedBiomeImport;
	    	
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
	    	
	    	allowModdedFeatures = SERVER.allowModdedFeatures.get();
	    	allowModdedStructures = SERVER.allowModdedStructures.get();
	    	allowModdedCarvers = SERVER.allowModdedCarvers.get();
	    	allowModdedSpawns = SERVER.allowModdedSpawns.get();
	    	allowModdedSurfaces = SERVER.allowModdedSurfaces.get();
	    }
}
