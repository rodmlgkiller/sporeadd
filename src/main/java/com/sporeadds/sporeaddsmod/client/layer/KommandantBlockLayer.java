package com.sporeadds.sporeaddsmod.client.layer;

import net.minecraft.core.registries.BuiltInRegistries;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KommandantBlockLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public static class BlomfungConfig {
        public float offsetX, offsetY, offsetZ, rotX, rotY, rotZ, scale;

        public BlomfungConfig(float ox, float oy, float oz, float rx, float ry, float rz, float s) {
            this.offsetX = ox;
            this.offsetY = oy;
            this.offsetZ = oz;
            this.rotX = rx;
            this.rotY = ry;
            this.rotZ = rz;
            this.scale = s;
        }
    }

    public static final Map<UUID, BlomfungConfig[]> playerConfigs = new ConcurrentHashMap<>();

    public static BlomfungConfig[] getDefaultConfigs() {
        return new BlomfungConfig[]{
                new BlomfungConfig(-0.15F, -0.485F, -0.05F, 180.0F, -22.5F, 12.5F, 0.5F),
                new BlomfungConfig(0.1F, -0.13F, 0.00F, 0.0F, 35.0F, -157.463F, 0.3F)
        };
    }

    public KommandantBlockLayer(@NotNull RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    private static boolean isKommandant(AbstractClientPlayer player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> {
                    String id = data.getIdentifier();
                    return id != null && id.equals("kommandant");
                })
                .orElse(false);
    }

    @Override
    public void render(@NotNull PoseStack stack, @NotNull MultiBufferSource buffer, int packedLight,
                       @NotNull AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        if (!SporeAddsConfig.KOMMANDANT_BLOMFUNG_EFFECTS.get()) return;
        if (!player.isAlive() || player.isInvisible() || !isKommandant(player)) return;

        BlockState blockState = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("spore", "blomfung")).defaultBlockState();
        if (blockState == null || blockState.isAir()) return;

        int packedOverlay = LivingEntityRenderer.getOverlayCoords(player, 0);
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        BlomfungConfig[] configs = playerConfigs.computeIfAbsent(player.getUUID(), k -> getDefaultConfigs());
        BlomfungConfig principal = configs[0];
        BlomfungConfig secundario = configs[1];

        stack.pushPose();
        this.getParentModel().head.translateAndRotate(stack);

        stack.pushPose();
        stack.translate(principal.offsetX, principal.offsetY, principal.offsetZ);
        stack.scale(principal.scale, principal.scale, principal.scale);

        if (principal.rotY != 0.0F) stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(principal.rotY));
        if (principal.rotX != 0.0F) stack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(principal.rotX));
        if (principal.rotZ != 0.0F) stack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(principal.rotZ));

        stack.translate(-0.5D, 0.0D, -0.5D);
        renderBlock(blockRenderer, blockState, stack, buffer, packedLight, packedOverlay);
        stack.popPose();
        stack.popPose();

        stack.pushPose();
        this.getParentModel().leftArm.translateAndRotate(stack);

        stack.pushPose();
        stack.translate(secundario.offsetX, secundario.offsetY, secundario.offsetZ);
        stack.scale(secundario.scale, secundario.scale, secundario.scale);

        if (secundario.rotY != 0.0F) stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(secundario.rotY));
        if (secundario.rotX != 0.0F) stack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(secundario.rotX));
        if (secundario.rotZ != 0.0F) stack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(secundario.rotZ));

        stack.translate(-0.5D, 0.0D, -0.5D);
        renderBlock(blockRenderer, blockState, stack, buffer, packedLight, packedOverlay);
        stack.popPose();
        stack.popPose();
    }

    private void renderBlock(BlockRenderDispatcher blockRenderer, BlockState blockState, PoseStack stack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        try {
            blockRenderer.renderSingleBlock(
                    blockState, stack, buffer, packedLight, packedOverlay,
                    net.neoforged.neoforge.client.model.data.ModelData.EMPTY, null
            );
        } catch (NoSuchMethodError e) {
            blockRenderer.renderSingleBlock(blockState, stack, buffer, packedLight, packedOverlay);
        }
    }
}