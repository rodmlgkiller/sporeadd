package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.config.GhostConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = "sporeadd")
public class DamageEventHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity le ? le : null;

        if (target.hasEffect(effects.AMBUSHED)) {
            event.setNewDamage(event.getNewDamage() * GhostConfig.ambushedIncomingDamageMultiplier());
        }

        if (attacker != null && attacker.hasEffect(effects.AMBUSHED)) {
            event.setNewDamage(event.getNewDamage() * GhostConfig.ambushedOutgoingDamageMultiplier());
        }

        if (attacker != null && attacker.hasEffect(effects.CRITICAL_WOUND)) {
            event.setNewDamage(event.getNewDamage() * GhostConfig.criticalWoundOutgoingDamageMultiplier());
        }

        if (target.hasEffect(effects.CRITICAL_WOUND)) {
            float woundedIncoming = GhostConfig.criticalWoundIncomingDamageMultiplier();
            if (woundedIncoming != 1.0F) {
                event.setNewDamage(event.getNewDamage() * woundedIncoming);
            }
        }
    }
}