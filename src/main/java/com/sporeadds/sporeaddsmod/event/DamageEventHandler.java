package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.effects.AmbushedEffect;
import com.sporeadds.sporeaddsmod.effects.CriticalWoundEffect;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd")
public class DamageEventHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity le ? le : null;

        if (target.hasEffect(effects.AMBUSHED.get())) {
            event.setAmount(event.getAmount() * AmbushedEffect.INCOMING_DAMAGE_MULTIPLIER);
        }

        if (attacker != null && attacker.hasEffect(effects.AMBUSHED.get())) {
            event.setAmount(event.getAmount() * AmbushedEffect.OUTGOING_DAMAGE_MULTIPLIER);
        }

        if (attacker != null && attacker.hasEffect(effects.CRITICAL_WOUND.get())) {
            event.setAmount(event.getAmount() * CriticalWoundEffect.OUTGOING_DAMAGE_MULTIPLIER);
        }
    }
}