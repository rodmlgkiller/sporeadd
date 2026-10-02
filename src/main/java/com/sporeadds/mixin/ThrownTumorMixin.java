package com.sporeadds.mixin;

import com.Harbinger.Spore.core.Seffects;
import com.Harbinger.Spore.Sentities.Projectile.ThrownTumor;
import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ThrownTumor.class, remap = false)
public class ThrownTumorMixin {

    @Inject(method = "freezeTargets", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$nerfFrozenTumor(List<Entity> entityList, CallbackInfo ci) {
        if (!SporeAddsConfig.FROZEN_TUMOR_NERF.get()) {
            return;
        }

        for (Entity entity : entityList) {
            if (entity instanceof LivingEntity livingEntity) {
                MobEffectInstance current = livingEntity.getEffect((MobEffect) Seffects.FROSTBITE.get());
                int amplifier = current == null ? 0 : Math.min(current.getAmplifier(), 0);
                livingEntity.addEffect(new MobEffectInstance((MobEffect) Seffects.FROSTBITE.get(), 600, amplifier));
            }
        }

        ci.cancel();
    }
}