package com.sporeadds.sporeaddsmod.hive;

import net.minecraft.core.Holder;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.effects.effects;
import com.sporeadds.sporeaddsmod.util.SporeClassUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Recompensa "devotion": cuando un Kommandant mata a un jugador no-Kommandant, o es el
 * responsable acreditado de su muerte (killCredit de Vanilla, que ya cubre las muertes
 * indirectas mientras el rastro de "último golpeado por" siga vigente), y hay al menos un proto
 * vivo en esa dimensión, todos los Kommandant a {@link SporeAddsConfig#DEVOTION_REWARD_RADIUS}
 * bloques de la víctima en el momento de la muerte reciben el efecto {@code devotion} (subiendo
 * un amplifier respecto al que ya tuvieran, hasta el tope), -1 de punishment y un mensaje de
 * recompensa del proto.
 */
@EventBusSubscriber(modid = "sporeadd")
public final class DevotionEvents {

    private static final int MAX_DEVOTION_AMPLIFIER = 2;

    private static final String[] NORMAL_MESSAGE_KEYS = {
            "message.sporeadd.proto.devotion_1",
            "message.sporeadd.proto.devotion_2",
            "message.sporeadd.proto.devotion_3",
            "message.sporeadd.proto.devotion_4"
    };

    private static final String[] ARMORED_MESSAGE_KEYS = {
            "message.sporeadd.proto.devotion_armored_1",
            "message.sporeadd.proto.devotion_armored_2",
            "message.sporeadd.proto.devotion_armored_3"
    };

    private DevotionEvents() {
    }

    @SubscribeEvent
    public static void onNonKommandantDeath(LivingDeathEvent event) {
        if (!SporeAddsConfig.DEVOTION_EVENT_ENABLED.get()) return;
        if (event.isCanceled()) return;
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        if (SporeClassUtil.hasClass(victim, "kommandant")) return;
        if (!(victim.level() instanceof ServerLevel level)) return;

        LivingEntity credited = victim.getKillCredit();
        if (!(credited instanceof ServerPlayer responsible) || !SporeClassUtil.hasClass(responsible, "kommandant")) {
            return;
        }

        if (!ProtoProximity.anyProtoAliveInDimension(level)) return;

        double radius = SporeAddsConfig.DEVOTION_REWARD_RADIUS.get();
        double radiusSq = radius * radius;

        List<ServerPlayer> rewarded = new ArrayList<>();
        for (ServerPlayer online : level.getServer().getPlayerList().getPlayers()) {
            if (online.level() != level) continue;
            if (!SporeClassUtil.hasClass(online, "kommandant")) continue;
            if (online.distanceToSqr(victim) > radiusSq) continue;
            rewarded.add(online);
        }

        if (rewarded.isEmpty()) return;

        boolean heavilyArmored = victim.getArmorValue() > SporeAddsConfig.DEVOTION_ARMOR_THRESHOLD.get();
        String[] pool = heavilyArmored ? ARMORED_MESSAGE_KEYS : NORMAL_MESSAGE_KEYS;
        String key = pool[ThreadLocalRandom.current().nextInt(pool.length)];
        Component message = Component.translatable(key).withStyle(ChatFormatting.DARK_RED);

        Holder<MobEffect> devotion = effects.DEVOTION;
        int durationTicks = SporeAddsConfig.DEVOTION_DURATION_TICKS.get();

        for (ServerPlayer kommandant : rewarded) {
            MobEffectInstance current = kommandant.getEffect(devotion);
            int newAmplifier = current == null ? 0 : Math.min(MAX_DEVOTION_AMPLIFIER, current.getAmplifier() + 1);
            kommandant.addEffect(new MobEffectInstance(devotion, durationTicks, newAmplifier, false, true, true));

            PunishmentTracker.setPoints(kommandant, PunishmentTracker.getPoints(kommandant) - 1);

            kommandant.sendSystemMessage(message);
        }
    }
}
