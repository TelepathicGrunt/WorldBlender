package com.telepathicgrunt.worldblender.configs;

import com.telepathicgrunt.worldblender.utils.ConfigHelper;
import net.minecraftforge.common.ForgeConfigSpec;

public class WBPortalConfigs {
    public static class WBConfigValues {

        public ConfigHelper.ConfigValueListener<Integer> uniqueBlocksNeeded;
        public ConfigHelper.ConfigValueListener<String> activationItem;
        public ConfigHelper.ConfigValueListener<String> requiredBlocksInChests;
        public ConfigHelper.ConfigValueListener<Boolean> consumeChests;

        public WBConfigValues(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
        {
            builder.push("Portal Options");

            uniqueBlocksNeeded = subscriber.subscribe(builder
                    .comment(" \n-----------------------------------------------------\n",
                           " How many kinds of block items are needed to be in the eight",
                           " chests (or other chest tagged blocks) to create the portal. ",
                           " ",
                           " Items with no block form will be ignored and not counted but still be consumed.",
                           " ",
                           " If you set this to beyond 216 (maximum slots of 8 vanilla chests), make",
                           " sure you have a mod that has a chest that has much more inventory slots"
                            +" to fill or else you cannot create the portal.")
                    .translation("world_blender.config.portal.uniqueblocksneeded")
                    .defineInRange("uniqueBlocksNeeded", 216, 0, 1000));

            activationItem = subscriber.subscribe(builder
                    .comment(" \n-----------------------------------------------------\n",
                           " Item(s) that you need in your hand when you're crouching and right",
                           " clicking a chest block to begin the portal creation process.",
                           " This activation item will then be consumed. You can specify more",
                           " than 1 item that can be used to make the portal. Just separate the",
                           " item identifiers with a comma.",
                           " ",
                           " NOTE: the 8 chests needs to be in a 2x2 pattern before this mod "
                            +" starts checking the contents of the chests and then create the"
                            +" portal if there are enough unique blocks in the chests."
                            +" ",
                           " You can remove a portal by crouch right clicking execpt for the",
                           " portal block at world origin in World Blender dimension.\n")
                    .translation("world_blender.config.portal.activationitem")
                    .define("activationItem", "minecraft:nether_star"));

            requiredBlocksInChests = subscriber.subscribe(builder
                    .comment(" \n-----------------------------------------------------\n",
                           " You can specify what specific blocks are required to be in",
                           " the chests to make the portal. Format is the block's resourcelocations",
                           " with each block separated by a comma. Example:",
                           " \"minecraft:dirt, minecraft:sand, minecraft:stone\"",
                           " ",
                            "If you specify 1 required block but the portal needs 3 blocks,",
                           " players will need to place that one required block and any two ",
                           " other blocks into the chests.",
                           " ",
                           " If you specify 4 required blocks but the portal needs 2 unique blocks,",
                           " then players only needs to add any 2 of the 4 blocks to make the portal.\n")
                    .translation("world_blender.config.portal.requiredblocksinchests")
                    .define("requiredBlocksInChests", ""));

            consumeChests = subscriber.subscribe(builder
                    .comment(" \n-----------------------------------------------------\n",
                           " If true, portal creation will destroy the chests and all contents in it",
                           " Non-block items and stacks of items will still be consumed.",
                           " ",
                           " If set to false, the chests and contents will be dropped when portal is made.\n")
                    .translation("world_blender.config.portal.consumechests")
                    .define("consumeChests", true));

            builder.pop();
        }
    }
}
