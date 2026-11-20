package com.sporeadds.sporeaddsmod.abilities;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.GhostConfig;
import com.sporeadds.sporeaddsmod.network.CamouflageLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Optional "disguise stacks" of the Ghost (config {@code ghost.disguise.stacks}).
 *
 * <p>When {@code consume_on} is {@link GhostConfig.StackConsume#NONE} (the default) nothing here does anything and the
 * disguise behaves exactly as before: it ends only when the Ghost toggles it off, takes damage, or (if configured) its
 * maximum duration runs out. Otherwise the disguise starts with {@code max_stacks} stacks, every configured trigger
 * (attack / backstab / kill) uses one up, and when the last one is gone the disguise ends like a normal toggle-off
 * (cooldown included). The stacks only live on the server and only while the disguise is on.
 */
@Mod.EventBusSubscriber(modid = "sporeadd")
public final class GhostDisguiseStacks {

    private static final Map<UUID, Integer> STACKS = new HashMap<>();

    private GhostDisguiseStacks() {
    }

    /** Called by {@link CamouflageLogic#activate} when the disguise starts. */
    public static void onDisguiseStart(ServerPlayer player) {
        if (GhostConfig.disguiseStackConsumeOn() == GhostConfig.StackConsume.NONE) {
            return;
        }
        int max = GhostConfig.disguiseMaxStacks();
        STACKS.put(player.getUUID(), max);
        notifyStacks(player, max, max);
    }

    /** Called by {@link CamouflageLogic} whenever the disguise ends, whatever the reason. */
    public static void onDisguiseEnd(ServerPlayer player) {
        STACKS.remove(player.getUUID());
    }

    /**
     * Uses up one stack if {@code trigger} is the configured one and the player is disguised. When no stack is left the
     * disguise ends.
     */
    public static void consume(ServerPlayer player, GhostConfig.StackConsume trigger) {
        if (GhostConfig.disguiseStackConsumeOn() != trigger) {
            return;
        }

        player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
            if (!data.isCamouflaged()) {
                return;
            }

            int max = GhostConfig.disguiseMaxStacks();
            int remaining = STACKS.getOrDefault(player.getUUID(), max) - 1;

            if (remaining > 0) {
                STACKS.put(player.getUUID(), remaining);
                notifyStacks(player, remaining, max);
            } else {
                CamouflageLogic.deactivate(player, data);
            }
        });
    }

    private static void notifyStacks(ServerPlayer player, int remaining, int max) {
        if (max > 1) {
            player.displayClientMessage(
                    Component.translatable("ability.sporeadds.ghost.disguise_stacks", remaining, max), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (GhostConfig.disguiseStackConsumeOn() != GhostConfig.StackConsume.ATTACK) {
            return;
        }

        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != event.getEntity()) {
            consume(attacker, GhostConfig.StackConsume.ATTACK);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (GhostConfig.disguiseStackConsumeOn() != GhostConfig.StackConsume.KILL) {
            return;
        }

        if (event.getSource().getEntity() instanceof ServerPlayer killer && killer != event.getEntity()) {
            consume(killer, GhostConfig.StackConsume.KILL);
        }
    }
}
