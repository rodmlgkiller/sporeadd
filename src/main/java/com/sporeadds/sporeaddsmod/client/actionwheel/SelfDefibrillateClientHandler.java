package com.sporeadds.sporeaddsmod.client.actionwheel;

import net.minecraft.client.Minecraft;

public final class SelfDefibrillateClientHandler {

    private SelfDefibrillateClientHandler() {
    }

    /** Opens the minigame with the settings of this client's own config. */
    public static void openScreen() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new SelfDefibrillateScreen());
    }

    /** Opens the minigame with the rules the server sent (time limit in ticks, icons, GUI pixels, pixels per tick). */
    public static void openScreen(int timeLimitTicks, int iconCount, int iconSize, boolean iconsMove, float iconSpeed) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new SelfDefibrillateScreen(timeLimitTicks, iconCount, iconSize, iconsMove, iconSpeed));
    }

    public static void closeScreenIfOpen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof SelfDefibrillateScreen) {
            mc.setScreen(null);
        }
    }
}