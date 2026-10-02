package com.sporeadds.sporeaddsmod.client.model;

import com.sporeadds.sporeaddsmod.entity.MeatAbomination;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class MeatAbominationModel<T extends MeatAbomination> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("sporeadd", "meat_abomination"), "main");

    public final ModelPart group1;
    public final ModelPart group2;
    public final ModelPart group3;

    public MeatAbominationModel(ModelPart root) {
        this.group1 = root.getChild("group1");
        this.group2 = root.getChild("group2");
        this.group3 = root.getChild("group3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // En PartPose.offset(0, 24, 0), Y desciende hacia arriba en negativo.
        // -16.0F es un bloque entero hacia arriba. -32.0F serían dos bloques.

        // GRUPO 1: La base ancha y gruesa (Textura 1)
        partdefinition.addOrReplaceChild("group1", CubeListBuilder.create()
                        // Base principal pisando el suelo
                        .texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F)
                        // Pilar central subiendo
                        .texOffs(0, 24).addBox(-5.0F, -20.0F, -4.0F, 10.0F, 12.0F, 10.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // GRUPO 2: Bultos que trepan por los lados y tapan huecos (Textura 2)
        partdefinition.addOrReplaceChild("group2", CubeListBuilder.create()
                        // Bulto asimétrico lado izquierdo y subiendo
                        .texOffs(0, 0).addBox(-9.0F, -14.0F, -3.0F, 6.0F, 12.0F, 8.0F)
                        // Bulto asimétrico frontal
                        .texOffs(0, 20).addBox(-3.0F, -16.0F, -7.0F, 8.0F, 10.0F, 5.0F)
                        // Remate superior deforme (torre)
                        .texOffs(28, 0).addBox(-2.0F, -25.0F, -2.0F, 6.0F, 10.0F, 6.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // GRUPO 3: Nódulos posteriores y la cúpula retorcida (Textura 3)
        partdefinition.addOrReplaceChild("group3", CubeListBuilder.create()
                        // Bulto trasero grande
                        .texOffs(0, 0).addBox(-4.0F, -18.0F, 3.0F, 8.0F, 14.0F, 6.0F)
                        // Bulto lateral derecho
                        .texOffs(0, 20).addBox(3.0F, -12.0F, -4.0F, 5.0F, 10.0F, 7.0F)
                        // Bultito extra trepando a la cima
                        .texOffs(28, 0).addBox(-6.0F, -24.0F, 0.0F, 5.0F, 8.0F, 5.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Animación de palpitación (sin cambios)
        float scale1 = 1.0F + 0.06F * (float) Math.sin(ageInTicks * 0.15F);
        float scale2 = 1.0F + 0.08F * (float) Math.sin(ageInTicks * 0.11F + 2.0F);
        float scale3 = 1.0F + 0.05F * (float) Math.sin(ageInTicks * 0.18F + 4.0F);

        this.group1.xScale = scale1;
        this.group1.yScale = scale1;
        this.group1.zScale = scale1;

        this.group2.xScale = scale2;
        this.group2.yScale = scale2;
        this.group2.zScale = scale2;

        this.group3.xScale = scale3;
        this.group3.yScale = scale3;
        this.group3.zScale = scale3;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.group1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}