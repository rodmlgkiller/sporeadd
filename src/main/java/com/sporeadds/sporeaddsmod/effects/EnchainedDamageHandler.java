package com.sporeadds.sporeaddsmod.effects;

import com.sporeadds.sporeaddsmod.config.BerserkerConfig;
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
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        Entity source = event.getSource().getEntity();
        if (source instanceof LivingEntity attacker) {
            if (attacker.hasEffect(effects.ENCHAINED)) {
                float damage = event.getNewDamage();

                // Config berserker.chains.enchained: por defecto -15% a jugadores, -65% al resto.
                if (attacker instanceof Player) {
                    damage *= BerserkerConfig.enchainedPlayerDamageMultiplier();
                } else {
                    damage *= BerserkerConfig.enchainedNonPlayerDamageMultiplier();
                }

                event.setNewDamage(damage);
            }
        }
    }
}