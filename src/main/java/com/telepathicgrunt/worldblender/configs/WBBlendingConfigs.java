package com.telepathicgrunt.worldblender.configs;

import net.minecraftforge.common.ForgeConfigSpec;

public class WBBlendingConfigs {
    public static final ForgeConfigSpec GENERAL_SPEC;
    
    public static ForgeConfigSpec.BooleanValue allowVanillaBiomeImport;
    public static ForgeConfigSpec.BooleanValue allowModdedBiomeImport;
    public static ForgeConfigSpec.BooleanValue disallowFireLavaBasaltFeatures;

    public static ForgeConfigSpec.BooleanValue allowVanillaFeatures;
    public static ForgeConfigSpec.BooleanValue allowVanillaStructures;
    public static ForgeConfigSpec.BooleanValue allowVanillaCarvers;
    public static ForgeConfigSpec.BooleanValue allowVanillaSpawns;
    public static ForgeConfigSpec.BooleanValue allowVanillaSurfaces;

    public static ForgeConfigSpec.BooleanValue allowModdedFeatures;
    public static ForgeConfigSpec.BooleanValue allowModdedStructures;
    public static ForgeConfigSpec.BooleanValue allowModdedCarvers;
    public static ForgeConfigSpec.BooleanValue allowModdedSpawns;
    public static ForgeConfigSpec.BooleanValue allowModdedSurfaces;

    public static ForgeConfigSpec.ConfigValue<String> blanketBlacklist;
    public static ForgeConfigSpec.ConfigValue<String> blacklistedFeatures;
    public static ForgeConfigSpec.ConfigValue<String> blacklistedStructures;
    public static ForgeConfigSpec.ConfigValue<String> blacklistedCarvers;
    public static ForgeConfigSpec.ConfigValue<String> blacklistedSpawns;
    public static ForgeConfigSpec.ConfigValue<String> blacklistedBiomeSurfaces;
    public static ForgeConfigSpec.BooleanValue resourceLocationDump;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Optimization Options");

        disallowFireLavaBasaltFeatures = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Will prevent importing vanilla lava/fire/basalt features and ",
                        " will attempt to make modded lava/fire features not spawn at all",
                        " in order to help reduce lag in the world due to fire spreading rapidly.",
                        " Also, basalt is ugly as it overwhelms the world.",
                        " If all else fail, do /gamerule doFireTick false to reduce fire lag.\n")
                .translation("world_blender.config.optimization.disallowfirelavafeatures")
                .define("disallowFireLavaFeatures", true);

        builder.pop();

        builder.push("Vanilla Options");

        allowVanillaBiomeImport = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension can import anything from vanilla biomes.",
                        " Note: If the other vanilla stuff options are set to true and you ",
                        " have the import from modded biome option set to true as well, then",
                        " vanilla stuff can still get imported if a modded biome has them.\n")
                .translation("world_blender.config.vanilla.allowvanillabiomeimport")
                .define("allowVanillaBiomeImport", true);


        allowVanillaFeatures = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension imports features like trees, plants, ores, etc.\n")
                .translation("world_blender.config.vanilla.allowvanillafeatures")
                .define("allowVanillaFeatures", true);

        allowVanillaStructures = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension imports structures like temples, villages, etc.\n")
                .translation("world_blender.config.vanilla.allowvanillastructures")
                .define("allowVanillaStructures", true);

        allowVanillaCarvers = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension imports carvers like caves, ravines, etc.\n")
                .translation("world_blender.config.vanilla.allowvanillacarvers")
                .define("allowVanillaCarvers", true);

        allowVanillaSpawns = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension imports natural mob spawns like zombies, cows, etc.\n")
                .translation("world_blender.config.vanilla.allowvanillaspawns")
                .define("allowVanillaSpawns", true);

        allowVanillaSurfaces = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension imports surfaces like desert's sand, giant tree taiga's podzol, etc.\n")
                .translation("world_blender.config.vanilla.allowvanillasurfaces")
                .define("allowVanillaSurfaces", true);

        builder.pop();

        builder.push("Modded Options");

        allowModdedBiomeImport = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension can import anything from modded biomes.",
                        " Note: If the other vanilla stuff options are set to true and you",
                        " this option set to true as well, then vanilla stuff can still",
                        " get imported if a modded biome has vanilla stuff in it.\n")
                .translation("world_blender.config.modded.allowmoddedbiomeimport")
                .define("allowModdedBiomeImport", true);

        allowModdedFeatures = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension imports features like trees, plants, ores, etc.\n")
                .translation("world_blender.config.modded.allowmoddedfeatures")
                .define("allowModdedFeatures", true);

        allowModdedStructures = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension imports structures like temples, villages, etc.\n")
                .translation("world_blender.config.modded.allowmoddedstructures")
                .define("allowModdedStructures", true);

        allowModdedCarvers = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension imports carvers like caves, ravines, etc.\n")
                .translation("world_blender.config.modded.allowmoddedcarvers")
                .define("allowModdedCarvers", true);

        allowModdedSpawns = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension imports natural mob spawns like zombies, cows, etc.\n")
                .translation("world_blender.config.modded.allowmoddedspawns")
                .define("allowModdedSpawns", true);

        allowModdedSurfaces = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Decides if the dimension imports surfaces like desert's sand, giant tree taiga's podzol, etc.\n")
                .translation("world_blender.config.modded.allowmoddedsurfaces")
                .define("allowModdedSurfaces", true);

        builder.pop();

        builder.push("Resource Location Dump Option");

        resourceLocationDump = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Dumps all resource locations (IDs) for all mods into a new file",
                        " at config/world_blender-identifier_dump.txt and can be found by",
                        " looking in the config folder in Minecraft's folder. The file is made",
                        " if you set this option to true and started a world.",
                        "",
                        " Use this option to look up the resource location or name of any registered features,",
                        " biomes, blocks, carvers, structures, or entities that you want to blacklist.\n")
                .translation("world_blender.config.resourcelocation.resourcelocationdump")
                .define("resourceLocationDump", false);

        builder.pop();

        builder.push("Blacklist Options");


        blanketBlacklist = builder
                .comment(" \n-----------------------------------------------------\n",
                        " This option can let you blacklist entire biomes or mods to",
                        " prevent any importing of any kind from them. You can also use",
                        " terms to ban any biome that contains the terms too.",
                        " ",
                        " To blacklist a mod's biome, you would enter the resourcelocation",
                        " for the biome. That means you need to enter the mod's ID first,",
                        " then put a : (semicolon), and then the ID of the biome.",
                        " For example, to blacklist just vanilla's Ice Spike biome, you",
                        " would put in minecraft:ice_spike and nothing will be imported",
                        " from that specific biome.",
                        " ",
                        " If you want to blacklist an entire mod itself so no importing",
                        " will happen for any of its biome, just enter the mod's ID and then",
                        " put an * at the end.",
                        " As default, Ultra Amplified Dimension is blacklisted because",
                        " its features are not setup for normal worldgen and will completely",
                        " destroy this dimension.",
                        " ",
                        " To blacklist by key terms, just enter the term alone such as \"ocean\"",
                        " and all biomes with ocean in their name will not be imported.",
                        " This uses Regex so you could do \"cold\\w+plateau\" to blacklist any name",
                        " that starts with 'cold' and ends in 'plateau'.",
                        " ",
                        " To blacklist by biome categories, just enter the category with a # in front like \"#DESERT\"",
                        " and all biomes that are desert category will not be imported. Do @ to blacklist based on Forge's Biome Dictionary.",
                        " The categories you can use are: DESERT, FOREST, SWAMP, ICY, TAIGA, EXTREME_HILLS,",
                        " JUNGLE, MESA, PLAINS, SAVANNA, BEACH, RIVER, OCEAN, MUSHROOM, THE_END, NETHER, NONE",
                        " ",
                        " NOTE: You can blacklist multiple things at a time. Just separate",
                        " each entry with a , (comma). Here's an example blacklisting a mod,",
                        " a vanilla biome, and all mushroom category biomes at the same time: ",
                        " \"ultra_amplified_dimension*, minecraft:ice_spike, #MUSHROOM\"\n")
                .translation("world_blender.config.blacklist.blanketblacklist")
                .define("blanketBlacklist", "ultra_amplified_dimension*");


        blacklistedFeatures = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Blacklist features by key terms, mod ID, or their resourcelocation",
                        " ",
                        " To blacklist by key terms, just enter the term alone such as \"tree\"",
                        " and all features with tree in their name will not be imported.",
                        " ",
                        " To blacklist by mod ID, just enter the mod ID with an * on the end such as",
                        " \"ultra_amplified_dimension*\" and all features from that mod will not be imported.",
                        " ",
                        " To blacklist a single feature, enter the mod ID, then :, and then the ",
                        " feature's name. For example, \"minecraft:icebergs\" will prevent vanilla's",
                        " icebergs from being imported but allow other mod's icebergs to be imported.",
                        " This uses Regex so you could do \"tall\\w+tree\" to blacklist any name",
                        " that starts with 'fire' and ends in 'tree'.",
                        " ",
                        " NOTE: You can blacklist multiple things at a time. Just separate",
                        " each entry with a , (comma). Here's an example blacklisting all trees",
                        " and vanilla's icebergs: ",
                        " \"tree, minecraft:iceberg\"\n")
                .translation("world_blender.config.blacklist.blacklistedfeatures")
                .define("blacklistedFeatures",
                        "betterendforge:purple_polypore_dense, " +
                        "betterendforge:twisted_umbrella_moss, " +
                        "betterendforge:umbrella_moss, " +
                        "betterendforge:sulphuric_lake, " +
                        "betterendforge:bubble_coral, " +
                        "betterendforge:bulb_moss, " +
                        "betterendforge:charnia_red, " +
                        "betterendforge:creeping_moss, " +
                        "betterendforge:end_lily, " +
                        "betterendforge:end_lake_rare, " +
                        "betterendforge:end_lake_normal, " +
                        "betterendforge:end_lake, " +
                        "betterendforge:desert_lake, " +
                        "minecraft:basalt_blobs, " +
                        "minecraft:blackstone_blobs, " +
                        "aoa3:lborean_barrier_roof");


        blacklistedStructures = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Blacklist structures by key terms, mod ID, or their resourcelocation",
                        " ",
                        " To blacklist by key terms, just enter the term alone such as \"temple\"",
                        " and all features with temple in their name will not be imported.",
                        " ",
                        " To blacklist by mod ID, just enter the mod ID with an * on the end such as",
                        " \"ultra_amplified_dimension*\" and all structures from that mod will not be imported.",
                        " ",
                        " To blacklist a single feature, enter the mod ID, then :, and then the ",
                        " feature's name. For example, \"minecraft:igloo\" will prevent vanilla's",
                        " igloos from being imported but allow other mod's igloos to be imported.",
                        " This uses Regex so you could do \"advanced\\w+village\" to blacklist any name",
                        " that starts with 'advanced' and ends in 'village'.",
                        " ",
                        " NOTE: You can blacklist multiple things at a time. Just separate",
                        " each entry with a , (comma). Here's an example blacklisting all temples",
                        " and vanilla's igloos: ",
                        " \"temple, minecraft:igloo\"\n")
                .translation("world_blender.config.blacklist.blacklistedstructures")
                .define("blacklistedStructures",
                        "betterendforge:painted_mountain_structure, " +
                        "betterendforge:mountain_structure, " +
                        "betterendforge:megalake_structure, " +
                        "betterendforge:megalake_small_structure");


        blacklistedCarvers = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Blacklist carvers by key terms, mod ID, or their resourcelocation",
                        " ",
                        " To blacklist by key terms, just enter the term alone such as \"cave\"",
                        " and all carvers with cave in their name will not be imported if they",
                        " are registered with a name. Not many mods register their carvers sadly.",
                        " ",
                        " To blacklist by mod ID, just enter the mod ID with an * on the end such as",
                        " \"ultra_amplified_dimension*\" and all carvers from that mod will not be imported.",
                        " ",
                        " To blacklist a single feature, enter the mod ID, then :, and then the ",
                        " feature's name. For example, \"minecraft:underwater_canyon\" will prevent ",
                        " vanilla's underwater canyons (ravines) from being imported. For underwater",
                        " caves, use \"minecraft:underwater_cave\" to stop them from being imported.",
                        " This uses Regex so you could do \"hot\\w+cavern\" to blacklist any name",
                        " that starts with 'hot' and ends in 'cavern'.",
                        " ",
                        " NOTE: You can blacklist multiple things at a time. Just separate",
                        " each entry with a , (comma). Here's an example blacklisting all caves",
                        " and vanilla's underwater canyons: ",
                        " \"cave, minecraft:underwater_canyon\"\n")
                .translation("world_blender.config.blacklist.blacklistedcarvers")
                .define("blacklistedCarvers", "");


        blacklistedSpawns = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Blacklist natural spawning mobs by key terms,",
                        " mod ID, or their resourcelocation",
                        " ",
                        " To blacklist by key terms, just enter the term alone such as \"zombie\"",
                        " and all mobs with zombie in their name will not be imported.",
                        " ",
                        " To blacklist by mod ID, just enter the mod ID with an * on the end such as",
                        " \"super_duper_mob_mod*\" and all mobs from that mod will not be imported.",
                        " This uses Regex so you could do \"turbo\\w+bat\" to blacklist any name",
                        " that starts with 'turbo' and ends in 'bat'.",
                        " ",
                        " To blacklist a single mob, enter the mod ID, then :, and then the ",
                        " mob's name. For example, \"minecraft:ghast\" will prevent ",
                        " vanilla's ghast from being imported.",
                        " ",
                        " NOTE: You can blacklist multiple things at a time. Just separate",
                        " each entry with a , (comma). Here's an example blacklisting all zombies",
                        " and vanilla's ghasts: ",
                        " \"zombie, minecraft:ghast\"\n")
                .translation("world_blender.config.blacklist.blacklistedspawns")
                .define("blacklistedSpawns", "");


        blacklistedBiomeSurfaces = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Blacklist surfaces by key terms, mod ID, or by block's resourcelocation",
                        " This will blacklist based on the very top most block that the surface uses.",
                        " ",
                        " To blacklist by key terms, just enter the term alone such as \"sand\"",
                        " and all biome surfaces that uses blocks with sand in its name will",
                        " not be imported. After all, sand is coarse and rough and gets everywhere!",
                        " This uses Regex so you could do \"raw\\w+ore\" to blacklist any name",
                        " that starts with 'raw' and ends in 'ore'.",
                        " ",
                        " To blacklist by mod ID, just enter the mod ID with an * on the end such as",
                        " \"weird_biome_mod*\" and all biome surfaces from that mod will not be imported.",
                        " ",
                        " To blacklist a block from being a surface, enter the mod ID, then :, and then the ",
                        " block's name. For example, \"minecraft:mycelium\" will prevent any surfaces that uses",
                        " Mycelium blocks from being imported.",
                        " ",
                        " Also, some biomes might add Air block as a surface block which will create pits in",
                        " the surface that looks like it is missing the top layer of land. Add minecraft:air to ",
                        " this config to prevent these kinds of surfaces from being added.",
                        " ",
                        " NOTE: You can blacklist multiple things at a time. Just separate",
                        " each entry with a , (comma). Here's an example blacklisting all sand",
                        " surfaces and vanilla Mushroom Biome's Mycelium surface: ",
                        " \"sand, minecraft:mycelium\"\n")
                .translation("world_blender.config.blacklist.blacklistedbiomesurfaces")
                .define("blacklistedBiomeSurfaces", "");


        builder.pop();
    }
}
