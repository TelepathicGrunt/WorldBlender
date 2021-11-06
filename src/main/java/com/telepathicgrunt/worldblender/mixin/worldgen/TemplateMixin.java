package com.telepathicgrunt.worldblender.mixin.worldgen;

import com.telepathicgrunt.worldblender.configs.WBDimensionConfigs;
import com.telepathicgrunt.worldblender.dimension.WBBiomeProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;


@Mixin(Template.class)
public class TemplateMixin {

    /**
     * @author TelepathicGrunt
     * @reason Prevent template structures from being placed at world bottom if disallowed in World Blender's config
     */
    @Inject(
            method = "func_237146_a_(Lnet/minecraft/world/IServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/feature/template/PlacementSettings;Ljava/util/Random;I)Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void worldblender_removeWorldBottomStructures(IServerWorld world, BlockPos pos, BlockPos pivot,
                                                          PlacementSettings placementData, Random random, int i,
                                                          CallbackInfoReturnable<Boolean> cir)
    {
        if(WBDimensionConfigs.removeWorldBottomStructures.get() &&
            world.getWorld().getChunkProvider().getChunkGenerator().getBiomeProvider() instanceof WBBiomeProvider &&
            pos.getY() <= 0)
        {
            cir.setReturnValue(false);
        }
    }
}
