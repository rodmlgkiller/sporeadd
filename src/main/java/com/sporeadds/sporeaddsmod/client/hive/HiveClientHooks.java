package com.sporeadds.sporeaddsmod.client.hive;

import net.minecraft.client.Minecraft;

/**
 * Puente entre los paquetes de red de la colmena y el cliente. Solo se carga en cliente
 * (los paquetes que lo usan son PLAY_TO_CLIENT), igual que {@code SelfDefibrillateClientHandler}.
 */
public final class HiveClientHooks {

    private HiveClientHooks() {
    }

    public static void setDowned(int entityId, boolean downed) {
        HiveDbnoClientState.setDowned(entityId, downed);
    }

    public static void openCinematic(boolean forceSurrender) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof HiveCinematicScreen)) {
            mc.setScreen(new HiveCinematicScreen(forceSurrender));
        }
    }

    public static void advancePhase(String phase) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof HiveCinematicScreen screen) {
            screen.onServerPhase(phase);
        }
    }
}
