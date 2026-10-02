package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.spore.PlayerSporeProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber
public class StarvationHandler {

    private static boolean hasKommandantClass(ServerPlayer player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    @SubscribeEvent
    public static void onStarveDamage(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!hasKommandantClass(player)) return;

        if ("starve".equals(event.getSource().getMsgId())) {
            if (!player.getPersistentData().getBoolean("AllowCustomStarveDamage")) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!hasKommandantClass(player)) return;

        MobEffect starvationEffect = BuiltInRegistries.MOB_EFFECT
                .getValue(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("spore", "starvation"));
        if (starvationEffect == null) return;

        boolean starving = player.getFoodData().getFoodLevel() <= 0;

        if (!starving) {
            if (player.hasEffect(starvationEffect)) {
                player.removeEffect(starvationEffect);
            }
            player.getPersistentData().remove("SporeAddsStarvationTick");
            return;
        }

        if (!player.hasEffect(starvationEffect)) {
            player.addEffect(new MobEffectInstance(starvationEffect, 120, 0, false, true));
        } else {
            MobEffectInstance current = player.getEffect(starvationEffect);
            if (current != null && current.getDuration() <= 40) {
                player.addEffect(new MobEffectInstance(starvationEffect, 120, 0, false, true));
            }
        }

        long lastTick = player.getPersistentData().getLong("SporeAddsStarvationTick");
        long currentTick = player.level().getGameTime();

        if (currentTick - lastTick < 100) return; // 5 segundos
        player.getPersistentData().putLong("SporeAddsStarvationTick", currentTick);

        player.getCapability(PlayerSporeProvider.PLAYER_CAP).ifPresent(spore -> {
            if (spore.getSpore() > 0) {
                spore.addSpore(-1);
            } else {
                player.getPersistentData().putBoolean("AllowCustomStarveDamage", true);
                player.hurt(player.damageSources().starve(), 3.0F);
                player.getPersistentData().putBoolean("AllowCustomStarveDamage", false);
            }
        });
    }
}