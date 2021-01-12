package com.telepathicgrunt.world_blender.configs;

import com.telepathicgrunt.world_blender.utils.ConfigHelper;
import com.telepathicgrunt.world_blender.utils.ConfigHelper.ConfigValueListener;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class WBBlendingConfigs {
    public static class WBConfigValues
    {
        public ConfigValueListener<Boolean> allowVanillaBiomeImport;
        public ConfigValueListener<Boolean> allowModdedBiomeImport;
        public ConfigValueListener<Boolean> disallowLaggyFeatures;
        public ConfigValueListener<Boolean> cleanSlateWBBiomesAtStartup;

        public ConfigValueListener<Boolean> allowVanillaFeatures;
        public ConfigValueListener<Boolean> allowVanillaStructures;
        public ConfigValueListener<Boolean> allowVanillaCarvers;
        public ConfigValueListener<Boolean> allowVanillaSpawns;
        public ConfigValueListener<Boolean> allowVanillaSurfaces;

        public ConfigValueListener<Boolean> allowModdedFeatures;
        public ConfigValueListener<Boolean> allowModdedStructures;
        public ConfigValueListener<Boolean> allowModdedCarvers;
        public ConfigValueListener<Boolean> allowModdedSpawns;
        public ConfigValueListener<Boolean> allowModdedSurfaces;

        public ConfigValueListener<String> blanketBlacklist;
        public ConfigValueListener<String> blacklistedFeatures;
        public ConfigValueListener<String> blacklistedStructures;
        public ConfigValueListener<String> blacklistedCarvers;
        public ConfigValueListener<String> blacklistedSpawns;
        public ConfigValueListener<String> blacklistedBiomeSurfaces;
        public ConfigValueListener<Boolean> resourceLocationDump;

        public WBConfigValues(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
        {

            cleanSlateWBBiomesAtStartup = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Use this if another mod is adding stuff to World Blender's biomes \r\n"
                            +" and World Blender's blacklisting config is not working. This option \r\n"
                            +" will wipe clear WB's biomes so they have absolutely nothing in it \r\n"
                            +" and then it will import everyone else's stuff based on it's blacklist.\r\n")
                    .translation("world_blender.config.cleanslatewbbiomesatstartup")
                    .define("cleanSlateWBBiomesAtStartup", true));

            builder.push("Optimization Options");

            disallowLaggyFeatures = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Will make vanilla bamboo, lava, and fire features and will try to \r\n"
                            +" make modded bamboo, lava, and fire features not spawn at all\r\n"
                            +" in order to help reduce lag in the world due to bamboo\r\n"
                            +" breaking like crazy or fire spreading rapidly.\r\n"
                            +" \r\n"
                            +" If all else fail, do /gamerule doFireTick false to reduce fire lag.\r\n")
                    .translation("world_blender.config.optimization.disallowlaggyvanillafeatures")
                    .define("disallowLaggyVanillaFeatures", true));

            builder.pop();

            builder.push("Vanilla Options");

            allowVanillaBiomeImport = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension can import anything from vanilla biomes.\r\n"
                            +" Note: If the other vanilla stuff options are set to true and you \r\n"
                            +" have the import from modded biome option set to true as well, then\r\n"
                            +" vanilla stuff can still get imported if a modded biome has them.\r\n")
                    .translation("world_blender.config.vanilla.allowvanillabiomeimport")
                    .define("allowVanillaBiomeImport", true));


            allowVanillaFeatures = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension imports features like trees, plants, ores, etc.\r\n")
                    .translation("world_blender.config.vanilla.allowvanillafeatures")
                    .define("allowVanillaFeatures", true));

            allowVanillaStructures = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension imports structures like temples, villages, etc.\r\n")
                    .translation("world_blender.config.vanilla.allowvanillastructures")
                    .define("allowVanillaStructures", true));

            allowVanillaCarvers = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension imports carvers like caves, ravines, etc.\r\n")
                    .translation("world_blender.config.vanilla.allowvanillacarvers")
                    .define("allowVanillaCarvers", true));

            allowVanillaSpawns = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension imports natural mob spawns like zombies, cows, etc.\r\n")
                    .translation("world_blender.config.vanilla.allowvanillaspawns")
                    .define("allowVanillaSpawns", true));

            allowVanillaSurfaces = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension imports surfaces like desert's sand, giant tree taiga's podzol, etc.\r\n")
                    .translation("world_blender.config.vanilla.allowvanillasurfaces")
                    .define("allowVanillaSurfaces", true));

            builder.pop();

            builder.push("Modded Options");

            allowModdedBiomeImport = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension can import anything from modded biomes.\r\n"
                            +" Note: If the other vanilla stuff options are set to true and you\r\n"
                            +" this option set to true as well, then vanilla stuff can still\r\n"
                            +" get imported if a modded biome has vanilla stuff in it.\r\n")
                    .translation("world_blender.config.modded.allowmoddedbiomeimport")
                    .define("allowModdedBiomeImport", true));

            allowModdedFeatures = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension imports features like trees, plants, ores, etc.\r\n")
                    .translation("world_blender.config.modded.allowmoddedfeatures")
                    .define("allowModdedFeatures", true));

            allowModdedStructures = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension imports structures like temples, villages, etc.\r\n")
                    .translation("world_blender.config.modded.allowmoddedstructures")
                    .define("allowModdedStructures", true));

            allowModdedCarvers = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension imports carvers like caves, ravines, etc.\r\n")
                    .translation("world_blender.config.modded.allowmoddedcarvers")
                    .define("allowModdedCarvers", true));

            allowModdedSpawns = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension imports natural mob spawns like zombies, cows, etc.\r\n")
                    .translation("world_blender.config.modded.allowmoddedspawns")
                    .define("allowModdedSpawns", true));

            allowModdedSurfaces = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Decides if the dimension imports surfaces like desert's sand, giant tree taiga's podzol, etc.\r\n")
                    .translation("world_blender.config.modded.allowmoddedsurfaces")
                    .define("allowModdedSurfaces", true));

            builder.pop();

            builder.push("Resource Location Dump Option");

            resourceLocationDump = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            +" Dumps all resource locations (IDs) for all mods into a new file\r\n"
                            +" at config/world_blender-identifier_dump.txt and can be found by\r\n"
                            +" looking in the config folder in Minecraft's folder. The file is made\r\n"
                            +" if you set this option to true and started a world.\r\n"
                            +"\r\n"
                            +" Use this option to look up the resource location or name of any registered features,\r\n"
                            +" biomes, blocks, carvers, structures, or entities that you want to blacklist.\r\n")
                    .translation("world_blender.config.resourcelocation.resourcelocationdump")
                    .define("resourceLocationDump", false));

            builder.pop();

            builder.push("Blacklist Options");


            blanketBlacklist = subscriber.subscribe(builder
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
                    .define("blanketBlacklist", "ultra_amplified_dimension*"));


            blacklistedFeatures = subscriber.subscribe(builder
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
                    .define("blacklistedFeatures", "minecraft:basalt_blobs,minecraft:blackstone_blobs"));


            blacklistedStructures = subscriber.subscribe(builder
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
                    .define("blacklistedStructures", ""));


            blacklistedCarvers = subscriber.subscribe(builder
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
                    .define("blacklistedCarvers", ""));


            blacklistedSpawns = subscriber.subscribe(builder
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
                            +" NOTE: You can blacklist multiple things at a time. Just separate\r\n"
                            +" each entry with a , (comma). Here's an example blacklisting all zombies\r\n"
                            +" and vanilla's ghasts: \r\n"
                            +" \"zombie, minecraft:ghast\"\r\n")
                    .translation("world_blender.config.blacklist.blacklistedspawns")
                    .define("blacklistedSpawns", ""));


            blacklistedBiomeSurfaces = subscriber.subscribe(builder
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
                    .define("blacklistedBiomeSurfaces", ""));


            builder.pop();
        }
    }
}
