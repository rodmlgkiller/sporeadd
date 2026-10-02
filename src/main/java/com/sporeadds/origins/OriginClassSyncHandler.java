package com.sporeadds.origins;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierData;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.util.ClassAssignmentUtil;
import com.sporeadds.sporeaddsmod.util.OriginSyncUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.UUID;

/**
 * Sincronización inversa: cuando un jugador se coloca (por la pantalla de Origins o por comando)
 * el origin que corresponde a una clase, su clase de SporeAdds se actualiza para coincidir.
 *
 *  - {@code sporeadd:<clase>}  -> identificador {@code <clase>}
 *  - cualquier otro origin ya elegido (humano u otro) -> {@code none}
 *  - origin sin elegir todavía ({@code origins:empty}) -> no se toca nada
 *
 * NO lleva {@code @EventBusSubscriber}: se registra a mano desde {@code SporeAddsMod} SOLO si
 * Origins está cargado, para que esta clase (y sus imports de Origins) no se carguen cuando
 * Origins pase a ser dependencia opcional y esté ausente.
 *
 * El bucle con {@link OriginSyncUtil} (clase -> origin) se evita con:
 *  - la supresión {@link OriginSyncUtil#beginSuppress}/{@link OriginSyncUtil#endSuppress}, y
 *  - {@link OriginSyncUtil#wasRecentlyApplied} (no reaccionar justo tras una escritura de origin).
 */
public final class OriginClassSyncHandler {

    private static final int POLL_INTERVAL_TICKS = 20;
    private static final String SPOREADD_NAMESPACE = "sporeadd";
    private static final ResourceLocation EMPTY_ORIGIN = ResourceLocation.fromNamespaceAndPath("origins", "empty");

    private OriginClassSyncHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % POLL_INTERVAL_TICKS != 0) return;
        syncClassFromOrigin(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.server == null) return;
        player.server.execute(() -> syncClassFromOrigin(player));
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        OriginSyncUtil.forget(event.getEntity().getUUID());
    }

    private static void syncClassFromOrigin(ServerPlayer player) {
        long now = player.level().getGameTime();
        if (OriginSyncUtil.wasRecentlyApplied(player.getUUID(), now)
                || OriginSyncUtil.isSettling(player.getUUID(), now)) {
            return;
        }

        String mapped = mappedIdentifier(player);
        if (mapped == null) return;

        String current = player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(SporeIdentifierData::getIdentifier)
                .orElse(null);

        if (current == null || mapped.equalsIgnoreCase(current)) return;

        UUID id = player.getUUID();
        OriginSyncUtil.beginSuppress(id);
        try {
            ClassAssignmentUtil.applyClass(player, mapped, "none");
        } finally {
            OriginSyncUtil.endSuppress(id);
        }
    }

    /**
     * @return el identificador de clase que implica el origin actual del jugador (capa principal),
     * {@code "none"} si tiene un origin ya elegido que no es de sporeadd, o {@code null} si aún
     * no ha elegido origin (o no se puede leer).
     */
    private static String mappedIdentifier(ServerPlayer player) {
        ResourceLocation origin;
        try {
            // A mod can present itself under modId "origins" (e.g. the real Fabric Origins mod
            // loaded through Sinytra Connector) without actually providing the EdwinMindcraft API
            // OriginApiBridge is compiled against - ModList.isLoaded("origins") alone can't catch
            // that, so guard the actual API call itself.
            origin = OriginApiBridge.currentMainOrigin(player);
        } catch (Throwable ignored) {
            return null;
        }
        if (origin == null || EMPTY_ORIGIN.equals(origin)) {
            return null;
        }

        if (SPOREADD_NAMESPACE.equals(origin.getNamespace())
                && SporeIdentifierData.VALID_IDS.contains(origin.getPath())) {
            return origin.getPath();
        }

        return "none";
    }
}
