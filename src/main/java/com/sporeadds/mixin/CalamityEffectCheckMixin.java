package com.sporeadds.mixin;

import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Calamity.class, remap = false)
public class CalamityEffectCheckMixin {

    @Redirect(
            method = "m_147207_(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffect;m_19483_()Lnet/minecraft/world/effect/MobEffectCategory;"
            ),
            remap = false
    )
    private MobEffectCategory sporeadds$ignoreHarmfulForSpecificEffects(MobEffect effect) {
        if (effect == effects.ENCHAINED.get()
                || effect == effects.VULNERABLE.get()
                || effect == effects.CRITICAL_WOUND.get()
                || effect == effects.EXPOSED_WEAKNESS.get()) {
            return MobEffectCategory.BENEFICIAL;
        }
        return effect.getCategory();
    }
}