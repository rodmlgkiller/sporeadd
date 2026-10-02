package com.sporeadds.sporeaddsmod.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MoundTerrariumBlock;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MoundTerrariumBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;

public class MoundTerrariumBlockEntityRenderer implements BlockEntityRenderer<MoundTerrariumBlockEntity> {

    private static final ResourceLocation MOUND_ID = new ResourceLocation("spore", "mound");
    private Entity cachedMound;
    private Boolean lastLinkedState = null;

    public MoundTerrariumBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MoundTerrariumBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        if (Minecraft.getInstance().level == null) return;

        if (!blockEntity.getBlockState().getValue(MoundTerrariumBlock.HAS_MOUND)) {
            return;
        }

        boolean shouldBeLinked = blockEntity.getHp() < 15;

        if (cachedMound == null || lastLinkedState == null || lastLinkedState != shouldBeLinked) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(MOUND_ID);
            if (type == null) return;

            cachedMound = type.create(Minecraft.getInstance().level);
            if (cachedMound == null) return;

            trySetMoundAge(cachedMound, 1);
            trySetMoundLinked(cachedMound, shouldBeLinked);
            lastLinkedState = shouldBeLinked;
        }

        cachedMound.tickCount = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.tickCount
                : cachedMound.tickCount;

        poseStack.pushPose();

        poseStack.translate(0.5D, 0.0625D, 0.5D);
        poseStack.scale(1.0F, 1.0F, 1.0F);

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        dispatcher.render(cachedMound, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, poseStack, buffer, packedLight);
        dispatcher.setRenderShadow(true);

        poseStack.popPose();
    }

    private void trySetMoundAge(Entity entity, int age) {
        try {
            Method setAge = entity.getClass().getMethod("setAge", int.class);
            setAge.invoke(entity, age);
        } catch (Exception ignored) {
        }
    }

    private void trySetMoundLinked(Entity entity, boolean linked) {
        try {
            Method setLinked = entity.getClass().getMethod("setLinked", boolean.class);
            setLinked.invoke(entity, linked);
            return;
        } catch (Exception ignored) {
        }

        entity.getPersistentData().putBoolean("linked", linked);
    }
}