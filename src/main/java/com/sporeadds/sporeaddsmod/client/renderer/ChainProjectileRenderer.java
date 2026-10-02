package com.sporeadds.sporeaddsmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sporeadds.sporeaddsmod.entity.projectile.ChainProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class ChainProjectileRenderer extends EntityRenderer<ChainProjectileEntity> {

    private static final ResourceLocation VANILLA_CHAIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/chain.png");
    private static final ResourceLocation MOD_CHAIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/chain.png");
    private static final ResourceLocation HOOK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/hook.png");

    public ChainProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ChainProjectileEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Entity owner = entity.getOwner();
        if (owner != null) {
            poseStack.pushPose();

            if (entity.shakeTimer > 0) {
                float shakeIntensity = 0.15F;
                float shakeX = (entity.level().random.nextFloat() - 0.5F) * shakeIntensity;
                float shakeY = (entity.level().random.nextFloat() - 0.5F) * shakeIntensity;
                float shakeZ = (entity.level().random.nextFloat() - 0.5F) * shakeIntensity;
                poseStack.translate(shakeX, shakeY, shakeZ);
            }

            boolean attached = entity.isAttached();
            Entity target = attached ? entity.level().getEntity(entity.getTargetId()) : null;

            Vec3 projPos = entity.getPosition(partialTicks);
            Vec3 rawStartPos = owner.getPosition(partialTicks).add(0.0D, owner.getBbHeight() * 0.5D, 0.0D);
            Vec3 startPos = rawStartPos;

            double ownerScale = getEffectiveScale(owner);
            if (Math.abs(ownerScale - 1.0D) > 0.001D) {
                startPos = getCompensatedAnchorPosition(rawStartPos, projPos, owner);
            }

            Vec3 endPos = projPos.add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
            if (attached && target != null) {
                endPos = target.getPosition(partialTicks).add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            }

            Vec3 startRel = startPos.subtract(projPos);
            Vec3 endRel = endPos.subtract(projPos);

            VertexConsumer mainChainConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(VANILLA_CHAIN_TEXTURE));
            drawMainChain(poseStack, mainChainConsumer, startRel, endRel, packedLight);

            if (!attached) {
                VertexConsumer hookConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(HOOK_TEXTURE));
                drawHookAtTip(poseStack, hookConsumer, startRel, endRel, packedLight);
            }

            if (attached && target != null) {
                VertexConsumer wrappedChainConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(MOD_CHAIN_TEXTURE));
                drawWrappedChains(poseStack, wrappedChainConsumer, owner, projPos, partialTicks, packedLight, true);
                drawWrappedChains(poseStack, wrappedChainConsumer, target, projPos, partialTicks, packedLight, false);
            }

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private Vec3 getCompensatedAnchorPosition(Vec3 anchorWorld, Vec3 projectileWorld, Entity owner) {
        double distance = anchorWorld.distanceTo(projectileWorld);
        Vec3 toProjectile = projectileWorld.subtract(anchorWorld);

        if (toProjectile.lengthSqr() < 1.0E-6D) {
            return anchorWorld;
        }

        double ownerScale = getEffectiveScale(owner);
        double compensationRatio = getCompensationRatioFromScale(ownerScale);
        Vec3 dir = toProjectile.normalize();
        double compensation = distance * compensationRatio;

        return anchorWorld.add(dir.scale(compensation));
    }

    private double getCompensationRatioFromScale(double scale) {
        if (scale <= 1.5D) {
            double x = scale - 1.0D;
            return 1.01668492D * x - 0.99113413D * x * x + 0.58265762D * x * x * x;
        } else {
            double s15 = 1.5D, v15 = 0.33339113D;
            double s16 = 1.6D, v16 = 0.376D;
            double s17 = 1.7D, v17 = 0.41D;
            double s18 = 1.8D, v18 = 0.444D;
            double s19 = 1.9D, v19 = 0.475D;
            double s20 = 2.0D, v20 = 0.5D;

            if (scale <= s16) return lerp(s15, v15, s16, v16, scale);
            if (scale <= s17) return lerp(s16, v16, s17, v17, scale);
            if (scale <= s18) return lerp(s17, v17, s18, v18, scale);
            if (scale <= s19) return lerp(s18, v18, s19, v19, scale);
            return lerp(s19, v19, s20, v20, scale);
        }
    }

    private double lerp(double x0, double y0, double x1, double y1, double x) {
        double t = (x - x0) / (x1 - x0);
        return y0 + (y1 - y0) * t;
    }

    private double getEffectiveScale(Entity owner) {
        ScaleData scaleData = ScaleTypes.BASE.getScaleData(owner);
        return scaleData.getScale();
    }

    private void drawMainChain(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 start, Vec3 end, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(start.x, start.y, start.z);

        float dx = (float) (end.x - start.x);
        float dy = (float) (end.y - start.y);
        float dz = (float) (end.z - start.z);

        float distance = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        float yaw = (float) (Mth.atan2(dx, dz) * (180.0D / Math.PI));
        float pitch = (float) (Mth.atan2(dy, Mth.sqrt(dx * dx + dz * dz)) * (180.0D / Math.PI));

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));

        PoseStack.Pose matrix = poseStack.last();

        float w = 0.25F;
        float offsetX = 0.15F;
        float offsetY = 0.15F;
        float uvOffsetZ = 0.5F;

        vertex(vertexConsumer, matrix, 0.0F, -w + offsetY, 0.0F, 0.0F, 0.0F, packedLight, 1, 0, 0);
        vertex(vertexConsumer, matrix, 0.0F,  w + offsetY, 0.0F, 1.0F, 0.0F, packedLight, 1, 0, 0);
        vertex(vertexConsumer, matrix, 0.0F,  w + offsetY, distance, 1.0F, distance, packedLight, 1, 0, 0);
        vertex(vertexConsumer, matrix, 0.0F, -w + offsetY, distance, 0.0F, distance, packedLight, 1, 0, 0);

        vertex(vertexConsumer, matrix, -w + offsetX, 0.0F, 0.0F, 0.0F, 0.0F + uvOffsetZ, packedLight, 0, 1, 0);
        vertex(vertexConsumer, matrix,  w + offsetX, 0.0F, 0.0F, 1.0F, 0.0F + uvOffsetZ, packedLight, 0, 1, 0);
        vertex(vertexConsumer, matrix,  w + offsetX, 0.0F, distance, 1.0F, distance + uvOffsetZ, packedLight, 0, 1, 0);
        vertex(vertexConsumer, matrix, -w + offsetX, 0.0F, distance, 0.0F, distance + uvOffsetZ, packedLight, 0, 1, 0);

        poseStack.popPose();
    }

    private void drawHookAtTip(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 start, Vec3 end, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(start.x, start.y, start.z);

        float dx = (float) (end.x - start.x);
        float dy = (float) (end.y - start.y);
        float dz = (float) (end.z - start.z);

        float distance = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        float yaw = (float) (Mth.atan2(dx, dz) * (180.0D / Math.PI));
        float pitch = (float) (Mth.atan2(dy, Mth.sqrt(dx * dx + dz * dz)) * (180.0D / Math.PI));

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.translate(0.0F, 0.15F, distance);
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));

        PoseStack.Pose matrix = poseStack.last();
        float hookW = 0.22F;
        float hookH = 0.22F;

        vertex(vertexConsumer, matrix, -hookW, -hookH, 0.0F, 0.0F, 1.0F, packedLight, 0, 0, 1);
        vertex(vertexConsumer, matrix,  hookW, -hookH, 0.0F, 1.0F, 1.0F, packedLight, 0, 0, 1);
        vertex(vertexConsumer, matrix,  hookW,  hookH, 0.0F, 1.0F, 0.0F, packedLight, 0, 0, 1);
        vertex(vertexConsumer, matrix, -hookW,  hookH, 0.0F, 0.0F, 0.0F, packedLight, 0, 0, 1);

        poseStack.popPose();
    }

    private void drawWrappedChains(PoseStack poseStack, VertexConsumer vc, Entity entity, Vec3 projPos,
                                   float partialTicks, int packedLight, boolean isOwner) {
        poseStack.pushPose();

        Vec3 rawAnchor = entity.getPosition(partialTicks).add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        Vec3 finalAnchor = rawAnchor;

        if (isOwner) {
            double scale = getEffectiveScale(entity);
            if (Math.abs(scale - 1.0D) > 0.001D) {
                finalAnchor = getCompensatedAnchorPosition(rawAnchor, projPos, entity);
            }
        }

        Vec3 pos = finalAnchor.subtract(projPos);
        poseStack.translate(pos.x, pos.y, pos.z);

        if (entity instanceof LivingEntity living) {
            float yRot = Mth.rotLerp(partialTicks, living.yBodyRotO, living.yBodyRot);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yRot));
        }

        float w = entity.getBbWidth() / 2.0F + 0.1F;
        float chainH = 0.15F;
        float uTiles = w * 4.0F;

        float[][] rings = new float[][]{
                {15.0F, 0.0F, 10.0F, 1.0F},
                {-20.0F, 25.0F, -15.0F, 1.05F},
                {10.0F, 45.0F, -25.0F, 1.02F},
                {-12.0F, -30.0F, 20.0F, 0.98F}
        };

        for (float[] ring : rings) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(ring[0]));
            poseStack.mulPose(Axis.YP.rotationDegrees(ring[1]));
            poseStack.mulPose(Axis.ZP.rotationDegrees(ring[2]));
            poseStack.scale(ring[3], ring[3], ring[3]);

            PoseStack.Pose matrix = poseStack.last();

            addQuad(matrix, vc, -w, chainH, -w,  w, chainH, -w,  w, -chainH, -w, -w, -chainH, -w, uTiles, packedLight,  0, 0, -1);
            addQuad(matrix, vc,  w, chainH,  w, -w, chainH,  w, -w, -chainH,  w,  w, -chainH,  w, uTiles, packedLight,  0, 0,  1);
            addQuad(matrix, vc, -w, chainH,  w, -w, chainH, -w, -w, -chainH, -w, -w, -chainH,  w, uTiles, packedLight, -1, 0,  0);
            addQuad(matrix, vc,  w, chainH, -w,  w, chainH,  w,  w, -chainH,  w,  w, -chainH, -w, uTiles, packedLight,  1, 0,  0);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void addQuad(PoseStack.Pose matrix, VertexConsumer vc,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float uTiles, int light,
                         float nx, float ny, float nz) {
        vertex(vc, matrix, x1, y1, z1, 0.0F, 0.0F, light, nx, ny, nz);
        vertex(vc, matrix, x2, y2, z2, uTiles, 0.0F, light, nx, ny, nz);
        vertex(vc, matrix, x3, y3, z3, uTiles, 1.0F, light, nx, ny, nz);
        vertex(vc, matrix, x4, y4, z4, 0.0F, 1.0F, light, nx, ny, nz);
    }

    private void vertex(VertexConsumer vc, PoseStack.Pose matrix,
                        float x, float y, float z,
                        float u, float v, int light,
                        float nx, float ny, float nz) {
        vc.vertex(matrix.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(matrix.normal(), nx, ny, nz)
                .endVertex();
    }

    @Override
    public boolean shouldRender(ChainProjectileEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(ChainProjectileEntity entity) {
        return VANILLA_CHAIN_TEXTURE;
    }
}