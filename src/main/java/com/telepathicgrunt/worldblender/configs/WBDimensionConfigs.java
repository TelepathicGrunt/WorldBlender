package com.telepathicgrunt.worldblender.configs;

import net.minecraftforge.common.ForgeConfigSpec;


public class WBDimensionConfigs{
    public static final ForgeConfigSpec GENERAL_SPEC;

    public static ForgeConfigSpec.DoubleValue surfaceScale;
    public static ForgeConfigSpec.BooleanValue spawnEnderDragon;
    public static ForgeConfigSpec.BooleanValue carversCanCarveMoreBlocks;

    public static ForgeConfigSpec.BooleanValue preventFallingBlocks;
    public static ForgeConfigSpec.BooleanValue containFloatingLiquids;
    public static ForgeConfigSpec.BooleanValue preventLavaTouchingWater;

    public static ForgeConfigSpec.BooleanValue removeWorldBottomStructures;
    public static ForgeConfigSpec.BooleanValue removeStructurePillars;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {

        builder.push("Misc Options");

        removeWorldBottomStructures = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Attempts to remove any nbt piece or structure being place at the bottom of the world.",
                        " Best for floating island World Blender terrain\n")
                .translation("world_blender.config.misc.removeworldbottomstructures")
                .define("removeWorldBottomStructures", false);

        removeStructurePillars = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Attempts to remove the pillars from nether fortress and desert temples and the likes.",
                        " Best for floating island World Blender terrain\n")
                .translation("world_blender.config.misc.removestructurepillars")
                .define("removeStructurePillars", false);

        surfaceScale = builder
                .comment(" \n-----------------------------------------------------\n",
                        " The size of the different kinds of surfaces. Higher numbers means",
                        " each surface will be larger but might make some surfaces harder to"
                        + " find. Lower numbers means the surfaces are smaller but could become"
                        + " too chaotic or small for some features to spawn on.\n")
                .translation("world_blender.config.misc.surfacescale")
                .defineInRange("surfaceScale", 240D, 1D, 100000D);

        spawnEnderDragon = builder
                .comment(" \n-----------------------------------------------------\n",
                        " If true, the Enderdragon will spawn at world origin in the",
                        " World Blender dimension and can respawn if you put back the",
                        " End Crystals on the podiums. Once killed, the podium's portal ",
                        " will take you to the End where you can battle the End's Enderdragon. ",
                        " ",
                        " And yes, you can respawn the EnderDragon by placing 4 End Crystals ",
                        " on the edges of the Bedrock Podium. ",
                        " ",
                        " If set to false, the Enderdragon will not spawn.",
                        " NOTE: Once the Enderdragon is spawned, changing this to false"
                        + " will not despawn the Enderdragon. Also, this option will not",
                        " work in the World Blender Worldtype due to how fight managers are ",
                        " set up. It will only work for the dimension. \n")
                .translation("world_blender.config.misc.spawnenderdragon")
                .define("spawnEnderDragon", true);

        carversCanCarveMoreBlocks = builder
                .comment(" \n-----------------------------------------------------\n",
                        " If true, carvers (mainly vanilla caves and ravines) can now carve",
                        " out Netherrack, End Stone, and some modded blocks as well.",
                        " ",
                        " If turned off, you might see Vanilla caves and stuff gets cutoff ",
                        " by a wall of End Stone, Netherrack, or modded blocks. \n")
                .translation("world_blender.config.misc.carversCanCarveMoreBlocks")
                .define("carversCanCarveMoreBlocks", true);


        builder.pop();

        builder.push("Optimization Options");

        preventFallingBlocks = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Will try its best to place Terracotta blocks under all floating",
                        " fallable blocks to prevent lag when the blocks begins to fall.\n")
                .translation("world_blender.config.optimization.preventfallingblocks")
                .define("preventFallingBlocks", true);

        containFloatingLiquids = builder
                .comment(" \n-----------------------------------------------------\n",
                        " This will also place Terracotta next to fluids to try and prevent.",
                        " them from floating and then flowing downward like crazy.",
                        " ",
                        " It isn't perfect but it does do mostly a good job with how",
                        " messy and chaotic having all features and carvers together is.\n")
                .translation("world_blender.config.optimization.containfloatingliquids")
                .define("containFloatingLiquids", true);

        preventLavaTouchingWater = builder
                .comment(" \n-----------------------------------------------------\n",
                        " Will place Obsidian to separate lava tagged fluids ",
                        " from water tagged fluids underground.\n")
                .translation("world_blender.config.optimization.preventlavatouchingwater")
                .define("preventLavaTouchingWater", true);

        builder.pop();
    }
}
