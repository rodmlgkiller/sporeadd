package com.sporeadds.sporeaddsmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sporeadds.sporeaddsmod.entity.projectile.TentacleProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TentacleProjectileRenderer extends EntityRenderer<TentacleProjectile> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/tentacle.png");
    private static final ResourceLocation BODY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/tendril.png");

    private static final double BASE_FADE_MIN_DISTANCE = 1.0D;
    private static final double BASE_FADE_MAX_DISTANCE = 3.5D;

    private final Map<UUID, List<Vec3>> chainsMap = new HashMap<>();

    public TentacleProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(TentacleProjectile entity, Frustum frustum, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void render(TentacleProjectile entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        Vec3 entityPos = entity.getPosition(partialTick);
        Vec3 cameraPos = this.entityRenderDispatcher.camera.getPosition();

        Entity owner = entity.getOwner();
        double ownerScale = owner != null ? getEffectiveScale(owner) : 1.0D;
        double safeOwnerScale = Math.max(ownerScale, 0.001D);

        float projectileAlpha = calculateAlphaFromDistance(entityPos.distanceTo(cameraPos) / safeOwnerScale);

        poseStack.pushPose();
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.scale(0.9F, 0.9F, 0.9F);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        renderCrossedSprite(poseStack, consumer, packedLight, projectileAlpha);
        poseStack.popPose();

        if (owner != null) {
            Vec3 rawOwnerPos = owner.getPosition(partialTick).add(0.0D, owner.getBbHeight() * 0.65D, 0.0D);
            Vec3 compensatedOwnerPos = getCompensatedAnchorPosition(rawOwnerPos, entityPos, owner);

            applyIK(partialTick, entity, compensatedOwnerPos);

            boolean localOwnerView = isLocalOwnerView(entity);
            boolean hasCapturedVictim = entity.getVictimById() != null;
            boolean applyOwnerTransparency = localOwnerView && !hasCapturedVictim;

            poseStack.pushPose();
            poseStack.translate(-entityPos.x, -entityPos.y, -entityPos.z);
            renderTentacleChain(entity, poseStack, packedLight, buffer, applyOwnerTransparency);
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private boolean isLocalOwnerView(TentacleProjectile projectile) {
        Minecraft minecraft = Minecraft.getInstance();
        Player localPlayer = minecraft.player;
        if (localPlayer == null) {
            return false;
        }

        Entity owner = projectile.getOwnerById();
        return owner != null && owner.getId() == localPlayer.getId();
    }

    private float calculateAlphaFromDistance(double normalizedDistance) {
        if (normalizedDistance <= BASE_FADE_MIN_DISTANCE) return 0.0F;
        if (normalizedDistance >= BASE_FADE_MAX_DISTANCE) return 1.0F;

        return (float) ((normalizedDistance - BASE_FADE_MIN_DISTANCE)
                / (BASE_FADE_MAX_DISTANCE - BASE_FADE_MIN_DISTANCE));
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
        double clampedScale = Mth.clamp(scale, 1.0D, 1.50D);
        double x = clampedScale - 1.0D;

        return 1.01668492D * x
                - 0.99113413D * x * x
                + 0.58265762D * x * x * x;
    }

    private double getEffectiveScale(Entity owner) {
        ScaleData scaleData = ScaleTypes.BASE.getScaleData(owner);
        return scaleData.getScale();
    }

    public void applyIK(float partial, TentacleProjectile t, Vec3 camera) {
        UUID id = t.getUUID();
        List<Vec3> entities = chainsMap.computeIfAbsent(id, k -> new ArrayList<>());

        Vec3 basePos = t.getPosition(partial);
        double distance = basePos.distanceTo(camera);
        int desiredSegments = Mth.clamp((int) (distance * 1.1D), 4, 40);

        if (desiredSegments != entities.size()) {
            entities.clear();

            for (int i = 0; i < desiredSegments; ++i) {
                double pct = (double) i / (double) (desiredSegments - 1);
                entities.add(new Vec3(
                        Mth.lerp(pct, basePos.x, camera.x),
                        Mth.lerp(pct, basePos.y, camera.y),
                        Mth.lerp(pct, basePos.z, camera.z)
                ));
            }
        }

        if (entities.size() >= 3) {
            moveSegmentTowards(entities, entities.size() - 1, camera, true);

            for (int i = entities.size() - 2; i >= 0; --i) {
                Vec3 nextPos = entities.get(i + 1);
                Vec3 dir = entities.get(i).subtract(nextPos);
                float segmentLength = 1.0F;

                if (dir.lengthSqr() > 1.0E-4D) {
                    dir = dir.normalize().scale(segmentLength);
                } else {
                    dir = new Vec3(segmentLength, 0.0D, 0.0D);
                }

                Vec3 solvedPos = nextPos.add(dir);
                moveSegmentTowards(entities, i, solvedPos, entities.get(i + 1).distanceTo(entities.get(i)) > 10.0D);
            }

            entities.set(0, basePos);

            for (int i = 1; i < entities.size(); ++i) {
                Vec3 prevPos = entities.get(i - 1);
                Vec3 dir = entities.get(i).subtract(prevPos);
                float segmentLength = 1.0F;

                if (dir.lengthSqr() > 1.0E-4D) {
                    dir = dir.normalize().scale(segmentLength);
                } else {
                    dir = new Vec3(segmentLength, 0.0D, 0.0D);
                }

                Vec3 solvedPos = prevPos.add(dir);
                moveSegmentTowards(entities, i, solvedPos, entities.get(i - 1).distanceTo(entities.get(i)) > 10.0D);
            }
        }
    }

    private void moveSegmentTowards(List<Vec3> entities, int index, Vec3 target, boolean far) {
        Vec3 currentPos = entities.get(index);
        Vec3 newPos = currentPos.lerp(target, 0.35F);
        entities.set(index, far ? target : newPos);
    }

    private void renderTentacleChain(TentacleProjectile t, PoseStack stack, int light,
                                     MultiBufferSource buffer, boolean applyOwnerTransparency) {
        List<Vec3> entities = chainsMap.get(t.getUUID());
        if (entities == null || entities.size() < 2) return;

        Vec3 origin = null;

        for (int i = 0; i < entities.size(); i++) {
            Vec3 currentPos = entities.get(i);
            if (origin != null) {
                float alpha = applyOwnerTransparency ? getOwnerSegmentAlpha(i, entities.size()) : 1.0F;

                if (alpha > 0.0F) {
                    renderTentacleSegment(origin, currentPos, light, stack, buffer, i, entities.size(), alpha);
                }
            }
            origin = currentPos;
        }
    }

    private float getOwnerSegmentAlpha(int index, int totalSize) {
        int segmentFromOwner = (totalSize - 1) - index;

        return switch (segmentFromOwner) {
            case 0 -> 0.0F;
            case 1 -> 0.2F;
            case 2 -> 0.4F;
            case 3 -> 0.6F;
            case 4 -> 0.8F;
            default -> 1.0F;
        };
    }

    private void renderTentacleSegment(Vec3 from, Vec3 to, int light, PoseStack stack,
                                       MultiBufferSource buffer, int index, int totalSize, float alpha) {
        Vec3 direction = to.subtract(from);
        float length = (float) direction.length();
        if (length < 0.0001F) return;

        direction = direction.normalize();
        float yaw = (float) Math.atan2(direction.x, direction.z);
        float pitch = (float) (-Math.asin(direction.y));

        float progress = (float) index / Math.max(1, totalSize - 1);
        float width = Mth.lerp(progress, 0.04F, 0.12F);

        stack.pushPose();
        stack.translate(from.x, from.y, from.z);
        stack.mulPose(Axis.YP.rotation(yaw));
        stack.mulPose(Axis.XP.rotation(pitch));

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(BODY_TEXTURE));
        renderBodyBox(stack, consumer, light, width, length, alpha);

        stack.popPose();
    }

    private void renderBodyBox(PoseStack poseStack, VertexConsumer consumer, int light, float hw, float length, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float z0 = 0.0F;
        float z1 = length;

        addVertex(consumer, pose, normal, light, -hw, hw, z0, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, hw, hw, z0, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, hw, hw, z1, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, -hw, hw, z1, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, alpha);

        addVertex(consumer, pose, normal, light, -hw, -hw, z1, 0.0F, 1.0F, 0.0F, -1.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, hw, -hw, z1, 1.0F, 1.0F, 0.0F, -1.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, hw, -hw, z0, 1.0F, 0.0F, 0.0F, -1.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, -hw, -hw, z0, 0.0F, 0.0F, 0.0F, -1.0F, 0.0F, alpha);

        addVertex(consumer, pose, normal, light, hw, hw, z0, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, hw, -hw, z0, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, hw, -hw, z1, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, hw, hw, z1, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, alpha);

        addVertex(consumer, pose, normal, light, -hw, hw, z1, 0.0F, 1.0F, -1.0F, 0.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, -hw, -hw, z1, 1.0F, 1.0F, -1.0F, 0.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, -hw, -hw, z0, 1.0F, 0.0F, -1.0F, 0.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, -hw, hw, z0, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, alpha);
    }

    private void renderCrossedSprite(PoseStack poseStack, VertexConsumer consumer, int light, float alpha) {
        renderProjectilePlane(poseStack, consumer, light, alpha);
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        renderProjectilePlane(poseStack, consumer, light, alpha);
        poseStack.popPose();
    }

    private void renderProjectilePlane(PoseStack poseStack, VertexConsumer consumer, int light, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        float halfWidth = 0.25F;
        float back = -0.5F;
        float front = 0.5F;

        addVertex(consumer, pose, normal, light, 0.0F, -halfWidth, back, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, 0.0F, halfWidth, back, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, 0.0F, halfWidth, front, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, alpha);
        addVertex(consumer, pose, normal, light, 0.0F, -halfWidth, front, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, alpha);
    }

    private void addVertex(VertexConsumer consumer, PoseStack.Pose pose, Matrix3f normal, int light,
                           float x, float y, float z, float u, float v, float nx, float ny, float nz, float alpha) {
        int alphaInt = (int) (alpha * 255.0F);
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, alphaInt)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz)
                ;
    }

    @Override
    public ResourceLocation getTextureLocation(TentacleProjectile entity) {
        return TEXTURE;
    }
}