package com.telepathicgrunt.world_blender.mixin;

import com.telepathicgrunt.world_blender.utils.GoVote;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class GoVoteMixin {

    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    private void handleVoteScreen(Screen screen, CallbackInfo ci) {
        // Handle the go vote screen. Go vote. Please.
        if (GoVote.show((MinecraftClient)(Object)this, screen)) {
            ci.cancel();
        }
    }
}