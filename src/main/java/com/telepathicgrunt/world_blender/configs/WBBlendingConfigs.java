package com.telepathicgrunt.world_blender.configs;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "blending")
public class WBBlendingConfigs implements ConfigData {

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " This option can let you blacklist entire biomes or mods to\r\n"
            +" prevent any importing of any kind from them. You can also use\r\n"
            +" terms to ban any biome that contains the terms too.\r\n"
            +" \r\n"
            +" To blacklist a mod's biome, you would enter the identifier\r\n"
            +" for the biome. That means you need to enter the mod's ID first,\r\n"
            +" then put a : (semicolon), and then the ID of the biome.\r\n"
            +" For example, to blacklist just vanilla's Ice Spike biome, you\r\n"
            +" would put in minecraft:ice_spike and nothing will be imported\r\n"
            +" from that specific biome.\r\n"
            +" \r\n"
            +" If you want to blacklist an entire mod itself so no importing\r\n"
            +" will happen for any of its biome, just enter the mod's ID and thenr\n"
            +" put an * at the end.r\n"
            +" \r\n"
            +" To blacklist by key terms, just enter the term alone such as \"ocean\"\r\n"
            +" and all biomes with ocean in their name will not be imported.\r\n"
            +" \r\n"
            +" NOTE: You can blacklist multiple things at a time. Just separate\r\n"
            +" each entry with a , (comma). Here's an example blacklisting a mod\r\n"
            +" and a vanilla biome at the same time: \r\n"
            +" \"ultra_amplified_dimension*, minecraft:jungle_edge\"\r\n")
    public String blanketBlacklist = "ultra_amplified_dimension*";


    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Blacklist features by key terms, mod ID, or their identifier\r\n"
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
    public String blacklistedFeatures = "minecraft:basalt_blobs, minecraft:blackstone_blobs";


    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Blacklist structures by key terms, mod ID, or their identifier\r\n"
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
    public String blacklistedStructures = "";


    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Blacklist carvers by key terms, mod ID, or their identifier\r\n"
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
    public String blacklistedCarvers = "";


    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Blacklist natural spawning mobs by key terms,\r\n"
            +" mod ID, or their identifier\r\n"
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
            +" NOTE: You can blacklist multiple things at a time. Just separate\r\n"
            +" each entry with a , (comma). Here's an example blacklisting all zombies\r\n"
            +" and vanilla's ghasts: \r\n"
            +" \"zombie, minecraft:ghast\"\r\n")
    public String blacklistedSpawns = "";



    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Blacklist surfaces by key terms, mod ID, or by block's identifier\r\n"
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
    public String blacklistedBiomeSurfaces = "";




    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension can import anything from modded biomes.\r\n"
            +" Note: If the other vanilla stuff options are set to true and you\r\n"
            +" this option set to true as well, then vanilla stuff can still\r\n"
            +" get imported if a modded biome has vanilla stuff in it.\r\n")
    public boolean allowModdedBiomeImport = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension imports features like trees, plants, ores, etc.\r\n")
    public boolean allowModdedFeatures = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension imports structures like temples, villages, etc.\r\n")
    public boolean allowModdedStructures = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension imports carvers like caves, ravines, etc.\r\n")
    public boolean allowModdedCarvers = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension imports natural mob spawns like zombies, cows, etc.\r\n")
    public boolean allowModdedSpawns = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension imports surfaces like desert's sand, giant tree taiga's podzol, etc.\r\n")
    public boolean allowModdedSurfaces = true;




    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension can import anything from vanilla biomes.\r\n"
            +" Note: If the other vanilla stuff options are set to true and you \r\n"
            +" have the import from modded biome option set to true as well, then\r\n"
            +" vanilla stuff can still get imported if a modded biome has them.\r\n")
    public boolean allowVanillaBiomeImport = true;


    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension imports features like trees, plants, ores, etc.\r\n")
    public boolean allowVanillaFeatures = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension imports structures like temples, villages, etc.\r\n")
    public boolean allowVanillaStructures = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension imports carvers like caves, ravines, etc.\r\n")
    public boolean allowVanillaCarvers = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension imports natural mob spawns like zombies, cows, etc.\r\n")
    public boolean allowVanillaSpawns = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Decides if the dimension imports surfaces like desert's sand, giant tree taiga's podzol, etc.\r\n")
    public boolean allowVanillaSurfaces = true;




    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Will make vanilla bamboo, lava, and fire features and will try to \r\n"
            +" make modded bamboo, lava, and fire features not spawn at all\r\n"
            +" in order to help reduce lag in the world due to bamboo\r\n"
            +" breaking like crazy or fire spreading rapidly.\r\n"
            +" \r\n"
            +" If all else fail, do /gamerule doFireTick false to reduce fire lag.\r\n")
    public boolean disallowLaggyFeatures = true;


    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Dumps all resource locations (IDs) for all mods into a new file\r\n"
            +" called identifierDump.txt and can be found by looking below\r\n"
            +" the saves and mods folder in Minecraft's folder. The file is made if"
            +" you set this option to true and then run modded Minecraft until you"
            +" reach the title menu.\r\n"
            +"\r\n"
            +" Use this option to look up the resource location or name of features,"
            +" biomes, blocks, carvers, structures, or entities that you want to blacklist.\r\n")
    public boolean identifierDump = false;
}
