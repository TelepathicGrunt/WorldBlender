package com.telepathicgrunt.worldblender.mixin;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Block.class)
public interface BlockAccessor {
    @Accessor("SHOULD_SIDE_RENDER_CACHE")
    static ThreadLocal<Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey>> wb_getSHOULD_SIDE_RENDER_CACHE() {
        throw new UnsupportedOperationException();
    }
}
