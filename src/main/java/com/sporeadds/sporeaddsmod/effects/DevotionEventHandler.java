package com.sporeadds.sporeaddsmod.effects;

import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Lógica de la parte "resistencia" de {@link DevotionEffect}: reduce el daño RECIBIDO por quien
 * tenga el efecto, un 10% por nivel (amplifier + 1). El +10% de daño de ataque va por atributo
 * (en el propio {@link DevotionEffect}); esto es lo único que necesita ir por evento.
 */
@EventBusSubscriber(modid = "sporeadd")
public final class DevotionEventHandler {

    private DevotionEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        MobEffectInstance inst = victim.getEffect(effects.DEVOTION);
        if (inst == null) return;

        float reduction = DevotionEffect.INCOMING_DAMAGE_REDUCTION_PER_LEVEL * (inst.getAmplifier() + 1);
        event.setNewDamage(Math.max(0.0F, event.getNewDamage() * (1.0F - reduction)));
    }
}
