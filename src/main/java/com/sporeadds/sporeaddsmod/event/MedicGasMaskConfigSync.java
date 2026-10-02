package com.sporeadds.sporeaddsmod.event;

import com.sporeadds.sporeaddsmod.commands.ClassCommand;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

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
@Mod.EventBusSubscriber(modid = "sporeadd")
public final class MedicGasMaskConfigSync {

    private MedicGasMaskConfigSync() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ClassCommand.syncGasMaskCurseOfBinding(player);
        }
    }

    @Mod.EventBusSubscriber(modid = "sporeadd", bus = Mod.EventBusSubscriber.Bus.MOD)
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
