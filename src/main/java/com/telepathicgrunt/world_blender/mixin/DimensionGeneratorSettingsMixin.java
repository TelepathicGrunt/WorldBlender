package com.telepathicgrunt.world_blender.mixin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.telepathicgrunt.world_blender.utils.WorldSeedHolder;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DimensionGeneratorSettings.class)
public class DimensionGeneratorSettingsMixin {

	/**
	 * World seed for worldgen when not specified by JSON by Haven King
	 * https://github.com/Hephaestus-Dev/seedy-behavior/blob/master/src/main/java/dev/hephaestus/seedy/mixin/world/gen/GeneratorOptionsMixin.java
	 */
	@Redirect(method = "lambda$static$1(Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/kinds/App;",
		at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/PrimitiveCodec;fieldOf(Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;", ordinal = 0),
		remap = false)
	private static MapCodec<Long> giveUsRandomSeeds(PrimitiveCodec<Long> codec, final String name) {
		return codec.fieldOf(name).xmap(WorldSeedHolder::setSeed, seed -> seed);
	}
}