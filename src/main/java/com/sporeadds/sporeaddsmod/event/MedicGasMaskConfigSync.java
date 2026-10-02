package com.sporeadds.sporeaddsmod.event;

import net.neoforged.fml.common.EventBusSubscriber;

import com.sporeadds.sporeaddsmod.commands.ClassCommand;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Mantiene la máscara de gas del Medic sincronizada con
 * {@code SporeAddsConfig.MEDIC_GAS_MASK_CURSE_OF_BINDING}:
 * <ul>
 *   <li>al entrar un jugador (por si su máscara se creó bajo un valor de config distinto);</li>
 *   <li>en caliente, en cuanto el config se recarga (archivo editado o GUI de config), sin
 *       reiniciar el servidor ni volver a equipar la máscara.</li>
 * </ul>
 * En ambos casos solo se toca el enchant Curse of Binding; el resto del NBT de la máscara
 * (nombre, lore, Vanishing Curse, modificadores de atributo, Unbreakable...) no se modifica.
 */
@EventBusSubscriber(modid = "sporeadd")
public final class MedicGasMaskConfigSync {

    private MedicGasMaskConfigSync() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ClassCommand.syncGasMaskCurseOfBinding(player);
        }
    }

    @EventBusSubscriber(modid = "sporeadd", bus = EventBusSubscriber.Bus.MOD)
    public static final class ConfigReload {

        private ConfigReload() {
        }

        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event) {
            ModConfig config = event.getConfig();
            if (!"sporeadd".equals(config.getModId()) || config.getType() != ModConfig.Type.COMMON) {
                return;
            }

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;

            // Se ejecuta en el hilo del servidor para no pisar el inventario desde el hilo que recarga el archivo.
            server.execute(() -> {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    ClassCommand.syncGasMaskCurseOfBinding(player);
                }
            });
        }
    }
}
