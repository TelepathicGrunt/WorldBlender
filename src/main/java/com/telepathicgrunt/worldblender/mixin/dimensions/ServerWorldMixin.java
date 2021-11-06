package com.telepathicgrunt.worldblender.mixin.dimensions;

import com.telepathicgrunt.worldblender.configs.WBDimensionConfigs;
import com.telepathicgrunt.worldblender.dimension.AltarManager;
import com.telepathicgrunt.worldblender.dimension.WBBiomeProvider;
import com.telepathicgrunt.worldblender.utils.ServerWorldAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements ServerWorldAccess {

	@Mutable
	@Final
	@Shadow
	private DragonFightManager field_241105_O_;

	@Unique
	public AltarManager ALTAR = null;

	@Override
	public AltarManager getAltar() {
		return ALTAR;
	}

	@Inject(method = "<init>",
			at = @At(value = "TAIL"))
	private void setupWorld(MinecraftServer server, Executor workerExecutor, SaveFormat.LevelSave session,
							IServerWorldInfo properties, RegistryKey<World> registryKey, DimensionType dimensionType,
							IChunkStatusListener worldGenerationProgressListener, ChunkGenerator chunkGenerator,
							boolean bl, long seed, List<ISpecialSpawner> list, boolean bl2, CallbackInfo ci)
	{
		ServerWorld serverWorld = (ServerWorld)(Object)this;

		if(serverWorld.getChunkProvider().getChunkGenerator().getBiomeProvider() instanceof WBBiomeProvider){
			if(WBDimensionConfigs.spawnEnderDragon.get()) {
				((DimensionTypeAccessor)dimensionType).wb_setHasDragonFight(true);
				field_241105_O_ = new DragonFightManager(serverWorld, server.getServerConfiguration().getDimensionGeneratorSettings().getSeed(), server.getServerConfiguration().getDragonFightData());
			}

			ALTAR = new AltarManager(serverWorld);
		}
	}


	//Generate altar here for dimension
	@Inject(
			method = "tick(Ljava/util/function/BooleanSupplier;)V",
			at = @At(value = "HEAD")
	)
	private void tickAltar(CallbackInfo ci) {
		if(((ServerWorld)(Object)this).getChunkProvider().getChunkGenerator().getBiomeProvider() instanceof WBBiomeProvider){
			ALTAR.tick();
		}
	}
}
