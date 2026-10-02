package com.sporeadds.sporeaddsmod.client.renderer.layer;

import com.sporeadds.sporeaddsmod.items.ReinforcedCombatChainsItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class ChainArmLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation MOD_CHAIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/chain2.png");

    public ChainArmLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        boolean inMainHand = mainHandItem.getItem() instanceof ReinforcedCombatChainsItem;
        boolean inOffHand = offHandItem.getItem() instanceof ReinforcedCombatChainsItem;

        if (!inMainHand && !inOffHand) return;

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(MOD_CHAIN_TEXTURE));

        if (inMainHand) {
            renderWrappedChains(poseStack, vc, player, player.getMainArm(), packedLight);
        }

        if (inOffHand) {
            HumanoidArm offArm = player.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            renderWrappedChains(poseStack, vc, player, offArm, packedLight);
        }
    }

    private void renderWrappedChains(PoseStack poseStack, VertexConsumer vc, AbstractClientPlayer player, HumanoidArm arm, int packedLight) {
        poseStack.pushPose();

        PlayerModel<AbstractClientPlayer> model = this.getParentModel();
        ModelPart armPart = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        armPart.translateAndRotate(poseStack);

        boolean slim = player.getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM;

        float torsoOffset = slim ? 0.0925F : 0.10F;
        poseStack.translate(arm == HumanoidArm.RIGHT ? -torsoOffset : torsoOffset, 0.0F, 0.0F);

        float radius = slim ? 0.145F : 0.165F;

        float[][] wraps = {
                { -0.05F,  15.0F,   5.0F },
                {  0.15F, -10.0F,  15.0F },
                {  0.35F,   5.0F, -10.0F },
                {  0.55F, -15.0F,   0.0F }
        };

        for (float[] wrap : wraps) {
            poseStack.pushPose();
            poseStack.translate(0.0F, wrap[0], 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(wrap[1]));
            poseStack.mulPose(Axis.ZP.rotationDegrees(wrap[2]));

            for (int i = 0; i < 4; i++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(i * 90.0F));
                poseStack.translate(0.0F, 0.0F, radius);
                draw3DChainSegment(poseStack, vc, radius, packedLight);
                poseStack.popPose();
            }

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void draw3DChainSegment(PoseStack poseStack, VertexConsumer vc, float radius, int packedLight) {
        PoseStack.Pose matrix = poseStack.last();

        float w = 0.25F;
        float length = (radius * 2.0F) + 0.08F;
        float startX = -length / 2.0F;

        vc.addVertex(matrix.pose(), startX, 0.0F, -w).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(matrix, 0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix.pose(), startX + length, 0.0F, -w).setColor(255, 255, 255, 255).setUv(2.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(matrix, 0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix.pose(), startX + length, 0.0F, w).setColor(255, 255, 255, 255).setUv(2.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(matrix, 0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix.pose(), startX, 0.0F, w).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(matrix, 0.0F, 1.0F, 0.0F);

        vc.addVertex(matrix.pose(), startX, -w, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(matrix, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix.pose(), startX + length, -w, 0.0F).setColor(255, 255, 255, 255).setUv(2.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(matrix, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix.pose(), startX + length, w, 0.0F).setColor(255, 255, 255, 255).setUv(2.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(matrix, 0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix.pose(), startX, w, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(matrix, 0.0F, 0.0F, 1.0F);
    }
}