package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

import com.sporeadds.sporeaddsmod.config.SporeAddsConfig;
import com.sporeadds.sporeaddsmod.network.NetworkHandle;
import com.sporeadds.sporeaddsmod.network.SyncKommandantEspPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "sporeadd")
public class KommandantEspSyncHandler {

    private static final ResourceLocation MARKER_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("spore", "marker");
    private static final ResourceLocation UNEASY_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("spore", "uneasy");
    private static final ResourceLocation SEASONED_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("sporeadd", "seasoned");

    private static final Map<UUID, State> LAST_PLAYER_STATES = new HashMap<>();
    private static final Map<UUID, State> LAST_ENTITY_STATES = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        if (!(event.player instanceof ServerPlayer target)) return;

        MobEffect marker = BuiltInRegistries.MOB_EFFECT.get(MARKER_EFFECT_ID);
        MobEffect uneasy = BuiltInRegistries.MOB_EFFECT.get(UNEASY_EFFECT_ID);
        MobEffect seasoned = BuiltInRegistries.MOB_EFFECT.get(SEASONED_EFFECT_ID);

        MobEffectInstance markerInstance = marker != null ? target.getEffect(marker) : null;
        boolean hasMarker = markerInstance != null;
        int markerAmplifier = markerInstance != null ? markerInstance.getAmplifier() : 0;
        boolean hasUneasy = uneasy != null && target.hasEffect(uneasy);
        boolean hasSeasoned = seasoned != null && target.hasEffect(seasoned);

        State current = new State(hasMarker, markerAmplifier, hasUneasy, hasSeasoned);
        State previous = LAST_PLAYER_STATES.get(target.getUUID());

        if (!current.equals(previous)) {
            LAST_PLAYER_STATES.put(target.getUUID(), current);

            SyncKommandantEspPacket packet = new SyncKommandantEspPacket(
                    target.getId(),
                    hasMarker,
                    markerAmplifier,
                    hasUneasy,
                    hasSeasoned
            );

            target.serverLevel().players().forEach(watcher ->
                    NetworkHandle.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> watcher),
                            packet
                    )
            );
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.level.isClientSide()) return;
        if (!(event.level instanceof ServerLevel level)) return;

        MobEffect marker = BuiltInRegistries.MOB_EFFECT.get(MARKER_EFFECT_ID);
        MobEffect uneasy = BuiltInRegistries.MOB_EFFECT.get(UNEASY_EFFECT_ID);
        MobEffect seasoned = BuiltInRegistries.MOB_EFFECT.get(SEASONED_EFFECT_ID);

        if (marker == null && uneasy == null && seasoned == null) return;

        Set<String> allowedIds = getConfiguredEspEntityIds();

        for (Entity raw : level.getAllEntities()) {
            if (!(raw instanceof LivingEntity entity)) continue;
            if (entity instanceof Player) continue;

            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            boolean allowedByConfig = key != null && allowedIds.contains(key.toString());

            MobEffectInstance markerInstance = marker != null ? entity.getEffect(marker) : null;
            boolean hasMarker = markerInstance != null;
            int markerAmplifier = markerInstance != null ? markerInstance.getAmplifier() : 0;
            boolean hasUneasy = uneasy != null && entity.hasEffect(uneasy);
            boolean hasSeasoned = seasoned != null && entity.hasEffect(seasoned);

            if (!allowedByConfig && !hasSeasoned) {
                continue;
            }

            State current = new State(hasMarker, markerAmplifier, hasUneasy, hasSeasoned);
            State previous = LAST_ENTITY_STATES.get(entity.getUUID());

            if (!current.equals(previous)) {
                LAST_ENTITY_STATES.put(entity.getUUID(), current);

                SyncKommandantEspPacket packet = new SyncKommandantEspPacket(
                        entity.getId(),
                        hasMarker,
                        markerAmplifier,
                        hasUneasy,
                        hasSeasoned
                );

                NetworkHandle.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                        packet
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_PLAYER_STATES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            LAST_PLAYER_STATES.remove(player.getUUID());
        } else if (entity instanceof LivingEntity living) {
            LAST_ENTITY_STATES.remove(living.getUUID());
        }
    }

    private static Set<String> getConfiguredEspEntityIds() {
        List<? extends String> configured = SporeAddsConfig.KOMMANDANT_ESP_ENTITIES.get();
        Set<String> result = new HashSet<>();

        if (configured == null) {
            return result;
        }

        for (String entry : configured) {
            if (entry == null) continue;
            String trimmed = entry.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }

        return result;
    }

    private record State(boolean hasMarker, int markerAmplifier, boolean hasUneasy, boolean hasSeasoned) {
    }
}