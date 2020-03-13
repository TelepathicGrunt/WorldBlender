    Made for Minecraft v.1.15.2
    Created by TelepathicGrunt

Welcome to the Github! If you are looking for the most recent stable version, then checkout the master branch! Branches dedicated to the latest version of Minecraft may be unstable or broken as I test and experiment so stick with the master branch instead.

------------------------------------------------
       | World Blender changelog |
           
           
    (V.1.2.3 Changes) (1.15.2 Minecraft)
           
    Config: 
·Changed config entry of disableLaggyVanillaFeatures to disableLaggyFeatures as it will now attempt to detect other mod's bamboo, sugar cane, lava, and fire based features and prevent their generation.  
·TerraForged and Dimensional Dungeons is now able to be filtered by more configs that should've be able to filter them.
          
    Misc: 
·Removed some log spam caused by my mod with Dimensional Dungeons.
·Optimized a bit in backend to use less memory.
  
           
    (V.1.2.2 Changes) (1.15.2 Minecraft)
                  
	Importing Features: 
·Fixed a crash with importing from a certain biome with a certain feature.
·Fixed some vanilla trees being sneaky and still spawning when turning off vanilla features config.  
     
    Config: 
·End Spikes and End Podium can only be turned off now by putting their resource location into the feature blacklist due to them being needed for Enderdragon fights. Basically, this is to reduce the chance of people accidentally turning them off when turning off other features and if they really don't want the spike or podium, they have to explicitly tell the game to not spawn it.
       
       
    (V.1.2.1 Changes) (1.15.2 Minecraft)
               
  	Block: 
·Fixed bug where portal block that are player-made cannot be removed by crouch right-clicking with empty hand.
     
    Config: 
·End Spikes (Obsidian pillars) will now not be removed when turning off vanilla features config as it is needed for the Enderdragon and is a good way to visually mark world origin.
      
    Misc: 
·Removed the log spam caused by World Blender when Dimension Dungeons is generating in the dimension.

     
    (V.1.2.0 Changes) (1.15.2 Minecraft)
               
	Importing Features: 
·Added dedicated support for DimDungeon mod!
·Added dedicated support for TerraForged mod!
·Fixed bug where Ocean Monuments would never spawn.
·Fixed bug where turning off config for features could cause structures to not spawn.

	Dimension: 
·Added option to spawn Enderdragon at world origin in this dimension! (Set to false by default in configs as it is highly experimental)
 
	Config: 
·Added the ability to blacklist mods, biomes, structures, features, carvers, entities, and surfaces from being import into World Blender.
·Added option to print out the resource location (IDs) into a file called resourceLocationDump.txt so you can target certain features or biomes to blacklist easier.
·Added option to spawn Enderdragon or not at world origin. (false by default)

	Teleportation: 
·Made World Blender Portal slightly less intense on the eyes add just a tad less laggy.
·World Blender Portal now has the Dragon Immune, Impermeable, Portals, and Wither Immune tags.
·Slightly reduced collision box of World Blender Portal so you have to go more into it to teleport rather than graze the surface of the block.
       
       
    (V.1.1.0 Changes) (1.15.2 Minecraft)
           
	Importing Features: 
·Fixed bug where some modded features are seen as vanilla features by mistake.
              
	Importing Structures: 
·Fixed bug where importing structures also need importing features turned on. Now that option works without needing feature option also set to true.
    
	Teleportation: 
·Added World Blender Portal to teleport between Overworld and World Blender dimension. You make the portal by placing 8 chests in a 2x2 area and then fill all of their slots with an unique block (stacks of blocks will not count as extra and items without block form will be ignored). Then crouch and right click the chests while holding a Nether Star to create the portal to this overpowered dimension! Crouch right click the portal block without holding any item to remove the portal for good. 
      
	Dimension: 
·Added World Blender Portal Altar at world origin in the dimension where the World Blender Portal block cannot be removed by crouch right clicking.
       
	Worldtype: 
·Created worldtype as an alternative for the dimension. For server owners, add "use-modded-worldtype=world-blender" as a new entry in your server.properties file to use this worldtype.

	Config: 
·Added config to changed the amount of unique items needed to create the portal.
·Added config to changed what item is needed to be held to create the portal.
·Added config to turn off vanilla bamboo, fire, and lava based features to help reduce lag.
           
       
    (V.1.0.0 Changes) (1.15.2 Minecraft)
    
	Major: 
·FIRST RELEASE OF THIS MOD
