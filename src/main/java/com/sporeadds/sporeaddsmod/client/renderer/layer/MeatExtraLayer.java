package com.sporeadds.sporeaddsmod.client.renderer.layer;

import com.sporeadds.sporeaddsmod.client.model.MeatAbominationModel;
import com.sporeadds.sporeaddsmod.entity.MeatAbomination;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class MeatExtraLayer extends RenderLayer<MeatAbomination, MeatAbominationModel<MeatAbomination>> {

    private final ResourceLocation texture;
    private final int groupId;

    public MeatExtraLayer(RenderLayerParent<MeatAbomination, MeatAbominationModel<MeatAbomination>> parent, ResourceLocation texture, int groupId) {
        super(parent);
        this.texture = texture;
        this.groupId = groupId;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, MeatAbomination entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F); // Para que parpadee en rojo al recibir daño

        if (groupId == 2) {
            this.getParentModel().group2.render(poseStack, vertexConsumer, packedLight, overlay);
        } else if (groupId == 3) {
            this.getParentModel().group3.render(poseStack, vertexConsumer, packedLight, overlay);
        }
    }
}