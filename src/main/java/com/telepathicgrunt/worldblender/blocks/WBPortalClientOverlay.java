package com.telepathicgrunt.worldblender.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.gen.PerlinNoiseGenerator;

import java.util.stream.IntStream;

public class WBPortalClientOverlay {
    private static final ResourceLocation TEXTURE_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final ResourceLocation TEXTURE_FORCE_FIELD = new ResourceLocation("textures/misc/forcefield.png");
    private static final PerlinNoiseGenerator NOISE_SAMPLER = new PerlinNoiseGenerator(new SharedSeedRandom(564566989L), IntStream.rangeClosed(-1, 0));

    public static void portalOverlay(PlayerEntity player, MatrixStack matrixStack) {

        if (player.world.getBlockState(new BlockPos(player.getEyePosition(1))).getBlock() == WBBlocks.WORLD_BLENDER_PORTAL.get()) {
            Minecraft minecraftIn = Minecraft.getInstance();
            float brightnessAtEyes = player.getBrightness();
            float yaw = Math.abs(player.rotationPitch) / 360;
            float pitch = Math.abs(player.rotationPitch) / 360;
            float yPos = (float) player.getPositionVec().y / 5F;

            minecraftIn.getTextureManager().bindTexture(TEXTURE_FORCE_FIELD);
            beginDrawingOverlay(matrixStack, brightnessAtEyes, yPos, yaw, pitch, 9945924F);

            minecraftIn.getTextureManager().bindTexture(TEXTURE_GLINT);
            beginDrawingOverlay(matrixStack, brightnessAtEyes, yPos, yaw, pitch, 23565F);
        }

    }

    private static void beginDrawingOverlay(MatrixStack matrixStack, float brightnessAtEyes, float yPos, float yaw, float pitch, float inheritOffser) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        drawTexture(bufferbuilder, brightnessAtEyes, yPos, yaw, pitch, inheritOffser, matrix4f);
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.disableBlend();
        //RenderSystem.translatef(17.0F, (Util.getMeasuringTimeMs() % 1000000000000000000L / 100000.0F), 0.0F);
    }

    private static void drawTexture(BufferBuilder bufferbuilder, float brightnessAtEyes, float yPos, float yaw, float pitch, float inheritOffser, Matrix4f matrix4f) {
        float timeOffset = (Util.milliTime() % 1000000000000000000L / 5000.0F);
        float red = Math.min(((float) Math.abs(NOISE_SAMPLER.noiseAt(timeOffset + inheritOffser, yaw, pitch, yPos)) * 4.95F) * brightnessAtEyes, 1F);
        float green = Math.min(((float) Math.abs(NOISE_SAMPLER.noiseAt(yaw + inheritOffser, timeOffset, pitch + 10000F, yPos) * 3.95F)) * brightnessAtEyes, 1F);
        float blue = Math.min(((float) Math.abs(NOISE_SAMPLER.noiseAt(pitch + 10540F + inheritOffser, yPos, yaw + 1012100F, timeOffset) * 4.0F)) * brightnessAtEyes, 1F);
        float alpha = Math.min(Math.max(((float) NOISE_SAMPLER.noiseAt(pitch + 6500F + inheritOffser, yPos, timeOffset + 3540F, yaw + 13540F) * 3.0F), 0.7F), 0.85F);
        bufferbuilder.pos(matrix4f, -1.0F, -1.0F, -0.5F).color(red, green, blue, alpha).tex(4.0F + yaw, 4.0F + pitch).endVertex();
        bufferbuilder.pos(matrix4f, 1.0F, -1.0F, -0.5F).color(red, green, blue, alpha).tex(0.0F + yaw, 4.0F + pitch).endVertex();
        bufferbuilder.pos(matrix4f, 1.0F, 1.0F, -0.5F).color(red, green, blue, alpha).tex(0.0F + yaw, 0.0F + pitch).endVertex();
        bufferbuilder.pos(matrix4f, -1.0F, 1.0F, -0.5F).color(red, green, blue, alpha).tex(4.0F + yaw, 0.0F + pitch).endVertex();
    }
}
