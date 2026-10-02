package com.sporeadds.sporeaddsmod.client.renderer.item;

import com.sporeadds.sporeaddsmod.util.ItemNbt;

import net.minecraft.core.registries.BuiltInRegistries;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sporeadds.sporeaddsmod.blocks.modblocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class MoundTerrariumItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation MOUND_ID = ResourceLocation.fromNamespaceAndPath("spore", "mound");
    private Entity cachedMound;

    public MoundTerrariumItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
        boolean hasMound = ItemNbt.hasTag(stack) && ItemNbt.getTag(stack) != null && ItemNbt.getTag(stack).getBoolean("HasMound");
        BlockState state = modblocks.MOUND_TERRARIUM.get().defaultBlockState()
                .setValue(com.sporeadds.sporeaddsmod.blocks.blocks_entity.MoundTerrariumBlock.HAS_MOUND, hasMound)
                .setValue(com.sporeadds.sporeaddsmod.blocks.blocks_entity.MoundTerrariumBlock.LINKED, false);

        poseStack.pushPose();

        if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            poseStack.translate(0.5D, 0.35D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(25.0F));
            poseStack.scale(0.3333F, 0.3333F, 0.3333F);
            poseStack.translate(-0.5D, 0.0D, -0.5D);
        } else if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            poseStack.translate(0.5D, 0.25D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
            poseStack.scale(0.3333F, 0.3333F, 0.3333F);
            poseStack.translate(-0.5D, 0.0D, -0.5D);
        } else {
            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(-0.5D, 0.0D, -0.5D);
        }

        blockRenderer.renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        if (hasMound) {
            if (cachedMound == null) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(MOUND_ID);
                if (type != null) {
                    cachedMound = type.create(mc.level);
                }
            }

            if (cachedMound != null) {
                cachedMound.tickCount = mc.player != null ? mc.player.tickCount : cachedMound.tickCount;

                poseStack.pushPose();
                poseStack.translate(0.5D, 0.0625D, 0.5D);

                if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                        || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(25.0F));
                } else if (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                        || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
                }

                poseStack.scale(0.23F, 0.23F, 0.23F);

                EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
                dispatcher.setRenderShadow(false);
                dispatcher.render(cachedMound, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, poseStack, buffer, packedLight);
                dispatcher.setRenderShadow(true);

                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }
}