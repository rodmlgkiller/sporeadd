package com.sporeadds.sporeaddsmod.client.renderer.block;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.blocks.blocks_entity.ScientistBlockEntity;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.modblocksentity;
import com.sporeadds.sporeaddsmod.blocks.scientistblock;
import com.sporeadds.sporeaddsmod.client.model.Modelscientist_block;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ScientistblokcRenderer implements BlockEntityRenderer<ScientistBlockEntity> {
    private final CustomHierarchicalModel model;
    private final ResourceLocation texture;

    ScientistblokcRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CustomHierarchicalModel(context.bakeLayer(Modelscientist_block.LAYER_LOCATION));
        this.texture = ResourceLocation.parse("sporeadd:textures/block/scientistblock_texture.png");
    }

    @Override
    public void render(ScientistBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource renderer, int light, int overlayLight) {
        poseStack.pushPose();
        poseStack.scale(-1, -1, 1);
        poseStack.translate(-0.5, -0.5, 0.5);
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(scientistblock.FACING);
        switch (facing) {
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
        }
        poseStack.translate(0, -1, 0);
        VertexConsumer builder = renderer.getBuffer(RenderType.entityCutout(texture));
        model.setupBlockEntityAnim(blockEntity, blockEntity.getLevel().getGameTime() + partialTick);
        model.renderToBuffer(poseStack, builder, light, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(modblocksentity.SCIENTIST_BLOCK_ENTITY.get(), ScientistblokcRenderer::new);
    }

    private static final class CustomHierarchicalModel extends Modelscientist_block {
        private final ModelPart root;
        private final BlockEntityHierarchicalModel animator = new BlockEntityHierarchicalModel();

        public CustomHierarchicalModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        public void setupBlockEntityAnim(ScientistBlockEntity blockEntity, float ageInTicks) {
            animator.setupBlockEntityAnim(blockEntity, ageInTicks);
            super.setupAnim(null, 0, 0, ageInTicks, 0, 0);
        }

        public ModelPart getRoot() {
            return root;
        }

        private class BlockEntityHierarchicalModel extends HierarchicalModel<Entity> {
            @Override
            public ModelPart root() {
                return root;
            }

            @Override
            public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            }

            public void setupBlockEntityAnim(ScientistBlockEntity blockEntity, float ageInTicks) {
                animator.root().getAllParts().forEach(ModelPart::resetPose);
            }
        }
    }
}