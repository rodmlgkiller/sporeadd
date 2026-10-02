package com.sporeadds.mixin;

import com.Harbinger.Spore.core.Seffects;
import com.Harbinger.Spore.Sentities.AI.InfectedConsumeFromRemains;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.entity.MeatAbomination;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(value = InfectedConsumeFromRemains.class, remap = false)
public abstract class MixinInfectedConsumeFromRemains {

    @Shadow public Infected infected;

    // Interruptor para saber si estamos persiguiendo una abominación
    @Unique
    private boolean sporeadds$isHuntingAbomination = false;

    @Inject(method = "isCorpse", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$consumeMeatAbomination(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // Reseteamos el interruptor por defecto
        this.sporeadds$isHuntingAbomination = false;

        AABB aabb = entity.getBoundingBox().inflate(42.0D);
        List<MeatAbomination> abominations = entity.level().getEntitiesOfClass(
                MeatAbomination.class,
                aabb,
                abom -> abom.isAlive() && abom.getBiomass() > 0.0F
        );

        if (abominations.isEmpty()) {
            return; // Si no hay abominaciones, dejamos la comida vanilla intacta
        }

        MeatAbomination target = abominations.stream()
                .min(Comparator.comparingDouble(entity::distanceToSqr))
                .orElse(null);

        if (target == null) {
            return;
        }

        // --- ENCONTRAMOS ABOMINACIÓN ---
        // Marcamos el interruptor a TRUE para silenciar el sonido falso en start()
        this.sporeadds$isHuntingAbomination = true;

        this.infected.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double eatDistanceSqr = 4.0D;

        if (target.distanceToSqr(entity) > eatDistanceSqr) {
            // Está lejos: navega hacia ella sin hacer sonido de comer aún
            this.infected.getNavigation().moveTo(target, 1.2D);
            cir.setReturnValue(true);
            return;
        }

        // Está cerca: detenerse y comer
        this.infected.getNavigation().stop();
        this.infected.setDeltaMovement(0.0D, this.infected.getDeltaMovement().y, 0.0D);

        // Probabilidad de darle un bocado
        if (Math.random() < 0.3D) {
            float newBiomass = target.getBiomass() - 0.1F;
            target.setBiomass(newBiomass);

            this.infected.setHunger(0);
            this.infected.removeEffect(Seffects.STARVATION);

            this.infected.addEffect(new MobEffectInstance(
                    effects.EXQUISITE_CUISINE,
                    12000,
                    0,
                    false,
                    true,
                    true
            ));

            // Reproducimos el sonido de comer manualmente AQUÍ (cuando de verdad come)
            this.infected.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);

            if (newBiomass <= 0.0F) {
                target.discard();
            }
        }

        cir.setReturnValue(true);
    }

    // Inyectamos en el método start() del goal original (start) para cancelar el sonido falso
    @Inject(method = "start", at = @At("HEAD"), cancellable = true, remap = false)
    private void sporeadds$silenceStartSound(CallbackInfo ci) {
        if (this.sporeadds$isHuntingAbomination) {
            // Cancelamos el start original, evitando que haga el sonido de comer a distancia
            ci.cancel();
        }
    }
}