package com.telepathicgrunt.worldblender.mixin.blocks;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractRailBlock.class)
public interface AbstractRailBlockInvoker {
    @Invoker("isValidRailDirection")
    static boolean wb_callIsValidRailDirection(BlockPos pos, World world, RailShape railShape) {
        throw new UnsupportedOperationException();
    }
}
