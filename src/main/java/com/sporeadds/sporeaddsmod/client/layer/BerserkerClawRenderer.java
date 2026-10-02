package com.sporeadds.sporeaddsmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Garras de "Claws of Brutality": 4 hojas por mano que cuelgan del puño apuntando HACIA ABAJO,
 * colocadas en fila de ADELANTE hacia ATRÁS (no de lado a lado), con la cara interior del sprite
 * mirando hacia el jugador. Anchas, con un leve abanico; las de los extremos a menor escala.
 *
 * Sprite {@code textures/entity/claws.png} (32x32): BASE arriba, PUNTAS abajo, interior a la
 * derecha. Plano de la hoja: YZ (largo en Y, ancho en Z), normal hacia el cuerpo del jugador.
 * Constantes al principio para ajustar en juego.
 */
public final class BerserkerClawRenderer {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/claws.png");

    // --- forma de cada hoja ---
    private static final float CLAW_BREADTH = 0.34F;    // ancho (a lo largo del eje adelante-atrás)
    private static final float CLAW_LENGTH = 0.42F;     // largo base -> punta (hacia abajo)
    private static final float BASE_INSET = 0.05F;      // cuánto sube la base dentro del puño

    // --- colocación en el puño ---
    private static final float FIST_DOWN = 0.63F;       // bajar por el brazo hasta el puño (menor = más arriba)
    private static final float FIST_FORWARD = 0.05F;    // Z: + = hacia atrás (hacia el jugador), - = hacia los nudillos

    // --- fila de 4 hojas de adelante hacia atrás ---
    private static final float[] Z_OFFSET = {-0.090F, -0.030F, 0.030F, 0.090F};  // -Z = adelante, +Z = atrás
    private static final float[] FAN_PITCH = {-12.0F, -4.0F, 4.0F, 12.0F};       // abanico (inclina cada hoja)
    private static final float[] SCALE = {0.60F, 1.0F, 1.0F, 0.60F};            // extremos más pequeños

    /** Giro de cada sprite sobre su eje (largo) para que la parte derecha de la textura mire al jugador. */
    private static final float SPRITE_ROLL = 90.0F;

    /** Si el interior del sprite (u=1) queda hacia el lado equivocado, pon esto a true. */
    private static final boolean INTERIOR_FLIP = false;

    private BerserkerClawRenderer() {
    }

    public static void renderBothHands(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                       ModelPart rightArm, ModelPart leftArm, boolean slim) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        renderHand(poseStack, vc, packedLight, rightArm, HumanoidArm.RIGHT, slim);
        renderHand(poseStack, vc, packedLight, leftArm, HumanoidArm.LEFT, slim);
    }

    public static void renderSingleHand(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                        ModelPart armPart, HumanoidArm arm, boolean slim) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        renderHand(poseStack, vc, packedLight, armPart, arm, slim);
    }

    private static void renderHand(PoseStack poseStack, VertexConsumer vc, int light,
                                   ModelPart armPart, HumanoidArm arm, boolean slim) {
        poseStack.pushPose();
        armPart.translateAndRotate(poseStack);

        float side = slim ? 0.09F : 0.105F;
        int dir = arm == HumanoidArm.RIGHT ? 1 : -1;

        poseStack.translate(arm == HumanoidArm.RIGHT ? -side : side, FIST_DOWN, FIST_FORWARD);

        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();

            poseStack.translate(0.0F, 0.0F, Z_OFFSET[i]);                 // fila adelante-atrás
            poseStack.mulPose(Axis.XP.rotationDegrees(FAN_PITCH[i]));     // abanico
            poseStack.mulPose(Axis.YP.rotationDegrees(SPRITE_ROLL * dir)); // gira el sprite sobre su eje

            float s = SCALE[i];
            poseStack.scale(s, s, s);

            drawClaw(poseStack, vc, light, dir);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    /**
     * Hoja plana en el plano YZ (x = 0): largo en Y (base y=-BASE_INSET dentro del puño,
     * punta y=+CLAW_LENGTH hacia abajo), ancho en Z. Normal hacia el cuerpo del jugador.
     */
    private static void drawClaw(PoseStack poseStack, VertexConsumer vc, int light, int dir) {
        PoseStack.Pose pose = poseStack.last();

        float hb = CLAW_BREADTH * 0.5F;
        float yBase = -BASE_INSET;
        float yTip = CLAW_LENGTH;
        float nx = -dir;   // normal hacia dentro del jugador (der: -X, izq: +X)

        float uFront = INTERIOR_FLIP ? 1.0F : 0.0F;
        float uBack = INTERIOR_FLIP ? 0.0F : 1.0F;

        vertex(vc, pose, light, nx, 0.0F, yBase, -hb, uFront, 0.0F);
        vertex(vc, pose, light, nx, 0.0F, yBase, hb, uBack, 0.0F);
        vertex(vc, pose, light, nx, 0.0F, yTip, hb, uBack, 1.0F);
        vertex(vc, pose, light, nx, 0.0F, yTip, -hb, uFront, 1.0F);
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, int light, float nx,
                               float x, float y, float z, float u, float v) {
        vc.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(pose.normal(), nx, 0.0F, 0.0F)
                .endVertex();
    }
}
