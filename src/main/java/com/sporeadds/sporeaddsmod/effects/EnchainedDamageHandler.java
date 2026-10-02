package com.sporeadds.sporeaddsmod.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "sporeadd")
public class EnchainedDamageHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        Entity source = event.getSource().getEntity();
        if (source instanceof LivingEntity attacker) {
            if (attacker.hasEffect(effects.ENCHAINED)) {
                float damage = event.getAmount();

                if (attacker instanceof Player) {
                    damage *= 0.85F;
                } else {
                    damage *= 0.35F;
                }

                event.setAmount(damage);
            }
        }
    }
}