    package com.sporeadds.sporeaddsmod.PlayerData;

    import net.minecraft.client.Minecraft;

    /**
     * Clase simple para almacenar armorHp sincronizada del lado cliente.
     * Cuando se actualiza, muestra un mensaje en el chat para debug.
     */
    public class PlayerDataClient {
        private static int armorHp = 0;

        public static void setClientArmorHp(int value) {
            armorHp = value;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
            }
        }

        public static int getClientArmorHp() {
            return armorHp;
        }
    }
