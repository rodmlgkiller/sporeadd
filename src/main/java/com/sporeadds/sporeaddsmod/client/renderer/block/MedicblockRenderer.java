package com.sporeadds.sporeaddsmod.client.renderer.block;

import net.neoforged.fml.common.EventBusSubscriber;

import com.mojang.math.Axis;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.MedicBlockEntity;
import com.sporeadds.sporeaddsmod.blocks.blocks_entity.modblocksentity;
import com.sporeadds.sporeaddsmod.client.model.ModelMedic_block;
import com.sporeadds.sporeaddsmod.client.model.animations.Medic_blockAnimation;
import com.sporeadds.sporeaddsmod.procedures.MedicblockClosePlaybackConditionProcedure;
import com.sporeadds.sporeaddsmod.procedures.MedicblockOpenPlaybackConditionProcedure;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HierarchicalModel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MedicblockRenderer implements BlockEntityRenderer<MedicBlockEntity> {
	private final CustomHierarchicalModel model;
	private final ResourceLocation texture;
	private int tickCount;

	MedicblockRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new CustomHierarchicalModel(context.bakeLayer(ModelMedic_block.LAYER_LOCATION));
		this.texture = ResourceLocation.parse("sporeadd:textures/block/medic_block_texture.png");
	}

	private void updateRenderState(MedicBlockEntity blockEntity) {
		tickCount = (int) blockEntity.getLevel().getGameTime();
		blockEntity.animationState0.animateWhen(blockEntity.getPersistentData().getBoolean("open"), tickCount);
		blockEntity.animationState1.animateWhen(!blockEntity.getPersistentData().getBoolean("open"), tickCount);
	}

	@Override
	public void render(MedicBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource renderer, int light, int overlayLight) {
		updateRenderState(blockEntity);
		poseStack.pushPose();
		Direction direction = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

		poseStack.translate(0.5, 0.5, 0.5);

		Quaternionf angle = switch (direction) {
			case NORTH -> Axis.YP.rotationDegrees(180f);
			case EAST  -> Axis.YP.rotationDegrees(90f);
			case SOUTH -> Axis.YP.rotationDegrees(0f);
			case WEST  -> Axis.YP.rotationDegrees(270f);
			default -> Axis.YP.rotationDegrees(0f);
		};
		poseStack.mulPose(angle);

		poseStack.mulPose(Axis.XP.rotationDegrees(180f));

		poseStack.translate(0, -1, 0);

		VertexConsumer builder = renderer.getBuffer(RenderType.entityCutout(texture));
		model.setupBlockEntityAnim(blockEntity, blockEntity.getLevel().getGameTime() + partialTick);
		model.renderToBuffer(poseStack, builder, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
		poseStack.popPose();
		if (blockEntity.getLevel().isClientSide()) {blockEntity.animationState2.animateWhen(!blockEntity.getPersistentData().getBoolean("open") && blockEntity.isCrafting() && !blockEntity.animationState1.isStarted(), getTickCount());}
	}

	public int getTickCount() {
		return tickCount;
	}

	@SubscribeEvent
	public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(modblocksentity.MEDIC_BLOCK_ENTITY.get(), MedicblockRenderer::new);
	}

	public static final class CustomHierarchicalModel extends ModelMedic_block {
		private final ModelPart root;
		private final BlockEntityHierarchicalModel animator = new BlockEntityHierarchicalModel();

		public CustomHierarchicalModel(ModelPart root) {
			super(root);
			this.root = root;
		}

		public void setupBlockEntityAnim(MedicBlockEntity blockEntity, float ageInTicks) {
			animator.setupBlockEntityAnim(blockEntity, ageInTicks);
			super.setupAnim(null, 0, 0, ageInTicks, 0, 0);
		}

		public ModelPart getRoot() {
			return root;
		}

		public class BlockEntityHierarchicalModel extends HierarchicalModel<Entity> {
			@Override
			public ModelPart root() {
				return root;
			}

			@Override
			public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			}

			public void setupBlockEntityAnim(MedicBlockEntity blockEntity, float ageInTicks) {
				animator.root().getAllParts().forEach(ModelPart::resetPose);
				animator.animate(blockEntity.animationState0, Medic_blockAnimation.open, ageInTicks, 1f);
				animator.animate(blockEntity.animationState1, Medic_blockAnimation.close, ageInTicks, 1f);
				animator.animate(blockEntity.animationState2, Medic_blockAnimation.woriking, ageInTicks, 1f);
			}
		}
	}
}