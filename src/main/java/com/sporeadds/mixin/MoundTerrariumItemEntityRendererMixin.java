package com.sporeadds.mixin;

import net.minecraft.core.registries.BuiltInRegistries;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.blocks.modblocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class MoundTerrariumItemEntityRendererMixin {

    @Unique
    private static final ResourceLocation SPORE_BIOMASS_BULB_ID = ResourceLocation.fromNamespaceAndPath("spore", "biomass_bulb");

    @Unique
    private static boolean sporeadds$shouldShrink(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        Block block = blockItem.getBlock();

        if (block == modblocks.MOUND_TERRARIUM.get()) {
            return true;
        }

        Block biomassBulb = BuiltInRegistries.BLOCK.get(SPORE_BIOMASS_BULB_ID);
        return biomassBulb != null && block == biomassBulb;
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void sporeadds$scaleDroppedItems(ItemEntity itemEntity, float entityYaw, float partialTicks,
                                             PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                             CallbackInfo ci) {
        if (sporeadds$shouldShrink(itemEntity.getItem())) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.18D, 0.0D);
            poseStack.scale(0.3333F, 0.3333F, 0.3333F);
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN")
    )
    private void sporeadds$unscaleDroppedItems(ItemEntity itemEntity, float entityYaw, float partialTicks,
                                               PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                               CallbackInfo ci) {
        if (sporeadds$shouldShrink(itemEntity.getItem())) {
            poseStack.popPose();
        }
    }
}