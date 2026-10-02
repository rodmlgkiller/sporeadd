package com.sporeadds.mixin;

import com.Harbinger.Spore.Client.Renderers.AcidBulletRenderer;
import com.Harbinger.Spore.Sentities.Projectile.GunProjectiles.AssassinBullet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sporeadds.sporeaddsmod.client.ClientCausticShotScaleState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Assassin bullets (Kommandant Caustic's charged shot) render via the base Spore mod's
 * AcidBulletRenderer. Scaling here is render-only: PoseStack.scale() only affects what gets drawn
 * this frame, never the entity's EntityDimensions/hitbox, so a heavily charged shot can look up to
 * 50% bigger without becoming any easier or harder to hit.
 */
@Mixin(AcidBulletRenderer.class)
public class CausticBulletRenderScaleMixin {

    @Inject(
            method = "render(Lcom/Harbinger/Spore/Sentities/Projectile/GunProjectiles/AssassinBullet;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            remap = false
    )
    private void sporeadds$scaleChargedCausticShot(
            AssassinBullet entity, float yaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci
    ) {
        float extraScale = ClientCausticShotScaleState.get(entity.getId());
        if (extraScale != 1.0F) {
            poseStack.scale(extraScale, extraScale, extraScale);
        }
    }
}
