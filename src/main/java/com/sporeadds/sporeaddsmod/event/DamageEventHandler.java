package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.config.GhostConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sporeadd")
public class DamageEventHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity le ? le : null;

        if (target.hasEffect(effects.AMBUSHED.get())) {
            event.setAmount(event.getAmount() * GhostConfig.ambushedIncomingDamageMultiplier());
        }

        if (attacker != null && attacker.hasEffect(effects.AMBUSHED.get())) {
            event.setAmount(event.getAmount() * GhostConfig.ambushedOutgoingDamageMultiplier());
        }

        if (attacker != null && attacker.hasEffect(effects.CRITICAL_WOUND.get())) {
            event.setAmount(event.getAmount() * GhostConfig.criticalWoundOutgoingDamageMultiplier());
        }

        if (target.hasEffect(effects.CRITICAL_WOUND.get())) {
            float woundedIncoming = GhostConfig.criticalWoundIncomingDamageMultiplier();
            if (woundedIncoming != 1.0F) {
                event.setAmount(event.getAmount() * woundedIncoming);
            }
        }
    }
}