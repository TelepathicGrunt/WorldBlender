package com.telepathicgrunt.world_blender.dimension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.SkyProperties;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class WBSkyProperty extends SkyProperties {
    public WBSkyProperty() {
        super(155, false, SkyType.NORMAL, true, true);
    }

    @Override
    public Vec3d adjustSkyColor(Vec3d color, float sunHeight) {
        return color.multiply(sunHeight * 0.85F + 0.06F, sunHeight * 0.90F + 0.06F, sunHeight * 0.89F + 0.10F);
    }

    @Override
    public boolean useThickFog(int camX, int camY) {
        return false;
    }

}
