package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.client.renderer.RenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderState.class)
public interface RenderPhaseAccessor {

    @Accessor("TRANSLUCENT_TRANSPARENCY")
    static RenderState.TransparencyState getTRANSLUCENT_TRANSPARENCY() {
        throw new UnsupportedOperationException();
    }

    @Accessor("ADDITIVE_TRANSPARENCY")
    static RenderState.TransparencyState getADDITIVE_TRANSPARENCY() {
        throw new UnsupportedOperationException();
    }

    @Accessor("BLACK_FOG")
    static RenderState.FogState getBLACK_FOG() {
        throw new UnsupportedOperationException();
    }
}