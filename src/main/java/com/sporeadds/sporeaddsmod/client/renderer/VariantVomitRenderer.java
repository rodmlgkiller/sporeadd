package com.sporeadds.sporeaddsmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sporeadds.sporeaddsmod.entity.projectile.VariantVomitProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class VariantVomitRenderer extends EntityRenderer<VariantVomitProjectile> {

    private static final ResourceLocation TEXTURE_BONE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/vomit_bone.png");

    private static final ResourceLocation TEXTURE_GORE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/vomit_gore.png");

    private static final ResourceLocation TEXTURE_DEFAULT =
            TEXTURE_BONE;

    public VariantVomitRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(VariantVomitProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        poseStack.scale(0.5F, 0.5F, 0.5F);

        ResourceLocation texture = getTextureLocation(entity);
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutout(texture));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        vertex(vertexconsumer, matrix4f, matrix3f, packedLight, 0.0F, 0, 0, 1);
        vertex(vertexconsumer, matrix4f, matrix3f, packedLight, 1.0F, 0, 1, 1);
        vertex(vertexconsumer, matrix4f, matrix3f, packedLight, 1.0F, 1, 1, 0);
        vertex(vertexconsumer, matrix4f, matrix3f, packedLight, 0.0F, 1, 0, 0);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, int light, float x, float y, int u, int v) {
        consumer.vertex(pose, x - 0.5F, y - 0.25F, 0.0F)
                .color(255, 255, 255, 255)
                .uv((float) u, (float) v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(VariantVomitProjectile entity) {
        String variant = entity.getVariant();

        return switch (variant.toLowerCase()) {
            case "bone" -> TEXTURE_BONE;
            case "gore" -> TEXTURE_GORE;
            default -> TEXTURE_DEFAULT;
        };
    }
}