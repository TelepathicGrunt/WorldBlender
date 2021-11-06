package com.telepathicgrunt.worldblender.mixin.dimensions;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.telepathicgrunt.worldblender.configs.WBBlendingConfigs;
import com.telepathicgrunt.worldblender.theblender.IdentifierPrinting;
import com.telepathicgrunt.worldblender.theblender.TheBlender;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;


@Mixin(value = MinecraftServer.class, priority = Integer.MAX_VALUE)
public class MinecraftServerMixin {

    @Inject(
            method = "<init>(Ljava/lang/Thread;Lnet/minecraft/util/registry/DynamicRegistries$Impl;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerConfiguration;Lnet/minecraft/resources/ResourcePackList;Ljava/net/Proxy;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/resources/DataPackRegistries;Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfileRepository;Lnet/minecraft/server/management/PlayerProfileCache;Lnet/minecraft/world/chunk/listener/IChunkStatusListenerFactory;)V",
            at = @At(value = "TAIL")
    )
    private void modifyBiomeRegistry(Thread thread, DynamicRegistries.Impl impl, SaveFormat.LevelSave session,
                                     IServerConfiguration saveProperties, ResourcePackList resourcePackManager,
                                     Proxy proxy, DataFixer dataFixer, DataPackRegistries serverResourceManager,
                                     MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository,
                                     PlayerProfileCache userCache, IChunkStatusListenerFactory worldGenerationProgressListenerFactory,
                                     CallbackInfo ci)
    {
        if(WBBlendingConfigs.resourceLocationDump.get()){
            IdentifierPrinting.printAllResourceLocations(impl);
        }

        if(impl.func_230521_a_(Registry.BIOME_KEY).isPresent()) {
            TheBlender.blendTheWorld(impl);
        }

        // Reset for every world.
        TheBlender.STRUCTURE_CONFIGS.clear();
    }
}
