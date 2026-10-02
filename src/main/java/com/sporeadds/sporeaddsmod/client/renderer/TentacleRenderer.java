package com.sporeadds.sporeaddsmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sporeadds.sporeaddsmod.entity.Tentacle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TentacleRenderer extends EntityRenderer<Tentacle> {

    private static final ResourceLocation BODY_TEXTURE =
            new ResourceLocation("sporeadd", "textures/entity/tendril.png");

    public TentacleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(Tentacle entity, Frustum frustum, double camX, double camY, double camZ) {
        if (entity.shouldHideRender()) {
            return false;
        }
        return true;
    }

    @Override
    public void render(Tentacle entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        if (entity.shouldHideRender()) {
            return;
        }

        poseStack.pushPose();

        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BODY_TEXTURE));

        float age = entity.tickCount + partialTick;
        renderWrappedHelix(entity, poseStack, consumer, packedLight, age);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void renderWrappedHelix(Tentacle entity, PoseStack stack, VertexConsumer consumer, int light, float age) {
        double torsoBottom = 0.18D;
        double torsoTop = 1.18D;
        double height = torsoTop - torsoBottom;

        int coils = 4;
        int segmentsPerCoil = 18;
        int totalSegments = coils * segmentsPerCoil;

        for (int band = 0; band < 3; band++) {
            double phaseOffset = (Math.PI * 2.0D / 3.0D) * band + age * 0.045D;
            Vec3 previous = null;

            for (int i = 0; i <= totalSegments; i++) {
                double t = (double) i / (double) totalSegments;
                double angle = (Math.PI * 2.0D * coils * t) + phaseOffset;

                double segmentPulse =
                        Math.sin(age * 0.21D + t * 12.0D + band * 1.3D) * 0.014D +
                                Math.sin(age * 0.11D - t * 19.0D + band * 0.8D) * 0.009D +
                                Math.cos(age * 0.31D + i * 0.45D + band * 2.1D) * 0.007D;

                double widthPulse =
                        Math.sin(age * 0.26D + t * 15.0D + band * 1.7D) * 0.010D +
                                Math.cos(age * 0.14D - t * 10.0D + band * 0.5D) * 0.006D;

                double radiusBase = 0.26D + (band * 0.025D);
                double radius = radiusBase + segmentPulse;

                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;

                double y = torsoBottom + height * t;

                x += Math.cos(age * 0.08D + i * 0.6D + band) * 0.018D;
                z += Math.sin(age * 0.10D + i * 0.55D + band) * 0.018D;
                y += Math.sin(age * 0.12D + i * 0.40D + band) * 0.015D;

                Vec3 current = new Vec3(x, y, z);

                if (previous != null) {
                    float progress = (float) t;
                    float width = Mth.lerp(progress, 0.095F, 0.065F) + (float) widthPulse;
                    width = Math.max(0.045F, width);
                    renderTentacleSegment(previous, current, light, stack, consumer, width);
                }

                previous = current;
            }
        }

        renderCounterRing(stack, consumer, light, age, 0.34D, 0.295D, 1.0D, 0.072F, 0.0D);
        renderCounterRing(stack, consumer, light, age, 0.61D, 0.275D, 0.85D, 0.066F, 1.7D);
        renderCounterRing(stack, consumer, light, age, 0.90D, 0.305D, 1.15D, 0.070F, 3.4D);

        renderCrossWrap(stack, consumer, light, age, 0.52D, 0.0F);
        renderCrossWrap(stack, consumer, light, age, 0.74D, 180.0F);
    }

    private void renderCounterRing(PoseStack stack, VertexConsumer consumer, int light,
                                   float age, double centerY, double radiusBase,
                                   double turns, float baseWidth, double phaseOffset) {
        int segments = Math.max(20, (int) (turns * 28.0D));
        Vec3 previous = null;

        double enlargedRadiusBase = radiusBase * 1.25D;
        double oppositeLean = Math.sin(phaseOffset * 1.37D) > 0.0D ? -0.11D : 0.11D;
        double verticalTilt = Math.sin(phaseOffset * 0.91D) > 0.0D ? -0.035D : 0.035D;

        for (int i = 0; i <= segments; i++) {
            double t = (double) i / (double) segments;
            double signed = (t - 0.5D) * 2.0D;

            double localPulse =
                    Math.sin(age * 0.18D + t * 11.0D + phaseOffset) * 0.012D +
                            Math.cos(age * 0.09D - t * 16.0D + phaseOffset * 0.7D) * 0.007D;

            double angle =
                    phaseOffset
                            - (Math.PI * 2.0D * turns * t)
                            + Math.sin(t * 8.0D + phaseOffset) * 0.10D;

            double radius =
                    enlargedRadiusBase
                            + Math.sin(t * 9.0D + phaseOffset) * 0.010D
                            + localPulse;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            x += signed * oppositeLean;
            z += Math.sin(phaseOffset + signed * Math.PI) * 0.035D;

            double y = centerY
                    + signed * verticalTilt
                    + Math.sin(t * Math.PI * 2.0D + phaseOffset) * 0.020D
                    + Math.sin(age * 0.10D + t * 7.0D + phaseOffset) * 0.012D;

            Vec3 current = new Vec3(x, y, z);

            if (previous != null) {
                float width =
                        baseWidth
                                + (float) (Math.sin(age * 0.22D + t * 13.0D + phaseOffset) * 0.008D)
                                + (float) (Math.cos(age * 0.15D - t * 9.0D + phaseOffset) * 0.004D);

                width = Math.max(0.042F, width);
                renderTentacleSegment(previous, current, light, stack, consumer, width);
            }

            previous = current;
        }
    }

    private void renderCrossWrap(PoseStack stack, VertexConsumer consumer, int light, float age, double y, float yawDeg) {
        stack.pushPose();
        stack.translate(0.0D, y, 0.0D);
        stack.mulPose(Axis.YP.rotationDegrees(yawDeg + Mth.sin(age * 0.1F + (float) y * 3.0F) * 6.0F));
        stack.mulPose(Axis.ZP.rotationDegrees(58.0F + Mth.cos(age * 0.07F + (float) y * 5.0F) * 4.0F));

        float localPulse = 0.010F * Mth.sin(age * 0.19F + (float) y * 11.0F);
        renderBodyBox(stack, consumer, light, 0.085F + localPulse, 0.62F + localPulse);

        stack.popPose();
    }

    private void renderTentacleSegment(Vec3 from, Vec3 to, int light, PoseStack stack,
                                       VertexConsumer consumer, float width) {
        Vec3 direction = to.subtract(from);
        float length = (float) direction.length();
        if (length < 0.0001F) return;

        direction = direction.normalize();
        float yaw = (float) Math.atan2(direction.x, direction.z);
        float pitch = (float) (-Math.asin(direction.y));

        stack.pushPose();
        stack.translate(from.x, from.y, from.z);
        stack.mulPose(Axis.YP.rotation(yaw));
        stack.mulPose(Axis.XP.rotation(pitch));

        renderBodyBox(stack, consumer, light, width, length);

        stack.popPose();
    }

    private void renderBodyBox(PoseStack poseStack, VertexConsumer consumer, int light, float hw, float length) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float z0 = 0.0F;
        float z1 = length;

        addVertex(consumer, matrix, normal, light, -hw,  hw, z0, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F);
        addVertex(consumer, matrix, normal, light,  hw,  hw, z0, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F);
        addVertex(consumer, matrix, normal, light,  hw,  hw, z1, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F);
        addVertex(consumer, matrix, normal, light, -hw,  hw, z1, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);

        addVertex(consumer, matrix, normal, light, -hw, -hw, z1, 0.0F, 1.0F, 0.0F, -1.0F, 0.0F);
        addVertex(consumer, matrix, normal, light,  hw, -hw, z1, 1.0F, 1.0F, 0.0F, -1.0F, 0.0F);
        addVertex(consumer, matrix, normal, light,  hw, -hw, z0, 1.0F, 0.0F, 0.0F, -1.0F, 0.0F);
        addVertex(consumer, matrix, normal, light, -hw, -hw, z0, 0.0F, 0.0F, 0.0F, -1.0F, 0.0F);

        addVertex(consumer, matrix, normal, light,  hw,  hw, z0, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);
        addVertex(consumer, matrix, normal, light,  hw, -hw, z0, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F);
        addVertex(consumer, matrix, normal, light,  hw, -hw, z1, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
        addVertex(consumer, matrix, normal, light,  hw,  hw, z1, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);

        addVertex(consumer, matrix, normal, light, -hw,  hw, z1, 0.0F, 1.0F, -1.0F, 0.0F, 0.0F);
        addVertex(consumer, matrix, normal, light, -hw, -hw, z1, 1.0F, 1.0F, -1.0F, 0.0F, 0.0F);
        addVertex(consumer, matrix, normal, light, -hw, -hw, z0, 1.0F, 0.0F, -1.0F, 0.0F, 0.0F);
        addVertex(consumer, matrix, normal, light, -hw,  hw, z0, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F);

        addVertex(consumer, matrix, normal, light, -hw, -hw, z0, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F);
        addVertex(consumer, matrix, normal, light,  hw, -hw, z0, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F);
        addVertex(consumer, matrix, normal, light,  hw,  hw, z0, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F);
        addVertex(consumer, matrix, normal, light, -hw,  hw, z0, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F);

        addVertex(consumer, matrix, normal, light, -hw,  hw, z1, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F);
        addVertex(consumer, matrix, normal, light,  hw,  hw, z1, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
        addVertex(consumer, matrix, normal, light,  hw, -hw, z1, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F);
        addVertex(consumer, matrix, normal, light, -hw, -hw, z1, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, int light,
                           float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        consumer.vertex(pose, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(Tentacle entity) {
        return BODY_TEXTURE;
    }
}