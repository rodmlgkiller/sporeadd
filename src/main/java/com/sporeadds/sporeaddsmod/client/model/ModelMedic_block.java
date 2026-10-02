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
public class ModelMedic_block<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("pruevasmod", "model_medic_block"), "main");
	public final ModelPart TODO;
	public final ModelPart Base;
	public final ModelPart group7;
	public final ModelPart group5;
	public final ModelPart group4;
	public final ModelPart group3;
	public final ModelPart group2;
	public final ModelPart Hinge_Puerta2;
	public final ModelPart HingePuerta;
	public final ModelPart bone;
	public final ModelPart Puerta;
	public final ModelPart CirculoPuerta;
	public final ModelPart HateCircle;
	public final ModelPart Centrifugadora;
	public final ModelPart bone2;
	public final ModelPart Hinge;
	public final ModelPart group8;
	public final ModelPart group9;
	public final ModelPart group10;
	public final ModelPart group11;
	public final ModelPart group12;
	public final ModelPart group13;
	public final ModelPart group14;
	public final ModelPart group15;
	public final ModelPart group;
	public final ModelPart group6;
	public final ModelPart Boton2;
	public final ModelPart Boton;

	public ModelMedic_block(ModelPart root) {
		this.TODO = root.getChild("TODO");
		this.Base = this.TODO.getChild("Base");
		this.group7 = this.Base.getChild("group7");
		this.group5 = this.Base.getChild("group5");
		this.group4 = this.Base.getChild("group4");
		this.group3 = this.Base.getChild("group3");
		this.group2 = this.Base.getChild("group2");
		this.Hinge_Puerta2 = this.Base.getChild("Hinge_Puerta2");
		this.HingePuerta = this.Hinge_Puerta2.getChild("HingePuerta");
		this.bone = this.Base.getChild("bone");
		this.Puerta = this.TODO.getChild("Puerta");
		this.CirculoPuerta = this.Puerta.getChild("CirculoPuerta");
		this.HateCircle = this.CirculoPuerta.getChild("HateCircle");
		this.Centrifugadora = this.TODO.getChild("Centrifugadora");
		this.bone2 = this.Centrifugadora.getChild("bone2");
		this.Hinge = this.Centrifugadora.getChild("Hinge");
		this.group8 = this.Centrifugadora.getChild("group8");
		this.group9 = this.group8.getChild("group9");
		this.group10 = this.group8.getChild("group10");
		this.group11 = this.group8.getChild("group11");
		this.group12 = this.group8.getChild("group12");
		this.group13 = this.group8.getChild("group13");
		this.group14 = this.group8.getChild("group14");
		this.group15 = this.group8.getChild("group15");
		this.group = this.group8.getChild("group");
		this.group6 = this.Centrifugadora.getChild("group6");
		this.Boton2 = this.TODO.getChild("Boton2");
		this.Boton = this.Boton2.getChild("Boton");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition TODO = partdefinition.addOrReplaceChild("TODO", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition Base = TODO.addOrReplaceChild("Base", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, 4.55F));
		PartDefinition group7 = Base.addOrReplaceChild("group7", CubeListBuilder.create(), PartPose.offset(0.0F, -1.25F, -1.25F));
		PartDefinition group5 = Base
				.addOrReplaceChild(
						"group5", CubeListBuilder.create().texOffs(48, 6).addBox(-0.95F, 5.25F, -0.2F, 1.9F, 0.25F, 4.5F, new CubeDeformation(0.0F)).texOffs(14, 54).addBox(-4.776F, 5.25F, -1.426F, 4.5F, 0.25F, 1.9F, new CubeDeformation(0.0F))
								.texOffs(46, 33).addBox(-0.95F, 5.25F, -5.2519F, 1.9F, 0.25F, 5.5F, new CubeDeformation(0.0F)).texOffs(14, 56).addBox(2.276F, 5.25F, -1.426F, 2.5F, 0.25F, 1.9F, new CubeDeformation(0.0F)),
						PartPose.offset(0.0F, -3.0F, -4.55F));
		PartDefinition cube_r1 = group5.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(48, 0).addBox(-0.95F, 2.625F, -5.25F, 1.9F, 0.25F, 5.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.732F, 2.625F, 3.7055F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r2 = group5.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(42, 49).addBox(-5.25F, 2.625F, -0.95F, 5.5F, 0.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.2003F, 2.625F, 2.7244F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r3 = group5.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(42, 53).addBox(-5.25F, 2.625F, -0.95F, 5.5F, 0.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.1814F, 2.625F, 1.256F, 0.0F, -0.3927F, 0.0F));
		PartDefinition cube_r4 = group5.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(16, 52).addBox(-3.25F, 2.625F, -0.95F, 3.5F, 0.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.1814F, 2.625F, -2.208F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r5 = group5.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(52, 55).addBox(-0.95F, 2.625F, -0.25F, 1.9F, 0.25F, 3.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.2003F, 2.625F, -3.6763F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r6 = group5.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(28, 49).addBox(-0.95F, 2.625F, -0.25F, 1.9F, 0.25F, 4.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.732F, 2.625F, -4.6574F, 0.0F, -0.3927F, 0.0F));
		PartDefinition cube_r7 = group5.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(48, 11).addBox(-0.95F, 2.625F, -0.25F, 1.9F, 0.25F, 4.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.732F, 2.625F, -4.6574F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r8 = group5.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 52).addBox(-0.25F, 2.625F, -0.95F, 5.5F, 0.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.2003F, 2.625F, -3.6763F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r9 = group5.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(42, 51).addBox(-0.25F, 2.625F, -0.95F, 5.5F, 0.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.1814F, 2.625F, -2.208F, 0.0F, -0.3927F, 0.0F));
		PartDefinition cube_r10 = group5.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 54).addBox(-0.25F, 2.625F, -0.95F, 4.5F, 0.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.1814F, 2.625F, 1.256F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r11 = group5.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(54, 43).addBox(-0.95F, 2.625F, -3.25F, 1.9F, 0.25F, 3.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.2003F, 2.625F, 2.7244F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r12 = group5.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(4, 56).addBox(-0.95F, 2.625F, -2.25F, 1.9F, 0.25F, 2.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.732F, 2.625F, 3.7055F, 0.0F, -0.3927F, 0.0F));
		PartDefinition group4 = Base
				.addOrReplaceChild(
						"group4", CubeListBuilder.create().texOffs(52, 64).addBox(-0.95F, 2.25F, 3.8F, 1.9F, 3.25F, 0.5F, new CubeDeformation(0.0F)).texOffs(10, 59).addBox(-4.776F, 2.25F, -1.426F, 0.5F, 3.25F, 1.9F, new CubeDeformation(0.0F))
								.texOffs(64, 62).addBox(-0.95F, 2.25F, -5.2519F, 1.9F, 3.25F, 0.5F, new CubeDeformation(0.0F)).texOffs(58, 59).addBox(4.276F, 2.25F, -1.426F, 0.5F, 3.25F, 1.9F, new CubeDeformation(0.0F)),
						PartPose.offset(0.0F, -3.0F, -4.55F));
		PartDefinition cube_r13 = group4.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(66, 37).addBox(-0.95F, -0.375F, -0.25F, 1.9F, 3.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.732F, 2.625F, 3.7055F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r14 = group4.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(73, 77).addBox(-0.25F, -0.375F, -0.95F, 0.5F, 3.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.2003F, 2.625F, 2.7244F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r15 = group4.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(62, 6).addBox(-0.25F, -0.375F, -0.95F, 0.5F, 3.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.1814F, 2.625F, 1.256F, 0.0F, -0.3927F, 0.0F));
		PartDefinition cube_r16 = group4.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(52, 59).addBox(-0.25F, -0.375F, -0.95F, 0.5F, 3.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.1814F, 2.625F, -2.208F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r17 = group4.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(34, 65).addBox(-0.95F, -0.375F, -0.25F, 1.9F, 3.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.2003F, 2.625F, -3.6763F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r18 = group4.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(28, 65).addBox(-0.95F, -0.375F, -0.25F, 1.9F, 3.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.732F, 2.625F, -4.6574F, 0.0F, -0.3927F, 0.0F));
		PartDefinition cube_r19 = group4.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(64, 58).addBox(-0.95F, -0.375F, -0.25F, 1.9F, 3.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.732F, 2.625F, -4.6574F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r20 = group4.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(22, 59).addBox(-0.25F, -0.375F, -0.95F, 0.5F, 3.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.2003F, 2.625F, -3.6763F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r21 = group4.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(16, 59).addBox(-0.25F, -0.375F, -0.95F, 0.5F, 3.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.1814F, 2.625F, -2.208F, 0.0F, -0.3927F, 0.0F));
		PartDefinition cube_r22 = group4.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(4, 59).addBox(-0.25F, -0.375F, -0.95F, 0.5F, 3.25F, 1.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.1814F, 2.625F, 1.256F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r23 = group4.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(58, 64).addBox(-0.95F, -0.375F, -0.25F, 1.9F, 3.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.2003F, 2.625F, 2.7244F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r24 = group4.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(64, 54).addBox(-0.95F, -0.375F, -0.25F, 1.9F, 3.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.732F, 2.625F, 3.7055F, 0.0F, -0.3927F, 0.0F));
		PartDefinition group3 = Base.addOrReplaceChild("group3", CubeListBuilder.create().texOffs(0, 28).addBox(-3.75F, -0.4F, -0.5F, 11.5F, 0.2F, 10.5F, new CubeDeformation(0.0F)).texOffs(54, 48)
				.addBox(-3.75F, -0.4F, -1.5F, 3.75F, 0.2F, 1.0F, new CubeDeformation(0.0F)).texOffs(41, 94).addBox(4.0F, -0.4F, -1.5F, 3.75F, 0.2F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 0.0F, -9.8F));
		PartDefinition cube_r25 = group3.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(58, 16).addBox(-0.25F, -0.2F, -0.25F, 1.25F, 0.2F, 1.35F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -0.2F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r26 = group3.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(53, 88).addBox(-0.85F, -0.2F, -0.25F, 1.35F, 0.2F, 1.25F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.5F, -0.2F, -0.6F, 0.0F, -0.7854F, 0.0F));
		PartDefinition group2 = Base.addOrReplaceChild("group2",
				CubeListBuilder.create().texOffs(0, 17).addBox(-4.0F, -0.2F, -0.75F, 12.0F, 0.2F, 11.0F, new CubeDeformation(0.0F)).texOffs(48, 16).addBox(4.0F, -0.2F, -1.75F, 4.0F, 0.2F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 0.0F, -9.8F));
		PartDefinition cube_r27 = group2.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(68, 6).addBox(-0.5F, -0.2F, -0.5F, 1.5F, 0.2F, 1.6F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r28 = group2.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(8, 68).addBox(-1.1F, -0.2F, -0.5F, 1.6F, 0.2F, 1.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.5F, 0.0F, -0.6F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r29 = group2.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(54, 47).addBox(-2.0F, -0.1F, -0.5F, 4.0F, 0.2F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, -0.1F, -1.25F, 3.1416F, 0.0F, 0.0F));
		PartDefinition Hinge_Puerta2 = Base.addOrReplaceChild("Hinge_Puerta2", CubeListBuilder.create().texOffs(40, 54).addBox(3.25F, 2.5126F, 4.5303F, 1.0F, 0.5F, 0.15F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -4.55F));
		PartDefinition cube_r30 = Hinge_Puerta2.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(62, 37).addBox(-2.75F, -0.75F, 1.0F, 1.0F, 0.5F, 0.5F, new CubeDeformation(0.0F)).texOffs(46, 68)
				.addBox(-1.75F, -0.75F, 0.0F, 1.0F, 0.5F, 1.5F, new CubeDeformation(0.0F)).texOffs(40, 68).addBox(-3.75F, -0.75F, 0.0F, 1.0F, 0.5F, 1.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(6.0F, 3.25F, 4.75F, 0.7854F, 0.0F, 0.0F));
		PartDefinition HingePuerta = Hinge_Puerta2.addOrReplaceChild("HingePuerta", CubeListBuilder.create().texOffs(62, 54).addBox(-4.25F, 2.5126F, 4.5303F, 1.0F, 0.5F, 0.15F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition cube_r31 = HingePuerta.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(48, 66).addBox(-2.75F, -0.75F, 1.0F, 1.0F, 0.5F, 0.5F, new CubeDeformation(0.0F)).texOffs(58, 68)
				.addBox(-1.75F, -0.75F, 0.0F, 1.0F, 0.5F, 1.5F, new CubeDeformation(0.0F)).texOffs(52, 68).addBox(-3.75F, -0.75F, 0.0F, 1.0F, 0.5F, 1.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5F, 3.25F, 4.75F, 0.7854F, 0.0F, 0.0F));
		PartDefinition bone = Base.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -5.0F, 1.0F, 12.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 5.0F, -12.55F));
		PartDefinition Puerta = TODO.addOrReplaceChild("Puerta",
				CubeListBuilder.create().texOffs(0, 39).addBox(-5.9766F, -10.9983F, 0.7668F, 12.0F, 10.5F, 2.0F, new CubeDeformation(0.0F)).texOffs(62, 97).addBox(-5.9766F, -10.9983F, 2.7668F, 12.0F, 4.5F, 0.5F, new CubeDeformation(0.0F))
						.texOffs(0, 0).addBox(-5.2266F, -9.9983F, 0.5168F, 0.25F, 1.0F, 0.25F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(4.7734F, -9.9983F, 0.5168F, 0.25F, 1.0F, 0.25F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-5.2266F, -2.7483F, 0.5168F, 0.25F, 1.0F, 0.25F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(5.0234F, -2.7483F, 0.5168F, 0.25F, 1.0F, 0.25F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-4.9766F, -9.9983F, 0.5168F, 0.75F, 0.25F, 0.25F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(4.0234F, -9.9983F, 0.5168F, 0.75F, 0.25F, 0.25F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-4.9766F, -1.9983F, 0.5168F, 0.75F, 0.25F, 0.25F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(4.2734F, -1.9983F, 0.5168F, 0.75F, 0.25F, 0.25F, new CubeDeformation(0.0F)).texOffs(0, 56)
						.addBox(-5.9766F, -10.9983F, 0.2668F, 0.25F, 10.5F, 0.5F, new CubeDeformation(0.0F)).texOffs(46, 20).addBox(-5.7266F, -10.9983F, 0.2668F, 11.75F, 0.25F, 0.5F, new CubeDeformation(0.0F)).texOffs(57, 92)
						.addBox(-5.9766F, -11.9983F, 0.2668F, 12.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(70, 14).addBox(-0.9766F, -10.9983F, -0.2332F, 2.0F, 0.25F, 0.5F, new CubeDeformation(0.0F)).texOffs(2, 56)
						.addBox(5.7734F, -10.7483F, 0.2668F, 0.25F, 10.25F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.0234F, -5.0017F, 4.4832F));
		PartDefinition cube_r32 = Puerta.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(46, 17).addBox(-7.0F, -10.5F, 1.25F, 12.0F, 1.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0234F, 2.5328F, -2.3682F, -0.3927F, 0.0F, 0.0F));
		PartDefinition CirculoPuerta = Puerta.addOrReplaceChild(
				"CirculoPuerta", CubeListBuilder.create().texOffs(68, 68).addBox(-4.776F, 2.326F, -0.125F, 0.5F, 1.9F, 1.0F, new CubeDeformation(0.0F)).texOffs(68, 25).addBox(-0.95F, 7.5519F, -0.125F, 1.9F, 0.5F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(36, 69).addBox(4.276F, 2.326F, -0.125F, 0.5F, 1.9F, 1.0F, new CubeDeformation(0.0F)).texOffs(68, 21).addBox(-0.95F, -1.5F, -0.125F, 1.9F, 0.5F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0234F, -8.9983F, 0.6917F));
		PartDefinition cube_r33 = CirculoPuerta.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(22, 68).addBox(-0.95F, -0.25F, -0.375F, 1.9F, 0.5F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.732F, -0.9055F, 0.25F, 0.0F, 0.0F, -0.3927F));
		PartDefinition cube_r34 = CirculoPuerta.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(68, 8).addBox(-0.95F, -0.25F, -0.375F, 1.9F, 0.5F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.2003F, 0.0756F, 0.25F, 0.0F, 0.0F, -0.7854F));
		PartDefinition cube_r35 = CirculoPuerta.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(64, 68).addBox(-0.25F, -0.95F, -0.375F, 0.5F, 1.9F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.1814F, 1.544F, 0.25F, 0.0F, 0.0F, 0.3927F));
		PartDefinition cube_r36 = CirculoPuerta.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(68, 29).addBox(-0.95F, -0.25F, -0.375F, 1.9F, 0.5F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.732F, -0.9055F, 0.25F, 0.0F, 0.0F, 0.3927F));
		PartDefinition cube_r37 = CirculoPuerta.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(4, 70).addBox(-0.25F, -0.95F, -0.375F, 0.5F, 1.9F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.2003F, 0.0756F, 0.25F, 0.0F, 0.0F, -0.7854F));
		PartDefinition cube_r38 = CirculoPuerta.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(0, 70).addBox(-0.25F, -0.95F, -0.375F, 0.5F, 1.9F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.1814F, 1.544F, 0.25F, 0.0F, 0.0F, -0.3927F));
		PartDefinition cube_r39 = CirculoPuerta.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(32, 69).addBox(-0.25F, -0.95F, -0.375F, 0.5F, 1.9F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.1814F, 5.008F, 0.25F, 0.0F, 0.0F, 0.3927F));
		PartDefinition cube_r40 = CirculoPuerta.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(16, 68).addBox(-0.95F, -0.25F, -0.375F, 1.9F, 0.5F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.2003F, 6.4763F, 0.25F, 0.0F, 0.0F, -0.7854F));
		PartDefinition cube_r41 = CirculoPuerta.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(68, 27).addBox(-0.95F, -0.25F, -0.375F, 1.9F, 0.5F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.732F, 7.4574F, 0.25F, 0.0F, 0.0F, -0.3927F));
		PartDefinition cube_r42 = CirculoPuerta.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(68, 23).addBox(-0.95F, -0.25F, -0.375F, 1.9F, 0.5F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.732F, 7.4574F, 0.25F, 0.0F, 0.0F, 0.3927F));
		PartDefinition cube_r43 = CirculoPuerta.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(8, 70).addBox(-0.25F, -0.95F, -0.375F, 0.5F, 1.9F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.2003F, 6.4763F, 0.25F, 0.0F, 0.0F, -0.7854F));
		PartDefinition cube_r44 = CirculoPuerta.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(28, 69).addBox(-0.25F, -0.95F, -0.375F, 0.5F, 1.9F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.1814F, 5.008F, 0.25F, 0.0F, 0.0F, -0.3927F));
		PartDefinition HateCircle = CirculoPuerta
				.addOrReplaceChild("HateCircle",
						CubeListBuilder.create().texOffs(46, 30).addBox(-4.776F, 3.576F, -0.875F, 9.5F, 1.9F, 0.5F, new CubeDeformation(0.0F)).texOffs(70, 12).addBox(-0.95F, 8.8019F, -0.875F, 1.9F, 0.5F, 0.5F, new CubeDeformation(0.0F))
								.texOffs(72, 3).addBox(4.276F, 3.576F, -0.875F, 0.5F, 1.9F, 0.5F, new CubeDeformation(0.0F)).texOffs(28, 54).addBox(-0.95F, -0.25F, -0.875F, 1.9F, 9.5F, 0.5F, new CubeDeformation(0.0F)),
						PartPose.offset(0.0F, -1.25F, 1.5F));
		PartDefinition cube_r45 = HateCircle.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(70, 10).addBox(-0.95F, -0.25F, -0.375F, 1.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.732F, 0.3445F, -0.5F, 0.0F, 0.0F, -0.3927F));
		PartDefinition cube_r46 = HateCircle.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(34, 54).addBox(-0.95F, -0.25F, -0.375F, 1.9F, 9.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.2003F, 1.3256F, -0.5F, 0.0F, 0.0F, -0.7854F));
		PartDefinition cube_r47 = HateCircle.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(46, 21).addBox(-0.25F, -0.95F, -0.375F, 9.5F, 1.9F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.1814F, 2.794F, -0.5F, 0.0F, 0.0F, 0.3927F));
		PartDefinition cube_r48 = HateCircle.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(12, 70).addBox(-0.95F, -0.25F, -0.375F, 1.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.732F, 0.3445F, -0.5F, 0.0F, 0.0F, 0.3927F));
		PartDefinition cube_r49 = HateCircle.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(46, 24).addBox(-9.25F, -0.95F, -0.375F, 9.5F, 1.9F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.2003F, 1.3256F, -0.5F, 0.0F, 0.0F, -0.7854F));
		PartDefinition cube_r50 = HateCircle.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(72, 0).addBox(-0.25F, -0.95F, -0.375F, 0.5F, 1.9F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.1814F, 2.794F, -0.5F, 0.0F, 0.0F, -0.3927F));
		PartDefinition cube_r51 = HateCircle.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(12, 72).addBox(-0.25F, -0.95F, -0.375F, 0.5F, 1.9F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.1814F, 6.258F, -0.5F, 0.0F, 0.0F, 0.3927F));
		PartDefinition cube_r52 = HateCircle.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(68, 31).addBox(-0.95F, -0.25F, -0.375F, 1.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.2003F, 7.7263F, -0.5F, 0.0F, 0.0F, -0.7854F));
		PartDefinition cube_r53 = HateCircle.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(40, 55).addBox(-0.95F, -9.25F, -0.375F, 1.9F, 9.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.732F, 8.7074F, -0.5F, 0.0F, 0.0F, -0.3927F));
		PartDefinition cube_r54 = HateCircle.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(46, 55).addBox(-0.95F, -9.25F, -0.375F, 1.9F, 9.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.732F, 8.7074F, -0.5F, 0.0F, 0.0F, 0.3927F));
		PartDefinition cube_r55 = HateCircle.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(72, 15).addBox(-0.25F, -0.95F, -0.375F, 0.5F, 1.9F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.2003F, 7.7263F, -0.5F, 0.0F, 0.0F, -0.7854F));
		PartDefinition cube_r56 = HateCircle.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(46, 27).addBox(-0.25F, -0.95F, -0.375F, 9.5F, 1.9F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.1814F, 6.258F, -0.5F, 0.0F, 0.0F, -0.3927F));
		PartDefinition Centrifugadora = TODO.addOrReplaceChild("Centrifugadora", CubeListBuilder.create(), PartPose.offset(0.0F, -4.75F, -0.4746F));
		PartDefinition bone2 = Centrifugadora.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(68, 41).addBox(-1.8814F, -0.1958F, 1.2593F, 3.75F, 0.5F, 0.9F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -1.75F));
		PartDefinition cube_r57 = bone2.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.6541F, 0.0F, 0.1301F, 0.6109F, -0.3927F, 0.0F));
		PartDefinition cube_r58 = bone2.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.2087F, 0.0F, 0.5007F, 0.6109F, -0.7854F, 0.0F));
		PartDefinition cube_r59 = bone2.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(7, 67).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5792F, 0.0F, 1.0552F, 0.6109F, -1.1781F, 0.0F));
		PartDefinition cube_r60 = bone2.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.7093F, 0.0F, 1.7093F, 0.0F, -1.5708F, 0.6109F));
		PartDefinition cube_r61 = bone2.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5792F, 0.0F, 2.3635F, -2.5307F, -1.1781F, -3.1416F));
		PartDefinition cube_r62 = bone2.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.2087F, 0.0F, 2.918F, -2.5307F, -0.7854F, -3.1416F));
		PartDefinition cube_r63 = bone2.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.6541F, 0.0F, 3.2886F, -2.5307F, -0.3927F, -3.1416F));
		PartDefinition cube_r64 = bone2.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 3.4187F, -2.5307F, 0.0F, -3.1416F));
		PartDefinition cube_r65 = bone2.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.6541F, 0.0F, 3.2886F, -2.5307F, 0.3927F, -3.1416F));
		PartDefinition cube_r66 = bone2.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.2087F, 0.0F, 2.918F, -2.5307F, 0.7854F, -3.1416F));
		PartDefinition cube_r67 = bone2.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5792F, 0.0F, 2.3635F, -2.5307F, 1.1781F, -3.1416F));
		PartDefinition cube_r68 = bone2.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.3F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.7093F, 0.05F, 1.7093F, 0.0F, 1.5708F, -0.6109F));
		PartDefinition cube_r69 = bone2.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5792F, 0.0F, 1.0552F, 0.6109F, 1.1781F, 0.0F));
		PartDefinition cube_r70 = bone2.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.2087F, 0.0F, 0.5007F, 0.6109F, 0.7854F, 0.0F));
		PartDefinition cube_r71 = bone2.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.6541F, 0.0F, 0.1301F, 0.6109F, 0.3927F, 0.0F));
		PartDefinition cube_r72 = bone2.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(68, 41).addBox(-1.875F, -0.25F, -0.45F, 3.75F, 0.5F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0064F, 0.0542F, 1.7093F, 0.0F, -0.3927F, 0.0F));
		PartDefinition cube_r73 = bone2.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(68, 41).addBox(-1.875F, -0.25F, -0.45F, 3.75F, 0.5F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0064F, 0.0542F, 1.7093F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r74 = bone2.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(68, 41).addBox(-1.875F, -0.25F, -0.45F, 3.75F, 0.5F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0064F, 0.0542F, 1.7093F, 0.0F, -1.1781F, 0.0F));
		PartDefinition cube_r75 = bone2.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(68, 41).addBox(-1.875F, -0.25F, -0.45F, 3.75F, 0.5F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0064F, 0.0542F, 1.7093F, 0.0F, -1.5708F, 0.0F));
		PartDefinition cube_r76 = bone2.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(68, 41).addBox(-1.875F, -0.25F, -0.45F, 3.75F, 0.5F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0064F, 0.0542F, 1.7093F, -3.1416F, -1.1781F, 3.1416F));
		PartDefinition cube_r77 = bone2.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(68, 41).addBox(-1.875F, -0.25F, -0.45F, 3.75F, 0.5F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0064F, 0.0542F, 1.7093F, -3.1416F, -0.7854F, 3.1416F));
		PartDefinition cube_r78 = bone2.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(68, 41).addBox(-1.875F, -0.25F, -0.45F, 3.75F, 0.5F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0064F, 0.0542F, 1.7093F, -3.1416F, -0.3927F, 3.1416F));
		PartDefinition cube_r79 = bone2.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(6, 66).addBox(-0.45F, -0.25F, -0.5F, 0.9F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6109F, 0.0F, 0.0F));
		PartDefinition Hinge = Centrifugadora.addOrReplaceChild("Hinge", CubeListBuilder.create(), PartPose.offset(0.0F, -3.25F, 0.4746F));
		PartDefinition cube_r80 = Hinge.addOrReplaceChild("cube_r80",
				CubeListBuilder.create().texOffs(70, 36).addBox(-0.25F, -1.125F, -0.25F, 0.5F, 0.25F, 0.5F, new CubeDeformation(0.0F)).texOffs(25, 88).addBox(-0.5F, -0.875F, -0.5F, 1.0F, 2.25F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 3.625F, -0.5F, 0.0F, -0.7854F, 0.0F));
		PartDefinition group8 = Centrifugadora.addOrReplaceChild("group8", CubeListBuilder.create(), PartPose.offset(0.0F, -3.25F, 0.4746F));
		PartDefinition group9 = group8.addOrReplaceChild("group9", CubeListBuilder.create(), PartPose.offset(0.0854F, 3.75F, 2.2293F));
		PartDefinition cube_r81 = group9.addOrReplaceChild("cube_r81",
				CubeListBuilder.create().texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.1104F, 0.0F, -1.3604F, 0.2444F, -0.7703F, -0.1719F));
		PartDefinition cube_r82 = group9.addOrReplaceChild("cube_r82",
				CubeListBuilder.create().texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.1104F, 0.0F, -1.3604F, 0.1745F, 0.0F, 0.0F));
		PartDefinition group10 = group8.addOrReplaceChild("group10", CubeListBuilder.create(), PartPose.offset(0.0854F, 3.75F, 2.2293F));
		PartDefinition cube_r83 = group10.addOrReplaceChild("cube_r83",
				CubeListBuilder.create().texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.2646F, 0.0F, -2.7354F, 0.0F, 0.7854F, -0.1745F));
		PartDefinition cube_r84 = group10.addOrReplaceChild("cube_r84",
				CubeListBuilder.create().texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.2646F, 0.0F, -2.7354F, 0.0F, 0.0F, -0.1745F));
		PartDefinition group11 = group8.addOrReplaceChild("group11", CubeListBuilder.create(), PartPose.offset(0.0854F, 3.75F, 2.2293F));
		PartDefinition cube_r85 = group11.addOrReplaceChild("cube_r85",
				CubeListBuilder.create().texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.7887F, 0.0F, -3.6344F, -0.0869F, -0.0076F, -0.0869F));
		PartDefinition cube_r86 = group11.addOrReplaceChild("cube_r86",
				CubeListBuilder.create().texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.7887F, 0.0F, -3.6344F, -0.1217F, 0.7741F, -0.1729F));
		PartDefinition group12 = group8.addOrReplaceChild("group12", CubeListBuilder.create(), PartPose.offset(0.0854F, 3.75F, 2.2293F));
		PartDefinition cube_r87 = group12.addOrReplaceChild("cube_r87",
				CubeListBuilder.create().texOffs(92, 65).addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.0094F, 0.0F, -3.6344F, -0.0869F, 0.0076F, 0.0869F));
		PartDefinition cube_r88 = group12.addOrReplaceChild("cube_r88",
				CubeListBuilder.create().texOffs(92, 65).addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.0094F, 0.0F, -3.6344F, -0.1236F, 0.7892F, -0.0003F));
		PartDefinition group13 = group8.addOrReplaceChild("group13", CubeListBuilder.create(), PartPose.offset(0.0854F, 3.75F, 2.2293F));
		PartDefinition cube_r89 = group13.addOrReplaceChild("cube_r89",
				CubeListBuilder.create().texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.1104F, 0.0F, -4.1104F, -0.2444F, -0.7703F, 0.1719F));
		PartDefinition cube_r90 = group13.addOrReplaceChild("cube_r90",
				CubeListBuilder.create().texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.1104F, 0.0F, -4.1104F, -0.1745F, 0.0F, 0.0F));
		PartDefinition group14 = group8.addOrReplaceChild("group14", CubeListBuilder.create(), PartPose.offset(0.0854F, 3.75F, 2.2293F));
		PartDefinition cube_r91 = group14.addOrReplaceChild("cube_r91",
				CubeListBuilder.create().texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.4854F, 0.0F, -2.7354F, 0.0F, 0.7854F, 0.1745F));
		PartDefinition cube_r92 = group14.addOrReplaceChild("cube_r92",
				CubeListBuilder.create().texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.4854F, 0.0F, -2.7354F, 0.0F, 0.0F, 0.1745F));
		PartDefinition group15 = group8.addOrReplaceChild("group15", CubeListBuilder.create(), PartPose.offset(0.0854F, 3.75F, 2.2293F));
		PartDefinition cube_r93 = group15.addOrReplaceChild("cube_r93",
				CubeListBuilder.create().texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.0094F, 0.0F, -1.8363F, 0.0873F, 0.0F, 0.0873F));
		PartDefinition cube_r94 = group15.addOrReplaceChild("cube_r94",
				CubeListBuilder.create().texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.0094F, 0.0F, -1.8363F, 0.1231F, 0.7816F, 0.1742F));
		PartDefinition group = group8.addOrReplaceChild("group", CubeListBuilder.create(), PartPose.offset(0.0854F, 3.75F, 2.2293F));
		PartDefinition cube_r95 = group.addOrReplaceChild("cube_r95",
				CubeListBuilder.create().texOffs(92, 65).addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.7887F, 0.0F, -1.8363F, 0.0869F, 0.0076F, -0.0869F));
		PartDefinition cube_r96 = group.addOrReplaceChild("cube_r96",
				CubeListBuilder.create().texOffs(92, 65).addBox(-0.1811F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, 0.1311F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)).texOffs(92, 65)
						.addBox(0.1311F, -1.0F, -0.075F, 0.05F, 2.0F, 0.15F, new CubeDeformation(0.0F)).texOffs(92, 65).addBox(-0.075F, -1.0F, -0.1811F, 0.15F, 2.0F, 0.05F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.7887F, 0.0F, -1.8363F, 0.1236F, 0.7892F, 0.0003F));
		PartDefinition group6 = Centrifugadora.addOrReplaceChild(
				"group6", CubeListBuilder.create().texOffs(40, 71).addBox(-0.45F, 1.75F, -2.0F, 0.9F, 1.75F, 0.5F, new CubeDeformation(0.0F)).texOffs(18, 70).addBox(-2.2623F, 1.75F, -4.2123F, 0.5F, 1.75F, 0.9F, new CubeDeformation(0.0F))
						.texOffs(56, 71).addBox(-0.45F, 1.75F, -6.0246F, 0.9F, 1.75F, 0.5F, new CubeDeformation(0.0F)).texOffs(70, 33).addBox(1.7623F, 1.75F, -4.2123F, 0.5F, 1.75F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -1.5F, 3.7746F));
		PartDefinition cube_r97 = group6.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(68, 71).addBox(-0.45F, -0.5F, -0.25F, 0.9F, 1.75F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.7701F, 2.25F, -1.9032F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r98 = group6.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(70, 60).addBox(-0.25F, -0.5F, -0.45F, 0.5F, 1.75F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.4229F, 2.25F, -2.3394F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r99 = group6.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(70, 57).addBox(-0.25F, -0.5F, -0.45F, 0.5F, 1.75F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.8591F, 2.25F, -2.9922F, 0.0F, -0.3927F, 0.0F));
		PartDefinition cube_r100 = group6.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(70, 54).addBox(-0.25F, -0.5F, -0.45F, 0.5F, 1.75F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.8591F, 2.25F, -4.5324F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r101 = group6.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(64, 71).addBox(-0.45F, -0.5F, -0.25F, 0.9F, 1.75F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.4229F, 2.25F, -5.1852F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r102 = group6.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(60, 71).addBox(-0.45F, -0.5F, -0.25F, 0.9F, 1.75F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.7701F, 2.25F, -5.6214F, 0.0F, -0.3927F, 0.0F));
		PartDefinition cube_r103 = group6.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(52, 71).addBox(-0.45F, -0.5F, -0.25F, 0.9F, 1.75F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.7701F, 2.25F, -5.6214F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r104 = group6.addOrReplaceChild("cube_r104", CubeListBuilder.create().texOffs(22, 70).addBox(-0.25F, -0.5F, -0.45F, 0.5F, 1.75F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.4229F, 2.25F, -5.1852F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r105 = group6.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(70, 63).addBox(-0.25F, -0.5F, -0.45F, 0.5F, 1.75F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.8591F, 2.25F, -4.5324F, 0.0F, -0.3927F, 0.0F));
		PartDefinition cube_r106 = group6.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(70, 51).addBox(-0.25F, -0.5F, -0.45F, 0.5F, 1.75F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.8591F, 2.25F, -2.9922F, 0.0F, 0.3927F, 0.0F));
		PartDefinition cube_r107 = group6.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(48, 71).addBox(-0.45F, -0.5F, -0.25F, 0.9F, 1.75F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.4229F, 2.25F, -2.3394F, 0.0F, -0.7854F, 0.0F));
		PartDefinition cube_r108 = group6.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(44, 71).addBox(-0.45F, -0.5F, -0.25F, 0.9F, 1.75F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.7701F, 2.25F, -1.9032F, 0.0F, -0.3927F, 0.0F));
		PartDefinition Boton2 = TODO.addOrReplaceChild("Boton2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition Boton = Boton2.addOrReplaceChild("Boton", CubeListBuilder.create().texOffs(64, 51).addBox(-1.0F, -1.0F, 0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.2F, -2.5F, -8.0F));
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		TODO.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
}