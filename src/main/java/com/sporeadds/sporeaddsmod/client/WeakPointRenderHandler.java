package com.sporeadds.sporeaddsmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
public final class WeakPointRenderHandler {

    private static final float MARKER_SIZE = 0.35F;
    private static final int FULL_LIGHT = 0xF000F0;

    private WeakPointRenderHandler() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Map<Integer, Vec3> allWeakPoints = WeakPointClientState.getAll();
        if (allWeakPoints.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();

        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.disableCull();
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorTexShader);

        com.mojang.blaze3d.vertex.BufferBuilder bufferBuilder =
                com.mojang.blaze3d.vertex.Tesselator.getInstance().getBuilder();

        for (Map.Entry<Integer, Vec3> entry : allWeakPoints.entrySet()) {
            int entityId = entry.getKey();
            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                continue;
            }

            if (entity.isInvisible()) {
                continue;
            }

            if (mc.player != null && entity.isInvisibleTo(mc.player)) {
                continue;
            }

            var texture = com.sporeadds.sporeaddsmod.combat.WeakPointManager.getTexture(entityId);
            com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, texture);

            Vec3 offset = entry.getValue();
            Vec3 worldPos = entity.getPosition(event.getPartialTick()).add(offset);

            poseStack.pushPose();
            poseStack.translate(worldPos.x - camPos.x, worldPos.y - camPos.y, worldPos.z - camPos.z);
            poseStack.mulPose(mc.getEntityRenderDispatcher().camera.rotation());

            float half = MARKER_SIZE / 2.0F;
            var matrix = poseStack.last().pose();

            bufferBuilder.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                    com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR_TEX);

            bufferBuilder.vertex(matrix, -half, -half, 0).color(255, 255, 255, 255).uv(0, 1).endVertex();
            bufferBuilder.vertex(matrix, half, -half, 0).color(255, 255, 255, 255).uv(1, 1).endVertex();
            bufferBuilder.vertex(matrix, half, half, 0).color(255, 255, 255, 255).uv(1, 0).endVertex();
            bufferBuilder.vertex(matrix, -half, half, 0).color(255, 255, 255, 255).uv(0, 0).endVertex();

            com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(bufferBuilder.end());

            poseStack.popPose();
        }

        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.enableCull();
    }
}