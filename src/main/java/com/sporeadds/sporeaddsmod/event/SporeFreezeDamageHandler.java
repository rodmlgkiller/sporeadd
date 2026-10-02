package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SporeFreezeDamageHandler {

    @SubscribeEvent
    public static void onFreezeDamage(LivingHurtEvent event) {
        DamageSource source = event.getSource();

        if ("freeze".equals(source.getMsgId()) && event.getEntity() instanceof Player player) {
            if (player.getTeam() != null && "spore".equalsIgnoreCase(player.getTeam().getName())) {

                player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
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