package com.sporeadds.sporeaddsmod.client.renderer;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

public class DataDropItemRenderer extends ItemEntityRenderer {

    public DataDropItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ItemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Player localPlayer = Minecraft.getInstance().player;

        boolean isScientist = localPlayer != null
                && localPlayer.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "scientist".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);

        if (!isScientist) {
            return;
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}