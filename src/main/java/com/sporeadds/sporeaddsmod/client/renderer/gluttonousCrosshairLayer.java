package com.sporeadds.sporeaddsmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.sporeadds.sporeaddsmod.capabilities.LazyOptional;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class gluttonousCrosshairLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation CROSSHAIR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/crosshairbile.png");

    private static final int TOTAL_SPRITES = 12;

    private static final int IDLE_FRAMES = 3;
    private static final int TARGET_FRAME = 3;
    private static final int ATTACK_START_FRAME = 4;
    private static final int ATTACK_FRAME_COUNT = 8;
    private static final int ATTACK_LAST_FRAME = ATTACK_START_FRAME + ATTACK_FRAME_COUNT - 1;

    private static final long ATTACK_DURATION_MS = 50L;
    private static final long RECOVERY_DURATION_MS = 1000L;

    public gluttonousCrosshairLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    public static boolean isValidgluttonous(Player player) {
        LazyOptional<SporeIdentifierData> cap = SporeIdentifierProvider.SPORE_IDENTIFIER.get(player);
        if (cap.isPresent()) {
            SporeIdentifierData data = cap.orElseThrow(IllegalStateException::new);
            return "gluttonous".equalsIgnoreCase(data.getSubclass())
                    && ClientgluttonousCrosshairRenderState.shouldRender(player.getId());
        }
        return false;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!isValidgluttonous(player)) return;

        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON) return;

        int frameIndex = resolveFrame(player);

        poseStack.pushPose();
        this.getParentModel().getHead().translateAndRotate(poseStack);
        poseStack.translate(0.0D, -0.25D, -0.4D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

        float scale = 0.5f;
        poseStack.scale(scale, scale, scale);

        float minU = 0.0f;
        float maxU = 1.0f;
        float minV = (float) frameIndex / TOTAL_SPRITES;
        float maxV = (float) (frameIndex + 1) / TOTAL_SPRITES;

        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();

        int fullBright = LightTexture.pack(15, 15);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(CROSSHAIR_TEXTURE));

        vertexConsumer.vertex(matrix4f, -0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullBright).normal(matrix3f, 0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix4f, 0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullBright).normal(matrix3f, 0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix4f, 0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullBright).normal(matrix3f, 0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix4f, -0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullBright).normal(matrix3f, 0, 0, 1).endVertex();

        poseStack.popPose();
    }

    public static int resolveFrame(Player player) {
        long now = System.currentTimeMillis();
        long attackStartMs = ClientgluttonousCrosshairRenderState.getAttackStart(player.getId());

        if (attackStartMs >= 0L) {
            long elapsed = now - attackStartMs;
            long totalAnimation = ATTACK_DURATION_MS + RECOVERY_DURATION_MS;

            if (elapsed >= totalAnimation) {
                ClientgluttonousCrosshairRenderState.clearAttack(player.getId());
            } else if (elapsed < ATTACK_DURATION_MS) {
                float attackProgress = elapsed / (float) ATTACK_DURATION_MS;
                int attackFrameOffset = Math.min(ATTACK_FRAME_COUNT - 1, (int) (attackProgress * ATTACK_FRAME_COUNT));
                return ATTACK_START_FRAME + attackFrameOffset;
            } else {
                long recoveryElapsed = elapsed - ATTACK_DURATION_MS;
                float recoveryProgress = Math.min(1.0f, recoveryElapsed / (float) RECOVERY_DURATION_MS);
                return ATTACK_LAST_FRAME - Math.round(recoveryProgress * (ATTACK_FRAME_COUNT - 1));
            }
        }

        if (isLookingAtTarget(player)) {
            return TARGET_FRAME;
        }

        if (player.getAttackStrengthScale(0.0f) >= 1.0f) {
            return (player.tickCount / 4) % IDLE_FRAMES;
        }

        return 0;
    }

    private static boolean isLookingAtTarget(Player player) {
        Minecraft mc = Minecraft.getInstance();

        if (player == mc.player) {
            return mc.crosshairPickEntity != null;
        }

        float partialTicks = mc.getFrameTime();
        Vec3 eyePos = player.getEyePosition(partialTicks);
        Vec3 lookVec = player.getViewVector(partialTicks);
        double range = 4.0D;
        Vec3 endPos = eyePos.add(lookVec.scale(range));
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(1.0D);

        for (Entity entity : player.level().getEntities(player, searchBox)) {
            if (entity.isPickable()) {
                AABB entityBox = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (entityBox.clip(eyePos, endPos).isPresent()) {
                    return true;
                }
            }
        }

        return false;
    }
}