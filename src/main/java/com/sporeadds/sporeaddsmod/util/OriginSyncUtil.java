package com.sporeadds.sporeaddsmod.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Punto único de sincronización con el mod Origins para el sistema de clases.
 *
 * - Cada identificador de clase tiene un origin asociado.
 * - Las subclases de kommandant NO son un origin (el origin es siempre {@code sporeadd:kommandant}).
 * - El identificador {@code none} equivale al origin humano ({@code origins:human}).
 * - Se aplica vía la API de Origins ({@code OriginApiBridge}), no por comando, y siempre detrás de
 *   {@code ModList.isLoaded("origins")}: si no está, no-op; si está, se aplica siempre que se
 *   selecciona/quita una clase. La clase puente solo se carga cuando Origins está presente.
 */
public final class OriginSyncUtil {

    /**
     * Jugadores para los que se suprime temporalmente la escritura de origin. Lo usa la
     * sincronización inversa (origin -> clase) para no volver a escribir el origin y evitar bucle.
     */
    private static final Set<UUID> SUPPRESSED = ConcurrentHashMap.newKeySet();

    /** Último instante (gameTime) en que se escribió un origin para cada jugador. */
    private static final Map<UUID, Long> LAST_APPLY_TICK = new ConcurrentHashMap<>();

    /** Ventana en la que la sincronización inversa ignora al jugador tras una escritura de origin. */
    public static final long RECENT_APPLY_WINDOW_TICKS = 60L;

    /**
     * Tras una transición del jugador (clone por muerte/regreso del End, respawn, cambio de
     * dimensión) el contenedor de Origins y nuestra capability se restauran por separado y
     * pueden leerse desincronizados durante unos ticks. En esa ventana la sync inversa
     * (origin -> clase) NO debe tocar la clase, o "degradaría" al jugador a {@code none} por
     * un estado transitorio. La clase que copia {@code ForgeEvents.onPlayerClone} es la buena.
     */
    private static final Map<UUID, Long> SETTLE_UNTIL = new ConcurrentHashMap<>();
    public static final long SETTLE_WINDOW_TICKS = 100L;

    private OriginSyncUtil() {
    }

    public static void beginSettleGrace(ServerPlayer player) {
        if (player != null) {
            SETTLE_UNTIL.put(player.getUUID(), player.level().getGameTime() + SETTLE_WINDOW_TICKS);
        }
    }

    public static boolean isSettling(UUID playerId, long nowGameTime) {
        Long until = SETTLE_UNTIL.get(playerId);
        return until != null && nowGameTime < until;
    }

    public static void beginSuppress(UUID playerId) {
        SUPPRESSED.add(playerId);
    }

    public static void endSuppress(UUID playerId) {
        SUPPRESSED.remove(playerId);
    }

    public static boolean wasRecentlyApplied(UUID playerId, long nowGameTime) {
        Long last = LAST_APPLY_TICK.get(playerId);
        return last != null && nowGameTime - last < RECENT_APPLY_WINDOW_TICKS;
    }

    public static void forget(UUID playerId) {
        SUPPRESSED.remove(playerId);
        LAST_APPLY_TICK.remove(playerId);
        SETTLE_UNTIL.remove(playerId);
    }

    /** Origin (capa por defecto) que corresponde a un identificador de clase, o null si no aplica. */
    public static String originForIdentifier(String identifier) {
        if (identifier == null) {
            return "origins:human";
        }
        return switch (identifier.toLowerCase(Locale.ROOT)) {
            case "kommandant" -> "sporeadd:kommandant";
            case "ghost" -> "sporeadd:ghost";
            case "medic" -> "sporeadd:medic";
            case "scientist" -> "sporeadd:scientist";
            case "berserker" -> "sporeadd:berserker";
            case "none", "" -> "origins:human";
            default -> null;
        };
    }

    /**
     * Aplica (o revierte a humano) el origin que corresponde a la clase indicada.
     * No-op si Origins no está cargado o si el identificador no mapea a ningún origin.
     */
    public static void applyForIdentifier(ServerPlayer player, String identifier) {
        if (player == null || player.getServer() == null) {
            return;
        }
        if (SUPPRESSED.contains(player.getUUID())) {
            return;
        }
        if (!ModList.get().isLoaded("origins")) {
            return;
        }

        String originId = originForIdentifier(identifier);
        if (originId == null) {
            return;
        }

        ResourceLocation loc = ResourceLocation.tryParse(originId);
        if (loc == null) {
            return;
        }

        try {
            com.sporeadds.origins.OriginApiBridge.setOrigin(player, loc);
            LAST_APPLY_TICK.put(player.getUUID(), player.level().getGameTime());
        } catch (Throwable ignored) {
        }
    }
}
