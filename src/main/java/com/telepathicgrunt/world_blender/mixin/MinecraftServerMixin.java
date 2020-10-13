package com.telepathicgrunt.world_blender.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.telepathicgrunt.world_blender.WBIdentifiers;
import com.telepathicgrunt.world_blender.WorldBlender;
import com.telepathicgrunt.world_blender.features.WBPortalAltar;
import com.telepathicgrunt.world_blender.the_blender.IdentifierPrinting;
import com.telepathicgrunt.world_blender.the_blender.TheBlender;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;


@Mixin(value = MinecraftServer.class, priority = Integer.MAX_VALUE)
public class MinecraftServerMixin {

    @Final
    @Shadow
    private TemplateManager field_240765_ak_;

    @Inject(
            method = "<init>(Ljava/lang/Thread;Lnet/minecraft/util/registry/DynamicRegistries$Impl;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerConfiguration;Lnet/minecraft/resources/ResourcePackList;Ljava/net/Proxy;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/resources/DataPackRegistries;Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfileRepository;Lnet/minecraft/server/management/PlayerProfileCache;Lnet/minecraft/world/chunk/listener/IChunkStatusListenerFactory;)V",
            at = @At(value = "TAIL")
    )
    private void modifyBiomeRegistry(Thread thread, DynamicRegistries.Impl impl, SaveFormat.LevelSave session,
                                     IServerConfiguration saveProperties, ResourcePackList resourcePackManager,
                                     Proxy proxy, DataFixer dataFixer, DataPackRegistries serverResourceManager,
                                     MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository,
                                     PlayerProfileCache userCache, IChunkStatusListenerFactory worldGenerationProgressListenerFactory,
                                     CallbackInfo ci) {

        if(impl.func_230521_a_(Registry.BIOME_KEY).isPresent()) {
            TheBlender.blendTheWorld(impl);
            WBPortalAltar.ALTAR_TEMPLATE = field_240765_ak_.getTemplate(WBIdentifiers.ALTAR_ID);
            if(WorldBlender.WBBlendingConfig.resourceLocationDump.get()){
                IdentifierPrinting.printAllResourceLocations(impl);
            }
        }
    }
}
