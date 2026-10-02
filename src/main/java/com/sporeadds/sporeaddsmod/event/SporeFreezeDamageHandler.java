package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber
public class SporeFreezeDamageHandler {

    @SubscribeEvent
    public static void onFreezeDamage(LivingHurtEvent event) {
        DamageSource source = event.getSource();

        if ("freeze".equals(source.getMsgId()) && event.getEntity() instanceof Player player) {
            if (player.getTeam() != null && "spore".equalsIgnoreCase(player.getTeam().getName())) {

                PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
                    float original = event.getAmount();

                    if (data.getArmorHp() > 0) {
                        event.setAmount(original * 8);
                    } else {
                        event.setAmount(original * 2);
                    }
                });
            }
        }
    }
}