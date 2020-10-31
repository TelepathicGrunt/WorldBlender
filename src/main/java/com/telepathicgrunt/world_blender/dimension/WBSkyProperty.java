package com.telepathicgrunt.world_blender.dimension;

import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WBSkyProperty extends DimensionRenderInfo {
    public WBSkyProperty() {
        super(155, true, FogType.NORMAL, false, false);
    }

    @Override
    // sky/fog color
    public Vector3d func_230494_a_(Vector3d color, float sunHeight) {
        return color.mul(sunHeight * 0.85F + 0.06F, sunHeight * 0.90F + 0.06F, sunHeight * 0.89F + 0.10F);
    }

    @Override
    // thick fog or no
    public boolean func_230493_a_(int camX, int camY) {
        return false;
    }

}
