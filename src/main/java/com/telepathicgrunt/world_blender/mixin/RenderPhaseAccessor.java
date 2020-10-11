package com.telepathicgrunt.world_blender.mixin;

import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPhase.class)
public interface RenderPhaseAccessor {

    @Accessor("TRANSLUCENT_TRANSPARENCY")
    static RenderPhase.Transparency getTRANSLUCENT_TRANSPARENCY() {
        throw new UnsupportedOperationException();
    }

    @Accessor("ADDITIVE_TRANSPARENCY")
    static RenderPhase.Transparency getADDITIVE_TRANSPARENCY() {
        throw new UnsupportedOperationException();
    }

    @Accessor("BLACK_FOG")
    static RenderPhase.Fog getBLACK_FOG() {
        throw new UnsupportedOperationException();
    }
}