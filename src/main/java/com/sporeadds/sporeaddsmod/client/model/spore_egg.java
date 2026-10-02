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
public class spore_egg<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("sporeaddsmod", "modelspore_egg"), "main");
	public final ModelPart SporePod;
	public final ModelPart TumorBase;
	public final ModelPart Base;
	public final ModelPart Petals;
	public final ModelPart Petal1;
	public final ModelPart Petal1Middle;
	public final ModelPart Membrane14;
	public final ModelPart Membrane10;
	public final ModelPart Petal1Middle2;
	public final ModelPart Membrane11;
	public final ModelPart Membrane15;
	public final ModelPart Petal1Top;
	public final ModelPart Membrane12;
	public final ModelPart Membrane16;
	public final ModelPart PetalTumor1;
	public final ModelPart Membrane9;
	public final ModelPart Membrane13;
	public final ModelPart Petal2;
	public final ModelPart bone4;
	public final ModelPart Petal2Middle;
	public final ModelPart Membrane18;
	public final ModelPart Membrane22;
	public final ModelPart Petal2Middle2;
	public final ModelPart Membrane23;
	public final ModelPart Membrane19;
	public final ModelPart Petal2Top;
	public final ModelPart Membrane20;
	public final ModelPart Membrane24;
	public final ModelPart PetalTumor2;
	public final ModelPart Petal2TopFungus;
	public final ModelPart Membrane17;
	public final ModelPart Membrane21;
	public final ModelPart Petal3;
	public final ModelPart bone2;
	public final ModelPart Petal3Middle;
	public final ModelPart Membrane30;
	public final ModelPart Membrane26;
	public final ModelPart Petal3Middle2;
	public final ModelPart Petal3Top;
	public final ModelPart Membrane28;
	public final ModelPart Membrane32;
	public final ModelPart PetalTumor3;
	public final ModelPart Membrane27;
	public final ModelPart Membrane31;
	public final ModelPart Membrane25;
	public final ModelPart Membrane29;
	public final ModelPart Petal4;
	public final ModelPart Petal4Middle;
	public final ModelPart Membrane38;
	public final ModelPart Membrane34;
	public final ModelPart Petal4Middle2;
	public final ModelPart Petal4Top;
	public final ModelPart PetalTumor4Sub1;
	public final ModelPart PetalTumor4Sub2;
	public final ModelPart Membrane36;
	public final ModelPart Membrane40;
	public final ModelPart Membrane35;
	public final ModelPart Membrane39;
	public final ModelPart Membrane33;
	public final ModelPart Membrane37;
	public final ModelPart Petal4BaseFungus;
	public final ModelPart Petal5;
	public final ModelPart Petal5Middle;
	public final ModelPart Membrane5;
	public final ModelPart Membrane6;
	public final ModelPart Petal5Middle2;
	public final ModelPart Membrane3;
	public final ModelPart Membrane4;
	public final ModelPart Petal5Top;
	public final ModelPart Membrane;
	public final ModelPart Membrane2;
	public final ModelPart PetalTumor5;
	public final ModelPart Membrane8;
	public final ModelPart Membrane7;
	public final ModelPart Jaw;
	public final ModelPart TopJaw;
	public final ModelPart BottomJaw;
	public final ModelPart InternalDetails;
	public final ModelPart bone;
	public final ModelPart bone3;
	public final ModelPart bb_main;

	public spore_egg(ModelPart root) {
		this.SporePod = root.getChild("SporePod");
		this.TumorBase = this.SporePod.getChild("TumorBase");
		this.Base = this.SporePod.getChild("Base");
		this.Petals = this.Base.getChild("Petals");
		this.Petal1 = this.Petals.getChild("Petal1");
		this.Petal1Middle = this.Petal1.getChild("Petal1Middle");
		this.Membrane14 = this.Petal1Middle.getChild("Membrane14");
		this.Membrane10 = this.Petal1Middle.getChild("Membrane10");
		this.Petal1Middle2 = this.Petal1Middle.getChild("Petal1Middle2");
		this.Membrane11 = this.Petal1Middle2.getChild("Membrane11");
		this.Membrane15 = this.Petal1Middle2.getChild("Membrane15");
		this.Petal1Top = this.Petal1Middle2.getChild("Petal1Top");
		this.Membrane12 = this.Petal1Top.getChild("Membrane12");
		this.Membrane16 = this.Petal1Top.getChild("Membrane16");
		this.PetalTumor1 = this.Petal1Top.getChild("PetalTumor1");
		this.Membrane9 = this.Petal1.getChild("Membrane9");
		this.Membrane13 = this.Petal1.getChild("Membrane13");
		this.Petal2 = this.Petals.getChild("Petal2");
		this.bone4 = this.Petal2.getChild("bone4");
		this.Petal2Middle = this.Petal2.getChild("Petal2Middle");
		this.Membrane18 = this.Petal2Middle.getChild("Membrane18");
		this.Membrane22 = this.Petal2Middle.getChild("Membrane22");
		this.Petal2Middle2 = this.Petal2Middle.getChild("Petal2Middle2");
		this.Membrane23 = this.Petal2Middle2.getChild("Membrane23");
		this.Membrane19 = this.Petal2Middle2.getChild("Membrane19");
		this.Petal2Top = this.Petal2Middle2.getChild("Petal2Top");
		this.Membrane20 = this.Petal2Top.getChild("Membrane20");
		this.Membrane24 = this.Petal2Top.getChild("Membrane24");
		this.PetalTumor2 = this.Petal2Top.getChild("PetalTumor2");
		this.Petal2TopFungus = this.Petal2Top.getChild("Petal2TopFungus");
		this.Membrane17 = this.Petal2.getChild("Membrane17");
		this.Membrane21 = this.Petal2.getChild("Membrane21");
		this.Petal3 = this.Petals.getChild("Petal3");
		this.bone2 = this.Petal3.getChild("bone2");
		this.Petal3Middle = this.Petal3.getChild("Petal3Middle");
		this.Membrane30 = this.Petal3Middle.getChild("Membrane30");
		this.Membrane26 = this.Petal3Middle.getChild("Membrane26");
		this.Petal3Middle2 = this.Petal3Middle.getChild("Petal3Middle2");
		this.Petal3Top = this.Petal3Middle2.getChild("Petal3Top");
		this.Membrane28 = this.Petal3Top.getChild("Membrane28");
		this.Membrane32 = this.Petal3Top.getChild("Membrane32");
		this.PetalTumor3 = this.Petal3Top.getChild("PetalTumor3");
		this.Membrane27 = this.Petal3Middle2.getChild("Membrane27");
		this.Membrane31 = this.Petal3Middle2.getChild("Membrane31");
		this.Membrane25 = this.Petal3.getChild("Membrane25");
		this.Membrane29 = this.Petal3.getChild("Membrane29");
		this.Petal4 = this.Petals.getChild("Petal4");
		this.Petal4Middle = this.Petal4.getChild("Petal4Middle");
		this.Membrane38 = this.Petal4Middle.getChild("Membrane38");
		this.Membrane34 = this.Petal4Middle.getChild("Membrane34");
		this.Petal4Middle2 = this.Petal4Middle.getChild("Petal4Middle2");
		this.Petal4Top = this.Petal4Middle2.getChild("Petal4Top");
		this.PetalTumor4Sub1 = this.Petal4Top.getChild("PetalTumor4Sub1");
		this.PetalTumor4Sub2 = this.Petal4Top.getChild("PetalTumor4Sub2");
		this.Membrane36 = this.Petal4Top.getChild("Membrane36");
		this.Membrane40 = this.Petal4Top.getChild("Membrane40");
		this.Membrane35 = this.Petal4Middle2.getChild("Membrane35");
		this.Membrane39 = this.Petal4Middle2.getChild("Membrane39");
		this.Membrane33 = this.Petal4.getChild("Membrane33");
		this.Membrane37 = this.Petal4.getChild("Membrane37");
		this.Petal4BaseFungus = this.Petal4.getChild("Petal4BaseFungus");
		this.Petal5 = this.Petals.getChild("Petal5");
		this.Petal5Middle = this.Petal5.getChild("Petal5Middle");
		this.Membrane5 = this.Petal5Middle.getChild("Membrane5");
		this.Membrane6 = this.Petal5Middle.getChild("Membrane6");
		this.Petal5Middle2 = this.Petal5Middle.getChild("Petal5Middle2");
		this.Membrane3 = this.Petal5Middle2.getChild("Membrane3");
		this.Membrane4 = this.Petal5Middle2.getChild("Membrane4");
		this.Petal5Top = this.Petal5Middle2.getChild("Petal5Top");
		this.Membrane = this.Petal5Top.getChild("Membrane");
		this.Membrane2 = this.Petal5Top.getChild("Membrane2");
		this.PetalTumor5 = this.Petal5Top.getChild("PetalTumor5");
		this.Membrane8 = this.Petal5.getChild("Membrane8");
		this.Membrane7 = this.Petal5.getChild("Membrane7");
		this.Jaw = this.Base.getChild("Jaw");
		this.TopJaw = this.Jaw.getChild("TopJaw");
		this.BottomJaw = this.Jaw.getChild("BottomJaw");
		this.InternalDetails = this.Base.getChild("InternalDetails");
		this.bone = root.getChild("bone");
		this.bone3 = root.getChild("bone3");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition SporePod = partdefinition.addOrReplaceChild("SporePod", CubeListBuilder.create(), PartPose.offset(-0.85F, 24.0F, -3.0F));
		PartDefinition TumorBase = SporePod.addOrReplaceChild("TumorBase", CubeListBuilder.create(), PartPose.offset(2.0062F, -1.7921F, 4.1882F));
		PartDefinition Base = SporePod.addOrReplaceChild("Base", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition Petals = Base.addOrReplaceChild("Petals", CubeListBuilder.create(), PartPose.offset(0.85F, -5.0F, 3.0F));
		PartDefinition Petal1 = Petals.addOrReplaceChild("Petal1", CubeListBuilder.create().texOffs(8, 114).addBox(-0.99F, -7.7267F, -2.825F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.15F, 0.0F, -5.5F, 0.48F, 0.0F, 0.0F));
		PartDefinition Petal1Middle = Petal1.addOrReplaceChild("Petal1Middle", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -11.0347F, 0.0245F, -0.48F, 0.0F, 0.0F));
		PartDefinition BaseRidgeMiddle_r1 = Petal1Middle.addOrReplaceChild("BaseRidgeMiddle_r1", CubeListBuilder.create().texOffs(0, 113).addBox(-0.99F, -6.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.8325F, -1.0571F, 0.1745F, 0.0F, 0.0F));
		PartDefinition Membrane14 = Petal1Middle.addOrReplaceChild("Membrane14", CubeListBuilder.create(), PartPose.offset(0.4341F, -1.7763F, -1.2381F));
		PartDefinition Membrane_r1 = Membrane14.addOrReplaceChild("Membrane_r1", CubeListBuilder.create().texOffs(0, 123).addBox(0.0F, -4.0F, -0.75F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 123)
				.addBox(0.0F, 1.0F, -0.75F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 90).addBox(0.0F, -7.0F, 0.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, -0.5236F, 0.0F));
		PartDefinition Membrane10 = Petal1Middle.addOrReplaceChild("Membrane10", CubeListBuilder.create(), PartPose.offset(-0.4341F, -1.7763F, -1.2381F));
		PartDefinition Membrane_r2 = Membrane10.addOrReplaceChild("Membrane_r2", CubeListBuilder.create().texOffs(3, 117).addBox(-5.0F, 2.0F, -0.75F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(1, 117)
						.addBox(-7.0F, -2.0F, -0.75F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(99, 105).addBox(-8.0F, -7.0F, 0.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.5236F, 0.0F));
		PartDefinition Petal1Middle2 = Petal1Middle.addOrReplaceChild("Petal1Middle2", CubeListBuilder.create(), PartPose.offset(0.01F, -11.6601F, -1.7151F));
		PartDefinition BaseRidgeMiddle_r2 = Petal1Middle2.addOrReplaceChild("BaseRidgeMiddle_r2",
				CubeListBuilder.create().texOffs(0, 113).addBox(-0.99F, -17.3704F, 3.2338F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 113).addBox(-0.99F, -17.3704F, 3.2338F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.01F, 12.4524F, -5.3395F, -0.0873F, 0.0F, 0.0F));
		PartDefinition Membrane11 = Petal1Middle2.addOrReplaceChild("Membrane11", CubeListBuilder.create(), PartPose.offset(-3.6636F, 0.3048F, 1.2143F));
		PartDefinition Membrane_r3 = Membrane11.addOrReplaceChild("Membrane_r3", CubeListBuilder.create().texOffs(3, 119).addBox(-2.0F, -5.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5923F, 1.9924F, 0.1407F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane_r4 = Membrane11.addOrReplaceChild("Membrane_r4", CubeListBuilder.create().texOffs(3, 119).addBox(-1.75F, 1.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.7217F, 0.0F, 0.4167F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane_r5 = Membrane11.addOrReplaceChild("Membrane_r5", CubeListBuilder.create().texOffs(99, 105).addBox(-10.125F, -16.3208F, 3.6666F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.2195F, 11.9543F, -6.7391F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane15 = Petal1Middle2.addOrReplaceChild("Membrane15", CubeListBuilder.create(), PartPose.offset(3.9322F, 0.3048F, 1.381F));
		PartDefinition Membrane_r6 = Membrane15.addOrReplaceChild("Membrane_r6", CubeListBuilder.create().texOffs(0, 120).addBox(-3.0F, -1.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.433F, 0.0F, -0.25F, -0.0873F, -0.5236F, 0.0F));
		PartDefinition Membrane_r7 = Membrane15.addOrReplaceChild("Membrane_r7", CubeListBuilder.create().texOffs(13, 90).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.433F, 0.0F, 0.25F, -0.0873F, -0.5236F, 0.0F));
		PartDefinition Petal1Top = Petal1Middle2.addOrReplaceChild("Petal1Top", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -9.2662F, 3.262F, -0.6545F, 0.0F, 0.0F));
		PartDefinition BaseRidgeTop_r1 = Petal1Top.addOrReplaceChild("BaseRidgeTop_r1", CubeListBuilder.create().texOffs(32, 117).addBox(-1.0F, -18.8851F, -0.972F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 12.7785F, 1.6541F, 0.1309F, 0.0F, 0.0F));
		PartDefinition Membrane12 = Petal1Top.addOrReplaceChild("Membrane12", CubeListBuilder.create(), PartPose.offset(-3.1484F, 2.001F, 1.4685F));
		PartDefinition Membrane_r8 = Membrane12.addOrReplaceChild("Membrane_r8", CubeListBuilder.create().texOffs(1, 121).addBox(-0.75F, 0.5F, 0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.0512F, -0.8649F, -0.6893F, 0.3123F, 0.4458F, 0.3853F));
		PartDefinition Membrane_r9 = Membrane12.addOrReplaceChild("Membrane_r9", CubeListBuilder.create().texOffs(111, 31).addBox(-6.8596F, -16.9441F, 0.0695F, 6.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.4995F, 10.3598F, -0.5265F, 0.1309F, 0.5236F, 0.0F));
		PartDefinition Membrane16 = Petal1Top.addOrReplaceChild("Membrane16", CubeListBuilder.create(), PartPose.offset(3.2035F, 0.9663F, 1.3545F));
		PartDefinition Membrane_r10 = Membrane16.addOrReplaceChild("Membrane_r10", CubeListBuilder.create().texOffs(0, 123).addBox(-4.0F, -1.5F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.4803F, 0.0393F, 0.2833F, 0.3317F, -0.432F, -0.431F));
		PartDefinition Membrane_r11 = Membrane16.addOrReplaceChild("Membrane_r11", CubeListBuilder.create().texOffs(110, 0).addBox(-3.0F, -5.5F, 0.0F, 6.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.4803F, 0.0393F, 0.2833F, 0.1309F, -0.5236F, 0.0F));
		PartDefinition PetalTumor1 = Petal1Top.addOrReplaceChild("PetalTumor1", CubeListBuilder.create(), PartPose.offset(0.99F, -0.9667F, 2.2073F));
		PartDefinition Membrane9 = Petal1.addOrReplaceChild("Membrane9", CubeListBuilder.create(), PartPose.offset(0.0F, -1.2363F, -1.9168F));
		PartDefinition Membrane_r12 = Membrane9.addOrReplaceChild("Membrane_r12", CubeListBuilder.create().texOffs(98, 7).addBox(-7.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));
		PartDefinition Membrane13 = Petal1.addOrReplaceChild("Membrane13", CubeListBuilder.create(), PartPose.offset(0.0F, -1.2363F, -1.9168F));
		PartDefinition Membrane_r13 = Membrane13.addOrReplaceChild("Membrane_r13", CubeListBuilder.create().texOffs(95, 92).addBox(1.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));
		PartDefinition Petal2 = Petals.addOrReplaceChild("Petal2", CubeListBuilder.create().texOffs(8, 114).addBox(-0.99F, -7.7267F, -2.825F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.8563F, 0.0F, -0.8541F, 0.48F, -1.2566F, 0.0F));
		PartDefinition bone4 = Petal2.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offsetAndRotation(4.7459F, -25.3238F, 4.7666F, 0.0F, 1.3963F, 0.0F));
		PartDefinition HeadLeft_r1 = bone4.addOrReplaceChild("HeadLeft_r1", CubeListBuilder.create().texOffs(56, 56).addBox(-1.5F, -3.5F, -3.0F, 4.0F, 7.0F, 8.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(-2.007F, 0.4122F, 0.8105F, 2.5792F, -1.0178F, -3.0976F));
		PartDefinition Petal2Middle = Petal2.addOrReplaceChild("Petal2Middle", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -11.0347F, 0.0245F, -0.48F, 0.0F, 0.0F));
		PartDefinition BaseRidgeMiddle_r3 = Petal2Middle.addOrReplaceChild("BaseRidgeMiddle_r3", CubeListBuilder.create().texOffs(0, 113).addBox(-0.99F, -6.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.8325F, -1.0571F, 0.1745F, 0.0F, 0.0F));
		PartDefinition Membrane18 = Petal2Middle.addOrReplaceChild("Membrane18", CubeListBuilder.create(), PartPose.offset(-0.3905F, -1.7763F, -1.384F));
		PartDefinition Membrane_r14 = Membrane18.addOrReplaceChild("Membrane_r14", CubeListBuilder.create().texOffs(0, 121).addBox(-8.0F, -5.75F, -0.5F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(2, 121)
						.addBox(-6.0F, -1.75F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(99, 105).addBox(-8.0F, -7.0F, 0.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.5236F, 0.0F));
		PartDefinition Membrane22 = Petal2Middle.addOrReplaceChild("Membrane22", CubeListBuilder.create(), PartPose.offset(0.4778F, -1.7763F, -1.384F));
		PartDefinition Membrane_r15 = Membrane22.addOrReplaceChild("Membrane_r15", CubeListBuilder.create().texOffs(0, 117).addBox(-3.5F, -1.0F, -0.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.7706F, 2.9544F, 2.2012F, 0.1745F, -0.5236F, 0.0F));
		PartDefinition Membrane_r16 = Membrane22.addOrReplaceChild("Membrane_r16", CubeListBuilder.create().texOffs(0, 117).addBox(-2.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.8552F, 0.0434F, 0.7868F, 0.1745F, -0.5236F, 0.0F));
		PartDefinition Membrane_r17 = Membrane22.addOrReplaceChild("Membrane_r17", CubeListBuilder.create().texOffs(13, 90).addBox(0.0F, -7.0F, 0.0F, 7.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, -0.5236F, 0.0F));
		PartDefinition Petal2Middle2 = Petal2Middle.addOrReplaceChild("Petal2Middle2", CubeListBuilder.create(), PartPose.offset(0.01F, -11.9634F, -1.6954F));
		PartDefinition BaseRidgeMiddle_r4 = Petal2Middle2.addOrReplaceChild("BaseRidgeMiddle_r4", CubeListBuilder.create().texOffs(0, 113).addBox(-0.99F, -17.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.01F, 12.4524F, -1.0894F, -0.0873F, 0.0F, 0.0F));
		PartDefinition Membrane23 = Petal2Middle2.addOrReplaceChild("Membrane23", CubeListBuilder.create(), PartPose.offset(1.4677F, 12.2592F, -1.4207F));
		PartDefinition Membrane_r18 = Membrane23.addOrReplaceChild("Membrane_r18", CubeListBuilder.create().texOffs(0, 121).addBox(-4.0F, -1.0F, -0.5F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.2965F, -13.9249F, 3.0224F, -0.0873F, -0.5236F, 0.0F));
		PartDefinition Membrane_r19 = Membrane23.addOrReplaceChild("Membrane_r19", CubeListBuilder.create().texOffs(0, 121).addBox(-2.5F, -1.0F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.1282F, -10.9363F, 2.0459F, -0.0873F, -0.5236F, 0.0F));
		PartDefinition Membrane_r20 = Membrane23.addOrReplaceChild("Membrane_r20", CubeListBuilder.create().texOffs(0, 119).addBox(-3.0F, -0.5F, -0.25F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.8873F, -8.1432F, 1.9061F, 0.2182F, -0.5236F, 0.0F));
		PartDefinition Membrane_r21 = Membrane23.addOrReplaceChild("Membrane_r21", CubeListBuilder.create().texOffs(13, 90).addBox(-3.5F, -4.0F, 0.0F, 7.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.5082F, -11.9543F, 2.6557F, -0.0873F, -0.5236F, 0.0F));
		PartDefinition Membrane19 = Petal2Middle2.addOrReplaceChild("Membrane19", CubeListBuilder.create(), PartPose.offset(-3.9304F, 0.8029F, 1.1973F));
		PartDefinition Membrane_r22 = Membrane19.addOrReplaceChild("Membrane_r22", CubeListBuilder.create().texOffs(2, 122).addBox(-3.0F, -1.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.4112F, 0.4981F, -0.2877F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane_r23 = Membrane19.addOrReplaceChild("Membrane_r23", CubeListBuilder.create().texOffs(99, 105).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.4112F, -0.4981F, 0.2877F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Petal2Top = Petal2Middle2.addOrReplaceChild("Petal2Top", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -9.2662F, 3.262F, -0.6545F, 0.0F, 0.0F));
		PartDefinition BaseRidgeTop_r2 = Petal2Top.addOrReplaceChild("BaseRidgeTop_r2", CubeListBuilder.create().texOffs(32, 117).addBox(-1.0F, -16.7601F, -4.6526F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 10.1913F, 5.0258F, 0.1309F, 0.0F, 0.0F));
		PartDefinition Membrane20 = Petal2Top.addOrReplaceChild("Membrane20", CubeListBuilder.create(), PartPose.offset(-3.6602F, 1.0944F, 1.5221F));
		PartDefinition Membrane_r24 = Membrane20.addOrReplaceChild("Membrane_r24",
				CubeListBuilder.create().texOffs(0, 122).addBox(-2.5F, 0.5F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(111, 31).addBox(-3.0F, -5.5F, 0.0F, 6.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1309F, 0.5236F, 0.0F));
		PartDefinition Membrane24 = Petal2Top.addOrReplaceChild("Membrane24", CubeListBuilder.create(), PartPose.offset(-0.3274F, 9.8624F, 4.1979F));
		PartDefinition Membrane_r25 = Membrane24.addOrReplaceChild("Membrane_r25", CubeListBuilder.create().texOffs(-1, 117).addBox(-2.5F, -1.0F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.7233F, -8.2723F, -3.3694F, 0.1309F, -0.5236F, 0.0F));
		PartDefinition Membrane_r26 = Membrane24.addOrReplaceChild("Membrane_r26", CubeListBuilder.create().texOffs(110, 0).addBox(-0.8263F, -14.7601F, -3.1632F, 6.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1309F, -0.5236F, 0.0F));
		PartDefinition PetalTumor2 = Petal2Top.addOrReplaceChild("PetalTumor2", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.7163F, -3.5539F, 4.9332F, 0.3054F, -0.48F, 0.0F));
		PartDefinition Petal2TopFungus = Petal2Top.addOrReplaceChild("Petal2TopFungus", CubeListBuilder.create(), PartPose.offset(1.0743F, 3.3829F, 5.0033F));
		PartDefinition Membrane17 = Petal2.addOrReplaceChild("Membrane17", CubeListBuilder.create(), PartPose.offset(0.0437F, -1.3037F, -2.0462F));
		PartDefinition Membrane_r27 = Membrane17.addOrReplaceChild("Membrane_r27", CubeListBuilder.create().texOffs(98, 7).addBox(-7.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));
		PartDefinition Membrane21 = Petal2.addOrReplaceChild("Membrane21", CubeListBuilder.create(), PartPose.offset(0.0437F, -1.3037F, -2.0462F));
		PartDefinition Membrane_r28 = Membrane21.addOrReplaceChild("Membrane_r28", CubeListBuilder.create().texOffs(95, 92).addBox(1.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));
		PartDefinition Petal3 = Petals.addOrReplaceChild("Petal3", CubeListBuilder.create().texOffs(8, 114).addBox(0.7634F, -8.1687F, -1.3058F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.7673F, 1.0936F, 5.7485F, -2.6616F, -0.6283F, 3.1416F));
		PartDefinition bone2 = Petal3.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(6.5983F, -27.0861F, -5.1418F));
		PartDefinition HeadLeft_r2 = bone2.addOrReplaceChild("HeadLeft_r2", CubeListBuilder.create().texOffs(60, 56).addBox(2.5F, -3.5F, -3.0F, 4.0F, 7.0F, 8.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(-14.9563F, 3.2464F, 16.6794F, -0.7446F, 0.337F, -0.4631F));
		PartDefinition Petal3Middle = Petal3.addOrReplaceChild("Petal3Middle", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.3172F, -14.9106F, -3.2672F, -0.48F, 0.0F, 0.0F));
		PartDefinition BaseRidgeMiddle_r5 = Petal3Middle.addOrReplaceChild("BaseRidgeMiddle_r5", CubeListBuilder.create().texOffs(0, 113).addBox(-0.99F, -6.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0705F, -1.0081F, 4.7958F, 0.1745F, 0.0F, 0.0F));
		PartDefinition Membrane30 = Petal3Middle.addOrReplaceChild("Membrane30", CubeListBuilder.create(), PartPose.offset(3.2826F, 0.1382F, 1.0607F));
		PartDefinition Membrane_r29 = Membrane30.addOrReplaceChild("Membrane_r29", CubeListBuilder.create().texOffs(-1, 116).addBox(-4.0F, -1.0F, -0.5F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.6141F, 1.1257F, 5.5383F, 0.1745F, -0.5236F, 0.0F));
		PartDefinition Membrane_r30 = Membrane30.addOrReplaceChild("Membrane_r30", CubeListBuilder.create().texOffs(-1, 116).addBox(-2.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.1643F, -2.075F, 4.0495F, 0.1745F, -0.5236F, 0.0F));
		PartDefinition Membrane_r31 = Membrane30.addOrReplaceChild("Membrane_r31", CubeListBuilder.create().texOffs(13, 90).addBox(-3.5F, -6.0F, 0.0F, 7.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.4633F, -2.075F, 4.7995F, 0.1745F, -0.5236F, 0.0F));
		PartDefinition Membrane26 = Petal3Middle.addOrReplaceChild("Membrane26", CubeListBuilder.create(), PartPose.offset(-0.3208F, 0.0562F, -0.5352F));
		PartDefinition Membrane_r32 = Membrane26.addOrReplaceChild("Membrane_r32", CubeListBuilder.create().texOffs(4, 117).addBox(-2.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.5221F, 0.9615F, 6.0965F, 0.1745F, 0.5236F, 0.0F));
		PartDefinition Membrane_r33 = Membrane26.addOrReplaceChild("Membrane_r33", CubeListBuilder.create().texOffs(0, 117).addBox(-4.0F, -1.0F, -0.5F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5572F, -2.9777F, 6.495F, 0.1745F, 0.5236F, 0.0F));
		PartDefinition Membrane_r34 = Membrane26.addOrReplaceChild("Membrane_r34", CubeListBuilder.create().texOffs(99, 105).addBox(-8.0F, -7.0F, 0.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0805F, -1.0081F, 4.7958F, 0.1745F, 0.5236F, 0.0F));
		PartDefinition Petal3Middle2 = Petal3Middle.addOrReplaceChild("Petal3Middle2", CubeListBuilder.create(), PartPose.offset(0.0F, -9.6328F, -0.6819F));
		PartDefinition BaseRidgeMiddle_r6 = Petal3Middle2.addOrReplaceChild("BaseRidgeMiddle_r6", CubeListBuilder.create().texOffs(0, 113).addBox(-0.99F, -16.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0705F, 10.9462F, 3.7499F, -0.0873F, 0.0F, 0.0F));
		PartDefinition Petal3Top = Petal3Middle2.addOrReplaceChild("Petal3Top", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -11.9204F, 4.832F, -0.6545F, 0.0F, 0.0F));
		PartDefinition BaseRidgeTop_r3 = Petal3Top.addOrReplaceChild("BaseRidgeTop_r3", CubeListBuilder.create().texOffs(32, 114).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0805F, -0.2311F, 3.2993F, 0.2182F, 0.0F, 0.0F));
		PartDefinition Membrane28 = Petal3Top.addOrReplaceChild("Membrane28", CubeListBuilder.create(), PartPose.offset(0.4743F, 9.6289F, 4.1343F));
		PartDefinition Membrane_r35 = Membrane28.addOrReplaceChild("Membrane_r35", CubeListBuilder.create().texOffs(1, 121).addBox(-2.5F, -1.0F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.4435F, -8.0001F, 0.4347F, 0.1309F, 0.5236F, 0.0F));
		PartDefinition Membrane_r36 = Membrane28.addOrReplaceChild("Membrane_r36", CubeListBuilder.create().texOffs(111, 31).addBox(-5.1737F, -14.7601F, -3.1632F, 6.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0806F, -0.7192F, 3.1911F, 0.1309F, 0.5236F, 0.0F));
		PartDefinition Membrane32 = Petal3Top.addOrReplaceChild("Membrane32", CubeListBuilder.create(), PartPose.offset(-0.3853F, 9.1176F, 4.6106F));
		PartDefinition Membrane_r37 = Membrane32.addOrReplaceChild("Membrane_r37",
				CubeListBuilder.create().texOffs(-1, 118).addBox(-1.8263F, -9.7601F, -3.4132F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(110, 0).addBox(-0.8263F, -14.7601F, -3.1632F, 6.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0805F, -0.7192F, 3.191F, 0.2182F, -0.5236F, 0.0F));
		PartDefinition PetalTumor3 = Petal3Top.addOrReplaceChild("PetalTumor3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.693F, -3.1104F, 5.1004F, 0.4638F, -0.3305F, -0.3961F));
		PartDefinition Membrane27 = Petal3Middle2.addOrReplaceChild("Membrane27", CubeListBuilder.create(), PartPose.offset(-1.3208F, 11.7611F, -1.5853F));
		PartDefinition Membrane_r38 = Membrane27.addOrReplaceChild("Membrane_r38", CubeListBuilder.create().texOffs(4, 120).addBox(-2.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0831F, -14.933F, 7.0682F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane_r39 = Membrane27.addOrReplaceChild("Membrane_r39", CubeListBuilder.create().texOffs(0, 120).addBox(-4.0F, -1.0F, -0.5F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.7797F, -11.9444F, 7.8417F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane_r40 = Membrane27.addOrReplaceChild("Membrane_r40", CubeListBuilder.create().texOffs(1, 125).addBox(-3.5F, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.4874F, -9.1513F, 7.3479F, 0.3054F, 0.5236F, 0.0F));
		PartDefinition Membrane_r41 = Membrane27.addOrReplaceChild("Membrane_r41", CubeListBuilder.create().texOffs(99, 105).addBox(-8.0F, -16.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0805F, -1.0081F, 4.7958F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane31 = Petal3Middle2.addOrReplaceChild("Membrane31", CubeListBuilder.create(), PartPose.offset(4.0337F, -0.6914F, 1.4082F));
		PartDefinition Membrane_r42 = Membrane31.addOrReplaceChild("Membrane_r42",
				CubeListBuilder.create().texOffs(-1, 122).addBox(-2.5F, -4.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(-1, 122).addBox(-2.5F, -1.0F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.9389F, 0.508F, 4.2732F, -0.0873F, -0.5236F, 0.0F));
		PartDefinition Membrane_r43 = Membrane31.addOrReplaceChild("Membrane_r43", CubeListBuilder.create().texOffs(13, 90).addBox(-3.5F, -4.0F, 0.0F, 7.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.1023F, -0.51F, 4.758F, -0.0873F, -0.5236F, 0.0F));
		PartDefinition Membrane25 = Petal3.addOrReplaceChild("Membrane25", CubeListBuilder.create(), PartPose.offset(-0.2039F, -3.1621F, -5.4311F));
		PartDefinition Membrane_r44 = Membrane25.addOrReplaceChild("Membrane_r44", CubeListBuilder.create().texOffs(98, 7).addBox(-7.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0805F, 1.3202F, 4.7194F, 0.0F, 0.5236F, 0.0F));
		PartDefinition Membrane29 = Petal3.addOrReplaceChild("Membrane29", CubeListBuilder.create(), PartPose.offset(-0.2039F, -3.1621F, -5.4311F));
		PartDefinition Membrane_r45 = Membrane29.addOrReplaceChild("Membrane_r45", CubeListBuilder.create().texOffs(95, 92).addBox(1.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0805F, 1.3202F, 4.7194F, 0.0F, -0.5236F, 0.0F));
		PartDefinition Petal4 = Petals.addOrReplaceChild("Petal4", CubeListBuilder.create().texOffs(8, 114).addBox(-0.99F, -7.7267F, -2.825F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.3767F, 0.0F, 5.8541F, -2.6616F, 0.6283F, 3.1416F));
		PartDefinition Petal4Middle = Petal4.addOrReplaceChild("Petal4Middle", CubeListBuilder.create(), PartPose.offsetAndRotation(0.01F, -13.1483F, -0.067F, -0.48F, 0.0F, 0.0F));
		PartDefinition BaseRidgeMiddle_r7 = Petal4Middle.addOrReplaceChild("BaseRidgeMiddle_r7", CubeListBuilder.create().texOffs(0, 113).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));
		PartDefinition Membrane38 = Petal4Middle.addOrReplaceChild("Membrane38", CubeListBuilder.create(), PartPose.offset(3.377F, 0.5269F, 1.1466F));
		PartDefinition Membrane_r46 = Membrane38.addOrReplaceChild("Membrane_r46",
				CubeListBuilder.create().texOffs(1, 117).addBox(0.5F, 1.0F, -0.25F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 90).addBox(0.0F, -7.0F, 0.0F, 7.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.9261F, -0.4707F, -1.9318F, 0.1745F, -0.5236F, 0.0F));
		PartDefinition Membrane34 = Petal4Middle.addOrReplaceChild("Membrane34", CubeListBuilder.create(), PartPose.offset(-0.6674F, 0.0562F, -0.7852F));
		PartDefinition Membrane_r47 = Membrane34.addOrReplaceChild("Membrane_r47", CubeListBuilder.create().texOffs(0, 116).addBox(-4.0F, -1.0F, -0.5F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.3106F, 6.2111F, 3.565F, 0.48F, 0.5236F, 0.0F));
		PartDefinition Membrane_r48 = Membrane34.addOrReplaceChild("Membrane_r48", CubeListBuilder.create().texOffs(0, 116).addBox(-4.0F, -1.0F, -0.5F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.1778F, -1.0282F, 2.0628F, 0.1745F, 0.5236F, 0.0F));
		PartDefinition Membrane_r49 = Membrane34.addOrReplaceChild("Membrane_r49", CubeListBuilder.create().texOffs(99, 105).addBox(-4.0F, -6.0F, 0.0F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.3009F, -0.9848F, 1.8496F, 0.1745F, 0.5236F, 0.0F));
		PartDefinition Petal4Middle2 = Petal4Middle.addOrReplaceChild("Petal4Middle2", CubeListBuilder.create(), PartPose.offset(0.0F, -9.6328F, -0.6819F));
		PartDefinition BaseRidgeMiddle_r8 = Petal4Middle2.addOrReplaceChild("BaseRidgeMiddle_r8", CubeListBuilder.create().texOffs(0, 113).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
		PartDefinition Petal4Top = Petal4Middle2.addOrReplaceChild("Petal4Top", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -9.3758F, 3.2408F, -0.6545F, 0.0F, 0.0F));
		PartDefinition BaseRidgeTop_r4 = Petal4Top.addOrReplaceChild("BaseRidgeTop_r4", CubeListBuilder.create().texOffs(32, 115).addBox(-1.0F, -6.5F, -1.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));
		PartDefinition PetalTumor4Sub1 = Petal4Top.addOrReplaceChild("PetalTumor4Sub1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.7464F, -3.0978F, 4.8138F, 0.562F, -0.0231F, -0.935F));
		PartDefinition PetalTumor4Sub2 = Petal4Top.addOrReplaceChild("PetalTumor4Sub2", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.7567F, -3.2453F, 4.6331F, 0.7093F, 0.1835F, -1.2706F));
		PartDefinition Membrane36 = Petal4Top.addOrReplaceChild("Membrane36", CubeListBuilder.create(), PartPose.offset(-0.9272F, 0.843F, -0.2886F));
		PartDefinition Membrane_r50 = Membrane36.addOrReplaceChild("Membrane_r50", CubeListBuilder.create().texOffs(0, 118).addBox(-3.25F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.2645F, -0.9631F, 0.8205F, 0.1278F, 0.4803F, -0.0064F));
		PartDefinition Membrane_r51 = Membrane36.addOrReplaceChild("Membrane_r51", CubeListBuilder.create().texOffs(111, 31).addBox(-3.0F, -5.5F, 0.0F, 6.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.75F, 0.0F, 1.5F, 0.1309F, 0.5236F, 0.0F));
		PartDefinition Membrane40 = Petal4Top.addOrReplaceChild("Membrane40", CubeListBuilder.create(), PartPose.offset(3.1519F, 0.3051F, 1.0376F));
		PartDefinition Membrane_r52 = Membrane40.addOrReplaceChild("Membrane_r52", CubeListBuilder.create().texOffs(0, 117).addBox(-2.5F, -1.0F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.4347F, -0.627F, -0.2299F, 0.3123F, -0.4458F, -0.3853F));
		PartDefinition Membrane_r53 = Membrane40.addOrReplaceChild("Membrane_r53", CubeListBuilder.create().texOffs(110, 0).addBox(-3.0F, -5.5F, 0.0F, 6.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.4348F, 0.627F, 0.2299F, 0.1309F, -0.5236F, 0.0F));
		PartDefinition Membrane35 = Petal4Middle2.addOrReplaceChild("Membrane35", CubeListBuilder.create(), PartPose.offset(-3.7127F, 0.1533F, 1.1558F));
		PartDefinition Membrane_r54 = Membrane35.addOrReplaceChild("Membrane_r54",
				CubeListBuilder.create().texOffs(2, 119).addBox(-1.0F, 2.0F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(-1, 119).addBox(-4.0F, -1.0F, -0.5F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.2612F, -1.321F, 0.3809F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane_r55 = Membrane35.addOrReplaceChild("Membrane_r55", CubeListBuilder.create().texOffs(99, 105).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.6458F, -0.3466F, 0.2147F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane39 = Petal4Middle2.addOrReplaceChild("Membrane39", CubeListBuilder.create(), PartPose.offset(3.5486F, 0.5809F, 0.9646F));
		PartDefinition Membrane_r56 = Membrane39.addOrReplaceChild("Membrane_r56", CubeListBuilder.create().texOffs(0, 119).addBox(-3.0F, -4.75F, -0.5F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0612F, 2.244F, -0.2273F, -0.0873F, -0.5236F, 0.0F));
		PartDefinition Membrane_r57 = Membrane39.addOrReplaceChild("Membrane_r57",
				CubeListBuilder.create().texOffs(0, 119).addBox(-3.5F, 2.0F, -0.1601F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 90).addBox(-3.5F, -4.0F, 0.25F, 7.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.4104F, -0.7742F, -0.0441F, -0.0873F, -0.5236F, 0.0F));
		PartDefinition Membrane33 = Petal4.addOrReplaceChild("Membrane33", CubeListBuilder.create(), PartPose.offset(-0.7233F, -5.2652F, -1.7026F));
		PartDefinition Membrane_r58 = Membrane33.addOrReplaceChild("Membrane_r58", CubeListBuilder.create().texOffs(98, 7).addBox(-7.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.75F, 3.75F, -0.75F, 0.0F, 0.5236F, 0.0F));
		PartDefinition Membrane37 = Petal4.addOrReplaceChild("Membrane37", CubeListBuilder.create(), PartPose.offset(0.9908F, -4.5152F, -1.7026F));
		PartDefinition Membrane_r59 = Membrane37.addOrReplaceChild("Membrane_r59", CubeListBuilder.create().texOffs(95, 92).addBox(1.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.9641F, 3.0F, -0.75F, 0.0F, -0.5236F, 0.0F));
		PartDefinition Petal4BaseFungus = Petal4.addOrReplaceChild("Petal4BaseFungus", CubeListBuilder.create(), PartPose.offset(0.8174F, -6.8392F, 0.7984F));
		PartDefinition Petal5 = Petals.addOrReplaceChild("Petal5", CubeListBuilder.create().texOffs(8, 114).addBox(-0.99F, -7.7267F, -2.825F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.5231F, 0.0F, -0.9534F, 0.48F, 1.2392F, 0.0F));
		PartDefinition Petal5Middle = Petal5.addOrReplaceChild("Petal5Middle", CubeListBuilder.create(), PartPose.offsetAndRotation(0.01F, -13.1483F, -0.067F, -0.48F, 0.0F, 0.0F));
		PartDefinition BaseRidgeMiddle_r9 = Petal5Middle.addOrReplaceChild("BaseRidgeMiddle_r9", CubeListBuilder.create().texOffs(0, 113).addBox(-0.99F, -6.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.01F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));
		PartDefinition Membrane5 = Petal5Middle.addOrReplaceChild("Membrane5", CubeListBuilder.create(), PartPose.offset(-3.2345F, 0.1627F, 0.9891F));
		PartDefinition Membrane_r60 = Membrane5.addOrReplaceChild("Membrane_r60",
				CubeListBuilder.create().texOffs(1, 117).addBox(-3.5F, -4.0F, -0.25F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(4, 117).addBox(-0.5F, -1.0F, -0.25F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.2738F, 1.8397F, 0.3591F, 0.2182F, 0.5236F, 0.0F));
		PartDefinition Membrane_r61 = Membrane5.addOrReplaceChild("Membrane_r61", CubeListBuilder.create().texOffs(99, 105).addBox(-4.0F, -5.5F, 0.25F, 8.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.794F, -0.5989F, -0.0419F, 0.1745F, 0.5236F, 0.0F));
		PartDefinition Membrane6 = Petal5Middle.addOrReplaceChild("Membrane6", CubeListBuilder.create(), PartPose.offset(2.9497F, 0.6837F, 0.8481F));
		PartDefinition Membrane_r62 = Membrane6.addOrReplaceChild("Membrane_r62", CubeListBuilder.create().texOffs(-1, 117).addBox(0.5F, 3.0F, -0.25F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(-1, 117)
						.addBox(0.5F, -2.0F, -0.25F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 90).addBox(0.0F, -7.0F, 0.0F, 7.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.6025F, -0.6276F, -1.8257F, 0.1745F, -0.5236F, 0.0F));
		PartDefinition Petal5Middle2 = Petal5Middle.addOrReplaceChild("Petal5Middle2", CubeListBuilder.create(), PartPose.offset(0.0F, -10.1309F, -0.7383F));
		PartDefinition BaseRidgeMiddle_r10 = Petal5Middle2.addOrReplaceChild("BaseRidgeMiddle_r10", CubeListBuilder.create().texOffs(0, 113).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0785F, 0.0F, 0.0F));
		PartDefinition Membrane3 = Petal5Middle2.addOrReplaceChild("Membrane3", CubeListBuilder.create(), PartPose.offset(-3.7675F, 0.4927F, 1.2042F));
		PartDefinition Membrane_r63 = Membrane3.addOrReplaceChild("Membrane_r63", CubeListBuilder.create().texOffs(3, 119).addBox(-2.5F, -1.0F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.8161F, -2.1584F, -0.2531F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane_r64 = Membrane3.addOrReplaceChild("Membrane_r64", CubeListBuilder.create().texOffs(1, 119).addBox(-3.0F, 1.5F, -0.75F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.4456F, -0.1442F, 0.5616F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane_r65 = Membrane3.addOrReplaceChild("Membrane_r65", CubeListBuilder.create().texOffs(99, 105).addBox(-8.0F, -16.0F, 0.25F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.2466F, 11.7665F, -2.7755F, -0.0873F, 0.5236F, 0.0F));
		PartDefinition Membrane4 = Petal5Middle2.addOrReplaceChild("Membrane4", CubeListBuilder.create(), PartPose.offset(3.8554F, 0.3048F, 0.8844F));
		PartDefinition Membrane_r66 = Membrane4.addOrReplaceChild("Membrane_r66", CubeListBuilder.create().texOffs(0, 119).addBox(-3.5F, -3.0F, -0.1F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 119)
				.addBox(-3.5F, 0.0F, -0.1F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 90).addBox(-3.5F, -4.0F, 0.0F, 7.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, -0.5236F, 0.0F));
		PartDefinition Petal5Top = Petal5Middle2.addOrReplaceChild("Petal5Top", CubeListBuilder.create().texOffs(-7, 31).addBox(-3.6651F, 14.2248F, 1.6404F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -9.2607F, 3.2222F, -0.6545F, 0.0F, 0.0F));
		PartDefinition BaseRidgeTop_r5 = Petal5Top.addOrReplaceChild("BaseRidgeTop_r5", CubeListBuilder.create().texOffs(32, 117).addBox(-1.0F, -16.7601F, -4.6526F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 10.1913F, 5.0258F, 0.1309F, 0.0F, 0.0F));
		PartDefinition Membrane = Petal5Top.addOrReplaceChild("Membrane", CubeListBuilder.create(), PartPose.offset(-2.6446F, 0.7653F, 1.0064F));
		PartDefinition Membrane_r67 = Membrane.addOrReplaceChild("Membrane_r67", CubeListBuilder.create().texOffs(3, 117).addBox(-2.0F, -1.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0123F, -0.3597F, -0.6391F, 0.3123F, 0.4458F, 0.3853F));
		PartDefinition Membrane_r68 = Membrane.addOrReplaceChild("Membrane_r68", CubeListBuilder.create().texOffs(111, 31).addBox(-3.0F, -5.5F, 0.0F, 6.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.0123F, 0.3597F, 0.6391F, 0.1309F, 0.5236F, 0.0F));
		PartDefinition Membrane2 = Petal5Top.addOrReplaceChild("Membrane2", CubeListBuilder.create(), PartPose.offset(2.9736F, 0.7029F, 0.9644F));
		PartDefinition Membrane_r69 = Membrane2.addOrReplaceChild("Membrane_r69", CubeListBuilder.create().texOffs(0, 122).addBox(-3.0F, -2.5F, 0.75F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.1291F, 0.7069F, -0.5509F, 0.2921F, -0.4588F, -0.339F));
		PartDefinition Membrane_r70 = Membrane2.addOrReplaceChild("Membrane_r70", CubeListBuilder.create().texOffs(110, 0).addBox(-0.8263F, -14.7601F, -3.1632F, 6.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.4215F, 9.3444F, 2.9836F, 0.1309F, -0.5236F, 0.0F));
		PartDefinition PetalTumor5 = Petal5Top.addOrReplaceChild("PetalTumor5", CubeListBuilder.create(), PartPose.offset(0.8929F, -4.8396F, 6.914F));
		PartDefinition Membrane8 = Petal5.addOrReplaceChild("Membrane8", CubeListBuilder.create(), PartPose.offset(3.3872F, -4.6041F, -1.1233F));
		PartDefinition Membrane_r71 = Membrane8.addOrReplaceChild("Membrane_r71", CubeListBuilder.create().texOffs(95, 92).addBox(1.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.4641F, 3.0F, -1.5F, 0.0F, -0.5236F, 0.0F));
		PartDefinition Membrane7 = Petal5.addOrReplaceChild("Membrane7", CubeListBuilder.create(), PartPose.offset(-0.0769F, -1.6041F, -2.6233F));
		PartDefinition Membrane_r72 = Membrane7.addOrReplaceChild("Membrane_r72", CubeListBuilder.create().texOffs(98, 7).addBox(-7.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));
		PartDefinition Jaw = Base.addOrReplaceChild("Jaw", CubeListBuilder.create(), PartPose.offsetAndRotation(4.8576F, -5.1743F, 5.0F, 1.5708F, -1.4835F, -3.1416F));
		PartDefinition TopJaw = Jaw.addOrReplaceChild("TopJaw", CubeListBuilder.create(), PartPose.offsetAndRotation(0.3838F, -9.8279F, 3.8621F, -0.6981F, 0.0F, 0.0F));
		PartDefinition BottomJaw = Jaw.addOrReplaceChild("BottomJaw", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.6314F, 1.8279F, 2.8621F, 0.6906F, -0.1119F, 0.1343F));
		PartDefinition InternalDetails = Base.addOrReplaceChild("InternalDetails", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(6.7801F, 3.9553F, -8.3605F));
		PartDefinition HeadRightTeeth_r1 = bone.addOrReplaceChild("HeadRightTeeth_r1",
				CubeListBuilder.create().texOffs(71, 85).addBox(-3.9991F, -2.8822F, -2.7545F, 8.0F, 2.0F, 3.0F, new CubeDeformation(-0.2F)).texOffs(67, 25).addBox(-4.0009F, -1.1178F, -2.7456F, 8.0F, 6.0F, 8.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.6109F, 0.0F));
		PartDefinition bone3 = partdefinition.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(-0.5627F, 0.8102F, 11.3003F));
		PartDefinition HeadCenter_r1 = bone3.addOrReplaceChild("HeadCenter_r1", CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, -6.5F, 1.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(-0.2F)),
				PartPose.offsetAndRotation(3.2593F, 0.669F, 5.1893F, 3.1283F, 0.084F, 2.2647F));
		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition Biomass_r1 = bb_main.addOrReplaceChild("Biomass_r1", CubeListBuilder.create().texOffs(6, 2).addBox(3.0F, -3.0F, -7.0F, 8.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.5202F, -0.1648F, 9.862F, 0.4094F, 0.4032F, -0.6728F));
		PartDefinition Biomass_r2 = bb_main.addOrReplaceChild("Biomass_r2", CubeListBuilder.create().texOffs(8, 4).addBox(3.0F, -3.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5202F, -0.1648F, -3.138F, 0.4094F, 0.4032F, -0.6728F));
		PartDefinition Biomass_r3 = bb_main.addOrReplaceChild("Biomass_r3", CubeListBuilder.create().texOffs(2, 1).addBox(-5.5F, -5.5F, -5.5F, 11.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(8.0551F, -1.9228F, 2.0819F, 0.2392F, -0.0133F, -0.3783F));
		PartDefinition Biomass_r4 = bb_main.addOrReplaceChild("Biomass_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.9324F, -1.0636F, -1.5631F, -0.329F, -1.0983F, 1.1961F));
		PartDefinition Plane_r1 = bb_main.addOrReplaceChild("Plane_r1", CubeListBuilder.create().texOffs(0, 131).mirror().addBox(-10.0F, -7.0076F, 0.1743F, 20.0F, 18.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, -17.0F, 1.0F, -0.0873F, -0.9599F, 0.0F));
		PartDefinition Plane_r2 = bb_main.addOrReplaceChild("Plane_r2", CubeListBuilder.create().texOffs(0, 131).addBox(-10.1743F, -7.0247F, -0.2601F, 20.0F, 18.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -17.0F, 1.0F, 0.1309F, 0.0F, -0.0873F));
		PartDefinition Plane_r3 = bb_main.addOrReplaceChild("Plane_r3", CubeListBuilder.create().texOffs(0, 131).addBox(-10.0F, -7.0171F, -0.2611F, 20.0F, 18.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -17.0F, 1.0F, -3.0107F, -1.0472F, 3.1416F));
		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		SporePod.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
}