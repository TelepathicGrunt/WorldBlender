package com.telepathicgrunt.worldblender.configs;

import com.telepathicgrunt.worldblender.utils.ConfigHelper;
import net.minecraftforge.common.ForgeConfigSpec;


public class WBDimensionConfigs{
    public static class WBConfigValues {
        public ConfigHelper.ConfigValueListener<Double> surfaceScale;
        public ConfigHelper.ConfigValueListener<Boolean> spawnEnderDragon;
        public ConfigHelper.ConfigValueListener<Boolean> carversCanCarveMoreBlocks;

        public ConfigHelper.ConfigValueListener<Boolean> preventFallingBlocks;
        public ConfigHelper.ConfigValueListener<Boolean> containFloatingLiquids;
        public ConfigHelper.ConfigValueListener<Boolean> preventLavaTouchingWater;

        public WBConfigValues(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber) {

            builder.push("Misc Options");

            surfaceScale = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            + " The size of the different kinds of surfaces. Higher numbers means\r\n"
                            + " each surface will be larger but might make some surfaces harder to"
                            + " find. Lower numbers means the surfaces are smaller but could become"
                            + " too chaotic or small for some features to spawn on.\r\n")
                    .translation("world_blender.config.misc.surfacescale")
                    .defineInRange("surfaceScale", 240D, 1D, 100000D));

            spawnEnderDragon = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            + " If true, the Enderdragon will spawn at world origin in the\r\n"
                            + " World Blender dimension and can respawn if you put back the\r\n"
                            + " End Crystals on the podiums. Once killed, the podium's portal \r\n"
                            + " will take you to the End where you can battle the End's Enderdragon. \r\n"
                            + " \r\n"
                            + " And yes, you can respawn the EnderDragon by placing 4 End Crystals \r\n"
                            + " on the edges of the Bedrock Podium. \r\n"
                            + " \r\n"
                            + " If set to false, the Enderdragon will not spawn.\r\n"
                            + " NOTE: Once the Enderdragon is spawned, changing this to false"
                            + " will not despawn the Enderdragon. Also, this option will not\r\n"
                            + " work in the World Blender Worldtype due to how fight managers are \r\n"
                            + " set up. It will only work for the dimension. \r\n")
                    .translation("world_blender.config.misc.spawnenderdragon")
                    .define("spawnEnderDragon", true));

            carversCanCarveMoreBlocks = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            + " If true, carvers (mainly vanilla caves and ravines) can now carve\r\n"
                            + " out Netherrack, End Stone, and some modded blocks as well.\r\n"
                            + " \r\n"
                            + " If turned off, you might see Vanilla caves and stuff gets cutoff \r\n"
                            + " by a wall of End Stone, Netherrack, or modded blocks. \r\n")
                    .translation("world_blender.config.misc.carversCanCarveMoreBlocks")
                    .define("carversCanCarveMoreBlocks", true));


            builder.pop();

            builder.push("Optimization Options");

            preventFallingBlocks = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            + " Will try its best to place Terracotta blocks under all floating\r\n"
                            + " fallable blocks to prevent lag when the blocks begins to fall.\r\n")
                    .translation("world_blender.config.optimization.preventfallingblocks")
                    .define("preventFallingBlocks", true));

            containFloatingLiquids = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            + " This will also place Terracotta next to fluids to try and prevent.\r\n"
                            + " them from floating and then flowing downward like crazy.\r\n"
                            + " \r\n"
                            + " It isn't perfect but it does do mostly a good job with how\r\n"
                            + " messy and chaotic having all features and carvers together is.\r\n")
                    .translation("world_blender.config.optimization.containfloatingliquids")
                    .define("containFloatingLiquids", true));

            preventLavaTouchingWater = subscriber.subscribe(builder
                    .comment(" \r\n-----------------------------------------------------\r\n\r\n"
                            + " Will place Obsidian to separate lava tagged fluids \r\n"
                            + " from water tagged fluids underground.\r\n")
                    .translation("world_blender.config.optimization.preventlavatouchingwater")
                    .define("preventLavaTouchingWater", true));

            builder.pop();
        }
    }
}
