package com.telepathicgrunt.world_blender.configs;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "dimension")
public class WBDimensionConfigs implements ConfigData {

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " The size of the different kinds of surfaces. Higher numbers means\r\n"
            +" each surface will be larger but might make some surfaces harder to"
            +" find. Lower numbers means the surfaces are smaller but could become"
            +" too chaotic or small for some features to spawn on.\r\n")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100000)
    public double surfaceScale = 240D;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " If true, the Enderdragon will spawn at world origin in the\r\n"
            +" World Blender dimension. Once killed, the podium's portal \r\n"
            +" will take you to the End where you can battle the End's Enderdragon. \r\n"
            +" \r\n"
            +" And yes, you can respawn the EnderDragon by placing 4 End Crystals \r\n"
            +" on the edges of the Bedrock Podium. \r\n"
            +" \r\n"
            +" If set to false, the Enderdragon will not spawn.\r\n"
            +" NOTE: Once the Enderdragon is spawned, changing this to false"
            +" will not despawn the Enderdragon.\r\n")
    public boolean spawnEnderDragon = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " If true, carvers (mainly vanilla caves and ravines) can now carve\r\n"
            +" out Netherrack, End Stone, and some modded blocks as well.\r\n"
            +" \r\n"
            +" If turned off, you might see Vanilla caves and stuff gets cutoff \r\n"
            +" by a wall of End Stone, Netherrack, or modded blocks. \r\n")
    public boolean carversCanCarveMoreBlocks = true;


    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Will try its best to place Terracotta blocks under all floating\r\n"
            +" fallable blocks to prevent lag when the blocks begins to fall.\r\n")
    public boolean preventFallingBlocks = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " This will also place Terracotta next to fluids to try and prevent.\r\n"
            +" them from floating and then flowing downward like crazy.\r\n"
            +" \r\n"
            +" It isn't perfect but it does do mostly a good job with how\r\n"
            +" messy and chaotic having all features and carvers together is.\r\n")
    public boolean containFloatingLiquids = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip(count = 0)
    @Comment(value = " Will place Obsidian to separate lava tagged fluids \r\n"
            +" from water tagged fluids underground.\r\n")
    public boolean preventLavaTouchingWater = true;

}
