package com.sporeadds.mixin;

import net.minecraft.core.registries.BuiltInRegistries;

import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Howler;
import com.Harbinger.Spore.core.SConfig;
import com.Harbinger.Spore.core.Ssounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Howler.class)
public abstract class HowlerMixin {

    /**
     * Inyectamos al FINAL de checkForInfected para que, si el original devolvió false,
     * nosotros comprobemos si hay algún jugador del equipo Spore.
     */
    @Inject(method = "checkForInfected", at = @At("RETURN"), remap = false, cancellable = true)
    private void addSporeTeamToInfectedCheck(Entity origin, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }

        AABB area = origin.getBoundingBox().inflate(4.0D);

        for (Entity entity : origin.level().getEntities(origin, area, (e) -> true)) {
            if (entity instanceof Player player) {
                Team team = player.getTeam();
                if (team != null && "spore".equals(team.getName())) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    /**
     * Cancelamos por completo el ScreamBuffInfected original
     * y lo reimplementamos añadiendo la comprobación del jugador.
     */
    @Inject(method = "ScreamBuffInfected", at = @At("HEAD"), remap = false, cancellable = true)
    private void modifyScreamBuffInfected(Entity origin, CallbackInfo ci) {
        ci.cancel();

        AABB area = origin.getBoundingBox().inflate(6.0D);
        List<Entity> allies = origin.level().getEntitiesOfClass(Entity.class, area, e -> e != origin);

        int duration;
        switch (origin.level().getDifficulty()) {
            case EASY -> duration = 100;
            case NORMAL -> duration = 200;
            case HARD -> duration = 400;
            default -> duration = 0;
        }

        int amplifier = origin.level().getDifficulty() == Difficulty.HARD ? 1 : 0;

        @SuppressWarnings("unchecked")
        List<String> buffs = (List<String>) SConfig.SERVER.howler_effects_buff.get();
        if (buffs.isEmpty()) return;

        String randomBuff = buffs.get(origin.level().random.nextInt(buffs.size()));
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(randomBuff));

        if (effect != null) {
            for (Entity ally : allies) {
                boolean isEligible = false;

                if (ally instanceof Infected) {
                    isEligible = true;
                }
                else if (ally instanceof Player player) {
                    Team team = player.getTeam();
                    if (team != null && "spore".equals(team.getName())) {
                        isEligible = true;
                    }
                }

                if (isEligible && ally instanceof LivingEntity livingAlly) {
                    livingAlly.addEffect(new MobEffectInstance(effect, duration, amplifier));
                }
            }
        }

        // Reproducimos el sonido casteando origin al Howler original y llamando a su método vanilla
        SoundEvent soundEvent = (SoundEvent) Ssounds.HOWLER_GROWL.get();
        if (soundEvent != null && origin instanceof LivingEntity) {
            origin.playSound(soundEvent, 1.0F, ((LivingEntity) origin).getVoicePitch());
        }
    }
}