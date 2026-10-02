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

public class Cocon<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("sporeadd", "cocon"), "main");
	private final ModelPart Animation_Elements;
	private final ModelPart Membrans;
	private final ModelPart Membrane_Bone_South;
	private final ModelPart Membrane_Bone_1;
	private final ModelPart Membrane_Bone_2;
	private final ModelPart Membrane_Bone_3;
	private final ModelPart Membrane_Bone_4;
	private final ModelPart Membrane_Bone_West;
	private final ModelPart Membrane_Bone_1_1;
	private final ModelPart Membrane_Bone_2_1;
	private final ModelPart Membrane_Bone_3_1;
	private final ModelPart Membrane_Bone_4_1;
	private final ModelPart Membrane_Bone_North;
	private final ModelPart Membrane_Bone_1_2;
	private final ModelPart Membrane_Bone_2_2;
	private final ModelPart Membrane_Bone_3_2;
	private final ModelPart Membrane_Bone_4_2;
	private final ModelPart Membrane_Bone_East;
	private final ModelPart Membrane_Bone_1_3;
	private final ModelPart Membrane_Bone_2_3;
	private final ModelPart Membrane_Bone_3_3;
	private final ModelPart Membrane_Bone_4_3;
	private final ModelPart Tentacle;
	private final ModelPart Blossom_Head_Tentacle;
	private final ModelPart Body_Tentackle3;
	private final ModelPart Cheast_Bones2;
	private final ModelPart Blossom_Head1;
	private final ModelPart RibSeg;
	private final ModelPart Rib1Seg1;
	private final ModelPart Rib2Seg1;
	private final ModelPart Rib1Seg2;
	private final ModelPart Rib2Seg2;
	private final ModelPart Rib3Seg2;
	private final ModelPart Blossom_Head_Tentacle2;
	private final ModelPart Body_Tentackle2;
	private final ModelPart Cheast_Bones3;
	private final ModelPart Blossom_Head2;
	private final ModelPart RibSeg2;
	private final ModelPart Rib1Seg3;
	private final ModelPart Rib2Seg3;
	private final ModelPart Rib1Seg4;
	private final ModelPart Rib2Seg4;
	private final ModelPart Rib3Seg3;
	private final ModelPart Mouth_Head_Tentacle;
	private final ModelPart torso_tentacle3;
	private final ModelPart ribs_torso2;
	private final ModelPart head1;
	private final ModelPart Ribs;
	private final ModelPart Rib2Seg6;
	private final ModelPart Rib3Seg5;
	private final ModelPart Rib4Seg3;
	private final ModelPart Rib5Seg2;
	private final ModelPart Mouth_Head_Tentacle2;
	private final ModelPart torso_tentacle2;
	private final ModelPart ribs_torso3;
	private final ModelPart head2;
	private final ModelPart Rib2Seg5;
	private final ModelPart Rib3Seg4;
	private final ModelPart Rib4Seg2;
	private final ModelPart Rib5Seg4;
	private final ModelPart Base;

	public Cocon(ModelPart root) {
		this.Animation_Elements = root.getChild("Animation_Elements");
		this.Membrans = this.Animation_Elements.getChild("Membrans");
		this.Membrane_Bone_South = this.Membrans.getChild("Membrane_Bone_South");
		this.Membrane_Bone_1 = this.Membrane_Bone_South.getChild("Membrane_Bone_1");
		this.Membrane_Bone_2 = this.Membrane_Bone_1.getChild("Membrane_Bone_2");
		this.Membrane_Bone_3 = this.Membrane_Bone_2.getChild("Membrane_Bone_3");
		this.Membrane_Bone_4 = this.Membrane_Bone_3.getChild("Membrane_Bone_4");
		this.Membrane_Bone_West = this.Membrans.getChild("Membrane_Bone_West");
		this.Membrane_Bone_1_1 = this.Membrane_Bone_West.getChild("Membrane_Bone_1_1");
		this.Membrane_Bone_2_1 = this.Membrane_Bone_1_1.getChild("Membrane_Bone_2_1");
		this.Membrane_Bone_3_1 = this.Membrane_Bone_2_1.getChild("Membrane_Bone_3_1");
		this.Membrane_Bone_4_1 = this.Membrane_Bone_3_1.getChild("Membrane_Bone_4_1");
		this.Membrane_Bone_North = this.Membrans.getChild("Membrane_Bone_North");
		this.Membrane_Bone_1_2 = this.Membrane_Bone_North.getChild("Membrane_Bone_1_2");
		this.Membrane_Bone_2_2 = this.Membrane_Bone_1_2.getChild("Membrane_Bone_2_2");
		this.Membrane_Bone_3_2 = this.Membrane_Bone_2_2.getChild("Membrane_Bone_3_2");
		this.Membrane_Bone_4_2 = this.Membrane_Bone_3_2.getChild("Membrane_Bone_4_2");
		this.Membrane_Bone_East = this.Membrans.getChild("Membrane_Bone_East");
		this.Membrane_Bone_1_3 = this.Membrane_Bone_East.getChild("Membrane_Bone_1_3");
		this.Membrane_Bone_2_3 = this.Membrane_Bone_1_3.getChild("Membrane_Bone_2_3");
		this.Membrane_Bone_3_3 = this.Membrane_Bone_2_3.getChild("Membrane_Bone_3_3");
		this.Membrane_Bone_4_3 = this.Membrane_Bone_3_3.getChild("Membrane_Bone_4_3");
		this.Tentacle = this.Animation_Elements.getChild("Tentacle");
		this.Blossom_Head_Tentacle = this.Tentacle.getChild("Blossom_Head_Tentacle");
		this.Body_Tentackle3 = this.Blossom_Head_Tentacle.getChild("Body_Tentackle3");
		this.Cheast_Bones2 = this.Body_Tentackle3.getChild("Cheast_Bones2");
		this.Blossom_Head1 = this.Cheast_Bones2.getChild("Blossom_Head1");
		this.RibSeg = this.Cheast_Bones2.getChild("RibSeg");
		this.Rib1Seg1 = this.RibSeg.getChild("Rib1Seg1");
		this.Rib2Seg1 = this.RibSeg.getChild("Rib2Seg1");
		this.Rib1Seg2 = this.RibSeg.getChild("Rib1Seg2");
		this.Rib2Seg2 = this.RibSeg.getChild("Rib2Seg2");
		this.Rib3Seg2 = this.RibSeg.getChild("Rib3Seg2");
		this.Blossom_Head_Tentacle2 = this.Tentacle.getChild("Blossom_Head_Tentacle2");
		this.Body_Tentackle2 = this.Blossom_Head_Tentacle2.getChild("Body_Tentackle2");
		this.Cheast_Bones3 = this.Body_Tentackle2.getChild("Cheast_Bones3");
		this.Blossom_Head2 = this.Cheast_Bones3.getChild("Blossom_Head2");
		this.RibSeg2 = this.Cheast_Bones3.getChild("RibSeg2");
		this.Rib1Seg3 = this.RibSeg2.getChild("Rib1Seg3");
		this.Rib2Seg3 = this.RibSeg2.getChild("Rib2Seg3");
		this.Rib1Seg4 = this.RibSeg2.getChild("Rib1Seg4");
		this.Rib2Seg4 = this.RibSeg2.getChild("Rib2Seg4");
		this.Rib3Seg3 = this.RibSeg2.getChild("Rib3Seg3");
		this.Mouth_Head_Tentacle = this.Tentacle.getChild("Mouth_Head_Tentacle");
		this.torso_tentacle3 = this.Mouth_Head_Tentacle.getChild("torso_tentacle3");
		this.ribs_torso2 = this.torso_tentacle3.getChild("ribs_torso2");
		this.head1 = this.ribs_torso2.getChild("head1");
		this.Ribs = this.ribs_torso2.getChild("Ribs");
		this.Rib2Seg6 = this.Ribs.getChild("Rib2Seg6");
		this.Rib3Seg5 = this.Ribs.getChild("Rib3Seg5");
		this.Rib4Seg3 = this.Ribs.getChild("Rib4Seg3");
		this.Rib5Seg2 = this.Ribs.getChild("Rib5Seg2");
		this.Mouth_Head_Tentacle2 = this.Tentacle.getChild("Mouth_Head_Tentacle2");
		this.torso_tentacle2 = this.Mouth_Head_Tentacle2.getChild("torso_tentacle2");
		this.ribs_torso3 = this.torso_tentacle2.getChild("ribs_torso3");
		this.head2 = this.ribs_torso3.getChild("head2");
		this.Rib2Seg5 = this.ribs_torso3.getChild("Rib2Seg5");
		this.Rib3Seg4 = this.ribs_torso3.getChild("Rib3Seg4");
		this.Rib4Seg2 = this.ribs_torso3.getChild("Rib4Seg2");
		this.Rib5Seg4 = this.ribs_torso3.getChild("Rib5Seg4");
		this.Base = this.Animation_Elements.getChild("Base");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Animation_Elements = partdefinition.addOrReplaceChild("Animation_Elements", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Membrans = Animation_Elements.addOrReplaceChild("Membrans", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Membrane_Bone_South = Membrans.addOrReplaceChild("Membrane_Bone_South", CubeListBuilder.create().texOffs(44, 235).addBox(-2.1012F, -23.587F, -2.0F, 3.0F, 24.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition LowerRidgeRib3Tip_r1 = Membrane_Bone_South.addOrReplaceChild("LowerRidgeRib3Tip_r1", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(316, 313).addBox(-2.5F, 7.99F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.4466F, -21.587F, 15.0746F, 0.0F, -0.6981F, 0.0F));

		PartDefinition LowerRidgeRib3_r1 = Membrane_Bone_South.addOrReplaceChild("LowerRidgeRib3_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(18, 223).addBox(-1.5F, 8.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 94).addBox(0.0F, -1.0F, 0.0F, 0.0F, 22.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6012F, -21.587F, 1.5F, 0.0F, -0.3491F, 0.0F));

		PartDefinition LowerRidgeRib2Tip_r1 = Membrane_Bone_South.addOrReplaceChild("LowerRidgeRib2Tip_r1", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.4466F, -14.587F, -15.0746F, 0.0F, 0.6981F, 0.0F));

		PartDefinition LowerRidgeRib2_r1 = Membrane_Bone_South.addOrReplaceChild("LowerRidgeRib2_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(28, 94).addBox(0.0F, -8.0F, -12.0F, 0.0F, 22.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6012F, -14.587F, -1.5F, 0.0F, 0.3491F, 0.0F));

		PartDefinition LowerRidgeFrontMembraneTip_r1 = Membrane_Bone_South.addOrReplaceChild("LowerRidgeFrontMembraneTip_r1", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -11.01F, -2.75F, 0.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7069F, -11.577F, 15.5254F, 0.0F, -0.6981F, 0.0F));

		PartDefinition LowerRidgeFrontMembraneTip_r2 = Membrane_Bone_South.addOrReplaceChild("LowerRidgeFrontMembraneTip_r2", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -11.01F, -5.25F, 0.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7069F, -11.577F, -15.5254F, 0.0F, 0.6981F, 0.0F));

		PartDefinition Membrane_Bone_1 = Membrane_Bone_South.addOrReplaceChild("Membrane_Bone_1", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.6883F, -22.0833F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition MiddleRidgeRib2Tip_r1 = Membrane_Bone_1.addOrReplaceChild("MiddleRidgeRib2Tip_r1", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9795F, -14.6988F, -15.0746F, 0.0F, 0.6981F, 0.0873F));

		PartDefinition MiddleRidgeRib2_r1 = Membrane_Bone_1.addOrReplaceChild("MiddleRidgeRib2_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8474F, -14.2765F, -1.5F, 0.0F, 0.3491F, 0.0873F));

		PartDefinition MiddleRidgeRib1Tip_r1 = Membrane_Bone_1.addOrReplaceChild("MiddleRidgeRib1Tip_r1", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.6768F, -6.7292F, -15.0746F, 0.0F, 0.6981F, 0.0873F));

		PartDefinition MiddleRidgeRib1_r1 = Membrane_Bone_1.addOrReplaceChild("MiddleRidgeRib1_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1502F, -6.3069F, -1.5F, 0.0F, 0.3491F, 0.0873F));

		PartDefinition MiddleRidgeFrontMembraneTip_r1 = Membrane_Bone_1.addOrReplaceChild("MiddleRidgeFrontMembraneTip_r1", CubeListBuilder.create().texOffs(110, 20).addBox(-1.5F, -8.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8529F, -8.6547F, -15.6758F, 0.0F, 0.6981F, 0.0873F));

		PartDefinition MiddleRidgeFrontMembrane_r1 = Membrane_Bone_1.addOrReplaceChild("MiddleRidgeFrontMembrane_r1", CubeListBuilder.create().texOffs(144, 171).addBox(-0.5F, -8.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3207F, -8.2122F, -1.5F, 0.0F, 0.3491F, 0.0873F));

		PartDefinition MiddleRidgeBackMembrane_r1 = Membrane_Bone_1.addOrReplaceChild("MiddleRidgeBackMembrane_r1", CubeListBuilder.create().texOffs(0, 176).addBox(-0.5F, -8.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3207F, -8.2122F, 1.5F, 0.0F, -0.3491F, 0.0873F));

		PartDefinition MiddleRidgeBackMembraneTip_r1 = Membrane_Bone_1.addOrReplaceChild("MiddleRidgeBackMembraneTip_r1", CubeListBuilder.create().texOffs(0, 130).addBox(-1.5F, -8.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8529F, -8.6547F, 15.6758F, 0.0F, -0.6981F, 0.0873F));

		PartDefinition MiddleRidge_r1 = Membrane_Bone_1.addOrReplaceChild("MiddleRidge_r1", CubeListBuilder.create().texOffs(268, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0815F, -0.8663F, 0.0F, 0.0F, 0.0F, 0.0873F));

		PartDefinition Membrane_Bone_2 = Membrane_Bone_1.addOrReplaceChild("Membrane_Bone_2", CubeListBuilder.create(), PartPose.offset(2.1883F, -16.6667F, 0.0F));

		PartDefinition TopRidgeRib3Tip_r1 = Membrane_Bone_2.addOrReplaceChild("TopRidgeRib3Tip_r1", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8604F, 17.9518F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib3_r1 = Membrane_Bone_2.addOrReplaceChild("TopRidgeRib3_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9114F, 17.1104F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidgeRib2Tip_r1 = Membrane_Bone_2.addOrReplaceChild("TopRidgeRib2Tip_r1", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6448F, 24.8454F, -15.0746F, 0.0F, 0.6981F, -0.1745F));

		PartDefinition TopRidgeRib2_r1 = Membrane_Bone_2.addOrReplaceChild("TopRidgeRib2_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.127F, 24.004F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition TopRidgeRib1Tip_r1 = Membrane_Bone_2.addOrReplaceChild("TopRidgeRib1Tip_r1", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1239F, 27.7999F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib1_r1 = Membrane_Bone_2.addOrReplaceChild("TopRidgeRib1_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6479F, 26.9585F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r1 = Membrane_Bone_2.addOrReplaceChild("LeftRidgeTopMemebraneTip_r1", CubeListBuilder.create().texOffs(67, 43).addBox(-1.5F, -43.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5208F, 27.6963F, -15.6758F, 0.0F, 0.6981F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r1 = Membrane_Bone_2.addOrReplaceChild("LeftRidgeTopMemebrane_r1", CubeListBuilder.create().texOffs(120, 170).addBox(-0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6327F, 26.7848F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r2 = Membrane_Bone_2.addOrReplaceChild("LeftRidgeTopMemebraneTip_r2", CubeListBuilder.create().texOffs(0, 70).addBox(-1.5F, -43.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5208F, 27.6963F, 15.6758F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidge_r1 = Membrane_Bone_2.addOrReplaceChild("TopRidge_r1", CubeListBuilder.create().texOffs(254, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7066F, -0.0084F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r2 = Membrane_Bone_2.addOrReplaceChild("LeftRidgeTopMemebrane_r2", CubeListBuilder.create().texOffs(96, 177).addBox(-0.5F, -43.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6327F, 26.7848F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition Membrane_Bone_3 = Membrane_Bone_2.addOrReplaceChild("Membrane_Bone_3", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.5F, -15.75F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition TopRidgeRib3Tip_r2 = Membrane_Bone_3.addOrReplaceChild("TopRidgeRib3Tip_r2", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.7577F, 17.9237F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib3_r2 = Membrane_Bone_3.addOrReplaceChild("TopRidgeRib3_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0141F, 17.0823F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidgeRib2Tip_r2 = Membrane_Bone_3.addOrReplaceChild("TopRidgeRib2Tip_r2", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5421F, 24.8173F, -15.0746F, 0.0F, 0.6981F, -0.1745F));

		PartDefinition TopRidgeRib2_r2 = Membrane_Bone_3.addOrReplaceChild("TopRidgeRib2_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2297F, 23.9759F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition TopRidgeRib1Tip_r2 = Membrane_Bone_3.addOrReplaceChild("TopRidgeRib1Tip_r2", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0212F, 27.7717F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib1_r2 = Membrane_Bone_3.addOrReplaceChild("TopRidgeRib1_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7506F, 26.9303F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r3 = Membrane_Bone_3.addOrReplaceChild("LeftRidgeTopMemebraneTip_r3", CubeListBuilder.create().texOffs(67, 43).addBox(-1.5F, -43.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6235F, 27.6682F, -15.6758F, 0.0F, 0.6981F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r3 = Membrane_Bone_3.addOrReplaceChild("LeftRidgeTopMemebrane_r3", CubeListBuilder.create().texOffs(120, 170).addBox(-0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7354F, 26.7567F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r4 = Membrane_Bone_3.addOrReplaceChild("LeftRidgeTopMemebraneTip_r4", CubeListBuilder.create().texOffs(0, 70).addBox(-1.5F, -43.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6235F, 27.6682F, 15.6758F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidge_r2 = Membrane_Bone_3.addOrReplaceChild("TopRidge_r2", CubeListBuilder.create().texOffs(254, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6039F, -0.0365F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r4 = Membrane_Bone_3.addOrReplaceChild("LeftRidgeTopMemebrane_r4", CubeListBuilder.create().texOffs(96, 181).addBox(-0.5F, -43.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7354F, 26.7567F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition Membrane_Bone_4 = Membrane_Bone_3.addOrReplaceChild("Membrane_Bone_4", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, -15.25F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition LeftRidgeTopMemebrane_r5 = Membrane_Bone_4.addOrReplaceChild("LeftRidgeTopMemebrane_r5", CubeListBuilder.create().texOffs(120, 170).addBox(-1.459F, -9.2858F, -6.036F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9299F, -6.3298F, -5.5F, 0.0F, 0.2182F, -0.1745F));

		PartDefinition TopRidgeRib3_r3 = Membrane_Bone_4.addOrReplaceChild("TopRidgeRib3_r3", CubeListBuilder.create().texOffs(22, 227).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3488F, 16.9957F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidgeRib2_r3 = Membrane_Bone_4.addOrReplaceChild("TopRidgeRib2_r3", CubeListBuilder.create().texOffs(20, 225).addBox(-1.5F, -33.0F, -10.0F, 3.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5643F, 28.8894F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition TopRidgeRib1_r3 = Membrane_Bone_4.addOrReplaceChild("TopRidgeRib1_r3", CubeListBuilder.create().texOffs(20, 225).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0853F, 26.8438F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r6 = Membrane_Bone_4.addOrReplaceChild("LeftRidgeTopMemebrane_r6", CubeListBuilder.create().texOffs(120, 170).addBox(-0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0701F, 26.6702F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidge_r3 = Membrane_Bone_4.addOrReplaceChild("TopRidge_r3", CubeListBuilder.create().texOffs(254, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2693F, -0.123F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition Membrane_Bone_West = Membrans.addOrReplaceChild("Membrane_Bone_West", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.0F, -15.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition LowerRidgeFrontMembrane_r1 = Membrane_Bone_West.addOrReplaceChild("LowerRidgeFrontMembrane_r1", CubeListBuilder.create().texOffs(28, 94).addBox(0.0F, -11.0F, -12.0F, 0.0F, 22.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.352F, -10.7554F, -1.5F, 0.0F, 0.3491F, 0.4363F));

		PartDefinition LowerRidgeFrontMembraneTip_r3 = Membrane_Bone_West.addOrReplaceChild("LowerRidgeFrontMembraneTip_r3", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -11.01F, -5.25F, 0.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2796F, -12.9041F, -15.5254F, 0.0F, 0.6981F, 0.4363F));

		PartDefinition LowerRidgeFrontMembrane_r2 = Membrane_Bone_West.addOrReplaceChild("LowerRidgeFrontMembrane_r2", CubeListBuilder.create().texOffs(0, 94).addBox(0.0F, -11.0F, 0.0F, 0.0F, 22.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(18, 223).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.352F, -10.7554F, 1.5F, 0.0F, -0.3491F, 0.4363F));

		PartDefinition LowerRidgeFrontMembraneTip_r4 = Membrane_Bone_West.addOrReplaceChild("LowerRidgeFrontMembraneTip_r4", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -11.01F, -2.75F, 0.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2796F, -12.9041F, 15.5254F, 0.0F, -0.6981F, 0.4363F));

		PartDefinition LowerRidgeRib1Tip_r1 = Membrane_Bone_West.addOrReplaceChild("LowerRidgeRib1Tip_r1", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0394F, -12.8032F, 15.0746F, 0.0F, -0.6981F, 0.4363F));

		PartDefinition LowerRidgeRib3_r2 = Membrane_Bone_West.addOrReplaceChild("LowerRidgeRib3_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6198F, -13.4744F, -1.5F, 0.0F, 0.3491F, 0.4363F));

		PartDefinition LowerRidgeRib2Tip_r2 = Membrane_Bone_West.addOrReplaceChild("LowerRidgeRib2Tip_r2", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2284F, -15.5221F, -15.0746F, 0.0F, 0.6981F, 0.4363F));

		PartDefinition LowerRidgeRib4_r1 = Membrane_Bone_West.addOrReplaceChild("LowerRidgeRib4_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.5782F, -19.8185F, 1.5F, 0.0F, -0.3491F, 0.4363F));

		PartDefinition LowerRidgeRib3Tip_r2 = Membrane_Bone_West.addOrReplaceChild("LowerRidgeRib3Tip_r2", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1868F, -21.8663F, 15.0746F, 0.0F, -0.6981F, 0.4363F));

		PartDefinition LowerRidge_r1 = Membrane_Bone_West.addOrReplaceChild("LowerRidge_r1", CubeListBuilder.create().texOffs(44, 235).addBox(-1.0F, -24.0F, -2.0F, 3.0F, 24.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1726F, -0.0911F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition Membrane_Bone_1_1 = Membrane_Bone_West.addOrReplaceChild("Membrane_Bone_1_1", CubeListBuilder.create(), PartPose.offset(8.709F, -20.3052F, 0.0F));

		PartDefinition MiddleRidgeRib2Tip_r2 = Membrane_Bone_1_1.addOrReplaceChild("MiddleRidgeRib2Tip_r2", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9795F, -14.6988F, -15.0746F, 0.0F, 0.6981F, 0.0873F));

		PartDefinition MiddleRidgeRib3_r1 = Membrane_Bone_1_1.addOrReplaceChild("MiddleRidgeRib3_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8474F, -14.2765F, -1.5F, 0.0F, 0.3491F, 0.0873F));

		PartDefinition MiddleRidgeRib1Tip_r2 = Membrane_Bone_1_1.addOrReplaceChild("MiddleRidgeRib1Tip_r2", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.6768F, -6.7292F, -15.0746F, 0.0F, 0.6981F, 0.0873F));

		PartDefinition MiddleRidgeRib2_r2 = Membrane_Bone_1_1.addOrReplaceChild("MiddleRidgeRib2_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1502F, -6.3069F, -1.5F, 0.0F, 0.3491F, 0.0873F));

		PartDefinition MiddleRidgeFrontMembraneTip_r2 = Membrane_Bone_1_1.addOrReplaceChild("MiddleRidgeFrontMembraneTip_r2", CubeListBuilder.create().texOffs(110, 20).addBox(-1.5F, -8.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8529F, -8.6547F, -15.6758F, 0.0F, 0.6981F, 0.0873F));

		PartDefinition MiddleRidgeFrontMembrane_r2 = Membrane_Bone_1_1.addOrReplaceChild("MiddleRidgeFrontMembrane_r2", CubeListBuilder.create().texOffs(144, 171).addBox(-0.5F, -8.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3207F, -8.2122F, -1.5F, 0.0F, 0.3491F, 0.0873F));

		PartDefinition MiddleRidgeBackMembrane_r2 = Membrane_Bone_1_1.addOrReplaceChild("MiddleRidgeBackMembrane_r2", CubeListBuilder.create().texOffs(0, 176).addBox(-0.5F, -8.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3207F, -8.2122F, 1.5F, 0.0F, -0.3491F, 0.0873F));

		PartDefinition MiddleRidgeBackMembraneTip_r2 = Membrane_Bone_1_1.addOrReplaceChild("MiddleRidgeBackMembraneTip_r2", CubeListBuilder.create().texOffs(0, 130).addBox(-1.5F, -8.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8529F, -8.6547F, 15.6758F, 0.0F, -0.6981F, 0.0873F));

		PartDefinition MiddleRidge_r2 = Membrane_Bone_1_1.addOrReplaceChild("MiddleRidge_r2", CubeListBuilder.create().texOffs(268, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0815F, -0.8663F, 0.0F, 0.0F, 0.0F, 0.0873F));

		PartDefinition Membrane_Bone_2_1 = Membrane_Bone_1_1.addOrReplaceChild("Membrane_Bone_2_1", CubeListBuilder.create(), PartPose.offset(2.1883F, -16.6667F, 0.0F));

		PartDefinition TopRidgeRib3Tip_r3 = Membrane_Bone_2_1.addOrReplaceChild("TopRidgeRib3Tip_r3", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8603F, 17.9518F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib4_r1 = Membrane_Bone_2_1.addOrReplaceChild("TopRidgeRib4_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9114F, 17.1104F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidgeRib2Tip_r3 = Membrane_Bone_2_1.addOrReplaceChild("TopRidgeRib2Tip_r3", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6448F, 24.8454F, -15.0746F, 0.0F, 0.6981F, -0.1745F));

		PartDefinition TopRidgeRib3_r4 = Membrane_Bone_2_1.addOrReplaceChild("TopRidgeRib3_r4", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.127F, 24.004F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition TopRidgeRib1Tip_r3 = Membrane_Bone_2_1.addOrReplaceChild("TopRidgeRib1Tip_r3", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1239F, 27.7999F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib2_r4 = Membrane_Bone_2_1.addOrReplaceChild("TopRidgeRib2_r4", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6479F, 26.9585F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r5 = Membrane_Bone_2_1.addOrReplaceChild("LeftRidgeTopMemebraneTip_r5", CubeListBuilder.create().texOffs(67, 43).addBox(-1.5F, -43.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5208F, 27.6963F, -15.6758F, 0.0F, 0.6981F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r7 = Membrane_Bone_2_1.addOrReplaceChild("LeftRidgeTopMemebrane_r7", CubeListBuilder.create().texOffs(120, 170).addBox(-0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6327F, 26.7848F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r6 = Membrane_Bone_2_1.addOrReplaceChild("LeftRidgeTopMemebraneTip_r6", CubeListBuilder.create().texOffs(0, 70).addBox(-1.5F, -43.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5208F, 27.6963F, 15.6758F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidge_r4 = Membrane_Bone_2_1.addOrReplaceChild("TopRidge_r4", CubeListBuilder.create().texOffs(254, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7066F, -0.0084F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r8 = Membrane_Bone_2_1.addOrReplaceChild("LeftRidgeTopMemebrane_r8", CubeListBuilder.create().texOffs(96, 175).addBox(-0.5F, -43.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6327F, 26.7848F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition Membrane_Bone_3_1 = Membrane_Bone_2_1.addOrReplaceChild("Membrane_Bone_3_1", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.5F, -15.75F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition TopRidgeRib3Tip_r4 = Membrane_Bone_3_1.addOrReplaceChild("TopRidgeRib3Tip_r4", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.7577F, 17.9237F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib4_r2 = Membrane_Bone_3_1.addOrReplaceChild("TopRidgeRib4_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0141F, 17.0823F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidgeRib3_r5 = Membrane_Bone_3_1.addOrReplaceChild("TopRidgeRib3_r5", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2297F, 23.9759F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition TopRidgeRib1Tip_r4 = Membrane_Bone_3_1.addOrReplaceChild("TopRidgeRib1Tip_r4", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0212F, 27.7717F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib2_r5 = Membrane_Bone_3_1.addOrReplaceChild("TopRidgeRib2_r5", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7506F, 26.9303F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r9 = Membrane_Bone_3_1.addOrReplaceChild("LeftRidgeTopMemebrane_r9", CubeListBuilder.create().texOffs(120, 170).addBox(-0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7354F, 26.7567F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r7 = Membrane_Bone_3_1.addOrReplaceChild("LeftRidgeTopMemebraneTip_r7", CubeListBuilder.create().texOffs(0, 70).addBox(-1.5F, -43.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6235F, 27.6682F, 15.6758F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidge_r5 = Membrane_Bone_3_1.addOrReplaceChild("TopRidge_r5", CubeListBuilder.create().texOffs(254, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6039F, -0.0365F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r10 = Membrane_Bone_3_1.addOrReplaceChild("LeftRidgeTopMemebrane_r10", CubeListBuilder.create().texOffs(24, 178).addBox(-0.5F, -43.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7354F, 26.7567F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition Membrane_Bone_4_1 = Membrane_Bone_3_1.addOrReplaceChild("Membrane_Bone_4_1", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, -15.25F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition RightRidgeTopMemebrane_r1 = Membrane_Bone_4_1.addOrReplaceChild("RightRidgeTopMemebrane_r1", CubeListBuilder.create().texOffs(184, 268).addBox(4.4117F, -9.3602F, -13.0097F, 0.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5617F, -6.2439F, 2.5F, 0.0612F, 0.1024F, -0.061F));

		PartDefinition TopRidgeRib4_r3 = Membrane_Bone_4_1.addOrReplaceChild("TopRidgeRib4_r3", CubeListBuilder.create().texOffs(22, 227).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3488F, 16.9957F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidgeRib2_r6 = Membrane_Bone_4_1.addOrReplaceChild("TopRidgeRib2_r6", CubeListBuilder.create().texOffs(20, 225).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0852F, 26.8438F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r11 = Membrane_Bone_4_1.addOrReplaceChild("LeftRidgeTopMemebrane_r11", CubeListBuilder.create().texOffs(185, 218).addBox(-0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0701F, 26.6702F, 2.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidge_r6 = Membrane_Bone_4_1.addOrReplaceChild("TopRidge_r6", CubeListBuilder.create().texOffs(254, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2693F, -0.123F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition Membrane_Bone_North = Membrans.addOrReplaceChild("Membrane_Bone_North", CubeListBuilder.create(), PartPose.offsetAndRotation(0.1227F, -0.2765F, 14.6451F, 0.0F, -1.5708F, 0.0F));

		PartDefinition LowerRidgeRib5_r1 = Membrane_Bone_North.addOrReplaceChild("LowerRidgeRib5_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9331F, -20.542F, 1.6227F, 0.0F, -0.3491F, 0.4363F));

		PartDefinition LowerRidgeRib2Tip_r3 = Membrane_Bone_North.addOrReplaceChild("LowerRidgeRib2Tip_r3", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5833F, -16.2456F, -14.9519F, 0.0F, 0.6981F, 0.4363F));

		PartDefinition LowerRidgeRib4_r2 = Membrane_Bone_North.addOrReplaceChild("LowerRidgeRib4_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9748F, -14.1979F, -1.3773F, 0.0F, 0.3491F, 0.4363F));

		PartDefinition LowerRidgeRib1Tip_r2 = Membrane_Bone_North.addOrReplaceChild("LowerRidgeRib1Tip_r2", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3155F, -13.5267F, 15.1973F, 0.0F, -0.6981F, 0.4363F));

		PartDefinition LowerRidgeRib3_r3 = Membrane_Bone_North.addOrReplaceChild("LowerRidgeRib3_r3", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 94).addBox(0.0F, -11.0F, 0.0F, 0.0F, 22.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7069F, -11.4789F, 1.6227F, 0.0F, -0.3491F, 0.4363F));

		PartDefinition LowerRidgeFrontMembrane_r3 = Membrane_Bone_North.addOrReplaceChild("LowerRidgeFrontMembrane_r3", CubeListBuilder.create().texOffs(28, 94).addBox(0.0F, -11.0F, -12.0F, 0.0F, 22.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7069F, -11.4789F, -1.3773F, 0.0F, 0.3491F, 0.4363F));

		PartDefinition LowerRidgeFrontMembraneTip_r5 = Membrane_Bone_North.addOrReplaceChild("LowerRidgeFrontMembraneTip_r5", CubeListBuilder.create().texOffs(170, 215).addBox(-1.0F, -11.01F, -5.25F, 0.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0754F, -13.6276F, -15.4027F, 0.0F, 0.6981F, 0.4363F));

		PartDefinition LowerRidgeFrontMembraneTip_r6 = Membrane_Bone_North.addOrReplaceChild("LowerRidgeFrontMembraneTip_r6", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -11.01F, -2.75F, 0.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0754F, -13.6276F, 15.6481F, 0.0F, -0.6981F, 0.4363F));

		PartDefinition LowerRidge_r2 = Membrane_Bone_North.addOrReplaceChild("LowerRidge_r2", CubeListBuilder.create().texOffs(44, 235).addBox(-1.0F, -24.0F, -2.0F, 3.0F, 24.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8177F, -0.8146F, 0.1227F, 0.0F, 0.0F, 0.4363F));

		PartDefinition LowerRidgeRib3Tip_r3 = Membrane_Bone_North.addOrReplaceChild("LowerRidgeRib3Tip_r3", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5417F, -22.5898F, 15.1973F, 0.0F, -0.6981F, 0.4363F));

		PartDefinition Membrane_Bone_1_2 = Membrane_Bone_North.addOrReplaceChild("Membrane_Bone_1_2", CubeListBuilder.create(), PartPose.offset(10.0639F, -22.0287F, 0.1227F));

		PartDefinition MiddleRidgeRib2Tip_r3 = Membrane_Bone_1_2.addOrReplaceChild("MiddleRidgeRib2Tip_r3", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9795F, -13.6988F, -15.0746F, 0.0F, 0.6981F, 0.0873F));

		PartDefinition MiddleRidgeRib4_r1 = Membrane_Bone_1_2.addOrReplaceChild("MiddleRidgeRib4_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8474F, -13.2765F, -1.5F, 0.0F, 0.3491F, 0.0873F));

		PartDefinition MiddleRidgeRib1Tip_r3 = Membrane_Bone_1_2.addOrReplaceChild("MiddleRidgeRib1Tip_r3", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6768F, -5.7292F, -15.0746F, 0.0F, 0.6981F, 0.0873F));

		PartDefinition MiddleRidgeRib3_r2 = Membrane_Bone_1_2.addOrReplaceChild("MiddleRidgeRib3_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1502F, -5.3069F, -1.5F, 0.0F, 0.3491F, 0.0873F));

		PartDefinition MiddleRidgeFrontMembraneTip_r3 = Membrane_Bone_1_2.addOrReplaceChild("MiddleRidgeFrontMembraneTip_r3", CubeListBuilder.create().texOffs(110, 20).addBox(-1.5F, -8.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8529F, -7.6547F, -15.6758F, 0.0F, 0.6981F, 0.0873F));

		PartDefinition MiddleRidgeFrontMembrane_r3 = Membrane_Bone_1_2.addOrReplaceChild("MiddleRidgeFrontMembrane_r3", CubeListBuilder.create().texOffs(144, 171).addBox(-0.5F, -8.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3207F, -7.2122F, -1.5F, 0.0F, 0.3491F, 0.0873F));

		PartDefinition MiddleRidgeBackMembrane_r3 = Membrane_Bone_1_2.addOrReplaceChild("MiddleRidgeBackMembrane_r3", CubeListBuilder.create().texOffs(0, 176).addBox(-0.5F, -8.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3207F, -7.2122F, 1.5F, 0.0F, -0.3491F, 0.0873F));

		PartDefinition MiddleRidgeBackMembraneTip_r3 = Membrane_Bone_1_2.addOrReplaceChild("MiddleRidgeBackMembraneTip_r3", CubeListBuilder.create().texOffs(0, 130).addBox(-1.5F, -8.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8529F, -7.6547F, 15.6758F, 0.0F, -0.6981F, 0.0873F));

		PartDefinition MiddleRidge_r3 = Membrane_Bone_1_2.addOrReplaceChild("MiddleRidge_r3", CubeListBuilder.create().texOffs(268, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9185F, 0.1337F, 0.0F, 0.0F, 0.0F, 0.0873F));

		PartDefinition Membrane_Bone_2_2 = Membrane_Bone_1_2.addOrReplaceChild("Membrane_Bone_2_2", CubeListBuilder.create(), PartPose.offset(1.1883F, -15.6667F, 0.0F));

		PartDefinition TopRidgeRib3Tip_r5 = Membrane_Bone_2_2.addOrReplaceChild("TopRidgeRib3Tip_r5", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8604F, 17.9518F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib5_r1 = Membrane_Bone_2_2.addOrReplaceChild("TopRidgeRib5_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9114F, 17.1104F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidgeRib2Tip_r4 = Membrane_Bone_2_2.addOrReplaceChild("TopRidgeRib2Tip_r4", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6448F, 24.8454F, -15.0746F, 0.0F, 0.6981F, -0.1745F));

		PartDefinition TopRidgeRib4_r4 = Membrane_Bone_2_2.addOrReplaceChild("TopRidgeRib4_r4", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.127F, 24.004F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition TopRidgeRib1Tip_r5 = Membrane_Bone_2_2.addOrReplaceChild("TopRidgeRib1Tip_r5", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1239F, 27.7999F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib3_r6 = Membrane_Bone_2_2.addOrReplaceChild("TopRidgeRib3_r6", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6479F, 26.9585F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r8 = Membrane_Bone_2_2.addOrReplaceChild("LeftRidgeTopMemebraneTip_r8", CubeListBuilder.create().texOffs(67, 43).addBox(-1.5F, -43.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5208F, 27.6963F, -15.6758F, 0.0F, 0.6981F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r12 = Membrane_Bone_2_2.addOrReplaceChild("LeftRidgeTopMemebrane_r12", CubeListBuilder.create().texOffs(120, 170).addBox(-0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6327F, 26.7848F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r9 = Membrane_Bone_2_2.addOrReplaceChild("LeftRidgeTopMemebraneTip_r9", CubeListBuilder.create().texOffs(0, 70).addBox(-1.5F, -43.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5208F, 27.6963F, 15.6758F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidge_r7 = Membrane_Bone_2_2.addOrReplaceChild("TopRidge_r7", CubeListBuilder.create().texOffs(254, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7066F, -0.0084F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r13 = Membrane_Bone_2_2.addOrReplaceChild("LeftRidgeTopMemebrane_r13", CubeListBuilder.create().texOffs(96, 177).addBox(-0.5F, -43.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6327F, 26.7848F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition Membrane_Bone_3_2 = Membrane_Bone_2_2.addOrReplaceChild("Membrane_Bone_3_2", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.5F, -15.75F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition TopRidgeRib5_r2 = Membrane_Bone_3_2.addOrReplaceChild("TopRidgeRib5_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0141F, 17.0823F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidgeRib2Tip_r5 = Membrane_Bone_3_2.addOrReplaceChild("TopRidgeRib2Tip_r5", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5421F, 24.8173F, -15.0746F, 0.0F, 0.6981F, -0.1745F));

		PartDefinition TopRidgeRib4_r5 = Membrane_Bone_3_2.addOrReplaceChild("TopRidgeRib4_r5", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2297F, 23.9759F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition TopRidgeRib1Tip_r6 = Membrane_Bone_3_2.addOrReplaceChild("TopRidgeRib1Tip_r6", CubeListBuilder.create().texOffs(316, 313).addBox(-2.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0212F, 27.7717F, 15.0746F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidgeRib3_r7 = Membrane_Bone_3_2.addOrReplaceChild("TopRidgeRib3_r7", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7506F, 26.9303F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r10 = Membrane_Bone_3_2.addOrReplaceChild("LeftRidgeTopMemebraneTip_r10", CubeListBuilder.create().texOffs(67, 43).addBox(-1.5F, -43.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6235F, 27.6682F, -15.6758F, 0.0F, 0.6981F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r14 = Membrane_Bone_3_2.addOrReplaceChild("LeftRidgeTopMemebrane_r14", CubeListBuilder.create().texOffs(120, 170).addBox(-0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7354F, 26.7567F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebraneTip_r11 = Membrane_Bone_3_2.addOrReplaceChild("LeftRidgeTopMemebraneTip_r11", CubeListBuilder.create().texOffs(0, 70).addBox(-1.5F, -43.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6235F, 27.6682F, 15.6758F, 0.0F, -0.6981F, -0.1745F));

		PartDefinition TopRidge_r8 = Membrane_Bone_3_2.addOrReplaceChild("TopRidge_r8", CubeListBuilder.create().texOffs(254, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.604F, -0.0365F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r15 = Membrane_Bone_3_2.addOrReplaceChild("LeftRidgeTopMemebrane_r15", CubeListBuilder.create().texOffs(96, 175).addBox(-0.5F, -43.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7354F, 26.7567F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition Membrane_Bone_4_2 = Membrane_Bone_3_2.addOrReplaceChild("Membrane_Bone_4_2", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, -15.25F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition TopRidgeRib4_r6 = Membrane_Bone_4_2.addOrReplaceChild("TopRidgeRib4_r6", CubeListBuilder.create().texOffs(20, 225).addBox(-1.5F, -33.0F, -10.0F, 3.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5643F, 23.8894F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition TopRidgeRib3_r8 = Membrane_Bone_4_2.addOrReplaceChild("TopRidgeRib3_r8", CubeListBuilder.create().texOffs(20, 225).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0852F, 26.8438F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r16 = Membrane_Bone_4_2.addOrReplaceChild("LeftRidgeTopMemebrane_r16", CubeListBuilder.create().texOffs(158, 211).addBox(-0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0701F, 26.6702F, 1.5F, 0.0F, -0.3491F, -0.1745F));

		PartDefinition TopRidge_r9 = Membrane_Bone_4_2.addOrReplaceChild("TopRidge_r9", CubeListBuilder.create().texOffs(254, 309).addBox(-1.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2693F, -0.123F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition LeftRidgeTopMemebrane_r17 = Membrane_Bone_4_2.addOrReplaceChild("LeftRidgeTopMemebrane_r17", CubeListBuilder.create().texOffs(169, 186).addBox(-0.5F, -35.0F, -11.0F, 0.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0701F, 26.6702F, -1.5F, 0.0F, 0.3491F, -0.1745F));

		PartDefinition Membrane_Bone_East = Membrans.addOrReplaceChild("Membrane_Bone_East", CubeListBuilder.create(), PartPose.offset(-16.0F, 0.0F, 0.0F));

		PartDefinition LowerRidgeRib3Tip_r4 = Membrane_Bone_East.addOrReplaceChild("LowerRidgeRib3Tip_r4", CubeListBuilder.create().texOffs(316, 313).addBox(-0.5F, -2.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.4044F, -21.9883F, 15.0746F, 0.0F, 0.6981F, -0.3927F));

		PartDefinition LowerRidgeRib3_r4 = Membrane_Bone_East.addOrReplaceChild("LowerRidgeRib3_r4", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.881F, -20.134F, 1.5F, 0.0F, 0.3491F, -0.3927F));

		PartDefinition LowerRidgeRib2Tip_r4 = Membrane_Bone_East.addOrReplaceChild("LowerRidgeRib2Tip_r4", CubeListBuilder.create().texOffs(316, 313).addBox(-0.5F, -2.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7256F, -15.5211F, 15.0746F, 0.0F, 0.6981F, -0.3927F));

		PartDefinition LowerRidgeRib2_r2 = Membrane_Bone_East.addOrReplaceChild("LowerRidgeRib2_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.2022F, -13.6669F, 1.5F, 0.0F, 0.3491F, -0.3927F));

		PartDefinition LowerRidgeRib1Tip_r3 = Membrane_Bone_East.addOrReplaceChild("LowerRidgeRib1Tip_r3", CubeListBuilder.create().texOffs(316, 313).addBox(-0.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4224F, -12.7495F, -15.0746F, 0.0F, -0.6981F, -0.3927F));

		PartDefinition LowerRidgeRib1_r1 = Membrane_Bone_East.addOrReplaceChild("LowerRidgeRib1_r1", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 154).addBox(0.0F, -11.0F, -12.0F, 0.0F, 22.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0541F, -10.8952F, -1.5F, 0.0F, -0.3491F, -0.3927F));

		PartDefinition LowerRidgeFrontMembraneTip_r7 = Membrane_Bone_East.addOrReplaceChild("LowerRidgeFrontMembraneTip_r7", CubeListBuilder.create().texOffs(186, 215).addBox(1.0F, -11.01F, -2.75F, 0.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6667F, -12.8398F, 15.5254F, 0.0F, 0.6981F, -0.3927F));

		PartDefinition LowerRidgeFrontMembrane_r4 = Membrane_Bone_East.addOrReplaceChild("LowerRidgeFrontMembrane_r4", CubeListBuilder.create().texOffs(128, 148).addBox(0.0F, -11.0F, 0.0F, 0.0F, 22.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0541F, -10.8952F, 1.5F, 0.0F, 0.3491F, -0.3927F));

		PartDefinition LowerRidgeFrontMembraneTip_r8 = Membrane_Bone_East.addOrReplaceChild("LowerRidgeFrontMembraneTip_r8", CubeListBuilder.create().texOffs(202, 216).addBox(1.0F, -11.01F, -5.25F, 0.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6667F, -12.8398F, -15.5254F, 0.0F, -0.6981F, -0.3927F));

		PartDefinition LowerRidge_r3 = Membrane_Bone_East.addOrReplaceChild("LowerRidge_r3", CubeListBuilder.create().texOffs(44, 235).addBox(-2.0F, -24.0F, -2.0F, 3.0F, 24.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

		PartDefinition Membrane_Bone_1_3 = Membrane_Bone_East.addOrReplaceChild("Membrane_Bone_1_3", CubeListBuilder.create(), PartPose.offset(-9.0F, -22.0F, 0.0F));

		PartDefinition MiddleRidgeRib2Tip_r4 = Membrane_Bone_1_3.addOrReplaceChild("MiddleRidgeRib2Tip_r4", CubeListBuilder.create().texOffs(316, 313).addBox(-0.5F, -2.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7582F, -10.1836F, 15.0746F, 0.0F, 0.6981F, -0.0436F));

		PartDefinition MiddleRidgeRib2_r3 = Membrane_Bone_1_3.addOrReplaceChild("MiddleRidgeRib2_r3", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0825F, -9.9723F, 1.5F, 0.0F, 0.3491F, -0.0436F));

		PartDefinition MiddleRidgeRib1Tip_r4 = Membrane_Bone_1_3.addOrReplaceChild("MiddleRidgeRib1Tip_r4", CubeListBuilder.create().texOffs(316, 313).addBox(-0.5F, -2.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.8891F, -7.1865F, -15.0746F, 0.0F, -0.6981F, -0.0436F));

		PartDefinition MiddleRidgeRib1_r2 = Membrane_Bone_1_3.addOrReplaceChild("MiddleRidgeRib1_r2", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -2.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(96, 184).addBox(0.5F, -8.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0483F, -6.9751F, -1.5F, 0.0F, -0.3491F, -0.0436F));

		PartDefinition MiddleRidgeFrontMembraneTip_r4 = Membrane_Bone_1_3.addOrReplaceChild("MiddleRidgeFrontMembraneTip_r4", CubeListBuilder.create().texOffs(184, 270).addBox(1.5F, -8.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.2363F, -7.1916F, -15.6758F, 0.0F, -0.6981F, -0.0436F));

		PartDefinition MiddleRidgeBackMembrane_r4 = Membrane_Bone_1_3.addOrReplaceChild("MiddleRidgeBackMembrane_r4", CubeListBuilder.create().texOffs(182, 75).addBox(0.5F, -8.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0483F, -6.9751F, 1.5F, 0.0F, 0.3491F, -0.0436F));

		PartDefinition MiddleRidgeBackMembraneTip_r4 = Membrane_Bone_1_3.addOrReplaceChild("MiddleRidgeBackMembraneTip_r4", CubeListBuilder.create().texOffs(110, 268).addBox(1.5F, -8.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.2363F, -7.1916F, 15.6758F, 0.0F, 0.6981F, -0.0436F));

		PartDefinition MiddleRidge_r4 = Membrane_Bone_1_3.addOrReplaceChild("MiddleRidge_r4", CubeListBuilder.create().texOffs(268, 309).addBox(-2.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9659F, 0.505F, 0.0F, 0.0F, 0.0F, -0.0436F));

		PartDefinition Membrane_Bone_2_3 = Membrane_Bone_1_3.addOrReplaceChild("Membrane_Bone_2_3", CubeListBuilder.create(), PartPose.offset(-0.25F, -15.5F, 0.0F));

		PartDefinition TopRidgeRib4Tip_r1 = Membrane_Bone_2_3.addOrReplaceChild("TopRidgeRib4Tip_r1", CubeListBuilder.create().texOffs(316, 313).addBox(-0.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1734F, 19.1298F, 15.0746F, 0.0F, 0.6981F, 0.2618F));

		PartDefinition TopRidgeRib4_r7 = Membrane_Bone_2_3.addOrReplaceChild("TopRidgeRib4_r7", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.8536F, 17.8757F, 1.5F, 0.0F, 0.3491F, 0.2618F));

		PartDefinition TopRidgeRib3Tip_r6 = Membrane_Bone_2_3.addOrReplaceChild("TopRidgeRib3Tip_r6", CubeListBuilder.create().texOffs(316, 313).addBox(-0.5F, -33.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9498F, 22.0275F, -15.0746F, 0.0F, -0.6981F, 0.2618F));

		PartDefinition TopRidgeRib3_r9 = Membrane_Bone_2_3.addOrReplaceChild("TopRidgeRib3_r9", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6301F, 20.7735F, -1.5F, 0.0F, -0.3491F, 0.2618F));

		PartDefinition TopRidgeRib2Tip_r6 = Membrane_Bone_2_3.addOrReplaceChild("TopRidgeRib2Tip_r6", CubeListBuilder.create().texOffs(316, 313).addBox(-0.5F, -33.01F, -2.75F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7616F, 28.789F, 15.0746F, 0.0F, 0.6981F, 0.2618F));

		PartDefinition TopRidgeRib2_r7 = Membrane_Bone_2_3.addOrReplaceChild("TopRidgeRib2_r7", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.4418F, 27.5349F, 1.5F, 0.0F, 0.3491F, 0.2618F));

		PartDefinition TopRidgeRib1Tip_r7 = Membrane_Bone_2_3.addOrReplaceChild("TopRidgeRib1Tip_r7", CubeListBuilder.create().texOffs(316, 313).addBox(-0.5F, -33.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5027F, 27.8231F, -15.0746F, 0.0F, -0.6981F, 0.2618F));

		PartDefinition TopRidgeRib1_r4 = Membrane_Bone_2_3.addOrReplaceChild("TopRidgeRib1_r4", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(144, 171).addBox(0.5F, -43.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.183F, 26.569F, -1.5F, 0.0F, -0.3491F, 0.2618F));

		PartDefinition RightRidgeTopMemebraneTip_r1 = Membrane_Bone_2_3.addOrReplaceChild("RightRidgeTopMemebraneTip_r1", CubeListBuilder.create().texOffs(42, 264).addBox(1.5F, -43.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.17F, 27.9226F, -15.6758F, 0.0F, -0.6981F, 0.2618F));

		PartDefinition RightRidgeTopMemebraneTip_r2 = Membrane_Bone_2_3.addOrReplaceChild("RightRidgeTopMemebraneTip_r2", CubeListBuilder.create().texOffs(26, 264).addBox(1.5F, -43.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.17F, 27.9226F, 15.6758F, 0.0F, 0.6981F, 0.2618F));

		PartDefinition RightRidgeTopMemebrane_r2 = Membrane_Bone_2_3.addOrReplaceChild("RightRidgeTopMemebrane_r2", CubeListBuilder.create().texOffs(24, 178).addBox(0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.183F, 26.569F, 1.5F, 0.0F, 0.3491F, 0.2618F));

		PartDefinition TopRidge_r10 = Membrane_Bone_2_3.addOrReplaceChild("TopRidge_r10", CubeListBuilder.create().texOffs(254, 309).addBox(-2.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5015F, 0.1715F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition Membrane_Bone_3_3 = Membrane_Bone_2_3.addOrReplaceChild("Membrane_Bone_3_3", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, -14.5F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition TopRidgeRib4_r8 = Membrane_Bone_3_3.addOrReplaceChild("TopRidgeRib4_r8", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.1036F, 17.8757F, 1.5F, 0.0F, 0.3491F, 0.2618F));

		PartDefinition TopRidgeRib3Tip_r7 = Membrane_Bone_3_3.addOrReplaceChild("TopRidgeRib3Tip_r7", CubeListBuilder.create().texOffs(316, 313).addBox(-0.5F, -33.01F, -5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1998F, 22.0275F, -15.0746F, 0.0F, -0.6981F, 0.2618F));

		PartDefinition TopRidgeRib3_r10 = Membrane_Bone_3_3.addOrReplaceChild("TopRidgeRib3_r10", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.8801F, 20.7735F, -1.5F, 0.0F, -0.3491F, 0.2618F));

		PartDefinition TopRidgeRib2_r8 = Membrane_Bone_3_3.addOrReplaceChild("TopRidgeRib2_r8", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.6918F, 27.5349F, 1.5F, 0.0F, 0.3491F, 0.2618F));

		PartDefinition RightRidgeTopMemebraneTip_r3 = Membrane_Bone_3_3.addOrReplaceChild("RightRidgeTopMemebraneTip_r3", CubeListBuilder.create().texOffs(42, 264).addBox(1.5F, -43.01F, -5.25F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.42F, 27.9226F, -15.6758F, 0.0F, -0.6981F, 0.2618F));

		PartDefinition RightRidgeTopMemebrane_r3 = Membrane_Bone_3_3.addOrReplaceChild("RightRidgeTopMemebrane_r3", CubeListBuilder.create().texOffs(168, 180).addBox(0.5F, -43.0F, -12.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.433F, 26.569F, -1.5F, 0.0F, -0.3491F, 0.2618F));

		PartDefinition RightRidgeTopMemebraneTip_r4 = Membrane_Bone_3_3.addOrReplaceChild("RightRidgeTopMemebraneTip_r4", CubeListBuilder.create().texOffs(26, 264).addBox(1.5F, -43.01F, -2.75F, 0.0F, 15.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.42F, 27.9226F, 15.6758F, 0.0F, 0.6981F, 0.2618F));

		PartDefinition RightRidgeTopMemebrane_r4 = Membrane_Bone_3_3.addOrReplaceChild("RightRidgeTopMemebrane_r4", CubeListBuilder.create().texOffs(24, 178).addBox(0.5F, -43.0F, 0.0F, 0.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.433F, 26.569F, 1.5F, 0.0F, 0.3491F, 0.2618F));

		PartDefinition TopRidge_r11 = Membrane_Bone_3_3.addOrReplaceChild("TopRidge_r11", CubeListBuilder.create().texOffs(254, 309).addBox(-2.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7485F, 0.1715F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition Membrane_Bone_4_3 = Membrane_Bone_3_3.addOrReplaceChild("Membrane_Bone_4_3", CubeListBuilder.create(), PartPose.offsetAndRotation(3.25F, -15.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition LeftRidgeTopMemebrane_r18 = Membrane_Bone_4_3.addOrReplaceChild("LeftRidgeTopMemebrane_r18", CubeListBuilder.create().texOffs(161, 215).addBox(-5.8848F, -7.5014F, 1.9599F, 0.0F, 13.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0701F, -4.0798F, -1.5F, 0.0622F, 0.2976F, 0.2907F));

		PartDefinition TopRidgeRib4_r9 = Membrane_Bone_4_3.addOrReplaceChild("TopRidgeRib4_r9", CubeListBuilder.create().texOffs(23, 228).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.2324F, 18.3128F, 1.5F, 0.0F, 0.3491F, 0.2618F));

		PartDefinition TopRidgeRib3_r11 = Membrane_Bone_4_3.addOrReplaceChild("TopRidgeRib3_r11", CubeListBuilder.create().texOffs(22, 227).addBox(-1.5F, -33.0F, -8.0F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0088F, 21.2106F, -1.5F, 0.0F, -0.3491F, 0.2618F));

		PartDefinition TopRidgeRib2_r9 = Membrane_Bone_4_3.addOrReplaceChild("TopRidgeRib2_r9", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, 0.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.8206F, 27.9721F, 1.5F, 0.0F, 0.3491F, 0.2618F));

		PartDefinition TopRidgeRib1_r5 = Membrane_Bone_4_3.addOrReplaceChild("TopRidgeRib1_r5", CubeListBuilder.create().texOffs(18, 223).addBox(-1.5F, -33.0F, -12.0F, 3.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5617F, 27.0061F, -1.5F, 0.0F, -0.3491F, 0.2618F));

		PartDefinition RightRidgeTopMemebrane_r5 = Membrane_Bone_4_3.addOrReplaceChild("RightRidgeTopMemebrane_r5", CubeListBuilder.create().texOffs(169, 182).addBox(0.5F, -43.0F, -10.0F, 0.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5617F, 30.0061F, -2.5F, 0.0F, -0.3491F, 0.2618F));

		PartDefinition TopRidge_r12 = Membrane_Bone_4_3.addOrReplaceChild("TopRidge_r12", CubeListBuilder.create().texOffs(254, 309).addBox(-2.0F, -16.0F, -2.0F, 3.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1227F, 0.6086F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition Tentacle = Animation_Elements.addOrReplaceChild("Tentacle", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.6981F, 0.0F));

		PartDefinition Blossom_Head_Tentacle = Tentacle.addOrReplaceChild("Blossom_Head_Tentacle", CubeListBuilder.create(), PartPose.offsetAndRotation(20.0F, -6.0F, 3.0F, 0.0F, 1.7017F, 0.0F));

		PartDefinition BodyBase_r1 = Blossom_Head_Tentacle.addOrReplaceChild("BodyBase_r1", CubeListBuilder.create().texOffs(109, 304).mirror().addBox(-2.0F, -6.0F, -4.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.7704F, -9.7651F, 2.1503F, -1.3245F, 1.3909F, -1.3283F));

		PartDefinition Arm_r1 = Blossom_Head_Tentacle.addOrReplaceChild("Arm_r1", CubeListBuilder.create().texOffs(318, 245).addBox(0.0953F, -1.6721F, -5.8679F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(132, 267).addBox(-0.4047F, -2.4221F, -1.8679F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9107F, -7.0779F, 3.2151F, -1.4266F, 1.2624F, -1.4333F));

		PartDefinition Jaw_r1 = Blossom_Head_Tentacle.addOrReplaceChild("Jaw_r1", CubeListBuilder.create().texOffs(260, 152).addBox(-4.8246F, 0.7803F, -1.5825F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9107F, -7.0779F, 3.2151F, -1.4167F, 0.2686F, -1.6718F));

		PartDefinition Head_r1 = Blossom_Head_Tentacle.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(206, 73).addBox(-4.2844F, -7.0707F, -1.7586F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9107F, -7.0779F, 3.2151F, -1.4742F, 0.4773F, -1.515F));

		PartDefinition BodyBase_r2 = Blossom_Head_Tentacle.addOrReplaceChild("BodyBase_r2", CubeListBuilder.create().texOffs(109, 304).addBox(-2.0F, -6.0F, -4.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8539F, 1.0F, 0.2376F, 0.0F, 1.5272F, 0.0F));

		PartDefinition Body_Tentackle3 = Blossom_Head_Tentacle.addOrReplaceChild("Body_Tentackle3", CubeListBuilder.create(), PartPose.offset(0.0F, -13.0F, 6.0F));

		PartDefinition BodyTop_r1 = Body_Tentackle3.addOrReplaceChild("BodyTop_r1", CubeListBuilder.create().texOffs(132, 267).addBox(-2.4641F, -5.7768F, -7.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9882F, -9.2026F, 2.0181F, 1.7698F, -1.3484F, -1.7651F));

		PartDefinition Fungus_r1 = Body_Tentackle3.addOrReplaceChild("Fungus_r1", CubeListBuilder.create().texOffs(153, 0).addBox(-20.0017F, 1.2949F, -12.8282F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4086F, -14.3422F, -19.437F, 3.0037F, 1.0522F, 2.7358F));

		PartDefinition Fungus_r2 = Body_Tentackle3.addOrReplaceChild("Fungus_r2", CubeListBuilder.create().texOffs(166, 0).addBox(-18.736F, 5.1275F, 6.6278F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.214F, -12.0824F, -14.981F, 0.4687F, 0.9305F, 0.5628F));

		PartDefinition Fungus_r3 = Body_Tentackle3.addOrReplaceChild("Fungus_r3", CubeListBuilder.create().texOffs(139, 0).addBox(-18.0139F, 8.3595F, -12.6627F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3612F, -8.4334F, -18.3088F, 2.3456F, 0.8102F, 2.4981F));

		PartDefinition Fungus_r4 = Body_Tentackle3.addOrReplaceChild("Fungus_r4", CubeListBuilder.create().texOffs(139, 0).addBox(-21.3407F, 2.684F, 2.4586F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6779F, -8.607F, -17.4994F, 0.4232F, 1.2197F, 0.2342F));

		PartDefinition BodyBase_r3 = Body_Tentackle3.addOrReplaceChild("BodyBase_r3", CubeListBuilder.create().texOffs(109, 304).addBox(-20.4367F, -1.8137F, -0.5391F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0331F, -5.5766F, -16.9589F, 1.3717F, 1.3484F, 1.3765F));

		PartDefinition Jaw_r2 = Body_Tentackle3.addOrReplaceChild("Jaw_r2", CubeListBuilder.create().texOffs(260, 152).addBox(-0.2701F, -19.3701F, -4.2097F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5139F, -9.1001F, -18.9276F, -1.5654F, -0.3515F, 1.5693F));

		PartDefinition Arm_r2 = Body_Tentackle3.addOrReplaceChild("Arm_r2", CubeListBuilder.create().texOffs(318, 245).addBox(-20.6824F, 2.3028F, -3.4591F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8926F, -8.0524F, -17.2155F, 2.087F, 1.3055F, -1.4094F));

		PartDefinition BodyBase_r4 = Body_Tentackle3.addOrReplaceChild("BodyBase_r4", CubeListBuilder.create().texOffs(109, 304).addBox(15.2035F, -14.0221F, -4.8288F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6119F, -5.0593F, -17.2192F, -1.6737F, -1.1324F, 1.6641F));

		PartDefinition Fungus_r5 = Body_Tentackle3.addOrReplaceChild("Fungus_r5", CubeListBuilder.create().texOffs(153, 0).addBox(-20.3016F, -4.9833F, -10.8399F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6024F, -4.8977F, -19.2672F, -2.5451F, 1.085F, -2.9124F));

		PartDefinition Fungus_r6 = Body_Tentackle3.addOrReplaceChild("Fungus_r6", CubeListBuilder.create().texOffs(153, 0).addBox(-19.8634F, -1.343F, 6.0622F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4371F, -3.2184F, -15.4616F, -0.1395F, 1.0377F, 0.0355F));

		PartDefinition Fungus_r7 = Body_Tentackle3.addOrReplaceChild("Fungus_r7", CubeListBuilder.create().texOffs(139, 0).addBox(-19.8982F, 2.1466F, -13.807F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7157F, -1.3058F, -21.7981F, 2.8453F, 0.9677F, 2.8751F));

		PartDefinition Fungus_r8 = Body_Tentackle3.addOrReplaceChild("Fungus_r8", CubeListBuilder.create().texOffs(139, 0).addBox(-20.7683F, -3.8082F, 3.4499F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.271F, -0.321F, -22.2332F, -0.5013F, 1.1405F, -0.6452F));

		PartDefinition Arm_r3 = Body_Tentackle3.addOrReplaceChild("Arm_r3", CubeListBuilder.create().texOffs(318, 245).addBox(-20.1845F, -8.5226F, 0.8288F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1614F, -4.2557F, -18.2783F, -1.4438F, 1.2191F, -1.4515F));

		PartDefinition BodyTop_r2 = Body_Tentackle3.addOrReplaceChild("BodyTop_r2", CubeListBuilder.create().texOffs(132, 267).addBox(-15.2881F, -19.7717F, -6.1712F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2483F, -1.5062F, -20.2885F, -1.5116F, 0.7409F, -1.5308F));

		PartDefinition Cheast_Bones2 = Body_Tentackle3.addOrReplaceChild("Cheast_Bones2", CubeListBuilder.create(), PartPose.offset(1.0F, -13.0F, 2.0F));

		PartDefinition BodyTop_r3 = Cheast_Bones2.addOrReplaceChild("BodyTop_r3", CubeListBuilder.create().texOffs(132, 267).addBox(-0.8602F, -10.954F, -1.8679F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0331F, 7.4234F, -1.9589F, -1.1064F, 1.4733F, -1.1083F));

		PartDefinition H5TorsoRight_r1 = Cheast_Bones2.addOrReplaceChild("H5TorsoRight_r1", CubeListBuilder.create().texOffs(12, 16).mirror().addBox(-1.0F, -5.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(4.1932F, -4.6632F, -0.2515F, -0.737F, 1.0213F, -0.5311F));

		PartDefinition H5TorsoLeft_r1 = Cheast_Bones2.addOrReplaceChild("H5TorsoLeft_r1", CubeListBuilder.create().texOffs(12, 16).addBox(-2.0F, -4.9111F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.4442F, -4.4133F, 0.3666F, -0.6239F, -1.1192F, 0.4455F));

		PartDefinition Torso_r1 = Cheast_Bones2.addOrReplaceChild("Torso_r1", CubeListBuilder.create().texOffs(0, 14).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.641F, -6.3322F, -1.5201F, -0.3927F, -0.0436F, 0.0F));

		PartDefinition Blossom_Head1 = Cheast_Bones2.addOrReplaceChild("Blossom_Head1", CubeListBuilder.create(), PartPose.offset(1.0F, -9.0F, 0.0F));

		PartDefinition Tumor_r1 = Blossom_Head1.addOrReplaceChild("Tumor_r1", CubeListBuilder.create().texOffs(92, 109).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.3062F, -3.7266F, 4.6073F, 1.6966F, -0.011F, -1.0924F));

		PartDefinition Tumor_r2 = Blossom_Head1.addOrReplaceChild("Tumor_r2", CubeListBuilder.create().texOffs(97, 97).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.351F, -6.5457F, 3.5822F, 1.394F, -0.0342F, -1.2213F));

		PartDefinition Tumor_r3 = Blossom_Head1.addOrReplaceChild("Tumor_r3", CubeListBuilder.create().texOffs(95, 99).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3335F, -5.417F, 3.4109F, 0.0235F, -0.0368F, -0.5677F));

		PartDefinition Petal5_r1 = Blossom_Head1.addOrReplaceChild("Petal5_r1", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1797F, -2.54F, -3.2368F, 1.3616F, 0.529F, 0.2922F));

		PartDefinition Petal4_r1 = Blossom_Head1.addOrReplaceChild("Petal4_r1", CubeListBuilder.create().texOffs(154, 13).addBox(-2.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5062F, -5.575F, -0.4089F, 1.5021F, 0.0113F, 0.6588F));

		PartDefinition Petal3_r1 = Blossom_Head1.addOrReplaceChild("Petal3_r1", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1849F, -0.9415F, -4.0534F, 0.8029F, 0.1194F, 0.4523F));

		PartDefinition Petal2_r1 = Blossom_Head1.addOrReplaceChild("Petal2_r1", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3998F, -7.6096F, -1.4676F, 1.6838F, -0.4013F, 0.3955F));

		PartDefinition Petal4_r2 = Blossom_Head1.addOrReplaceChild("Petal4_r2", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.179F, -2.8739F, 5.3029F, -1.0564F, -0.712F, 0.5628F));

		PartDefinition Petal3_r2 = Blossom_Head1.addOrReplaceChild("Petal3_r2", CubeListBuilder.create().texOffs(154, 13).addBox(-2.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.921F, -7.451F, 1.0459F, -1.0333F, -0.089F, 0.5999F));

		PartDefinition Petal2_r2 = Blossom_Head1.addOrReplaceChild("Petal2_r2", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0268F, -8.0952F, 3.1315F, -1.7402F, -0.2947F, 0.7106F));

		PartDefinition Petal1_r1 = Blossom_Head1.addOrReplaceChild("Petal1_r1", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.4713F, -2.6459F, 5.1507F, -0.8984F, 0.1136F, 1.0421F));

		PartDefinition Petal5_r2 = Blossom_Head1.addOrReplaceChild("Petal5_r2", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3772F, -5.4811F, 4.9006F, -0.9369F, -0.4768F, -0.4842F));

		PartDefinition Petal4_r3 = Blossom_Head1.addOrReplaceChild("Petal4_r3", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.345F, -4.1921F, 5.5121F, -1.1511F, 0.4056F, 0.5F));

		PartDefinition Petal3_r3 = Blossom_Head1.addOrReplaceChild("Petal3_r3", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1249F, -7.3049F, 3.0762F, -1.7453F, -0.0436F, 0.0F));

		PartDefinition Petal2_r3 = Blossom_Head1.addOrReplaceChild("Petal2_r3", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0538F, -1.5576F, 7.1479F, -0.4251F, -0.2752F, 0.2022F));

		PartDefinition Head_r2 = Blossom_Head1.addOrReplaceChild("Head_r2", CubeListBuilder.create().texOffs(218, 224).addBox(-4.0F, -5.5F, -3.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1951F, -0.7011F, 0.2631F, 0.3491F, -0.0436F, 0.0F));

		PartDefinition RibSeg = Cheast_Bones2.addOrReplaceChild("RibSeg", CubeListBuilder.create(), PartPose.offset(10.551F, -5.7366F, 4.1506F));

		PartDefinition Rib1Seg1 = RibSeg.addOrReplaceChild("Rib1Seg1", CubeListBuilder.create(), PartPose.offset(-8.0F, -1.0F, -4.0F));

		PartDefinition Rib1Seg3_r1 = Rib1Seg1.addOrReplaceChild("Rib1Seg3_r1", CubeListBuilder.create().texOffs(230, 238).addBox(-1.0F, -1.0F, -0.2F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(8.0F, 1.0F, 4.0F, -0.327F, 0.253F, -0.0999F));

		PartDefinition Rib1Seg2_r1 = Rib1Seg1.addOrReplaceChild("Rib1Seg2_r1", CubeListBuilder.create().texOffs(208, 4).mirror().addBox(-1.0F, -1.01F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(4.4646F, 0.6599F, 2.7968F, -0.7698F, 1.1076F, -0.7295F));

		PartDefinition Rib1Seg1_r1 = Rib1Seg1.addOrReplaceChild("Rib1Seg1_r1", CubeListBuilder.create().texOffs(112, 215).mirror().addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(0.26F, -0.18F, 0.0353F, -0.5213F, 0.8964F, -0.4369F));

		PartDefinition Rib2Seg1 = RibSeg.addOrReplaceChild("Rib2Seg1", CubeListBuilder.create(), PartPose.offset(-8.5399F, 2.9949F, -4.2133F));

		PartDefinition Rib2Seg3_r1 = Rib2Seg1.addOrReplaceChild("Rib2Seg3_r1", CubeListBuilder.create().texOffs(230, 238).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(10.0F, 2.0F, 5.0F, -0.3028F, 0.3558F, -0.0332F));

		PartDefinition Rib2Seg2_r1 = Rib2Seg1.addOrReplaceChild("Rib2Seg2_r1", CubeListBuilder.create().texOffs(204, 0).mirror().addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(2.9513F, 0.8801F, 2.9105F, -0.8869F, 1.2019F, -0.7775F));

		PartDefinition Rib2Seg1_r1 = Rib2Seg1.addOrReplaceChild("Rib2Seg1_r1", CubeListBuilder.create().texOffs(113, 216).mirror().addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(0.8139F, -0.168F, -0.0924F, -0.3465F, 0.6042F, -0.1271F));

		PartDefinition Rib1Seg2 = RibSeg.addOrReplaceChild("Rib1Seg2", CubeListBuilder.create(), PartPose.offset(-12.6861F, -2.2203F, -2.659F));

		PartDefinition Rib3Seg2_r1 = Rib1Seg2.addOrReplaceChild("Rib3Seg2_r1", CubeListBuilder.create().texOffs(208, 4).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-5.0F, -1.0F, 0.0F, -0.0177F, -0.6811F, 0.1186F));

		PartDefinition Rib3Seg1_r1 = Rib1Seg2.addOrReplaceChild("Rib3Seg1_r1", CubeListBuilder.create().texOffs(112, 215).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.0665F, -0.4057F, -1.1476F, -0.054F, -1.3435F, 0.1753F));

		PartDefinition Rib3Seg3_r1 = Rib1Seg2.addOrReplaceChild("Rib3Seg3_r1", CubeListBuilder.create().texOffs(230, 238).addBox(-1.0F, -1.0F, -0.35F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-7.4199F, -1.2704F, 3.1122F, -0.0197F, -0.9074F, 0.1382F));

		PartDefinition Rib2Seg2 = RibSeg.addOrReplaceChild("Rib2Seg2", CubeListBuilder.create(), PartPose.offset(-12.7383F, -0.1474F, -4.1326F));

		PartDefinition Rib4Seg1_r1 = Rib2Seg2.addOrReplaceChild("Rib4Seg1_r1", CubeListBuilder.create().texOffs(113, 216).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3922F, -0.5926F, 0.4429F));

		PartDefinition Rib4Seg2_r1 = Rib2Seg2.addOrReplaceChild("Rib4Seg2_r1", CubeListBuilder.create().texOffs(204, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.3635F, 0.51F, 2.973F, -0.947F, -1.1695F, 1.1231F));

		PartDefinition Rib3Seg2 = RibSeg.addOrReplaceChild("Rib3Seg2", CubeListBuilder.create(), PartPose.offset(-12.7269F, 1.8355F, -4.3934F));

		PartDefinition Rib5Seg1_r1 = Rib3Seg2.addOrReplaceChild("Rib5Seg1_r1", CubeListBuilder.create().texOffs(110, 213).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3003F, -0.7648F, -0.1516F));

		PartDefinition Rib5Seg2_r1 = Rib3Seg2.addOrReplaceChild("Rib5Seg2_r1", CubeListBuilder.create().texOffs(204, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-4.0912F, 2.6533F, 4.6624F, -2.3568F, -1.264F, 2.0179F));

		PartDefinition Rib5Seg3_r1 = Rib3Seg2.addOrReplaceChild("Rib5Seg3_r1", CubeListBuilder.create().texOffs(230, 238).addBox(-3.0F, -1.0F, -2.25F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-10.9017F, 5.4571F, 5.6916F, -0.3135F, -0.8064F, -0.1329F));

		PartDefinition Blossom_Head_Tentacle2 = Tentacle.addOrReplaceChild("Blossom_Head_Tentacle2", CubeListBuilder.create(), PartPose.offsetAndRotation(-21.0F, -6.0F, -3.0F, 0.0F, -1.5708F, 0.0873F));

		PartDefinition BodyBase_r5 = Blossom_Head_Tentacle2.addOrReplaceChild("BodyBase_r5", CubeListBuilder.create().texOffs(109, 304).mirror().addBox(-2.0F, -6.0F, -4.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.7704F, -9.7651F, 2.1503F, -1.3245F, 1.3909F, -1.3283F));

		PartDefinition Arm_r4 = Blossom_Head_Tentacle2.addOrReplaceChild("Arm_r4", CubeListBuilder.create().texOffs(318, 245).addBox(0.0953F, -1.6721F, -5.8679F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(132, 267).addBox(-0.4047F, -2.4221F, -1.8679F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9107F, -7.0779F, 3.2151F, -1.4266F, 1.2624F, -1.4333F));

		PartDefinition Jaw_r3 = Blossom_Head_Tentacle2.addOrReplaceChild("Jaw_r3", CubeListBuilder.create().texOffs(260, 152).addBox(-4.8246F, 0.7803F, -1.5825F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9107F, -7.0779F, 3.2151F, -1.4167F, 0.2686F, -1.6718F));

		PartDefinition Head_r3 = Blossom_Head_Tentacle2.addOrReplaceChild("Head_r3", CubeListBuilder.create().texOffs(206, 73).addBox(-4.2844F, -7.0707F, -1.7586F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9107F, -7.0779F, 3.2151F, -1.4742F, 0.4773F, -1.515F));

		PartDefinition BodyBase_r6 = Blossom_Head_Tentacle2.addOrReplaceChild("BodyBase_r6", CubeListBuilder.create().texOffs(109, 304).addBox(-2.0F, -6.0F, -4.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8539F, 1.0F, 0.2376F, 0.0F, 1.5272F, 0.0F));

		PartDefinition Body_Tentackle2 = Blossom_Head_Tentacle2.addOrReplaceChild("Body_Tentackle2", CubeListBuilder.create(), PartPose.offset(0.0F, -13.0F, 6.0F));

		PartDefinition BodyTop_r4 = Body_Tentackle2.addOrReplaceChild("BodyTop_r4", CubeListBuilder.create().texOffs(132, 267).addBox(-2.4641F, -5.7768F, -7.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9882F, -9.2026F, 2.0181F, 1.7698F, -1.3484F, -1.7651F));

		PartDefinition Fungus_r9 = Body_Tentackle2.addOrReplaceChild("Fungus_r9", CubeListBuilder.create().texOffs(153, 0).addBox(-20.0017F, 1.2949F, -12.8282F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4086F, -14.3422F, -19.437F, 3.0037F, 1.0522F, 2.7358F));

		PartDefinition Fungus_r10 = Body_Tentackle2.addOrReplaceChild("Fungus_r10", CubeListBuilder.create().texOffs(166, 0).addBox(-18.736F, 5.1275F, 6.6278F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.214F, -12.0824F, -14.981F, 0.4687F, 0.9305F, 0.5628F));

		PartDefinition Fungus_r11 = Body_Tentackle2.addOrReplaceChild("Fungus_r11", CubeListBuilder.create().texOffs(139, 0).addBox(-18.0139F, 8.3595F, -12.6627F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3612F, -8.4334F, -18.3088F, 2.3456F, 0.8102F, 2.4981F));

		PartDefinition Fungus_r12 = Body_Tentackle2.addOrReplaceChild("Fungus_r12", CubeListBuilder.create().texOffs(139, 0).addBox(-21.3407F, 2.684F, 2.4586F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6779F, -8.607F, -17.4994F, 0.4232F, 1.2197F, 0.2342F));

		PartDefinition BodyBase_r7 = Body_Tentackle2.addOrReplaceChild("BodyBase_r7", CubeListBuilder.create().texOffs(109, 304).addBox(-20.4367F, -1.8137F, -0.5391F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0331F, -5.5766F, -16.9589F, 1.3717F, 1.3484F, 1.3765F));

		PartDefinition Jaw_r4 = Body_Tentackle2.addOrReplaceChild("Jaw_r4", CubeListBuilder.create().texOffs(260, 152).addBox(-0.2701F, -19.3701F, -4.2097F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5139F, -9.1001F, -18.9276F, -1.5654F, -0.3515F, 1.5693F));

		PartDefinition Arm_r5 = Body_Tentackle2.addOrReplaceChild("Arm_r5", CubeListBuilder.create().texOffs(318, 245).addBox(-20.6824F, 2.3028F, -3.4591F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8926F, -8.0524F, -17.2155F, 2.087F, 1.3055F, -1.4094F));

		PartDefinition BodyBase_r8 = Body_Tentackle2.addOrReplaceChild("BodyBase_r8", CubeListBuilder.create().texOffs(109, 304).addBox(15.2035F, -14.0221F, -4.8288F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6119F, -5.0593F, -17.2192F, -1.6737F, -1.1324F, 1.6641F));

		PartDefinition Fungus_r13 = Body_Tentackle2.addOrReplaceChild("Fungus_r13", CubeListBuilder.create().texOffs(153, 0).addBox(-20.3016F, -4.9833F, -10.8399F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6024F, -4.8977F, -19.2672F, -2.5451F, 1.085F, -2.9124F));

		PartDefinition Fungus_r14 = Body_Tentackle2.addOrReplaceChild("Fungus_r14", CubeListBuilder.create().texOffs(153, 0).addBox(-19.8634F, -1.343F, 6.0622F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.4371F, -3.2184F, -15.4616F, -0.1395F, 1.0377F, 0.0355F));

		PartDefinition Fungus_r15 = Body_Tentackle2.addOrReplaceChild("Fungus_r15", CubeListBuilder.create().texOffs(139, 0).addBox(-19.8982F, 2.1466F, -13.807F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7157F, -1.3058F, -21.7981F, 2.8453F, 0.9677F, 2.8751F));

		PartDefinition Fungus_r16 = Body_Tentackle2.addOrReplaceChild("Fungus_r16", CubeListBuilder.create().texOffs(139, 0).addBox(-20.7684F, -3.8082F, 3.4499F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.271F, -0.321F, -22.2333F, -0.5013F, 1.1405F, -0.6452F));

		PartDefinition Arm_r6 = Body_Tentackle2.addOrReplaceChild("Arm_r6", CubeListBuilder.create().texOffs(318, 245).addBox(-20.1845F, -8.5226F, 0.8288F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1614F, -4.2557F, -18.2783F, -1.4438F, 1.2191F, -1.4515F));

		PartDefinition BodyTop_r5 = Body_Tentackle2.addOrReplaceChild("BodyTop_r5", CubeListBuilder.create().texOffs(132, 267).addBox(-15.2881F, -19.7717F, -6.1712F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2483F, -1.5062F, -20.2885F, -1.5116F, 0.7409F, -1.5308F));

		PartDefinition Cheast_Bones3 = Body_Tentackle2.addOrReplaceChild("Cheast_Bones3", CubeListBuilder.create(), PartPose.offset(1.0F, -13.0F, 2.0F));

		PartDefinition BodyTop_r6 = Cheast_Bones3.addOrReplaceChild("BodyTop_r6", CubeListBuilder.create().texOffs(132, 267).addBox(-0.8602F, -10.954F, -1.8679F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0331F, 7.4234F, -1.9589F, -1.1064F, 1.4733F, -1.1083F));

		PartDefinition H5TorsoRight_r2 = Cheast_Bones3.addOrReplaceChild("H5TorsoRight_r2", CubeListBuilder.create().texOffs(12, 16).mirror().addBox(-1.0F, -5.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(4.1932F, -4.6632F, -0.2515F, -0.737F, 1.0213F, -0.5311F));

		PartDefinition H5TorsoLeft_r2 = Cheast_Bones3.addOrReplaceChild("H5TorsoLeft_r2", CubeListBuilder.create().texOffs(12, 16).addBox(-2.0F, -4.9111F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.4442F, -4.4133F, 0.3666F, -0.6239F, -1.1192F, 0.4455F));

		PartDefinition Torso_r2 = Cheast_Bones3.addOrReplaceChild("Torso_r2", CubeListBuilder.create().texOffs(0, 14).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.641F, -6.3322F, -1.5201F, -0.3927F, -0.0436F, 0.0F));

		PartDefinition Blossom_Head2 = Cheast_Bones3.addOrReplaceChild("Blossom_Head2", CubeListBuilder.create(), PartPose.offset(1.0F, -9.0F, 0.0F));

		PartDefinition Tumor_r4 = Blossom_Head2.addOrReplaceChild("Tumor_r4", CubeListBuilder.create().texOffs(92, 109).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.3062F, -3.7266F, 4.6073F, 1.6966F, -0.011F, -1.0924F));

		PartDefinition Tumor_r5 = Blossom_Head2.addOrReplaceChild("Tumor_r5", CubeListBuilder.create().texOffs(97, 97).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.351F, -6.5457F, 3.5822F, 1.394F, -0.0342F, -1.2213F));

		PartDefinition Tumor_r6 = Blossom_Head2.addOrReplaceChild("Tumor_r6", CubeListBuilder.create().texOffs(95, 99).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3335F, -5.417F, 3.4109F, 0.0235F, -0.0368F, -0.5677F));

		PartDefinition Petal6_r1 = Blossom_Head2.addOrReplaceChild("Petal6_r1", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1797F, -2.54F, -3.2368F, 1.3616F, 0.529F, 0.2922F));

		PartDefinition Petal5_r3 = Blossom_Head2.addOrReplaceChild("Petal5_r3", CubeListBuilder.create().texOffs(154, 13).addBox(-2.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5062F, -5.575F, -0.4089F, 1.5021F, 0.0113F, 0.6588F));

		PartDefinition Petal4_r4 = Blossom_Head2.addOrReplaceChild("Petal4_r4", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.1849F, -0.9415F, -4.0534F, 0.8029F, 0.1194F, 0.4523F));

		PartDefinition Petal3_r4 = Blossom_Head2.addOrReplaceChild("Petal3_r4", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3998F, -7.6096F, -1.4676F, 1.6838F, -0.4013F, 0.3955F));

		PartDefinition Petal5_r4 = Blossom_Head2.addOrReplaceChild("Petal5_r4", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.179F, -2.8739F, 5.3029F, -1.0564F, -0.712F, 0.5628F));

		PartDefinition Petal4_r5 = Blossom_Head2.addOrReplaceChild("Petal4_r5", CubeListBuilder.create().texOffs(154, 13).addBox(-2.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.921F, -7.451F, 1.0459F, -1.0333F, -0.089F, 0.5999F));

		PartDefinition Petal3_r5 = Blossom_Head2.addOrReplaceChild("Petal3_r5", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0268F, -8.0952F, 3.1315F, -1.7402F, -0.2947F, 0.7106F));

		PartDefinition Petal2_r4 = Blossom_Head2.addOrReplaceChild("Petal2_r4", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.4713F, -2.6459F, 5.1507F, -0.8984F, 0.1136F, 1.0421F));

		PartDefinition Petal6_r2 = Blossom_Head2.addOrReplaceChild("Petal6_r2", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3772F, -5.4811F, 4.9006F, -0.9369F, -0.4768F, -0.4842F));

		PartDefinition Petal5_r5 = Blossom_Head2.addOrReplaceChild("Petal5_r5", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.345F, -4.1921F, 5.5121F, -1.1511F, 0.4056F, 0.5F));

		PartDefinition Petal4_r6 = Blossom_Head2.addOrReplaceChild("Petal4_r6", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1249F, -7.3049F, 3.0762F, -1.7453F, -0.0436F, 0.0F));

		PartDefinition Petal3_r6 = Blossom_Head2.addOrReplaceChild("Petal3_r6", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0538F, -1.5576F, 7.1479F, -0.4251F, -0.2752F, 0.2022F));

		PartDefinition Head_r4 = Blossom_Head2.addOrReplaceChild("Head_r4", CubeListBuilder.create().texOffs(218, 224).addBox(-4.0F, -5.5F, -3.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1951F, -0.7011F, 0.2631F, 0.3491F, -0.0436F, 0.0F));

		PartDefinition RibSeg2 = Cheast_Bones3.addOrReplaceChild("RibSeg2", CubeListBuilder.create(), PartPose.offset(10.551F, -5.7366F, 4.1506F));

		PartDefinition Rib1Seg3 = RibSeg2.addOrReplaceChild("Rib1Seg3", CubeListBuilder.create(), PartPose.offset(-8.0F, -1.0F, -4.0F));

		PartDefinition Rib1Seg4_r1 = Rib1Seg3.addOrReplaceChild("Rib1Seg4_r1", CubeListBuilder.create().texOffs(230, 238).addBox(-1.0F, -1.0F, -0.2F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(8.0F, 1.0F, 4.0F, -0.327F, 0.253F, -0.0999F));

		PartDefinition Rib1Seg3_r2 = Rib1Seg3.addOrReplaceChild("Rib1Seg3_r2", CubeListBuilder.create().texOffs(208, 4).mirror().addBox(-1.0F, -1.01F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(4.4646F, 0.6599F, 2.7968F, -0.7698F, 1.1076F, -0.7295F));

		PartDefinition Rib2Seg2_r2 = Rib1Seg3.addOrReplaceChild("Rib2Seg2_r2", CubeListBuilder.create().texOffs(112, 215).mirror().addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(0.26F, -0.18F, 0.0353F, -0.5213F, 0.8964F, -0.4369F));

		PartDefinition Rib2Seg3 = RibSeg2.addOrReplaceChild("Rib2Seg3", CubeListBuilder.create(), PartPose.offset(-8.5399F, 2.9949F, -6.2133F));

		PartDefinition Rib2Seg4_r1 = Rib2Seg3.addOrReplaceChild("Rib2Seg4_r1", CubeListBuilder.create().texOffs(230, 238).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(10.0F, 2.0F, 7.0F, -0.3028F, 0.3558F, -0.0332F));

		PartDefinition Rib3Seg3_r2 = Rib2Seg3.addOrReplaceChild("Rib3Seg3_r2", CubeListBuilder.create().texOffs(204, 0).mirror().addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(2.9513F, 0.8801F, 4.9105F, -0.8869F, 1.2019F, -0.7775F));

		PartDefinition Rib2Seg2_r3 = Rib2Seg3.addOrReplaceChild("Rib2Seg2_r3", CubeListBuilder.create().texOffs(113, 216).mirror().addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(0.8139F, -0.168F, 1.9076F, -0.3465F, 0.6042F, -0.1271F));

		PartDefinition Rib1Seg4 = RibSeg2.addOrReplaceChild("Rib1Seg4", CubeListBuilder.create(), PartPose.offset(-11.6861F, -2.2203F, -2.6589F));

		PartDefinition Rib3Seg3_r3 = Rib1Seg4.addOrReplaceChild("Rib3Seg3_r3", CubeListBuilder.create().texOffs(208, 4).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-6.0F, -1.0F, 0.0F, -0.0177F, -0.6811F, 0.1186F));

		PartDefinition Rib3Seg2_r2 = Rib1Seg4.addOrReplaceChild("Rib3Seg2_r2", CubeListBuilder.create().texOffs(112, 215).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-1.0665F, -0.4057F, -1.1477F, -0.054F, -1.3435F, 0.1753F));

		PartDefinition Rib4Seg4_r1 = Rib1Seg4.addOrReplaceChild("Rib4Seg4_r1", CubeListBuilder.create().texOffs(230, 238).addBox(-1.0F, -1.0F, -0.35F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-8.4199F, -1.2704F, 3.1122F, -0.0197F, -0.9074F, 0.1382F));

		PartDefinition Rib2Seg4 = RibSeg2.addOrReplaceChild("Rib2Seg4", CubeListBuilder.create(), PartPose.offset(-12.7383F, -0.1474F, -4.1326F));

		PartDefinition Rib4Seg2_r2 = Rib2Seg4.addOrReplaceChild("Rib4Seg2_r2", CubeListBuilder.create().texOffs(113, 216).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3922F, -0.5926F, 0.4429F));

		PartDefinition Rib4Seg3_r1 = Rib2Seg4.addOrReplaceChild("Rib4Seg3_r1", CubeListBuilder.create().texOffs(204, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.3635F, 0.51F, 2.973F, -0.947F, -1.1695F, 1.1231F));

		PartDefinition Rib3Seg3 = RibSeg2.addOrReplaceChild("Rib3Seg3", CubeListBuilder.create(), PartPose.offset(-11.7269F, 1.8355F, -5.3934F));

		PartDefinition Rib5Seg2_r2 = Rib3Seg3.addOrReplaceChild("Rib5Seg2_r2", CubeListBuilder.create().texOffs(110, 213).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 9.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 1.0F, -0.3003F, -0.7648F, -0.1516F));

		PartDefinition Rib5Seg3_r2 = Rib3Seg3.addOrReplaceChild("Rib5Seg3_r2", CubeListBuilder.create().texOffs(204, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-5.0912F, 2.6533F, 5.6624F, -2.3568F, -1.264F, 2.0179F));

		PartDefinition Rib5Seg4_r1 = Rib3Seg3.addOrReplaceChild("Rib5Seg4_r1", CubeListBuilder.create().texOffs(230, 238).addBox(-3.0F, -1.0F, -2.25F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-11.9017F, 5.4571F, 6.6916F, -0.3135F, -0.8064F, -0.1329F));

		PartDefinition Mouth_Head_Tentacle = Tentacle.addOrReplaceChild("Mouth_Head_Tentacle", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, -3.0F, -22.0F, 0.0F, -0.1745F, 0.0F));

		PartDefinition Arm_r7 = Mouth_Head_Tentacle.addOrReplaceChild("Arm_r7", CubeListBuilder.create().texOffs(318, 245).addBox(-1.6527F, -2.0304F, 0.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7323F, -18.4313F, -3.599F, 1.6844F, 1.1758F, 1.6757F));

		PartDefinition BodyTop_r7 = Mouth_Head_Tentacle.addOrReplaceChild("BodyTop_r7", CubeListBuilder.create().texOffs(132, 267).addBox(-2.5359F, -5.7768F, -7.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8243F, -15.7703F, -1.4708F, 1.6277F, 0.6973F, 1.6074F));

		PartDefinition BodyBase_r9 = Mouth_Head_Tentacle.addOrReplaceChild("BodyBase_r9", CubeListBuilder.create().texOffs(109, 304).addBox(-2.0F, -6.0F, -4.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3554F, -11.0534F, -0.7034F, 1.7698F, 1.3484F, 1.7651F));

		PartDefinition Arm_r8 = Mouth_Head_Tentacle.addOrReplaceChild("Arm_r8", CubeListBuilder.create().texOffs(318, 245).addBox(-4.0953F, -1.6721F, -5.8679F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(132, 267).addBox(-4.5953F, -2.4221F, -1.8679F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.3405F, -8.5433F, -2.1086F, 1.8171F, 1.3909F, 1.8133F));

		PartDefinition Jaw_r5 = Mouth_Head_Tentacle.addOrReplaceChild("Jaw_r5", CubeListBuilder.create().texOffs(260, 152).addBox(-3.487F, 1.7899F, -4.1072F, 8.0F, 1.0F, 8.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(4.0542F, -6.7713F, -1.9303F, 0.4352F, 0.4437F, 0.7105F));

		PartDefinition Head_r5 = Mouth_Head_Tentacle.addOrReplaceChild("Head_r5", CubeListBuilder.create().texOffs(206, 73).mirror().addBox(-3.9835F, -5.8414F, -4.1892F, 8.0F, 7.0F, 8.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(4.0542F, -6.7713F, -1.9303F, 0.2154F, 0.6292F, 0.5071F));

		PartDefinition BodyBase_r10 = Mouth_Head_Tentacle.addOrReplaceChild("BodyBase_r10", CubeListBuilder.create().texOffs(109, 304).addBox(-2.0F, -6.0F, -4.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3769F, -0.1305F, -0.2109F, -1.8936F, 1.4329F, -1.8907F));

		PartDefinition torso_tentacle3 = Mouth_Head_Tentacle.addOrReplaceChild("torso_tentacle3", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, -6.0F));

		PartDefinition Fungus_r17 = torso_tentacle3.addOrReplaceChild("Fungus_r17", CubeListBuilder.create().texOffs(139, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1867F, -9.0996F, -0.9221F, 0.6377F, -0.9897F, -0.2648F));

		PartDefinition Fungus_r18 = torso_tentacle3.addOrReplaceChild("Fungus_r18", CubeListBuilder.create().texOffs(139, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.7936F, -3.0254F, -0.3384F, 2.9081F, -1.1248F, -3.0941F));

		PartDefinition Fungus_r19 = torso_tentacle3.addOrReplaceChild("Fungus_r19", CubeListBuilder.create().texOffs(139, 0).addBox(-4.25F, -1.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6573F, -2.6984F, 2.7603F, -0.1926F, -0.8923F, 0.1832F));

		PartDefinition Fungus_r20 = torso_tentacle3.addOrReplaceChild("Fungus_r20", CubeListBuilder.create().texOffs(153, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.2898F, -7.5799F, 1.4175F, 2.4122F, -1.1749F, -2.2873F));

		PartDefinition Arm_r9 = torso_tentacle3.addOrReplaceChild("Arm_r9", CubeListBuilder.create().texOffs(318, 245).addBox(-2.6223F, -0.6831F, -4.1F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.48F, -8.8756F, 1.2371F, -1.4837F, -1.0456F, 1.4954F));

		PartDefinition Arm_r10 = torso_tentacle3.addOrReplaceChild("Arm_r10", CubeListBuilder.create().texOffs(318, 245).addBox(-1.6527F, -2.0304F, 0.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.3955F, -8.4427F, -0.3735F, 0.7849F, -1.5091F, -0.7859F));

		PartDefinition BodyTop_r8 = torso_tentacle3.addOrReplaceChild("BodyTop_r8", CubeListBuilder.create().texOffs(132, 267).addBox(-2.5359F, -5.7768F, -7.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4298F, -5.1492F, -1.1827F, -1.4679F, -1.1324F, 1.4775F));

		PartDefinition BodyBase_r11 = torso_tentacle3.addOrReplaceChild("BodyBase_r11", CubeListBuilder.create().texOffs(109, 304).addBox(-2.0F, -6.0F, -4.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1287F, -0.5957F, 0.103F, 1.3717F, -1.3484F, -1.3765F));

		PartDefinition ribs_torso2 = torso_tentacle3.addOrReplaceChild("ribs_torso2", CubeListBuilder.create(), PartPose.offset(0.0F, -10.0F, 1.0F));

		PartDefinition BodyTop_r9 = ribs_torso2.addOrReplaceChild("BodyTop_r9", CubeListBuilder.create().texOffs(132, 281).addBox(-4.1398F, -10.954F, -1.8679F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4495F, -0.0752F, 0.3726F, 1.8936F, 1.4329F, 1.8907F));

		PartDefinition BodyBase_r12 = ribs_torso2.addOrReplaceChild("BodyBase_r12", CubeListBuilder.create().texOffs(109, 304).addBox(-2.0953F, -5.9221F, -1.3679F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4495F, -0.0752F, 0.3726F, -1.8171F, 1.3909F, -1.8133F));

		PartDefinition head1 = ribs_torso2.addOrReplaceChild("head1", CubeListBuilder.create(), PartPose.offset(1.0F, -9.0F, 1.0F));

		PartDefinition Petal5_r6 = head1.addOrReplaceChild("Petal5_r6", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2616F, -6.9185F, -4.4279F, -1.056F, 1.1683F, -1.507F));

		PartDefinition Petal4_r7 = head1.addOrReplaceChild("Petal4_r7", CubeListBuilder.create().texOffs(154, 7).addBox(-2.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9883F, -5.2216F, 4.1087F, 1.9478F, 1.3473F, 1.4664F));

		PartDefinition Petal3_r7 = head1.addOrReplaceChild("Petal3_r7", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.663F, -6.0154F, -0.991F, -0.0432F, 1.4288F, 0.1457F));

		PartDefinition Petal2_r5 = head1.addOrReplaceChild("Petal2_r5", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0197F, -7.986F, 0.0547F, -1.462F, 0.4953F, -2.2078F));

		PartDefinition Jaw_r6 = head1.addOrReplaceChild("Jaw_r6", CubeListBuilder.create().texOffs(260, 152).addBox(-4.3984F, 0.0432F, -3.7628F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8502F, -1.26F, -2.5472F, -0.3376F, 1.3009F, -0.7287F));

		PartDefinition Head_r6 = head1.addOrReplaceChild("Head_r6", CubeListBuilder.create().texOffs(206, 73).mirror().addBox(-4.5019F, -7.7725F, -4.1423F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.8502F, -1.26F, -2.5472F, -1.2344F, 1.2195F, -1.545F));

		PartDefinition Ribs = ribs_torso2.addOrReplaceChild("Ribs", CubeListBuilder.create(), PartPose.offset(-3.0365F, -5.4955F, 0.8695F));

		PartDefinition Rib2Seg6 = Ribs.addOrReplaceChild("Rib2Seg6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Rib2Seg3_r2 = Rib2Seg6.addOrReplaceChild("Rib2Seg3_r2", CubeListBuilder.create().texOffs(79, 12).addBox(-3.5203F, -5.6109F, -2.381F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-6.0F, 1.0F, -7.0F, -1.9934F, 0.3954F, -1.7995F));

		PartDefinition Rib2Seg2_r4 = Rib2Seg6.addOrReplaceChild("Rib2Seg2_r4", CubeListBuilder.create().texOffs(152, 112).addBox(-3.5303F, -1.9766F, -6.1712F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-0.8036F, -0.565F, -6.1447F, -2.9534F, 0.3954F, -1.7995F));

		PartDefinition Rib3Seg5 = Ribs.addOrReplaceChild("Rib3Seg5", CubeListBuilder.create(), PartPose.offset(0.7918F, 3.2253F, 1.0133F));

		PartDefinition Rib3Seg3_r4 = Rib3Seg5.addOrReplaceChild("Rib3Seg3_r4", CubeListBuilder.create().texOffs(79, 12).addBox(-2.5789F, -5.5579F, -1.1509F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-9.0F, 2.0F, -5.0F, -2.1898F, 0.157F, -1.9365F));

		PartDefinition Rib3Seg2_r3 = Rib3Seg5.addOrReplaceChild("Rib3Seg2_r3", CubeListBuilder.create().texOffs(152, 112).addBox(-2.5789F, -2.4907F, -4.8202F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.0763F, -0.6903F, -4.6406F, 3.1335F, 0.157F, -1.9365F));

		PartDefinition Rib4Seg3 = Ribs.addOrReplaceChild("Rib4Seg3", CubeListBuilder.create(), PartPose.offset(7.092F, 1.5392F, -1.5445F));

		PartDefinition Rib4Seg2_r3 = Rib4Seg3.addOrReplaceChild("Rib4Seg2_r3", CubeListBuilder.create().texOffs(152, 112).addBox(-1.9684F, -2.2801F, 0.3651F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(2.0F, 0.0F, -2.0F, -0.21F, 0.2632F, -1.3367F));

		PartDefinition Rib4Seg3_r2 = Rib4Seg3.addOrReplaceChild("Rib4Seg3_r2", CubeListBuilder.create().texOffs(79, 12).addBox(-1.961F, -3.0097F, -0.9516F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(5.5213F, 1.0296F, -2.8731F, -0.729F, 0.2732F, -1.3503F));

		PartDefinition Rib5Seg2 = Ribs.addOrReplaceChild("Rib5Seg2", CubeListBuilder.create(), PartPose.offset(7.3899F, -2.3559F, -1.7152F));

		PartDefinition Rib5Seg2_r3 = Rib5Seg2.addOrReplaceChild("Rib5Seg2_r3", CubeListBuilder.create().texOffs(152, 112).addBox(-1.9175F, -1.1786F, 2.361F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(3.0F, 0.0F, -2.0F, 0.7416F, 0.2065F, -1.3971F));

		PartDefinition Rib5Seg4_r2 = Rib5Seg2.addOrReplaceChild("Rib5Seg4_r2", CubeListBuilder.create().texOffs(79, 12).addBox(-1.9275F, -2.8024F, -3.3756F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(10.9618F, 1.2356F, -1.3389F, -1.3092F, 0.2065F, -1.3971F));

		PartDefinition Rib5Seg3_r3 = Rib5Seg2.addOrReplaceChild("Rib5Seg3_r3", CubeListBuilder.create().texOffs(152, 112).addBox(-1.9175F, -4.4823F, -1.1791F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(7.2264F, 0.0129F, 1.3285F, -0.6111F, 0.2065F, -1.3971F));

		PartDefinition Mouth_Head_Tentacle2 = Tentacle.addOrReplaceChild("Mouth_Head_Tentacle2", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, -3.0F, 21.0F, -3.1416F, 0.1745F, 3.1416F));

		PartDefinition Arm_r11 = Mouth_Head_Tentacle2.addOrReplaceChild("Arm_r11", CubeListBuilder.create().texOffs(318, 245).addBox(-1.6527F, -2.0304F, 0.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7323F, -18.4313F, -3.599F, 1.6844F, 1.1758F, 1.6757F));

		PartDefinition BodyTop_r10 = Mouth_Head_Tentacle2.addOrReplaceChild("BodyTop_r10", CubeListBuilder.create().texOffs(132, 267).addBox(-2.5359F, -5.7768F, -7.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8243F, -15.7703F, -1.4708F, 1.6277F, 0.6973F, 1.6074F));

		PartDefinition BodyBase_r13 = Mouth_Head_Tentacle2.addOrReplaceChild("BodyBase_r13", CubeListBuilder.create().texOffs(109, 304).addBox(-2.0F, -6.0F, -4.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3554F, -11.0534F, -0.7034F, 1.7698F, 1.3484F, 1.7651F));

		PartDefinition Arm_r12 = Mouth_Head_Tentacle2.addOrReplaceChild("Arm_r12", CubeListBuilder.create().texOffs(318, 245).addBox(-4.0953F, -1.6721F, -5.8679F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(132, 267).addBox(-4.5953F, -2.4221F, -1.8679F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.3405F, -8.5433F, -2.1086F, 1.8171F, 1.3909F, 1.8133F));

		PartDefinition Jaw_r7 = Mouth_Head_Tentacle2.addOrReplaceChild("Jaw_r7", CubeListBuilder.create().texOffs(260, 152).addBox(-3.487F, 1.7899F, -4.1072F, 8.0F, 1.0F, 8.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(4.0542F, -6.7713F, -1.9303F, 0.4352F, 0.4437F, 0.7105F));

		PartDefinition Head_r7 = Mouth_Head_Tentacle2.addOrReplaceChild("Head_r7", CubeListBuilder.create().texOffs(206, 73).mirror().addBox(-3.9835F, -5.8414F, -4.1892F, 8.0F, 7.0F, 8.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(4.0542F, -6.7713F, -1.9303F, 0.2154F, 0.6292F, 0.5071F));

		PartDefinition BodyBase_r14 = Mouth_Head_Tentacle2.addOrReplaceChild("BodyBase_r14", CubeListBuilder.create().texOffs(109, 304).addBox(-2.0F, -6.0F, -4.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3769F, -0.1305F, -0.2109F, -1.8936F, 1.4329F, -1.8907F));

		PartDefinition torso_tentacle2 = Mouth_Head_Tentacle2.addOrReplaceChild("torso_tentacle2", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, -6.0F));

		PartDefinition Fungus_r21 = torso_tentacle2.addOrReplaceChild("Fungus_r21", CubeListBuilder.create().texOffs(139, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1867F, -9.0996F, -0.9221F, 0.6377F, -0.9897F, -0.2648F));

		PartDefinition Fungus_r22 = torso_tentacle2.addOrReplaceChild("Fungus_r22", CubeListBuilder.create().texOffs(139, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.7936F, -3.0254F, -0.3384F, 2.9081F, -1.1248F, -3.0941F));

		PartDefinition Fungus_r23 = torso_tentacle2.addOrReplaceChild("Fungus_r23", CubeListBuilder.create().texOffs(139, 0).addBox(-4.25F, -1.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6573F, -2.6984F, 2.7603F, -0.1926F, -0.8923F, 0.1832F));

		PartDefinition Fungus_r24 = torso_tentacle2.addOrReplaceChild("Fungus_r24", CubeListBuilder.create().texOffs(153, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.2898F, -7.5799F, 1.4175F, 2.4122F, -1.1749F, -2.2873F));

		PartDefinition Arm_r13 = torso_tentacle2.addOrReplaceChild("Arm_r13", CubeListBuilder.create().texOffs(318, 245).addBox(-2.6223F, -0.6831F, -4.1F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.48F, -8.8756F, 1.2371F, -1.4837F, -1.0456F, 1.4954F));

		PartDefinition Arm_r14 = torso_tentacle2.addOrReplaceChild("Arm_r14", CubeListBuilder.create().texOffs(318, 245).addBox(-1.6527F, -2.0304F, 0.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.3955F, -8.4427F, -0.3735F, 0.7849F, -1.5091F, -0.7859F));

		PartDefinition BodyTop_r11 = torso_tentacle2.addOrReplaceChild("BodyTop_r11", CubeListBuilder.create().texOffs(132, 267).addBox(-2.5359F, -5.7768F, -7.0F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4298F, -5.1492F, -1.1828F, -1.4679F, -1.1324F, 1.4775F));

		PartDefinition BodyBase_r15 = torso_tentacle2.addOrReplaceChild("BodyBase_r15", CubeListBuilder.create().texOffs(109, 304).addBox(-2.0F, -6.0F, -4.0F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1287F, -0.5957F, 0.103F, 1.3717F, -1.3484F, -1.3765F));

		PartDefinition ribs_torso3 = torso_tentacle2.addOrReplaceChild("ribs_torso3", CubeListBuilder.create(), PartPose.offset(0.0F, -10.0F, 1.0F));

		PartDefinition BodyTop_r12 = ribs_torso3.addOrReplaceChild("BodyTop_r12", CubeListBuilder.create().texOffs(132, 281).addBox(-4.1398F, -10.954F, -1.8679F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4496F, -0.0752F, 0.3726F, 1.8936F, 1.4329F, 1.8907F));

		PartDefinition BodyBase_r16 = ribs_torso3.addOrReplaceChild("BodyBase_r16", CubeListBuilder.create().texOffs(109, 304).addBox(-2.0953F, -5.9221F, -1.3679F, 4.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.4496F, -0.0752F, 0.3726F, -1.8171F, 1.3909F, -1.8133F));

		PartDefinition head2 = ribs_torso3.addOrReplaceChild("head2", CubeListBuilder.create(), PartPose.offset(1.0F, -9.0F, 1.5F));

		PartDefinition Petal5_r7 = head2.addOrReplaceChild("Petal5_r7", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.2616F, -6.9185F, -4.9279F, -1.056F, 1.1683F, -1.507F));

		PartDefinition Petal4_r8 = head2.addOrReplaceChild("Petal4_r8", CubeListBuilder.create().texOffs(154, 7).addBox(-2.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9883F, -5.2216F, 3.6087F, 1.9478F, 1.3473F, 1.4664F));

		PartDefinition Petal3_r8 = head2.addOrReplaceChild("Petal3_r8", CubeListBuilder.create().texOffs(154, 13).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.663F, -6.0154F, -1.491F, -0.0432F, 1.4288F, 0.1457F));

		PartDefinition Petal2_r6 = head2.addOrReplaceChild("Petal2_r6", CubeListBuilder.create().texOffs(154, 7).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0196F, -7.986F, -0.4453F, -1.462F, 0.4953F, -2.2078F));

		PartDefinition Jaw_r8 = head2.addOrReplaceChild("Jaw_r8", CubeListBuilder.create().texOffs(260, 152).addBox(-4.3984F, 0.0432F, -3.7628F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8502F, -1.26F, -3.0472F, -0.3376F, 1.3009F, -0.7287F));

		PartDefinition Head_r8 = head2.addOrReplaceChild("Head_r8", CubeListBuilder.create().texOffs(206, 73).mirror().addBox(-4.5019F, -7.7725F, -4.1423F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.8502F, -1.26F, -3.0472F, -1.2344F, 1.2195F, -1.545F));

		PartDefinition Rib2Seg5 = ribs_torso3.addOrReplaceChild("Rib2Seg5", CubeListBuilder.create(), PartPose.offset(-2.8401F, -6.0605F, -0.2752F));

		PartDefinition Rib2Seg2_r5 = Rib2Seg5.addOrReplaceChild("Rib2Seg2_r5", CubeListBuilder.create().texOffs(152, 112).addBox(-3.5303F, -1.9766F, -6.1712F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -5.0F, -2.9534F, 0.3954F, -1.7995F));

		PartDefinition Rib2Seg3_r3 = Rib2Seg5.addOrReplaceChild("Rib2Seg3_r3", CubeListBuilder.create().texOffs(79, 12).addBox(-3.5203F, -5.6109F, -2.381F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-6.1964F, 1.565F, -5.8553F, -1.9934F, 0.3954F, -1.7995F));

		PartDefinition Rib3Seg4 = ribs_torso3.addOrReplaceChild("Rib3Seg4", CubeListBuilder.create(), PartPose.offset(-2.321F, -2.9605F, 0.2421F));

		PartDefinition Rib3Seg2_r4 = Rib3Seg4.addOrReplaceChild("Rib3Seg2_r4", CubeListBuilder.create().texOffs(152, 112).addBox(-2.579F, -2.4907F, -4.8201F, 2.0F, 8.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-2.0F, 0.0F, -3.0F, 3.1335F, 0.157F, -1.9365F));

		PartDefinition Rib3Seg3_r5 = Rib3Seg4.addOrReplaceChild("Rib3Seg3_r5", CubeListBuilder.create().texOffs(79, 12).addBox(-2.5789F, -5.5579F, -1.1509F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-8.9237F, 2.6903F, -3.3594F, -2.1898F, 0.157F, -1.9365F));

		PartDefinition Rib4Seg2 = ribs_torso3.addOrReplaceChild("Rib4Seg2", CubeListBuilder.create(), PartPose.offset(4.0555F, -3.9564F, -0.6751F));

		PartDefinition Rib4Seg2_r4 = Rib4Seg2.addOrReplaceChild("Rib4Seg2_r4", CubeListBuilder.create().texOffs(152, 112).addBox(-1.9684F, -2.2801F, 0.3651F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(2.0F, 0.0F, -2.0F, -0.21F, 0.2632F, -1.3367F));

		PartDefinition Rib4Seg3_r3 = Rib4Seg2.addOrReplaceChild("Rib4Seg3_r3", CubeListBuilder.create().texOffs(79, 12).addBox(-1.961F, -3.0097F, -0.9516F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(5.5213F, 1.0296F, -2.8731F, -0.729F, 0.2732F, -1.3503F));

		PartDefinition Rib5Seg4 = ribs_torso3.addOrReplaceChild("Rib5Seg4", CubeListBuilder.create(), PartPose.offset(4.3151F, -7.6159F, -0.1847F));

		PartDefinition Rib5Seg4_r3 = Rib5Seg4.addOrReplaceChild("Rib5Seg4_r3", CubeListBuilder.create().texOffs(79, 12).addBox(-1.9275F, -2.8024F, -3.3756F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(11.0F, 1.0F, -2.0F, -1.3092F, 0.2065F, -1.3971F));

		PartDefinition Rib5Seg3_r4 = Rib5Seg4.addOrReplaceChild("Rib5Seg3_r4", CubeListBuilder.create().texOffs(152, 112).addBox(-1.9175F, -4.4823F, -1.1791F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(7.2647F, -0.2227F, 0.6674F, -0.6111F, 0.2065F, -1.3971F));

		PartDefinition Rib5Seg2_r4 = Rib5Seg4.addOrReplaceChild("Rib5Seg2_r4", CubeListBuilder.create().texOffs(152, 112).addBox(-1.9175F, -1.1786F, 2.361F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(3.0382F, -0.2356F, -2.6611F, 0.7416F, 0.2065F, -1.3971F));

		PartDefinition Base = Animation_Elements.addOrReplaceChild("Base", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Biomass_r1 = Base.addOrReplaceChild("Biomass_r1", CubeListBuilder.create().texOffs(82, 99).addBox(-9.0F, -8.0F, -6.0F, 19.0F, 14.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.9325F, -7.0636F, -6.5631F, 1.6268F, 0.2353F, -0.0517F));

		PartDefinition Biomass_r2 = Base.addOrReplaceChild("Biomass_r2", CubeListBuilder.create().texOffs(82, 99).addBox(-9.0F, -8.0F, -6.0F, 19.0F, 14.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.9325F, -7.0636F, 4.4369F, -1.626F, 0.1568F, 3.0682F));

		PartDefinition Biomass_r3 = Base.addOrReplaceChild("Biomass_r3", CubeListBuilder.create().texOffs(82, 99).addBox(-9.0F, -8.0F, -6.0F, 19.0F, 14.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0676F, -7.0636F, -1.5631F, 1.6846F, -1.0706F, -0.1648F));

		PartDefinition Biomass_r4 = Base.addOrReplaceChild("Biomass_r4", CubeListBuilder.create().texOffs(74, 91).addBox(-9.0F, -8.0F, -6.0F, 15.0F, 14.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9324F, -1.0636F, -16.5631F, -2.1986F, -1.1918F, -3.0689F));

		PartDefinition Biomass_r5 = Base.addOrReplaceChild("Biomass_r5", CubeListBuilder.create().texOffs(80, 93).addBox(0.0F, -5.0F, -7.0F, 11.0F, 12.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.5202F, -0.1648F, 18.862F, 0.4094F, 0.4032F, -0.6728F));

		PartDefinition Biomass_r6 = Base.addOrReplaceChild("Biomass_r6", CubeListBuilder.create().texOffs(82, 95).addBox(0.0F, -5.0F, -5.0F, 11.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.5202F, -0.1648F, -3.138F, 0.4094F, 0.4032F, -0.6728F));

		PartDefinition Biomass_r7 = Base.addOrReplaceChild("Biomass_r7", CubeListBuilder.create().texOffs(76, 92).addBox(-8.5F, -7.5F, -5.5F, 14.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0551F, -1.9228F, 14.082F, 0.2392F, -0.0133F, -0.3783F));

		PartDefinition Biomass_r8 = Base.addOrReplaceChild("Biomass_r8", CubeListBuilder.create().texOffs(74, 91).addBox(-9.0F, -8.0F, -6.0F, 15.0F, 14.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.9324F, -1.0636F, 7.4369F, -0.329F, -1.0983F, 1.1961F));

		PartDefinition Biomass_r9 = Base.addOrReplaceChild("Biomass_r9", CubeListBuilder.create().texOffs(76, 92).addBox(-8.5F, -7.5F, -5.5F, 14.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.9449F, -1.9228F, -11.918F, 0.3872F, -0.8927F, -0.6826F));

		PartDefinition Biomass_r10 = Base.addOrReplaceChild("Biomass_r10", CubeListBuilder.create().texOffs(76, 92).addBox(-8.5F, -7.5F, -5.5F, 14.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0551F, -1.9228F, 2.0819F, 0.2392F, -0.0133F, -0.3783F));

		PartDefinition Biomass_r11 = Base.addOrReplaceChild("Biomass_r11", CubeListBuilder.create().texOffs(76, 92).addBox(-8.5F, -7.5F, -5.5F, 14.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0551F, -1.9228F, -5.9181F, 0.3872F, -0.8927F, -0.6826F));

		PartDefinition Biomass_r12 = Base.addOrReplaceChild("Biomass_r12", CubeListBuilder.create().texOffs(82, 95).addBox(0.0F, -5.0F, -5.0F, 11.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5202F, -0.1648F, 20.862F, 0.4094F, 0.4032F, -0.6728F));

		PartDefinition Biomass_r13 = Base.addOrReplaceChild("Biomass_r13", CubeListBuilder.create().texOffs(80, 93).addBox(0.0F, -7.0F, -7.0F, 11.0F, 14.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4798F, -0.1648F, -16.138F, 0.4094F, 0.4032F, -0.6728F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Animation_Elements.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
