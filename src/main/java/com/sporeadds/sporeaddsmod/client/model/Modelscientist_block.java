package com.sporeadds.sporeaddsmod.client.model;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class Modelscientist_block<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("pruevasmod", "modelscientist_block"), "main");
	public final ModelPart bb_main;

	public Modelscientist_block(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main",
				CubeListBuilder.create().texOffs(89, 116).addBox(-7.55F, -1.75F, 5.65F, 0.25F, 1.25F, 0.2F, new CubeDeformation(0.0F)).texOffs(89, 116).addBox(-6.7F, -1.75F, 5.65F, 0.25F, 1.25F, 0.2F, new CubeDeformation(0.0F)).texOffs(60, 43)
						.addBox(-7.75F, -2.0F, 5.15F, 1.5F, 0.25F, 1.2F, new CubeDeformation(0.0F)).texOffs(22, 56).addBox(-2.0F, -11.9F, 4.775F, 9.0F, 9.25F, 2.225F, new CubeDeformation(0.0F)).texOffs(20, 71)
						.addBox(6.845F, -12.5F, 4.7684F, 0.75F, 10.5F, 1.775F, new CubeDeformation(0.0F)).texOffs(14, 71).addBox(-2.595F, -12.5F, 4.7684F, 0.75F, 10.5F, 1.775F, new CubeDeformation(0.0F)).texOffs(0, 47)
						.addBox(-3.0F, -12.5F, -5.0F, 0.5F, 10.5F, 9.65F, new CubeDeformation(0.0F)).texOffs(14, 68).addBox(5.75F, -2.0F, 5.15F, 1.5F, 0.25F, 1.2F, new CubeDeformation(0.0F)).texOffs(89, 116)
						.addBox(6.8F, -1.75F, 5.65F, 0.25F, 1.25F, 0.2F, new CubeDeformation(0.0F)).texOffs(89, 116).addBox(5.95F, -1.75F, 5.65F, 0.25F, 1.25F, 0.2F, new CubeDeformation(0.0F)).texOffs(36, 16)
						.addBox(7.545F, -6.7F, 5.2684F, 0.45F, 0.2F, 0.225F, new CubeDeformation(0.0F)).texOffs(36, 16).addBox(7.945F, -7.3F, 5.2684F, 0.25F, 0.8F, 0.225F, new CubeDeformation(0.0F)).texOffs(41, 107)
						.addBox(-8.0F, -12.5F, -4.5F, 5.0F, 0.5F, 11.0F, new CubeDeformation(0.0F)).texOffs(34, 44).addBox(-8.0F, -2.5F, -4.5F, 5.0F, 0.5F, 11.0F, new CubeDeformation(0.0F)).texOffs(66, 51)
						.addBox(-8.0F, -12.5F, 6.5F, 5.0F, 10.5F, 0.5F, new CubeDeformation(0.0F)).texOffs(62, 66).addBox(-8.0F, -12.5F, -5.0F, 5.0F, 10.5F, 0.5F, new CubeDeformation(0.0F)).texOffs(0, 21)
						.addBox(-8.0F, -9.25F, -5.0F, 5.0F, 0.5F, 12.0F, new CubeDeformation(0.0F)).texOffs(0, 34).addBox(-8.0F, -5.5F, -5.0F, 5.0F, 0.5F, 12.0F, new CubeDeformation(0.0F)).texOffs(34, 21)
						.addBox(-3.5F, -12.5F, -5.0F, 0.5F, 10.5F, 11.5F, new CubeDeformation(0.0F)).texOffs(83, 17).addBox(-5.1F, -5.7F, -4.0F, 0.7F, 0.2F, 10.0F, new CubeDeformation(0.0F)).texOffs(85, 28)
						.addBox(-6.1F, -5.7F, -4.0F, 0.7F, 0.2F, 8.0F, new CubeDeformation(0.0F)).texOffs(88, 11).addBox(-7.1F, -5.7F, -4.0F, 0.7F, 0.2F, 5.0F, new CubeDeformation(0.0F)).texOffs(30, 117)
						.addBox(-7.0F, -6.5F, 7.0F, 3.0F, 3.5F, 0.1F, new CubeDeformation(0.0F)).texOffs(110, 119).addBox(1.895F, -16.5428F, -6.5226F, 0.9F, 0.75F, 8.0F, new CubeDeformation(0.0F)).texOffs(116, 122)
						.addBox(-1.105F, -15.5428F, -6.5226F, 0.9F, 0.75F, 5.0F, new CubeDeformation(0.0F)).texOffs(116, 122).addBox(4.895F, -15.5428F, -6.5226F, 0.9F, 0.75F, 5.0F, new CubeDeformation(0.0F)).texOffs(21, 109)
						.addBox(-7.5F, -8.45F, 7.0F, 4.0F, 0.2F, 0.2F, new CubeDeformation(0.0F)).texOffs(21, 109).addBox(-3.7F, -10.65F, 7.0F, 0.2F, 2.4F, 0.2F, new CubeDeformation(0.0F)).texOffs(21, 109)
						.addBox(-7.5F, -10.75F, 7.0F, 4.0F, 0.2F, 0.2F, new CubeDeformation(0.0F)).texOffs(21, 109).addBox(-7.5F, -10.65F, 7.0F, 0.2F, 2.4F, 0.2F, new CubeDeformation(0.0F)).texOffs(18, 117)
						.addBox(-7.3F, -10.55F, 7.0F, 3.6F, 2.1F, 0.15F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(82, 94).addBox(-0.975F, -1.0532F, -0.4323F, 2.0F, 0.05F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.825F, -10.2438F, -0.1279F, 2.0944F, 0.0F, -1.5708F));
		PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(80, 94).addBox(-0.975F, -1.0504F, -0.4391F, 2.0F, 0.05F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.825F, -10.2438F, -0.1279F, 0.2618F, 0.0F, 1.5708F));
		PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(82, 93).addBox(-0.975F, -1.051F, -0.4405F, 2.0F, 0.05F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.825F, -10.2438F, -0.1279F, -2.618F, 0.0F, -1.5708F));
		PartDefinition cube_r4 = bb_main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(82, 94).addBox(-0.975F, -1.0547F, -0.4433F, 2.0F, 0.05F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.825F, -10.2438F, -0.1279F, -1.8326F, 0.0F, -1.5708F));
		PartDefinition cube_r5 = bb_main.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(82, 94).addBox(-0.975F, -1.0592F, -0.4427F, 2.0F, 0.05F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.825F, -10.2438F, -0.1279F, -1.0472F, 0.0F, -1.5708F));
		PartDefinition cube_r6 = bb_main.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(80, 94).addBox(-1.025F, -1.0621F, -0.4359F, 2.0F, 0.075F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.825F, -10.2438F, -0.1279F, -2.8798F, 0.0F, 1.5708F));
		PartDefinition cube_r7 = bb_main.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(82, 93).addBox(-0.975F, -1.0615F, -0.4345F, 2.0F, 0.05F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.825F, -10.2438F, -0.1279F, 0.5236F, 0.0F, -1.5708F));
		PartDefinition cube_r8 = bb_main.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(82, 94).addBox(-0.975F, -1.0578F, -0.4317F, 2.0F, 0.05F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.825F, -10.2438F, -0.1279F, 1.309F, 0.0F, -1.5708F));
		PartDefinition cube_r9 = bb_main.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(82, 94).addBox(-0.175F, -0.7734F, -0.7203F, 0.05F, 2.1F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.025F, -9.0938F, 0.2125F, -1.309F, 0.0F, 1.5708F));
		PartDefinition cube_r10 = bb_main.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(82, 93).addBox(-0.175F, -1.4562F, -0.4375F, 0.05F, 2.1F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.025F, -9.3938F, 0.2125F, 2.0944F, 0.0F, -1.5708F));
		PartDefinition cube_r11 = bb_main.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(82, 94).addBox(-0.175F, -1.3391F, -0.1547F, 0.05F, 2.1F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.025F, -9.3938F, 0.2125F, 2.8798F, 0.0F, -1.5708F));
		PartDefinition cube_r12 = bb_main.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(82, 94).addBox(-0.175F, -1.0562F, -0.0375F, 0.05F, 2.1F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.025F, -9.3938F, 0.2125F, -2.618F, 0.0F, -1.5708F));
		PartDefinition cube_r13 = bb_main.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -0.7734F, -0.7203F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4438F, 2.7125F, -1.309F, 0.0F, 1.5708F));
		PartDefinition cube_r14 = bb_main.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -1.0562F, -0.0375F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4438F, 2.7125F, -2.618F, 0.0F, -1.5708F));
		PartDefinition cube_r15 = bb_main.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -1.3391F, -0.1547F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4438F, 2.7125F, 2.8798F, 0.0F, -1.5708F));
		PartDefinition cube_r16 = bb_main.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(34, 91).addBox(-0.175F, -1.4562F, -0.4375F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4438F, 2.7125F, 2.0944F, 0.0F, -1.5708F));
		PartDefinition cube_r17 = bb_main.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(45, 90).addBox(-0.175F, -1.4562F, -0.4375F, 0.05F, 2.0F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4938F, 2.7125F, 2.0944F, 0.0F, -1.5708F));
		PartDefinition cube_r18 = bb_main.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(45, 91).addBox(-0.175F, -1.0562F, -0.0375F, 0.05F, 2.0F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4938F, 2.7125F, -2.618F, 0.0F, -1.5708F));
		PartDefinition cube_r19 = bb_main.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -1.3391F, -0.1547F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4438F, 2.7125F, 1.8326F, 0.0F, 1.5708F));
		PartDefinition cube_r20 = bb_main.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -1.0562F, -0.8375F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4438F, 2.7125F, 0.5236F, 0.0F, -1.5708F));
		PartDefinition cube_r21 = bb_main.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(45, 91).addBox(-0.175F, -0.7734F, -0.7203F, 0.05F, 2.0F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.1938F, 2.7125F, -1.309F, 0.0F, 1.5708F));
		PartDefinition cube_r22 = bb_main.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -0.7734F, -0.7203F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4438F, 2.7125F, -0.2618F, 0.0F, -1.5708F));
		PartDefinition cube_r23 = bb_main.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(34, 91).addBox(-0.175F, -0.6562F, -0.4375F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4438F, 2.7125F, -1.0472F, 0.0F, -1.5708F));
		PartDefinition cube_r24 = bb_main.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(45, 91).addBox(-0.175F, -1.3391F, -0.1547F, 0.05F, 2.0F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.4938F, 2.7125F, 2.8798F, 0.0F, -1.5708F));
		PartDefinition cube_r25 = bb_main.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(45, 91).addBox(-0.175F, -0.7734F, -0.7203F, 0.05F, 2.0F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.0938F, 5.2125F, -1.309F, 0.0F, 1.5708F));
		PartDefinition cube_r26 = bb_main.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(45, 90).addBox(-0.175F, -1.4562F, -0.4375F, 0.05F, 2.0F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.3938F, 5.2125F, 2.0944F, 0.0F, -1.5708F));
		PartDefinition cube_r27 = bb_main.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(45, 91).addBox(-0.175F, -1.3391F, -0.1547F, 0.05F, 2.0F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.3938F, 5.2125F, 2.8798F, 0.0F, -1.5708F));
		PartDefinition cube_r28 = bb_main.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(45, 91).addBox(-0.175F, -1.0562F, -0.0375F, 0.05F, 2.0F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.3938F, 5.2125F, -2.618F, 0.0F, -1.5708F));
		PartDefinition cube_r29 = bb_main.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -0.7734F, -0.7203F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.4438F, 5.2125F, -1.309F, 0.0F, 1.5708F));
		PartDefinition cube_r30 = bb_main.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(34, 91).addBox(-0.175F, -0.6562F, -0.4375F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.4438F, 5.2125F, -1.0472F, 0.0F, -1.5708F));
		PartDefinition cube_r31 = bb_main.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -1.0562F, -0.0375F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.4438F, 5.2125F, -2.618F, 0.0F, -1.5708F));
		PartDefinition cube_r32 = bb_main.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -1.3391F, -0.1547F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.4438F, 5.2125F, 2.8798F, 0.0F, -1.5708F));
		PartDefinition cube_r33 = bb_main.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(34, 91).addBox(-0.175F, -1.4562F, -0.4375F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.4438F, 5.2125F, 2.0944F, 0.0F, -1.5708F));
		PartDefinition cube_r34 = bb_main.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -1.3391F, -0.1547F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.4438F, 5.2125F, 1.8326F, 0.0F, 1.5708F));
		PartDefinition cube_r35 = bb_main.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -1.0562F, -0.8375F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.4438F, 5.2125F, 0.5236F, 0.0F, -1.5708F));
		PartDefinition cube_r36 = bb_main.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(34, 92).addBox(-0.175F, -0.7734F, -0.7203F, 0.35F, 0.25F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.425F, -9.4438F, 5.2125F, -0.2618F, 0.0F, -1.5708F));
		PartDefinition cube_r37 = bb_main.addOrReplaceChild("cube_r37",
				CubeListBuilder.create().texOffs(30, 102).addBox(-0.075F, -1.0062F, -0.8375F, 0.15F, 1.8F, 0.875F, new CubeDeformation(0.0F)).texOffs(125, 3).addBox(-0.175F, -1.0562F, -0.8375F, 0.35F, 0.35F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.3438F, -2.1875F, 0.5236F, 0.0F, -1.5708F));
		PartDefinition cube_r38 = bb_main.addOrReplaceChild("cube_r38",
				CubeListBuilder.create().texOffs(79, 23).addBox(-0.3F, -0.225F, -5.5375F, 0.6F, 0.45F, 1.125F, new CubeDeformation(0.0F)).texOffs(79, 23).addBox(-0.3F, -0.225F, 4.4125F, 0.6F, 0.45F, 1.125F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, -0.775F, 0.7875F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r39 = bb_main.addOrReplaceChild("cube_r39",
				CubeListBuilder.create().texOffs(79, 23).addBox(-0.3F, -0.225F, 4.4125F, 0.6F, 0.45F, 1.125F, new CubeDeformation(0.0F)).texOffs(79, 23).addBox(-0.3F, -0.225F, -5.5375F, 0.6F, 0.45F, 1.125F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.775F, 0.7875F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r40 = bb_main.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(0, 125).addBox(-3.7F, -3.425F, 7.0F, 7.55F, 0.3F, 1.75F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.095F, -12.65F, -8.9816F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r41 = bb_main.addOrReplaceChild("cube_r41",
				CubeListBuilder.create().texOffs(124, 125).addBox(-0.45F, -0.375F, -5.5F, 0.9F, 1.75F, 1.0F, new CubeDeformation(0.0F)).texOffs(124, 125).addBox(5.55F, -0.375F, -5.5F, 0.9F, 1.75F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.655F, -12.4F, -6.9137F, 1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r42 = bb_main.addOrReplaceChild("cube_r42",
				CubeListBuilder.create().texOffs(76, 11).addBox(-0.6F, -2.925F, -1.25F, 1.6F, 0.75F, 1.5F, new CubeDeformation(0.0F)).texOffs(30, 75).addBox(-6.7F, -2.925F, -1.25F, 1.8F, 0.75F, 1.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.12F, -6.9F, -8.1816F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r43 = bb_main.addOrReplaceChild("cube_r43",
				CubeListBuilder.create().texOffs(124, 125).addBox(-0.45F, -0.375F, -5.5F, 0.9F, 1.75F, 1.0F, new CubeDeformation(0.0F)).texOffs(110, 119).addBox(2.55F, -0.375F, -7.5F, 0.9F, 0.75F, 8.0F, new CubeDeformation(0.0F)).texOffs(114, 121)
						.addBox(-0.45F, -0.375F, -5.5F, 0.9F, 0.75F, 6.0F, new CubeDeformation(0.0F)).texOffs(110, 119).addBox(-3.45F, -0.375F, -7.5F, 0.9F, 0.75F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.345F, -14.4F, -6.9137F, 1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r44 = bb_main.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(0, 75).addBox(-0.7F, -2.925F, -1.25F, 1.8F, 0.75F, 1.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.095F, -8.9F, -8.1816F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r45 = bb_main.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(41, 119).addBox(-4.2F, -2.925F, 1.75F, 8.8F, 0.55F, 7.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.095F, -12.65F, -7.9816F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r46 = bb_main.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(124, 126).addBox(-0.45F, -0.375F, -0.5F, 0.9F, 0.75F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.345F, -15.4F, -6.9137F, 1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r47 = bb_main.addOrReplaceChild("cube_r47",
				CubeListBuilder.create().texOffs(124, 126).addBox(-0.45F, -0.375F, 0.5F, 0.9F, 0.75F, 1.0F, new CubeDeformation(0.0F)).texOffs(124, 126).addBox(5.55F, -0.375F, 0.5F, 0.9F, 0.75F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.655F, -14.2913F, -7.4306F, 0.6981F, 0.0F, 0.0F));
		PartDefinition cube_r48 = bb_main.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(124, 126).addBox(-0.45F, -0.375F, 0.5F, 0.9F, 0.75F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.345F, -15.2913F, -7.4306F, 0.6981F, 0.0F, 0.0F));
		PartDefinition cube_r49 = bb_main.addOrReplaceChild("cube_r49",
				CubeListBuilder.create().texOffs(124, 126).addBox(-0.45F, -0.375F, 0.5F, 0.9F, 0.75F, 1.0F, new CubeDeformation(0.0F)).texOffs(124, 126).addBox(-6.45F, -0.375F, 0.5F, 0.9F, 0.75F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.345F, -15.5769F, -2.1467F, -0.6981F, 0.0F, 0.0F));
		PartDefinition cube_r50 = bb_main.addOrReplaceChild("cube_r50",
				CubeListBuilder.create().texOffs(76, 17).addBox(-0.7F, -2.925F, -0.25F, 1.8F, 1.75F, 0.5F, new CubeDeformation(0.0F)).texOffs(122, 125).addBox(-0.2F, -2.425F, -2.25F, 0.9F, 0.75F, 2.0F, new CubeDeformation(0.0F)).texOffs(76, 14)
						.addBox(5.3F, -2.925F, -0.25F, 1.8F, 1.75F, 0.5F, new CubeDeformation(0.0F)).texOffs(122, 125).addBox(5.8F, -2.425F, -2.25F, 0.9F, 0.75F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.905F, -12.65F, -3.1816F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r51 = bb_main.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(124, 126).addBox(-0.45F, -0.375F, 0.5F, 0.9F, 0.75F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.345F, -16.5769F, 0.8533F, -0.6981F, 0.0F, 0.0F));
		PartDefinition cube_r52 = bb_main.addOrReplaceChild("cube_r52",
				CubeListBuilder.create().texOffs(120, 124).addBox(-0.2F, -2.425F, -3.25F, 0.9F, 0.75F, 3.0F, new CubeDeformation(0.0F)).texOffs(38, 75).addBox(-0.7F, -2.925F, -0.25F, 1.8F, 1.75F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.095F, -12.65F, -0.1816F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r53 = bb_main.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(88, 8).addBox(-2.75F, 0.0F, -1.5F, 3.1F, 0.1F, 2.2F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.75F, -2.6F, -2.5F, 0.0F, 0.3491F, 0.0F));
		PartDefinition cube_r54 = bb_main.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(88, 5).addBox(-2.75F, 0.0F, -0.5F, 3.1F, 0.1F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.75F, -2.6F, 0.5F, 0.0F, -0.2182F, 0.0F));
		PartDefinition cube_r55 = bb_main.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(88, 3).addBox(-2.75F, 0.0F, -0.5F, 3.1F, 0.1F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.75F, -2.6F, 2.5F, 0.0F, 0.1309F, 0.0F));
		PartDefinition cube_r56 = bb_main.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(92, 114).addBox(-2.75F, 0.0F, -1.5F, 3.1F, 0.1F, 2.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.75F, -2.6F, 5.5F, 0.0F, -0.48F, 0.0F));
		PartDefinition cube_r57 = bb_main.addOrReplaceChild("cube_r57",
				CubeListBuilder.create().texOffs(91, 44).addBox(-0.175F, -2.2562F, -0.1375F, 0.35F, 1.95F, 0.275F, new CubeDeformation(0.0F)).texOffs(125, 5).addBox(-0.175F, -0.6562F, -0.4375F, 0.35F, 0.35F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.3438F, -2.1875F, -1.0472F, 0.0F, -1.5708F));
		PartDefinition cube_r58 = bb_main.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(0, 102).addBox(-0.075F, -0.5029F, -0.4154F, 0.15F, 1.85F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.3438F, -2.1875F, -2.0944F, 0.0F, 1.5708F));
		PartDefinition cube_r59 = bb_main.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(48, 101).addBox(-0.075F, -0.7422F, -0.1234F, 0.15F, 2.05F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.3438F, -2.1875F, -2.8798F, 0.0F, 1.5708F));
		PartDefinition cube_r60 = bb_main.addOrReplaceChild("cube_r60",
				CubeListBuilder.create().texOffs(18, 95).addBox(-0.075F, -1.3391F, -0.1547F, 0.15F, 2.05F, 0.875F, new CubeDeformation(0.0F)).texOffs(124, 9).addBox(-0.175F, -1.3391F, -0.1547F, 0.35F, 0.35F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.3438F, -2.1875F, 1.8326F, 0.0F, 1.5708F));
		PartDefinition cube_r61 = bb_main.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(125, 4).addBox(-0.175F, -0.7734F, -0.7203F, 0.35F, 0.35F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.3438F, -2.1875F, -0.2618F, 0.0F, -1.5708F));
		PartDefinition cube_r62 = bb_main.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(124, 7).addBox(-0.175F, -0.7734F, -0.7203F, 0.35F, 0.35F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.3438F, -2.1875F, -1.309F, 0.0F, 1.5708F));
		PartDefinition cube_r63 = bb_main.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(125, 4).addBox(-0.175F, -1.0562F, -0.0375F, 0.35F, 0.35F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.3438F, -2.1875F, -2.618F, 0.0F, -1.5708F));
		PartDefinition cube_r64 = bb_main.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(125, 1).addBox(-0.175F, -1.3391F, -0.1547F, 0.35F, 0.35F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.3438F, -2.1875F, 2.8798F, 0.0F, -1.5708F));
		PartDefinition cube_r65 = bb_main.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(109, 75).addBox(-0.175F, -1.4562F, -0.4375F, 0.35F, 0.35F, 0.875F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-6.025F, -9.3438F, -2.1875F, 2.0944F, 0.0F, -1.5708F));
		PartDefinition cube_r66 = bb_main.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(110, 20).addBox(-3.9F, -1.3F, -0.825F, 0.5F, 0.55F, 0.1F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.095F, -11.875F, 3.0934F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r67 = bb_main.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(110, 20).addBox(-3.9F, -1.3F, -0.825F, 0.5F, 0.55F, 0.1F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.095F, -11.875F, 4.6434F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r68 = bb_main.addOrReplaceChild(
				"cube_r68", CubeListBuilder.create().texOffs(116, 92).addBox(-0.05F, -1.5644F, -0.2875F, 0.6F, 0.15F, 0.575F, new CubeDeformation(0.0F)).texOffs(100, 94)
						.addBox(-0.05F, -1.5644F, -0.2875F, 0.2F, 1.25F, 0.575F, new CubeDeformation(0.0F)).texOffs(119, 48).addBox(-0.25F, -0.3144F, -0.5875F, 0.5F, 1.25F, 1.175F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.75F, -7.0816F, 4.1414F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r69 = bb_main.addOrReplaceChild("cube_r69",
				CubeListBuilder.create().texOffs(38, 97).addBox(-4.1F, -1.825F, -0.825F, 0.9F, 2.475F, 0.1F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-4.7F, -0.3F, -0.725F, 10.2F, 9.65F, 10.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.095F, -11.775F, 4.3434F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r70 = bb_main.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(119, 56).addBox(-0.25F, -0.6377F, -1.3681F, 0.5F, 1.25F, 1.175F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.75F, -7.0816F, 4.1414F, -0.7854F, 0.0F, 3.1416F));
		PartDefinition cube_r71 = bb_main.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(119, 54).addBox(-0.25F, -0.6377F, -1.3681F, 0.5F, 1.25F, 1.175F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.75F, -7.0816F, 4.1414F, -0.7854F, 0.0F, 0.0F));
		PartDefinition cube_r72 = bb_main.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(71, 101).addBox(-0.25F, -0.3144F, -0.1302F, 0.5F, 1.25F, 1.175F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.75F, -5.9777F, 2.5802F, 2.3562F, 0.0F, 0.0F));
		PartDefinition cube_r73 = bb_main.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(75, 101).addBox(-0.25F, -0.3144F, -1.0448F, 0.5F, 1.25F, 1.175F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.75F, -5.9777F, 2.5802F, -3.1416F, 0.0F, 0.0F));
		PartDefinition cube_r74 = bb_main.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(119, 58).addBox(-0.25F, -0.125F, -0.5875F, 0.5F, 1.25F, 1.175F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.75F, -7.0816F, 1.7442F, 1.5708F, 0.0F, 3.1416F));
		PartDefinition cube_r75 = bb_main.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(119, 52).addBox(-0.25F, -0.125F, -0.5875F, 0.5F, 1.25F, 1.175F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.75F, -7.9962F, 2.123F, 2.3562F, 0.0F, 3.1416F));
		PartDefinition cube_r76 = bb_main.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(119, 50).addBox(-0.25F, -0.125F, -0.5875F, 0.5F, 1.25F, 1.175F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(7.75F, -8.375F, 3.0375F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r77 = bb_main.addOrReplaceChild("cube_r77",
				CubeListBuilder.create().texOffs(74, 76).addBox(6.25F, -0.6875F, -0.6F, 1.5F, 0.25F, 1.2F, new CubeDeformation(0.0F)).texOffs(68, 80).addBox(6.75F, -0.1875F, -0.325F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(13.5F, -1.3125F, -4.175F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r78 = bb_main.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(89, 116).addBox(7.5F, -0.4375F, -0.1F, 0.25F, 1.25F, 0.2F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(13.7F, -1.3125F, -4.175F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r79 = bb_main.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(89, 116).addBox(6.25F, -0.4375F, -0.1F, 0.25F, 1.25F, 0.2F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(13.3F, -1.3125F, -4.175F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r80 = bb_main.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(64, 80).addBox(-0.25F, -0.125F, -0.3375F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -1.173F, -3.6997F, -2.3562F, 0.0F, -3.1416F));
		PartDefinition cube_r81 = bb_main.addOrReplaceChild("cube_r81",
				CubeListBuilder.create().texOffs(80, 59).addBox(-0.25F, -0.226F, -0.5814F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)).texOffs(66, 78).addBox(-13.75F, -0.226F, -0.5814F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -1.274F, -4.4314F, 0.7854F, 0.0F, 0.0F));
		PartDefinition cube_r82 = bb_main.addOrReplaceChild("cube_r82",
				CubeListBuilder.create().texOffs(56, 80).addBox(-0.25F, -0.3985F, -0.7538F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)).texOffs(70, 78).addBox(-13.75F, -0.3985F, -0.7538F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.274F, -4.1875F, -2.3562F, 0.0F, 0.0F));
		PartDefinition cube_r83 = bb_main.addOrReplaceChild("cube_r83",
				CubeListBuilder.create().texOffs(80, 62).addBox(-0.25F, -0.226F, -0.3375F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)).texOffs(80, 61).addBox(-13.75F, -0.226F, -0.3375F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.274F, -4.1875F, -3.1416F, 0.0F, 0.0F));
		PartDefinition cube_r84 = bb_main.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(52, 80).addBox(-0.25F, -0.3985F, -0.7538F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.274F, -4.1875F, 0.7854F, 0.0F, -3.1416F));
		PartDefinition cube_r85 = bb_main.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(0, 0).addBox(-0.25F, -0.125F, -0.1625F, 0.5F, 0.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.6852F, -3.4977F, -1.5708F, 0.0F, -3.1416F));
		PartDefinition cube_r86 = bb_main.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(0, 0).addBox(-0.25F, -0.125F, -0.1625F, 0.5F, 0.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.6852F, -4.8727F, -1.5708F, 0.0F, -3.1416F));
		PartDefinition cube_r87 = bb_main.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(0, 0).addBox(-0.25F, -0.125F, -0.1625F, 0.5F, 0.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.6852F, 6.4523F, -1.5708F, 0.0F, -3.1416F));
		PartDefinition cube_r88 = bb_main.addOrReplaceChild("cube_r88",
				CubeListBuilder.create().texOffs(48, 80).addBox(-0.25F, -0.3985F, -0.7538F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)).texOffs(78, 78).addBox(-13.75F, -0.3985F, -0.7538F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.274F, 5.7625F, -2.3562F, 0.0F, 0.0F));
		PartDefinition cube_r89 = bb_main.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(76, 80).addBox(6.75F, -0.1875F, -0.325F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(13.5F, -1.3125F, 5.775F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r90 = bb_main.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(38, 80).addBox(-0.25F, -0.125F, -0.3375F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -1.173F, 6.2503F, -2.3562F, 0.0F, -3.1416F));
		PartDefinition cube_r91 = bb_main.addOrReplaceChild("cube_r91",
				CubeListBuilder.create().texOffs(34, 80).addBox(-0.25F, -0.226F, -0.5814F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)).texOffs(70, 79).addBox(-13.75F, -0.226F, -0.5814F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -1.274F, 5.5186F, 0.7854F, 0.0F, 0.0F));
		PartDefinition cube_r92 = bb_main.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(0, 0).addBox(-0.25F, -0.125F, -0.1625F, 0.5F, 0.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.6852F, 5.0773F, -1.5708F, 0.0F, -3.1416F));
		PartDefinition cube_r93 = bb_main.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(30, 80).addBox(-0.25F, -0.3985F, -0.7538F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.274F, 5.7625F, 0.7854F, 0.0F, -3.1416F));
		PartDefinition cube_r94 = bb_main.addOrReplaceChild("cube_r94",
				CubeListBuilder.create().texOffs(8, 80).addBox(-0.25F, -0.226F, -0.3375F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)).texOffs(4, 80).addBox(-13.75F, -0.226F, -0.3375F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.5F, -0.274F, 5.7625F, -3.1416F, 0.0F, 0.0F));
		PartDefinition cube_r95 = bb_main.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(78, 79).addBox(-0.25F, -0.3985F, -0.7538F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, -0.274F, 5.7625F, 0.7854F, 0.0F, -3.1416F));
		PartDefinition cube_r96 = bb_main.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(0, 0).addBox(-0.25F, -0.125F, -0.1625F, 0.5F, 0.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, -0.6852F, 5.0773F, -1.5708F, 0.0F, -3.1416F));
		PartDefinition cube_r97 = bb_main.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(66, 79).addBox(-0.25F, -0.125F, -0.3375F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, -1.173F, 6.2503F, -2.3562F, 0.0F, -3.1416F));
		PartDefinition cube_r98 = bb_main.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(72, 80).addBox(6.75F, -0.1875F, -0.325F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.3125F, 5.775F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r99 = bb_main.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(0, 0).addBox(-0.25F, -0.125F, -0.1625F, 0.5F, 0.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, -0.6852F, 6.4523F, -1.5708F, 0.0F, -3.1416F));
		PartDefinition cube_r100 = bb_main.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(0, 0).addBox(-0.25F, -0.125F, -0.1625F, 0.5F, 0.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, -0.6852F, -4.8727F, -1.5708F, 0.0F, -3.1416F));
		PartDefinition cube_r101 = bb_main.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(0, 0).addBox(-0.25F, -0.125F, -0.1625F, 0.5F, 0.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, -0.6852F, -3.4977F, -1.5708F, 0.0F, -3.1416F));
		PartDefinition cube_r102 = bb_main.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(78, 77).addBox(-0.25F, -0.3985F, -0.7538F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, -0.274F, -4.1875F, 0.7854F, 0.0F, -3.1416F));
		PartDefinition cube_r103 = bb_main.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(80, 60).addBox(-0.25F, -0.125F, -0.3375F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.0F, -1.173F, -3.6997F, -2.3562F, 0.0F, -3.1416F));
		PartDefinition cube_r104 = bb_main.addOrReplaceChild("cube_r104",
				CubeListBuilder.create().texOffs(60, 80).addBox(6.75F, -0.1875F, -0.325F, 0.5F, 0.25F, 0.675F, new CubeDeformation(0.0F)).texOffs(76, 20).addBox(6.25F, -0.6875F, -0.6F, 1.5F, 0.25F, 1.2F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.3125F, -4.175F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r105 = bb_main.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(89, 116).addBox(6.25F, -0.4375F, -0.1F, 0.25F, 1.25F, 0.2F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.2F, -1.3125F, -4.175F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r106 = bb_main.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(89, 116).addBox(7.5F, -0.4375F, -0.1F, 0.25F, 1.25F, 0.2F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.2F, -1.3125F, -4.175F, -3.1416F, 0.0F, 3.1416F));
		PartDefinition cube_r107 = bb_main.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(66, 63).addBox(-4.0F, -0.375F, -0.725F, 8.75F, 1.775F, 0.85F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.095F, -11.775F, 6.1684F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r108 = bb_main.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(14, 70).addBox(-4.5F, -0.375F, -0.125F, 9.0F, 0.75F, 0.25F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.5F, -12.1491F, 6.6931F, 0.5672F, 0.0F, 0.0F));
		PartDefinition cube_r109 = bb_main.addOrReplaceChild("cube_r109", CubeListBuilder.create().texOffs(66, 48).addBox(-4.75F, -0.375F, -0.625F, 9.5F, 1.825F, 0.775F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.5F, -2.1425F, 6.222F, -1.5708F, 0.0F, 0.0F));
		PartDefinition cube_r110 = bb_main.addOrReplaceChild("cube_r110", CubeListBuilder.create().texOffs(14, 69).addBox(-4.5F, -0.375F, -0.125F, 9.0F, 0.75F, 0.25F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.5F, -2.4009F, 6.6931F, -0.5672F, 0.0F, 0.0F));
		PartDefinition cube_r111 = bb_main.addOrReplaceChild("cube_r111", CubeListBuilder.create().texOffs(44, 74).addBox(-0.5F, 0.0F, 0.0F, 0.75F, 9.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.894F, -2.65F, 6.4511F, 0.0F, -0.6545F, 3.1416F));
		PartDefinition cube_r112 = bb_main.addOrReplaceChild("cube_r112", CubeListBuilder.create().texOffs(26, 71).addBox(-0.5F, -9.25F, 0.0F, 0.75F, 9.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.894F, -2.65F, 6.4511F, 0.0F, -0.6545F, 0.0F));
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
}