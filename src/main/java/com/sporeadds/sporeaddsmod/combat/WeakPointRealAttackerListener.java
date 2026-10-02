package com.sporeadds.sporeaddsmod.combat;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber
public class WeakPointRealAttackerListener {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        Entity attackerEntity = source.getEntity();

        if (attackerEntity instanceof ServerPlayer player) {
            WeakPointKillTracker.recordRealKillAttacker(entity.getId(), player.getUUID());
        }
    }
}