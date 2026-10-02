package com.sporeadds.sporeaddsmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sporeadds.sporeaddsmod.client.model.BushModel;
import com.sporeadds.sporeaddsmod.entity.DecoyEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class DecoyRenderer extends EntityRenderer<DecoyEntity> {

    private final BushModel bushModel;

    public DecoyRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart root = BushModel.createBodyLayer().bakeRoot();
        this.bushModel = new BushModel(root);
    }

    @Override
    public ResourceLocation getTextureLocation(DecoyEntity entity) {
        return BushModel.LOCATION;
    }

    @Override
    public void render(DecoyEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(BushModel.LOCATION));

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180F));

        bushModel.applyRestPose();
        bushModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}