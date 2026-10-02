package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.Organoids.Verwa;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = Verwa.class, remap = false)
public class VerwaAntifarmPropagationMixin {

    private static final String ANTIFARM_TAG = "antifarm";

    @Inject(
            method = "SummonStoredEntity()V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lcom/Harbinger/Spore/Sentities/Organoids/Verwa;getStoredEntity()Lnet/minecraft/world/entity/Entity;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void sporeadds$copyAntifarmTagToSummon(CallbackInfo ci, Entity entity) {
        Verwa self = (Verwa) (Object) this;

        if (self.getTags().contains(ANTIFARM_TAG) && entity != null) {
            entity.addTag(ANTIFARM_TAG);
        }
    }
}