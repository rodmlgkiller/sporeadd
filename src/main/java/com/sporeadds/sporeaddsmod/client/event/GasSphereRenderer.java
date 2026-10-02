package com.sporeadds.sporeaddsmod.client.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.sporeadds.sporeaddsmod.client.ClientCameraShakeData;
import com.sporeadds.sporeaddsmod.client.ClientGasSphereData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * Draws a translucent shell around each active caustic gas sphere so its interior isn't visible from
 * outside (particles alone only add haze, not real occlusion), tints the screen while the local player
 * is inside one, and drives the camera shake for the caustic mound's emerge animation. Vision inside the
 * sphere is impaired purely through the server-side Blindness effect - no client-side fog/render-distance
 * reduction on top of it.
 */
@EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class GasSphereRenderer {

    private static final int LAT_SEGMENTS = 12;
    private static final int LON_SEGMENTS = 16;

    private static final ResourceLocation GAS_BASE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/particle/gas_base.png");

    private static final int COLOR_R = 40;
    private static final int COLOR_G = 235;
    private static final int COLOR_B = 20;
    private static final int COLOR_A = Math.round(255 * 0.90F);

    private static final int OVERLAY_R = 170;
    private static final int OVERLAY_G = 255;
    private static final int OVERLAY_B = 0;
    private static final int OVERLAY_A = Math.round(255 * 0.20F);

    private static final java.util.Random SHAKE_RANDOM = new java.util.Random();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        List<ClientGasSphereData.RenderSphere> spheres = ClientGasSphereData.getRenderSpheres();
        if (spheres.isEmpty()) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GAS_BASE_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (ClientGasSphereData.RenderSphere sphere : spheres) {
            if (sphere.radius >= 1.0F) {
                addSphere(buffer, matrix, (float) sphere.x, (float) sphere.y, (float) sphere.z, sphere.radius);
            }
        }

        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private static void addSphere(BufferBuilder buffer, Matrix4f matrix, float cx, float cy, float cz, float radius) {
        for (int lat = 0; lat < LAT_SEGMENTS; lat++) {
            double theta1 = Math.PI * lat / LAT_SEGMENTS;
            double theta2 = Math.PI * (lat + 1) / LAT_SEGMENTS;

            for (int lon = 0; lon < LON_SEGMENTS; lon++) {
                double phi1 = 2 * Math.PI * lon / LON_SEGMENTS;
                double phi2 = 2 * Math.PI * (lon + 1) / LON_SEGMENTS;

                Vector3f p1 = spherePoint(cx, cy, cz, radius, theta1, phi1);
                Vector3f p2 = spherePoint(cx, cy, cz, radius, theta2, phi1);
                Vector3f p3 = spherePoint(cx, cy, cz, radius, theta2, phi2);
                Vector3f p4 = spherePoint(cx, cy, cz, radius, theta1, phi2);

                // UVs use raw segment indices (not normalized 0-1) so gas_base tiles across the whole
                // sphere instead of stretching a single 32x32 texture over the entire dome.
                float u1 = lon;
                float u2 = lon + 1;
                float v1 = lat;
                float v2 = lat + 1;

                vertex(buffer, matrix, p1, u1, v1);
                vertex(buffer, matrix, p2, u1, v2);
                vertex(buffer, matrix, p3, u2, v2);

                vertex(buffer, matrix, p1, u1, v1);
                vertex(buffer, matrix, p3, u2, v2);
                vertex(buffer, matrix, p4, u2, v1);
            }
        }
    }

    private static Vector3f spherePoint(float cx, float cy, float cz, float radius, double theta, double phi) {
        float x = (float) (radius * Math.sin(theta) * Math.cos(phi));
        float y = (float) (radius * Math.cos(theta));
        float z = (float) (radius * Math.sin(theta) * Math.sin(phi));
        return new Vector3f(cx + x, cy + y, cz + z);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, Vector3f pos, float u, float v) {
        buffer.vertex(matrix, pos.x(), pos.y(), pos.z()).uv(u, v).color(COLOR_R, COLOR_G, COLOR_B, COLOR_A).endVertex();
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        if (!ClientGasSphereData.isInsideAny(minecraft.player.position())) {
            return;
        }

        int color = (OVERLAY_A << 24) | (OVERLAY_R << 16) | (OVERLAY_G << 8) | OVERLAY_B;
        event.getGuiGraphics().fill(
                0, 0, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), color
        );
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float strength = ClientCameraShakeData.getCurrentStrength();
        if (strength <= 0.0F) {
            return;
        }

        event.setPitch(event.getPitch() + (SHAKE_RANDOM.nextFloat() - 0.5F) * strength);
        event.setYaw(event.getYaw() + (SHAKE_RANDOM.nextFloat() - 0.5F) * strength);
        event.setRoll(event.getRoll() + (SHAKE_RANDOM.nextFloat() - 0.5F) * strength * 0.5F);
    }
}
