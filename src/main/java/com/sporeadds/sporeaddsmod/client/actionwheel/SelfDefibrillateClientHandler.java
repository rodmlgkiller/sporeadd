package com.sporeadds.sporeaddsmod.client.actionwheel;

import net.minecraft.client.Minecraft;

public final class SelfDefibrillateClientHandler {

    private SelfDefibrillateClientHandler() {
    }

    public static void openScreen() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new SelfDefibrillateScreen());
    }

    public static void closeScreenIfOpen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof SelfDefibrillateScreen) {
            mc.setScreen(null);
        }
    }
}