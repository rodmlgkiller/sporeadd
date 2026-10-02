package com.sporeadds.sporeaddsmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;

public class BushModel extends HierarchicalModel<Entity> {

    public static final ResourceLocation LOCATION =
            new ResourceLocation("sporeadd", "textures/entity/bush_texture.png");

    private final ModelPart root;

    public BushModel(ModelPart root) {
        this.root = root;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Se controla manualmente desde el renderer
    }

    public void applyWalkingAnimation(AnimationState state, AnimationDefinition animation, float ageInTicks) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(state, animation, ageInTicks, 1.0F);
    }

    public void applyRawAnimation(net.minecraft.client.animation.AnimationDefinition animation, long timeMillis) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        net.minecraft.client.animation.KeyframeAnimations.animate(
                this, animation, timeMillis, 1.0F, new org.joml.Vector3f(1.0F, 1.0F, 1.0F)
        );
    }

    public void applyRestPose() {
        this.root().getAllParts().forEach(ModelPart::resetPose);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition center = partdefinition.addOrReplaceChild("center", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        center.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, -14).addBox(0.0F, -27.0F, -7.0F, 0.0F, 27.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
        center.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, -14).addBox(0.0F, -27.0F, -7.0F, 0.0F, 27.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bigSides = partdefinition.addOrReplaceChild("Big_sides", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        bigSides.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(36, -14).addBox(0.0F, -24.0F, -7.0F, 0.0F, 24.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 1.5708F, -1.4835F, -1.5708F));
        bigSides.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(36, -14).addBox(0.0F, -24.0F, 0.0F, 0.0F, 24.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, -7.0F, 0.0F, 0.0F, -0.0873F));
        bigSides.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(36, -14).addBox(0.0F, -24.0F, -7.0F, 0.0F, 24.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, -1.5708F, -1.4835F, 1.5708F));
        bigSides.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(36, 0).addBox(-6.0F, -24.0F, 0.0F, 14.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, -1.0F, 0.0F, -1.5708F, 0.0873F));

        PartDefinition mediumSides = partdefinition.addOrReplaceChild("medium_sides", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        mediumSides.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 37).addBox(0.0F, -13.0F, -7.0F, 0.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, -1.5708F, -1.309F, 1.5708F));
        mediumSides.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 51).addBox(-6.0F, -13.0F, 0.0F, 14.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, -1.0F, 0.0F, -1.5708F, 0.2618F));
        mediumSides.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 37).addBox(0.0F, -13.0F, 0.0F, 0.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, -7.0F, 0.0F, 0.0F, -0.3491F));
        mediumSides.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 37).addBox(0.0F, -13.0F, -7.0F, 0.0F, 13.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 1.5708F, -1.309F, -1.5708F));

        PartDefinition smallSides = partdefinition.addOrReplaceChild("small_sides", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        smallSides.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(36, 58).addBox(-6.0F, -6.0F, 0.0F, 14.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, -1.0F, 0.0F, -1.5708F, 0.6109F));
        smallSides.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(36, 44).addBox(0.0F, -6.0F, -7.0F, 0.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, 1.5708F, -0.9599F, -1.5708F));
        smallSides.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(36, 44).addBox(0.0F, -6.0F, 0.0F, 0.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, -7.0F, 0.0F, 0.0F, -0.6981F));
        smallSides.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(36, 44).addBox(0.0F, -6.0F, -7.0F, 0.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, -1.5708F, -0.9599F, 1.5708F));

        PartDefinition roof = partdefinition.addOrReplaceChild("roof", CubeListBuilder.create(), PartPose.offset(-0.5F, 4.0F, 0.0F));
        roof.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(15, 32).addBox(-2.9042F, -2.8679F, -6.0F, 8.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6109F));
        roof.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(11, 32).addBox(-4.174F, -2.3251F, -6.0F, 8.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6545F));
        roof.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(15, 32).addBox(-3.3138F, -2.5811F, -6.0F, 8.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 0.0F, 0.0F, 1.5708F, 0.9599F, 1.5708F));
        roof.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(12, 33).addBox(-4.5707F, -2.6295F, -5.0F, 8.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 0.0F, 0.0F, -1.5708F, 0.9163F, -1.5708F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}