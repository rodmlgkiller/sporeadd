package com.sporeadds.sporeaddsmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sporeadds.sporeaddsmod.client.CamouflageClientState;
import com.sporeadds.sporeaddsmod.client.model.animations.BushAnimation;
import com.sporeadds.sporeaddsmod.client.model.BushModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.HashMap;
import java.util.Map;

public class GhostCamouflageLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final double MOVEMENT_THRESHOLD_SQR = 0.0025D;
    private static final int GRACE_TICKS = 5;

    private static final float ANIM_LENGTH_MILLIS = 2000F;
    private static final float MAX_SPEED = 3.0F;
    private static final float ACCELERATION = 4.0F;

    private final BushModel bushModel;
    private final Map<Integer, Integer> lastMovementTick = new HashMap<>();
    private final Map<Integer, AnimState> animStates = new HashMap<>();

    public GhostCamouflageLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        ModelPart root = BushModel.createBodyLayer().bakeRoot();
        this.bushModel = new BushModel(root);
    }

    private static final class AnimState {
        float clockMillis = 0F;
        float speed = 1.0F;
        boolean finished = true;
        long lastNanos = System.nanoTime();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!CamouflageClientState.isCamouflaged(player.getId())) {
            return;
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(BushModel.LOCATION));

        int id = player.getId();

        double horizontalSpeedSqr = player.getDeltaMovement().horizontalDistanceSqr();
        boolean movingNow = horizontalSpeedSqr > MOVEMENT_THRESHOLD_SQR;

        if (movingNow) {
            lastMovementTick.put(id, player.tickCount);
        }

        int lastMoved = lastMovementTick.getOrDefault(id, -GRACE_TICKS - 1);
        boolean isWalking = (player.tickCount - lastMoved) <= GRACE_TICKS;

        AnimState state = animStates.computeIfAbsent(id, k -> new AnimState());

        long now = System.nanoTime();
        float deltaSeconds = (now - state.lastNanos) / 1_000_000_000F;
        deltaSeconds = Math.min(deltaSeconds, 0.1F);
        state.lastNanos = now;
        float deltaMillis = deltaSeconds * 1000F;

        if (isWalking) {
            state.finished = false;
            state.speed = 1.0F;
            state.clockMillis = (state.clockMillis + deltaMillis) % ANIM_LENGTH_MILLIS;
        } else if (!state.finished) {
            state.speed = Math.min(state.speed + ACCELERATION * deltaSeconds, MAX_SPEED);
            float next = state.clockMillis + deltaMillis * state.speed;
            if (next >= ANIM_LENGTH_MILLIS) {
                state.clockMillis = 0F;
                state.finished = true;
                state.speed = 1.0F;
            } else {
                state.clockMillis = next;
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180F));

        if (!state.finished) {
            bushModel.applyRawAnimation(BushAnimation.Walking_animation, (long) state.clockMillis);
        } else {
            bushModel.applyRestPose();
        }

        bushModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
    }
}