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

        /** Max Armor HP as computed by the server (-1 until the server has sent it). */
        private static int maxArmorHp = -1;

        public static void setClientMaxArmorHp(int value) {
            maxArmorHp = value;
        }

        public static int getClientMaxArmorHp() {
            return maxArmorHp;
        }
    }
