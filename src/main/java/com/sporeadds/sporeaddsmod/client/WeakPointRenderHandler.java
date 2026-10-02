package com.sporeadds.sporeaddsmod.client;

import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.Map;

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT)
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
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader);

        com.mojang.blaze3d.vertex.Tesselator tesselator = com.mojang.blaze3d.vertex.Tesselator.getInstance();

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
            Vec3 worldPos = entity.getPosition(event.getPartialTick().getGameTimeDeltaPartialTick(false)).add(offset);

            poseStack.pushPose();
            poseStack.translate(worldPos.x - camPos.x, worldPos.y - camPos.y, worldPos.z - camPos.z);
            poseStack.mulPose(mc.getEntityRenderDispatcher().camera.rotation());

            float half = MARKER_SIZE / 2.0F;
            var matrix = poseStack.last().pose();

            com.mojang.blaze3d.vertex.BufferBuilder bufferBuilder = tesselator.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                    com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR);

            bufferBuilder.addVertex(matrix, -half, -half, 0).setColor(255, 255, 255, 255).setUv(0, 1);
            bufferBuilder.addVertex(matrix, half, -half, 0).setColor(255, 255, 255, 255).setUv(1, 1);
            bufferBuilder.addVertex(matrix, half, half, 0).setColor(255, 255, 255, 255).setUv(1, 0);
            bufferBuilder.addVertex(matrix, -half, half, 0).setColor(255, 255, 255, 255).setUv(0, 0);

            com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

            poseStack.popPose();
        }

        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.enableCull();
    }
}