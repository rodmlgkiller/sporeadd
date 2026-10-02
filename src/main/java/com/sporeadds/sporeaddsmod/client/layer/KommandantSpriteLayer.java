package com.sporeadds.sporeaddsmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KommandantSpriteLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/pellet1.png"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/pellet2.png"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/pellet3.png"),
            ResourceLocation.fromNamespaceAndPath("sporeadd", "textures/entity/kommandant/pellet4.png")
    };

    public static class PelletConfig {
        public float offsetX, offsetY, offsetZ, rotX, rotY, rotZ, scale;
        public int textureIndex;

        public PelletConfig(float ox, float oy, float oz, float rx, float ry, float rz, float s, int texIndex) {
            this.offsetX = ox;
            this.offsetY = oy;
            this.offsetZ = oz;
            this.rotX = rx;
            this.rotY = ry;
            this.rotZ = rz;
            this.scale = s;
            this.textureIndex = texIndex;
        }
    }

    public static final Map<UUID, PelletConfig[]> playerPellets = new ConcurrentHashMap<>();

    public static PelletConfig[] getDefaultPellets(UUID playerUuid) {
        Random random = new Random(playerUuid.getLeastSignificantBits());

        return new PelletConfig[] {
                new PelletConfig(0.28F, -0.10F, -0.17F, 76.0F, -15.0F, 0.0F, 1.0F, random.nextInt(4)),
                new PelletConfig(0.28F,  0.25F, -0.25F, 100.0F, -15.0F, 0.0F, 1.0F, random.nextInt(4)),
                new PelletConfig(0.35F, 0.03F, -0.47F, 76.0F, 15.0F, 0.0F, 1.0F, random.nextInt(4)),
                new PelletConfig(0.35F, 0.35F, -0.30F, 100.0F, 15.0F, 0.0F, 1.0F, random.nextInt(4)),
                new PelletConfig(0.20F, 0.20F, -0.15F, 90.0F, -10.0F, 0.0F, 1.0F, random.nextInt(4)),
                new PelletConfig(0.25F, 0.52F, -0.15F, 105.0F, -10.0F, 0.0F, 1.0F, random.nextInt(4)),
                new PelletConfig(0.35F, 0.35F, -0.35F, 90.0F, 10.0F, 0.0F, 1.0F, random.nextInt(4)),
                new PelletConfig(0.35F, 0.65F, -0.35F, 105.0F, 10.0F, 0.0F, 1.0F, random.nextInt(4))
        };
    }

    public KommandantSpriteLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    private static boolean isKommandant(AbstractClientPlayer player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> {
                    String id = data.getIdentifier();
                    return id != null && id.equals("kommandant");
                })
                .orElse(false);
    }

    @Override
    public void render(@NotNull PoseStack stack, @NotNull MultiBufferSource buffer, int packedLight,
                       @NotNull AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!SporeAddsConfig.KOMMANDANT_PELLET_EFFECTS.get()) return;
        if (!player.isAlive() || player.isInvisible() || !isKommandant(player)) return;

        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
        PelletConfig[] config = playerPellets.computeIfAbsent(player.getUUID(), k -> getDefaultPellets(player.getUUID()));

        renderPelletGroup(stack, buffer, packedLight, overlay, this.getParentModel().rightArm, config, 0);
        renderPelletGroup(stack, buffer, packedLight, overlay, this.getParentModel().leftArm, config, 2);
        renderPelletGroup(stack, buffer, packedLight, overlay, this.getParentModel().rightLeg, config, 4);
        renderPelletGroup(stack, buffer, packedLight, overlay, this.getParentModel().leftLeg, config, 6);
    }

    private void renderPelletGroup(PoseStack stack, MultiBufferSource buffer, int packedLight, int overlay,
                                   net.minecraft.client.model.geom.ModelPart part, PelletConfig[] allPellets, int startIndex) {
        stack.pushPose();
        part.translateAndRotate(stack);

        for (int i = 0; i < 2; i++) {
            PelletConfig pellet = allPellets[startIndex + i];
            stack.pushPose();

            stack.translate(pellet.offsetX, pellet.offsetY, pellet.offsetZ);
            stack.scale(pellet.scale, pellet.scale, pellet.scale);

            if (pellet.rotY != 0.0F) stack.mulPose(Axis.YP.rotationDegrees(pellet.rotY));
            if (pellet.rotX != 0.0F) stack.mulPose(Axis.XP.rotationDegrees(pellet.rotX));
            if (pellet.rotZ != 0.0F) stack.mulPose(Axis.ZP.rotationDegrees(pellet.rotZ));

            PoseStack.Pose pose = stack.last();
            Matrix4f poseMatrix = pose.pose();
            Matrix3f normalMatrix = pose.normal();

            float minX = -0.5F;
            float maxX = 0.5F;
            float minY = -0.5F;
            float maxY = 0.5F;
            float frontZ = 0.001F;
            float backZ = -0.001F;

            ResourceLocation chosenTexture = TEXTURES[pellet.textureIndex];
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(chosenTexture));

            consumer.vertex(poseMatrix, minX, minY, frontZ).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
            consumer.vertex(poseMatrix, maxX, minY, frontZ).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
            consumer.vertex(poseMatrix, maxX, maxY, frontZ).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();
            consumer.vertex(poseMatrix, minX, maxY, frontZ).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, 1).endVertex();

            consumer.vertex(poseMatrix, minX, maxY, backZ).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, -1).endVertex();
            consumer.vertex(poseMatrix, maxX, maxY, backZ).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, -1).endVertex();
            consumer.vertex(poseMatrix, maxX, minY, backZ).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, -1).endVertex();
            consumer.vertex(poseMatrix, minX, minY, backZ).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, 0, 0, -1).endVertex();

            stack.popPose();
        }

        stack.popPose();
    }
}