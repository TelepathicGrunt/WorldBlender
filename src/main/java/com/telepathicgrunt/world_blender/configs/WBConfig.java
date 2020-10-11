package com.telepathicgrunt.world_blender.configs;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = "world_blender")
public class WBConfig implements ConfigData {

    @ConfigEntry.Category("blending")
    @ConfigEntry.Gui.TransitiveObject
    public WBBlendingConfigs WBBlendingConfig = new WBBlendingConfigs();

    @ConfigEntry.Category("dimension")
    @ConfigEntry.Gui.TransitiveObject
    public WBDimensionConfigs WBDimensionConfig = new WBDimensionConfigs();

    @ConfigEntry.Category("portal")
    @ConfigEntry.Gui.TransitiveObject
    public WBPortalConfigs WBPortalConfig = new WBPortalConfigs();
}
