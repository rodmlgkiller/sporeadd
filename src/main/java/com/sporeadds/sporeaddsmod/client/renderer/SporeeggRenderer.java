package com.sporeadds.sporeaddsmod.client.renderer;

import com.Harbinger.Spore.Client.Renderers.OrganoidMobRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.client.model.Cocon;
import com.sporeadds.sporeaddsmod.client.model.animations.CoconAnimation;
import com.sporeadds.sporeaddsmod.entity.StaticEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer del "cocoon" (static entity). Hereda de {@link OrganoidMobRenderer} para reciclar
 * la animación de emerger/enterrarse del suelo (solo desplaza el modelo en Y, sin tocar la
 * hitbox) que ya usan los organoides de Spore; el modelo, textura y animaciones son propias.
 */
public class SporeeggRenderer extends OrganoidMobRenderer<StaticEntity, Cocon<StaticEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("sporeadd", "textures/entity/cocon.png");

    /** El modelo se dibuja a la mitad de tamaño; la hitbox no cambia. */
    private static final float MODEL_SCALE = 0.5f;

    public SporeeggRenderer(EntityRendererProvider.Context context) {
        super(context, new AnimatedModel(context.bakeLayer(Cocon.LAYER_LOCATION)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(StaticEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(StaticEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }

    private static final class AnimatedModel extends Cocon<StaticEntity> {
        private final ModelPart root;
        private final HierarchicalModel<StaticEntity> animator = new HierarchicalModel<>() {
            @Override
            public ModelPart root() {
                return root;
            }

            @Override
            public void setupAnim(StaticEntity entity, float limbSwing, float limbSwingAmount,
                                  float ageInTicks, float netHeadYaw, float headPitch) {
                this.root().getAllParts().forEach(ModelPart::resetPose);
                this.animate(entity.idleAnimationState, CoconAnimation.STILL, ageInTicks, 1f);
                this.animate(entity.animationState0, CoconAnimation.OPENING, ageInTicks, 1f);
                this.animate(entity.closingAnimationState, CoconAnimation.CLOSING, ageInTicks, 1f);
            }
        };

        public AnimatedModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override
        public void setupAnim(StaticEntity entity, float limbSwing, float limbSwingAmount,
                              float ageInTicks, float netHeadYaw, float headPitch) {
            animator.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
