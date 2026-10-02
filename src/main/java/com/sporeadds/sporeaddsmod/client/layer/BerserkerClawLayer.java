package com.sporeadds.sporeaddsmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.client.BerserkerClawRenderState;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

/** Garras de Claws of Brutality en 3ª persona (y otros jugadores). */
public class BerserkerClawLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public BerserkerClawLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (player.isSpectator() || player.isInvisible()) return;
        if (!BerserkerClawRenderState.isActive(player.getId())) return;

        PlayerModel<AbstractClientPlayer> model = this.getParentModel();
        boolean slim = "slim".equals(player.getModelName());
        BerserkerClawRenderer.renderBothHands(poseStack, buffer, packedLight, model.rightArm, model.leftArm, slim);
    }
}
